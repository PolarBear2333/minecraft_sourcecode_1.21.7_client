/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class BrainDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean SHOW_NAME_FOR_ALL = true;
    private static final boolean SHOW_PROFESSION_FOR_ALL = false;
    private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
    private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
    private static final boolean SHOW_INVENTORY_FOR_ALL = false;
    private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
    private static final boolean SHOW_PATH_FOR_ALL = false;
    private static final boolean SHOW_HEALTH_FOR_ALL = false;
    private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
    private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
    private static final boolean SHOW_NAME_FOR_SELECTED = true;
    private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
    private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
    private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
    private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
    private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
    private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
    private static final boolean SHOW_PATH_FOR_SELECTED = true;
    private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
    private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
    private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
    private static final boolean SHOW_POI_INFO = true;
    private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
    private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final float TEXT_SCALE = 0.02f;
    private static final int CYAN = -16711681;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int ORANGE = -23296;
    private final Minecraft minecraft;
    private final Map<BlockPos, PoiInfo> pois = Maps.newHashMap();
    private final Map<UUID, BrainDebugPayload.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
    @Nullable
    private UUID lastLookedAtUuid;

    public BrainDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void clear() {
        this.pois.clear();
        this.brainDumpsPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addPoi(PoiInfo poiInfo) {
        this.pois.put(poiInfo.pos, poiInfo);
    }

    public void removePoi(BlockPos blockPos) {
        this.pois.remove(blockPos);
    }

    public void setFreeTicketCount(BlockPos blockPos, int n) {
        PoiInfo poiInfo = this.pois.get(blockPos);
        if (poiInfo == null) {
            LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)blockPos);
            return;
        }
        poiInfo.freeTicketCount = n;
    }

    public void addOrUpdateBrainDump(BrainDebugPayload.BrainDump brainDump) {
        this.brainDumpsPerEntity.put(brainDump.uuid(), brainDump);
    }

    public void removeBrainDump(int n) {
        this.brainDumpsPerEntity.values().removeIf(brainDump -> brainDump.id() == n);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        this.clearRemovedEntities();
        this.doRender(poseStack, multiBufferSource, d, d2, d3);
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void clearRemovedEntities() {
        this.brainDumpsPerEntity.entrySet().removeIf(entry -> {
            Entity entity = this.minecraft.level.getEntity(((BrainDebugPayload.BrainDump)entry.getValue()).id());
            return entity == null || entity.isRemoved();
        });
    }

    private void doRender(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        BlockPos blockPos = BlockPos.containing(d, d2, d3);
        this.brainDumpsPerEntity.values().forEach(brainDump -> {
            if (this.isPlayerCloseEnoughToMob((BrainDebugPayload.BrainDump)brainDump)) {
                this.renderBrainInfo(poseStack, multiBufferSource, (BrainDebugPayload.BrainDump)brainDump, d, d2, d3);
            }
        });
        for (BlockPos blockPos3 : this.pois.keySet()) {
            if (!blockPos.closerThan(blockPos3, 30.0)) continue;
            BrainDebugRenderer.highlightPoi(poseStack, multiBufferSource, blockPos3);
        }
        this.pois.values().forEach(poiInfo -> {
            if (blockPos.closerThan(poiInfo.pos, 30.0)) {
                this.renderPoiInfo(poseStack, multiBufferSource, (PoiInfo)poiInfo);
            }
        });
        this.getGhostPois().forEach((blockPos2, list) -> {
            if (blockPos.closerThan((Vec3i)blockPos2, 30.0)) {
                this.renderGhostPoi(poseStack, multiBufferSource, (BlockPos)blockPos2, (List<String>)list);
            }
        });
    }

    private static void highlightPoi(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void renderGhostPoi(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, List<String> list) {
        float f = 0.05f;
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        BrainDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, String.valueOf(list), blockPos, 0, -256);
        BrainDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, "Ghost POI", blockPos, 1, -65536);
    }

    private void renderPoiInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, PoiInfo poiInfo) {
        int n = 0;
        Set<String> set = this.getTicketHolderNames(poiInfo);
        if (set.size() < 4) {
            BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, "Owners: " + String.valueOf(set), poiInfo, n, -256);
        } else {
            BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, set.size() + " ticket holders", poiInfo, n, -256);
        }
        ++n;
        Set<String> set2 = this.getPotentialTicketHolderNames(poiInfo);
        if (set2.size() < 4) {
            BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, "Candidates: " + String.valueOf(set2), poiInfo, n, -23296);
        } else {
            BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, set2.size() + " potential owners", poiInfo, n, -23296);
        }
        BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, "Free tickets: " + poiInfo.freeTicketCount, poiInfo, ++n, -256);
        BrainDebugRenderer.renderTextOverPoi(poseStack, multiBufferSource, poiInfo.type, poiInfo, ++n, -1);
    }

    private void renderPath(PoseStack poseStack, MultiBufferSource multiBufferSource, BrainDebugPayload.BrainDump brainDump, double d, double d2, double d3) {
        if (brainDump.path() != null) {
            PathfindingRenderer.renderPath(poseStack, multiBufferSource, brainDump.path(), 0.5f, false, false, d, d2, d3);
        }
    }

    private void renderBrainInfo(PoseStack poseStack, MultiBufferSource multiBufferSource, BrainDebugPayload.BrainDump brainDump, double d, double d2, double d3) {
        boolean bl = this.isMobSelected(brainDump);
        int n = 0;
        BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, brainDump.name(), -1, 0.03f);
        ++n;
        if (bl) {
            BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, brainDump.profession() + " " + brainDump.xp() + " xp", -1, 0.02f);
            ++n;
        }
        if (bl) {
            int n2 = brainDump.health() < brainDump.maxHealth() ? -23296 : -1;
            BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, "health: " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brainDump.health())) + " / " + String.format(Locale.ROOT, "%.1f", Float.valueOf(brainDump.maxHealth())), n2, 0.02f);
            ++n;
        }
        if (bl && !brainDump.inventory().equals("")) {
            BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, brainDump.inventory(), -98404, 0.02f);
            ++n;
        }
        if (bl) {
            for (String string : brainDump.behaviors()) {
                BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, string, -16711681, 0.02f);
                ++n;
            }
        }
        if (bl) {
            for (String string : brainDump.activities()) {
                BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, string, -16711936, 0.02f);
                ++n;
            }
        }
        if (brainDump.wantsGolem()) {
            BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, "Wants Golem", -23296, 0.02f);
            ++n;
        }
        if (bl && brainDump.angerLevel() != -1) {
            BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, "Anger Level: " + brainDump.angerLevel(), -98404, 0.02f);
            ++n;
        }
        if (bl) {
            for (String string : brainDump.gossips()) {
                if (string.startsWith(brainDump.name())) {
                    BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, string, -1, 0.02f);
                } else {
                    BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, string, -23296, 0.02f);
                }
                ++n;
            }
        }
        if (bl) {
            for (String string : Lists.reverse(brainDump.memories())) {
                BrainDebugRenderer.renderTextOverMob(poseStack, multiBufferSource, brainDump.pos(), n, string, -3355444, 0.02f);
                ++n;
            }
        }
        if (bl) {
            this.renderPath(poseStack, multiBufferSource, brainDump, d, d2, d3);
        }
    }

    private static void renderTextOverPoi(PoseStack poseStack, MultiBufferSource multiBufferSource, String string, PoiInfo poiInfo, int n, int n2) {
        BrainDebugRenderer.renderTextOverPos(poseStack, multiBufferSource, string, poiInfo.pos, n, n2);
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

    private Set<String> getTicketHolderNames(PoiInfo poiInfo) {
        return this.getTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private Set<String> getPotentialTicketHolderNames(PoiInfo poiInfo) {
        return this.getPotentialTicketHolders(poiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private boolean isMobSelected(BrainDebugPayload.BrainDump brainDump) {
        return Objects.equals(this.lastLookedAtUuid, brainDump.uuid());
    }

    private boolean isPlayerCloseEnoughToMob(BrainDebugPayload.BrainDump brainDump) {
        LocalPlayer localPlayer = this.minecraft.player;
        BlockPos blockPos = BlockPos.containing(localPlayer.getX(), brainDump.pos().y(), localPlayer.getZ());
        BlockPos blockPos2 = BlockPos.containing(brainDump.pos());
        return blockPos.closerThan(blockPos2, 30.0);
    }

    private Collection<UUID> getTicketHolders(BlockPos blockPos) {
        return this.brainDumpsPerEntity.values().stream().filter(brainDump -> brainDump.hasPoi(blockPos)).map(BrainDebugPayload.BrainDump::uuid).collect(Collectors.toSet());
    }

    private Collection<UUID> getPotentialTicketHolders(BlockPos blockPos) {
        return this.brainDumpsPerEntity.values().stream().filter(brainDump -> brainDump.hasPotentialPoi(blockPos)).map(BrainDebugPayload.BrainDump::uuid).collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostPois() {
        HashMap hashMap = Maps.newHashMap();
        for (BrainDebugPayload.BrainDump brainDump : this.brainDumpsPerEntity.values()) {
            for (BlockPos blockPos2 : Iterables.concat(brainDump.pois(), brainDump.potentialPois())) {
                if (this.pois.containsKey(blockPos2)) continue;
                hashMap.computeIfAbsent(blockPos2, blockPos -> Lists.newArrayList()).add(brainDump.name());
            }
        }
        return hashMap;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> {
            this.lastLookedAtUuid = entity.getUUID();
        });
    }

    public static class PoiInfo {
        public final BlockPos pos;
        public final String type;
        public int freeTicketCount;

        public PoiInfo(BlockPos blockPos, String string, int n) {
            this.pos = blockPos;
            this.type = string;
            this.freeTicketCount = n;
        }
    }
}

