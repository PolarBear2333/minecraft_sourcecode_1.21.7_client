/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.portal;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

public record TeleportTransition(ServerLevel newLevel, Vec3 position, Vec3 deltaMovement, float yRot, float xRot, boolean missingRespawnBlock, boolean asPassenger, Set<Relative> relatives, PostTeleportTransition postTeleportTransition) {
    public static final PostTeleportTransition DO_NOTHING = entity -> {};
    public static final PostTeleportTransition PLAY_PORTAL_SOUND = TeleportTransition::playPortalSound;
    public static final PostTeleportTransition PLACE_PORTAL_TICKET = TeleportTransition::placePortalTicket;

    public TeleportTransition(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, float f, float f2, PostTeleportTransition postTeleportTransition) {
        this(serverLevel, vec3, vec32, f, f2, Set.of(), postTeleportTransition);
    }

    public TeleportTransition(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, float f, float f2, Set<Relative> set, PostTeleportTransition postTeleportTransition) {
        this(serverLevel, vec3, vec32, f, f2, false, false, set, postTeleportTransition);
    }

    public TeleportTransition(ServerLevel serverLevel, Entity entity, PostTeleportTransition postTeleportTransition) {
        this(serverLevel, TeleportTransition.findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, serverLevel.getSharedSpawnAngle(), 0.0f, false, false, Set.of(), postTeleportTransition);
    }

    private static void playPortalSound(Entity entity) {
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        }
    }

    private static void placePortalTicket(Entity entity) {
        entity.placePortalTicket(BlockPos.containing(entity.position()));
    }

    public static TeleportTransition missingRespawnBlock(ServerLevel serverLevel, Entity entity, PostTeleportTransition postTeleportTransition) {
        return new TeleportTransition(serverLevel, TeleportTransition.findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, serverLevel.getSharedSpawnAngle(), 0.0f, true, false, Set.of(), postTeleportTransition);
    }

    private static Vec3 findAdjustedSharedSpawnPos(ServerLevel serverLevel, Entity entity) {
        return entity.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter();
    }

    public TeleportTransition withRotation(float f, float f2) {
        return new TeleportTransition(this.newLevel(), this.position(), this.deltaMovement(), f, f2, this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTransition withPosition(Vec3 vec3) {
        return new TeleportTransition(this.newLevel(), vec3, this.deltaMovement(), this.yRot(), this.xRot(), this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTransition transitionAsPassenger() {
        return new TeleportTransition(this.newLevel(), this.position(), this.deltaMovement(), this.yRot(), this.xRot(), this.missingRespawnBlock(), true, this.relatives(), this.postTeleportTransition());
    }

    @FunctionalInterface
    public static interface PostTeleportTransition {
        public void onTransition(Entity var1);

        default public PostTeleportTransition then(PostTeleportTransition postTeleportTransition) {
            return entity -> {
                this.onTransition(entity);
                postTeleportTransition.onTransition(entity);
            };
        }
    }
}

