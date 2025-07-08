/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.phys.Vec3;

public class CompassAngleState
extends NeedleDirectionHelper {
    public static final MapCodec<CompassAngleState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("wobble", (Object)true).forGetter(NeedleDirectionHelper::wobble), (App)CompassTarget.CODEC.fieldOf("target").forGetter(CompassAngleState::target)).apply((Applicative)instance, CompassAngleState::new));
    private final NeedleDirectionHelper.Wobbler wobbler;
    private final NeedleDirectionHelper.Wobbler noTargetWobbler;
    private final CompassTarget compassTarget;
    private final RandomSource random = RandomSource.create();

    public CompassAngleState(boolean bl, CompassTarget compassTarget) {
        super(bl);
        this.wobbler = this.newWobbler(0.8f);
        this.noTargetWobbler = this.newWobbler(0.8f);
        this.compassTarget = compassTarget;
    }

    @Override
    protected float calculate(ItemStack itemStack, ClientLevel clientLevel, int n, Entity entity) {
        GlobalPos globalPos = this.compassTarget.get(clientLevel, itemStack, entity);
        long l = clientLevel.getGameTime();
        if (!CompassAngleState.isValidCompassTargetPos(entity, globalPos)) {
            return this.getRandomlySpinningRotation(n, l);
        }
        return this.getRotationTowardsCompassTarget(entity, l, globalPos.pos());
    }

    private float getRandomlySpinningRotation(int n, long l) {
        if (this.noTargetWobbler.shouldUpdate(l)) {
            this.noTargetWobbler.update(l, this.random.nextFloat());
        }
        float f = this.noTargetWobbler.rotation() + (float)CompassAngleState.hash(n) / 2.1474836E9f;
        return Mth.positiveModulo(f, 1.0f);
    }

    private float getRotationTowardsCompassTarget(Entity entity, long l, BlockPos blockPos) {
        float f;
        Player player;
        float f2 = (float)CompassAngleState.getAngleFromEntityToPos(entity, blockPos);
        float f3 = CompassAngleState.getWrappedVisualRotationY(entity);
        if (entity instanceof Player && (player = (Player)entity).isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (this.wobbler.shouldUpdate(l)) {
                this.wobbler.update(l, 0.5f - (f3 - 0.25f));
            }
            f = f2 + this.wobbler.rotation();
        } else {
            f = 0.5f - (f3 - 0.25f - f2);
        }
        return Mth.positiveModulo(f, 1.0f);
    }

    private static boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos globalPos) {
        return globalPos != null && globalPos.dimension() == entity.level().dimension() && !(globalPos.pos().distToCenterSqr(entity.position()) < (double)1.0E-5f);
    }

    private static double getAngleFromEntityToPos(Entity entity, BlockPos blockPos) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos);
        return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX()) / 6.2831854820251465;
    }

    private static float getWrappedVisualRotationY(Entity entity) {
        return Mth.positiveModulo(entity.getVisualRotationYInDegrees() / 360.0f, 1.0f);
    }

    private static int hash(int n) {
        return n * 1327217883;
    }

    protected CompassTarget target() {
        return this.compassTarget;
    }

    public static enum CompassTarget implements StringRepresentable
    {
        NONE("none"){

            @Override
            @Nullable
            public GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, Entity entity) {
                return null;
            }
        }
        ,
        LODESTONE("lodestone"){

            @Override
            @Nullable
            public GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, Entity entity) {
                LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
                return lodestoneTracker != null ? (GlobalPos)lodestoneTracker.target().orElse(null) : null;
            }
        }
        ,
        SPAWN("spawn"){

            @Override
            public GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, Entity entity) {
                return GlobalPos.of(clientLevel.dimension(), clientLevel.getSharedSpawnPos());
            }
        }
        ,
        RECOVERY("recovery"){

            @Override
            @Nullable
            public GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, Entity entity) {
                GlobalPos globalPos;
                if (entity instanceof Player) {
                    Player player = (Player)entity;
                    globalPos = player.getLastDeathLocation().orElse(null);
                } else {
                    globalPos = null;
                }
                return globalPos;
            }
        };

        public static final Codec<CompassTarget> CODEC;
        private final String name;

        CompassTarget(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Nullable
        abstract GlobalPos get(ClientLevel var1, ItemStack var2, Entity var3);

        static {
            CODEC = StringRepresentable.fromEnum(CompassTarget::values);
        }
    }
}

