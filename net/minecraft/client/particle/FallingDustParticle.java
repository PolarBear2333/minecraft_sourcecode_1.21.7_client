/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingDustParticle
extends TextureSheetParticle {
    private final float rotSpeed;
    private final SpriteSet sprites;

    FallingDustParticle(ClientLevel clientLevel, double d, double d2, double d3, float f, float f2, float f3, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3);
        this.sprites = spriteSet;
        this.rCol = f;
        this.gCol = f2;
        this.bCol = f3;
        float f4 = 0.9f;
        this.quadSize *= 0.67499995f;
        int n = (int)(32.0 / (Math.random() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)n * 0.9f, 1.0f);
        this.setSpriteFromAge(spriteSet);
        this.rotSpeed = ((float)Math.random() - 0.5f) * 0.1f;
        this.roll = (float)Math.random() * ((float)Math.PI * 2);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setSpriteFromAge(this.sprites);
        this.oRoll = this.roll;
        this.roll += (float)Math.PI * this.rotSpeed * 2.0f;
        if (this.onGround) {
            this.roll = 0.0f;
            this.oRoll = 0.0f;
        }
        this.move(this.xd, this.yd, this.zd);
        this.yd -= (double)0.003f;
        this.yd = Math.max(this.yd, (double)-0.14f);
    }

    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        @Nullable
        public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            BlockState blockState = blockParticleOption.getState();
            if (!blockState.isAir() && blockState.getRenderShape() == RenderShape.INVISIBLE) {
                return null;
            }
            BlockPos blockPos = BlockPos.containing(d, d2, d3);
            int n = Minecraft.getInstance().getBlockColors().getColor(blockState, clientLevel, blockPos);
            if (blockState.getBlock() instanceof FallingBlock) {
                n = ((FallingBlock)blockState.getBlock()).getDustColor(blockState, clientLevel, blockPos);
            }
            float f = (float)(n >> 16 & 0xFF) / 255.0f;
            float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
            float f3 = (float)(n & 0xFF) / 255.0f;
            return new FallingDustParticle(clientLevel, d, d2, d3, f, f2, f3, this.sprite);
        }

        @Override
        @Nullable
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((BlockParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

