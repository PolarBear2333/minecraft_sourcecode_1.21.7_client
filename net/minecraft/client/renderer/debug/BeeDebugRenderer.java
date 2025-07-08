/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;

public class BeeDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
    private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
    private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
    private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
    private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
    private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_MEMBERS = true;
    private static final boolean SHOW_BLACKLISTS = true;
    private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
    private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final int HIVE_TIMEOUT = 20;
    private static final float TEXT_SCALE = 0.02f;
    private static final int ORANGE = -23296;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private final Minecraft minecraft;
    private final Map<BlockPos, HiveDebugInfo> hives = new HashMap<BlockPos, HiveDebugInfo>();
    private final Map<UUID, BeeDebugPayload.BeeInfo> beeInfosPerEntity = new HashMap<UUID, BeeDebugPayload.BeeInfo>();
    @Nullable
    private UUID lastLookedAtUuid;

    public BeeDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void clear() {
        this.hives.clear();
        this.beeInfosPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addOrUpdateHiveInfo(HiveDebugPayload.HiveInfo hiveInfo, long l) {
        this.hives.put(hiveInfo.pos(), new HiveDebugInfo(hiveInfo, l));
    }

    public void addOrUpdateBeeInfo(BeeDebugPayload.BeeInfo beeInfo) {
        this.beeInfosPerEntity.put(beeInfo.uuid(), beeInfo);
    }

    public void removeBeeInfo(int n) {
        this.beeInfosPerEntity.values().removeIf(beeInfo -> beeInfo.id() == n);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        this.clearRemovedHives();
        this.clearRemovedBees();
        this.doRender(poseStack, multiBufferSource);
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void clearRemovedBees() {
        this.beeInfosPerEntity.entrySet().removeIf(entry -> this.minecraft.level.getEntity(((BeeDebugPayload.BeeInfo)entry.getValue()).id()) == null);
    }

    private void clearRemovedHives() {
        long l = this.minecraft.level.getGameTime() - 20L;
        this.hives.entrySet().removeIf(entry -> ((HiveDebugInfo)entry.getValue()).lastSeen() < l);
    }

    private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockPos blockPos = this.getCamera().getBlockPosition();
        this.beeInfosPerEntity.values().forEach(beeInfo -> {
            if (this.isPlayerCloseEnoughToMob((BeeDebugPayload.BeeInfo)beeInfo)) {
                this.renderBeeInfo(poseStack, multiBufferSource, (BeeDebugPayload.BeeInfo)beeInfo);
            }
        });
        this.renderFlowerInfos(poseStack, multiBufferSource);
        for (BlockPos blockPos3 : this.hives.keySet()) {
            if (!blockPos.closerThan(blockPos3, 30.0)) continue;
            BeeDebugRenderer.highlightHive(poseStack, multiBufferSource, blockPos3);
        }
        Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
        this.hives.values().forEach(hiveDebugInfo -> {
            if (blockPos.closerThan(hiveDebugInfo.info.pos(), 30.0)) {
                Set set = (Set)map.get(hiveDebugInfo.info.pos());
                this.renderHiveInfo(poseStack, multiBufferSource, hiveDebugInfo.info, set == null ? Sets.newHashSet() : set);
            }
        });
        this.getGhostHives().forEach((blockPos2, list) -> {
            if (blockPos.closerThan((Vec3i)blockPos2, 30.0)) {
                this.renderGhostHive(poseStack, multiBufferSource, (BlockPos)blockPos2, (List<String>)list);
            }
        });
    }

    private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
        HashMap hashMap = Maps.newHashMap();
        this.beeInfosPerEntity.values().forEach(beeInfo -> beeInfo.blacklistedHives().forEach(blockPos2 -> hashMap.computeIfAbsent(blockPos2, blockPos -> Sets.newHashSet()).add(beeInfo.uuid())));
        return hashMap;
    }

    private void renderFlowerInfos(PoseStack poseStack, MultiBufferSource multiBufferSource) {
        HashMap hashMap = Maps.newHashMap();
        this.beeInfosPerEntity.values().forEach(beeInfo -> {
            if (beeInfo.flowerPos() != null) {
                hashMap.computeIfAbsent(beeInfo.flowerPos(), blockPos -> new HashSet()).add(beeInfo.uuid());
            }
        });
        hashMap.forEach((blockPos, set) -> {
            Set set2 = set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
            int n = 1;
            BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, set2.toString(), blockPos, n++, -256);
            BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "Flower", blockPos, n++, -1);
            float f = 0.05f;
            DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.8f, 0.8f, 0.0f, 0.3f);
        });
    }

    private static String getBeeUuidsAsString(Collection<UUID> collection) {
        if (collection.isEmpty()) {
            return "-";
        }
        if (collection.size() > 3) {
            return collection.size() + " bees";
        }
        return collection.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
    }

    private static void highlightHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void renderGhostHive(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, List<String> list) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, String.valueOf(list), blockPos, 0, -256);
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "Ghost Hive", blockPos, 1, -65536);
    }

    private void renderHiveInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, HiveDebugPayload.HiveInfo hiveInfo, Collection<UUID> collection) {
        int n = 0;
        if (!collection.isEmpty()) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Blacklisted by " + BeeDebugRenderer.getBeeUuidsAsString(collection), hiveInfo, n++, -65536);
        }
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Out: " + BeeDebugRenderer.getBeeUuidsAsString(this.getHiveMembers(hiveInfo.pos())), hiveInfo, n++, -3355444);
        if (hiveInfo.occupantCount() == 0) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: -", hiveInfo, n++, -256);
        } else if (hiveInfo.occupantCount() == 1) {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: 1 bee", hiveInfo, n++, -256);
        } else {
            BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "In: " + hiveInfo.occupantCount() + " bees", hiveInfo, n++, -256);
        }
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, "Honey: " + hiveInfo.honeyLevel(), hiveInfo, n++, -23296);
        BeeDebugRenderer.renderTextOverHive(poseStack, multiBufferSource, hiveInfo.hiveType() + (hiveInfo.sedated() ? " (sedated)" : ""), hiveInfo, n++, -1);
    }

    private void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeDebugPayload.BeeInfo beeInfo) {
        if (beeInfo.path() != null) {
            PathfindingRenderer.renderPath(poseStack, multiBufferSource, beeInfo.path(), 0.5f, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z());
        }
    }

    private void renderBeeInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BeeDebugPayload.BeeInfo beeInfo) {
        boolean bl = this.isBeeSelected(beeInfo);
        int n = 0;
        BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, beeInfo.toString(), -1, 0.03f);
        if (beeInfo.hivePos() == null) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, "No hive", -98404, 0.02f);
        } else {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, "Hive: " + this.getPosDescription(beeInfo, beeInfo.hivePos()), -256, 0.02f);
        }
        if (beeInfo.flowerPos() == null) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, "No flower", -98404, 0.02f);
        } else {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, "Flower: " + this.getPosDescription(beeInfo, beeInfo.flowerPos()), -256, 0.02f);
        }
        for (String string : beeInfo.goals()) {
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, string, -16711936, 0.02f);
        }
        if (bl) {
            this.renderPath(poseStack, multiBufferSource, beeInfo);
        }
        if (beeInfo.travelTicks() > 0) {
            int n2 = beeInfo.travelTicks() < 2400 ? -3355444 : -23296;
            BeeDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, beeInfo.pos(), n++, "Travelling: " + beeInfo.travelTicks() + " ticks", n2, 0.02f);
        }
    }

    private static void renderTextOverHive(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, HiveDebugPayload.HiveInfo hiveInfo, int n, int n2) {
        BeeDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, string, hiveInfo.pos(), n, n2);
    }

    private static void renderTextOverPos(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, BlockPos blockPos, int n, int n2) {
        double d = 1.3;
        double d2 = 0.2;
        double d3 = (double)blockPos.getX() + 0.5;
        double d4 = (double)blockPos.getY() + 1.3 + (double)n * 0.2;
        double d5 = (double)blockPos.getZ() + 0.5;
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d3, d4, d5, n2, 0.02f, true, 0.0f, true);
    }

    private static void renderTextOverMob(PoseStack poseStack, MultiBufferSource multiBufferSource, Position position, int n, String string, int n2, float f) {
        double d = 2.4;
        double d2 = 0.25;
        BlockPos blockPos = BlockPos.containing(position);
        double d3 = (double)blockPos.getX() + 0.5;
        double d4 = position.y() + 2.4 + (double)n * 0.25;
        double d5 = (double)blockPos.getZ() + 0.5;
        float f2 = 0.5f;
        DebugRenderer.renderFloatingText(poseStack, multiBufferSource, string, d3, d4, d5, n2, f, false, 0.5f, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getHiveMemberNames(HiveDebugPayload.HiveInfo hiveInfo) {
        return this.getHiveMembers(hiveInfo.pos()).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private String getPosDescription(BeeDebugPayload.BeeInfo beeInfo, BlockPos blockPos) {
        double d = Math.sqrt(blockPos.distToCenterSqr(beeInfo.pos()));
        double d2 = (double)Math.round(d * 10.0) / 10.0;
        return blockPos.toShortString() + " (dist " + d2 + ")";
    }

    private boolean isBeeSelected(BeeDebugPayload.BeeInfo beeInfo) {
        return Objects.equals(this.lastLookedAtUuid, beeInfo.uuid());
    }

    private boolean isPlayerCloseEnoughToMob(BeeDebugPayload.BeeInfo beeInfo) {
        LocalPlayer localPlayer = this.minecraft.player;
        BlockPos blockPos = BlockPos.containing(localPlayer.getX(), beeInfo.pos().y(), localPlayer.getZ());
        BlockPos blockPos2 = BlockPos.containing(beeInfo.pos());
        return blockPos.closerThan(blockPos2, 30.0);
    }

    private Collection<UUID> getHiveMembers(BlockPos blockPos) {
        return this.beeInfosPerEntity.values().stream().filter(beeInfo -> beeInfo.hasHive(blockPos)).map(BeeDebugPayload.BeeInfo::uuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostHives() {
        HashMap hashMap = Maps.newHashMap();
        for (BeeDebugPayload.BeeInfo beeInfo : this.beeInfosPerEntity.values()) {
            if (beeInfo.hivePos() == null || this.hives.containsKey(beeInfo.hivePos())) continue;
            hashMap.computeIfAbsent(beeInfo.hivePos(), blockPos -> Lists.newArrayList()).add(beeInfo.generateName());
        }
        return hashMap;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> {
            this.lastLookedAtUuid = entity.getUUID();
        });
    }

    static final class HiveDebugInfo
    extends Record {
        final HiveDebugPayload.HiveInfo info;
        private final long lastSeen;

        HiveDebugInfo(HiveDebugPayload.HiveInfo hiveInfo, long l) {
            this.info = hiveInfo;
            this.lastSeen = l;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{HiveDebugInfo.class, "info;lastSeen", "info", "lastSeen"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{HiveDebugInfo.class, "info;lastSeen", "info", "lastSeen"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{HiveDebugInfo.class, "info;lastSeen", "info", "lastSeen"}, this, object);
        }

        public HiveDebugPayload.HiveInfo info() {
            return this.info;
        }

        public long lastSeen() {
            return this.lastSeen;
        }
    }
}

