/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class DripParticle
extends TextureSheetParticle {
    private final Fluid type;
    protected boolean isGlowing;

    DripParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid) {
        super(clientLevel, d, d2, d3);
        this.setSize(0.01f, 0.01f);
        this.gravity = 0.06f;
        this.type = fluid;
    }

    protected Fluid getType() {
        return this.type;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float f) {
        if (this.isGlowing) {
            return 240;
        }
        return super.getLightColor(f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();
        if (this.removed) {
            return;
        }
        this.yd -= (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.postMoveUpdate();
        if (this.removed) {
            return;
        }
        this.xd *= (double)0.98f;
        this.yd *= (double)0.98f;
        this.zd *= (double)0.98f;
        if (this.type == Fluids.EMPTY) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
        FluidState fluidState = this.level.getFluidState(blockPos);
        if (fluidState.getType() == this.type && this.y < (double)((float)blockPos.getY() + fluidState.getHeight(this.level, blockPos))) {
            this.remove();
        }
    }

    protected void preMoveUpdate() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }
    }

    protected void postMoveUpdate() {
    }

    public static TextureSheetParticle createWaterHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, d2, d3, Fluids.WATER, ParticleTypes.FALLING_WATER);
        dripHangParticle.setColor(0.2f, 0.3f, 1.0f);
        return dripHangParticle;
    }

    public static TextureSheetParticle createWaterFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        FallAndLandParticle fallAndLandParticle = new FallAndLandParticle(clientLevel, d, d2, d3, (Fluid)Fluids.WATER, ParticleTypes.SPLASH);
        fallAndLandParticle.setColor(0.2f, 0.3f, 1.0f);
        return fallAndLandParticle;
    }

    public static TextureSheetParticle createLavaHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        return new CoolingDripHangParticle(clientLevel, d, d2, d3, Fluids.LAVA, ParticleTypes.FALLING_LAVA);
    }

    public static TextureSheetParticle createLavaFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        FallAndLandParticle fallAndLandParticle = new FallAndLandParticle(clientLevel, d, d2, d3, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA);
        fallAndLandParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
        return fallAndLandParticle;
    }

    public static TextureSheetParticle createLavaLandParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripLandParticle dripLandParticle = new DripLandParticle(clientLevel, d, d2, d3, Fluids.LAVA);
        dripLandParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
        return dripLandParticle;
    }

    public static TextureSheetParticle createHoneyHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, d2, d3, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
        dripHangParticle.gravity *= 0.01f;
        dripHangParticle.lifetime = 100;
        dripHangParticle.setColor(0.622f, 0.508f, 0.082f);
        return dripHangParticle;
    }

    public static TextureSheetParticle createHoneyFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        HoneyFallAndLandParticle honeyFallAndLandParticle = new HoneyFallAndLandParticle(clientLevel, d, d2, d3, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
        honeyFallAndLandParticle.gravity = 0.01f;
        honeyFallAndLandParticle.setColor(0.582f, 0.448f, 0.082f);
        return honeyFallAndLandParticle;
    }

    public static TextureSheetParticle createHoneyLandParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripLandParticle dripLandParticle = new DripLandParticle(clientLevel, d, d2, d3, Fluids.EMPTY);
        dripLandParticle.lifetime = (int)(128.0 / (Math.random() * 0.8 + 0.2));
        dripLandParticle.setColor(0.522f, 0.408f, 0.082f);
        return dripLandParticle;
    }

    public static TextureSheetParticle createDripstoneWaterHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, d2, d3, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER);
        dripHangParticle.setColor(0.2f, 0.3f, 1.0f);
        return dripHangParticle;
    }

    public static TextureSheetParticle createDripstoneWaterFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripstoneFallAndLandParticle dripstoneFallAndLandParticle = new DripstoneFallAndLandParticle(clientLevel, d, d2, d3, (Fluid)Fluids.WATER, ParticleTypes.SPLASH);
        dripstoneFallAndLandParticle.setColor(0.2f, 0.3f, 1.0f);
        return dripstoneFallAndLandParticle;
    }

    public static TextureSheetParticle createDripstoneLavaHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        return new CoolingDripHangParticle(clientLevel, d, d2, d3, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA);
    }

    public static TextureSheetParticle createDripstoneLavaFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripstoneFallAndLandParticle dripstoneFallAndLandParticle = new DripstoneFallAndLandParticle(clientLevel, d, d2, d3, (Fluid)Fluids.LAVA, ParticleTypes.LANDING_LAVA);
        dripstoneFallAndLandParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
        return dripstoneFallAndLandParticle;
    }

    public static TextureSheetParticle createNectarFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        FallingParticle fallingParticle = new FallingParticle(clientLevel, d, d2, d3, Fluids.EMPTY);
        fallingParticle.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        fallingParticle.gravity = 0.007f;
        fallingParticle.setColor(0.92f, 0.782f, 0.72f);
        return fallingParticle;
    }

    public static TextureSheetParticle createSporeBlossomFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        int n = (int)(64.0f / Mth.randomBetween(clientLevel.getRandom(), 0.1f, 0.9f));
        FallingParticle fallingParticle = new FallingParticle(clientLevel, d, d2, d3, Fluids.EMPTY, n);
        fallingParticle.gravity = 0.005f;
        fallingParticle.setColor(0.32f, 0.5f, 0.22f);
        return fallingParticle;
    }

    public static TextureSheetParticle createObsidianTearHangParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, d2, d3, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
        dripHangParticle.isGlowing = true;
        dripHangParticle.gravity *= 0.01f;
        dripHangParticle.lifetime = 100;
        dripHangParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
        return dripHangParticle;
    }

    public static TextureSheetParticle createObsidianTearFallParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        FallAndLandParticle fallAndLandParticle = new FallAndLandParticle(clientLevel, d, d2, d3, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
        fallAndLandParticle.isGlowing = true;
        fallAndLandParticle.gravity = 0.01f;
        fallAndLandParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
        return fallAndLandParticle;
    }

    public static TextureSheetParticle createObsidianTearLandParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        DripLandParticle dripLandParticle = new DripLandParticle(clientLevel, d, d2, d3, Fluids.EMPTY);
        dripLandParticle.isGlowing = true;
        dripLandParticle.lifetime = (int)(28.0 / (Math.random() * 0.8 + 0.2));
        dripLandParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
        return dripLandParticle;
    }

    static class DripHangParticle
    extends DripParticle {
        private final ParticleOptions fallingParticle;

        DripHangParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, ParticleOptions particleOptions) {
            super(clientLevel, d, d2, d3, fluid);
            this.fallingParticle = particleOptions;
            this.gravity *= 0.02f;
            this.lifetime = 40;
        }

        @Override
        protected void preMoveUpdate() {
            if (this.lifetime-- <= 0) {
                this.remove();
                this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
        }

        @Override
        protected void postMoveUpdate() {
            this.xd *= 0.02;
            this.yd *= 0.02;
            this.zd *= 0.02;
        }
    }

    static class FallAndLandParticle
    extends FallingParticle {
        protected final ParticleOptions landParticle;

        FallAndLandParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, ParticleOptions particleOptions) {
            super(clientLevel, d, d2, d3, fluid);
            this.landParticle = particleOptions;
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
        }
    }

    static class CoolingDripHangParticle
    extends DripHangParticle {
        CoolingDripHangParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, ParticleOptions particleOptions) {
            super(clientLevel, d, d2, d3, fluid, particleOptions);
        }

        @Override
        protected void preMoveUpdate() {
            this.rCol = 1.0f;
            this.gCol = 16.0f / (float)(40 - this.lifetime + 16);
            this.bCol = 4.0f / (float)(40 - this.lifetime + 8);
            super.preMoveUpdate();
        }
    }

    static class DripLandParticle
    extends DripParticle {
        DripLandParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid) {
            super(clientLevel, d, d2, d3, fluid);
            this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        }
    }

    static class HoneyFallAndLandParticle
    extends FallAndLandParticle {
        HoneyFallAndLandParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, ParticleOptions particleOptions) {
            super(clientLevel, d, d2, d3, fluid, particleOptions);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                float f = Mth.randomBetween(this.random, 0.3f, 1.0f);
                this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, f, 1.0f, false);
            }
        }
    }

    static class DripstoneFallAndLandParticle
    extends FallAndLandParticle {
        DripstoneFallAndLandParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, ParticleOptions particleOptions) {
            super(clientLevel, d, d2, d3, fluid, particleOptions);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                SoundEvent soundEvent = this.getType() == Fluids.LAVA ? SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;
                float f = Mth.randomBetween(this.random, 0.3f, 1.0f);
                this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.BLOCKS, f, 1.0f, false);
            }
        }
    }

    static class FallingParticle
    extends DripParticle {
        FallingParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid) {
            this(clientLevel, d, d2, d3, fluid, (int)(64.0 / (Math.random() * 0.8 + 0.2)));
        }

        FallingParticle(ClientLevel clientLevel, double d, double d2, double d3, Fluid fluid, int n) {
            super(clientLevel, d, d2, d3, fluid);
            this.lifetime = n;
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
            }
        }
    }
}

