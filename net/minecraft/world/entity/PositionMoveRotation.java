/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public record PositionMoveRotation(Vec3 position, Vec3 deltaMovement, float yRot, float xRot) {
    public static final StreamCodec<FriendlyByteBuf, PositionMoveRotation> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, PositionMoveRotation::position, Vec3.STREAM_CODEC, PositionMoveRotation::deltaMovement, ByteBufCodecs.FLOAT, PositionMoveRotation::yRot, ByteBufCodecs.FLOAT, PositionMoveRotation::xRot, PositionMoveRotation::new);

    public static PositionMoveRotation of(Entity entity) {
        if (entity.isInterpolating()) {
            return new PositionMoveRotation(entity.getInterpolation().position(), entity.getKnownMovement(), entity.getInterpolation().yRot(), entity.getInterpolation().xRot());
        }
        return new PositionMoveRotation(entity.position(), entity.getKnownMovement(), entity.getYRot(), entity.getXRot());
    }

    public static PositionMoveRotation of(TeleportTransition teleportTransition) {
        return new PositionMoveRotation(teleportTransition.position(), teleportTransition.deltaMovement(), teleportTransition.yRot(), teleportTransition.xRot());
    }

    public static PositionMoveRotation calculateAbsolute(PositionMoveRotation positionMoveRotation, PositionMoveRotation positionMoveRotation2, Set<Relative> set) {
        double d = set.contains((Object)Relative.X) ? positionMoveRotation.position.x : 0.0;
        double d2 = set.contains((Object)Relative.Y) ? positionMoveRotation.position.y : 0.0;
        double d3 = set.contains((Object)Relative.Z) ? positionMoveRotation.position.z : 0.0;
        float f = set.contains((Object)Relative.Y_ROT) ? positionMoveRotation.yRot : 0.0f;
        float f2 = set.contains((Object)Relative.X_ROT) ? positionMoveRotation.xRot : 0.0f;
        Vec3 vec3 = new Vec3(d + positionMoveRotation2.position.x, d2 + positionMoveRotation2.position.y, d3 + positionMoveRotation2.position.z);
        float f3 = f + positionMoveRotation2.yRot;
        float f4 = Mth.clamp(f2 + positionMoveRotation2.xRot, -90.0f, 90.0f);
        Vec3 vec32 = positionMoveRotation.deltaMovement;
        if (set.contains((Object)Relative.ROTATE_DELTA)) {
            float f5 = positionMoveRotation.yRot - f3;
            float f6 = positionMoveRotation.xRot - f4;
            vec32 = vec32.xRot((float)Math.toRadians(f6));
            vec32 = vec32.yRot((float)Math.toRadians(f5));
        }
        Vec3 vec33 = new Vec3(PositionMoveRotation.calculateDelta(vec32.x, positionMoveRotation2.deltaMovement.x, set, Relative.DELTA_X), PositionMoveRotation.calculateDelta(vec32.y, positionMoveRotation2.deltaMovement.y, set, Relative.DELTA_Y), PositionMoveRotation.calculateDelta(vec32.z, positionMoveRotation2.deltaMovement.z, set, Relative.DELTA_Z));
        return new PositionMoveRotation(vec3, vec33, f3, f4);
    }

    private static double calculateDelta(double d, double d2, Set<Relative> set, Relative relative) {
        return set.contains((Object)relative) ? d + d2 : d2;
    }
}

