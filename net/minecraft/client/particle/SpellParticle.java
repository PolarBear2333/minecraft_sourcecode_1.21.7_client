/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SpellParticle
extends TextureSheetParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;
    private float originalAlpha = 1.0f;

    SpellParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, 0.5 - RANDOM.nextDouble(), d5, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96f;
        this.gravity = -0.1f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.yd *= (double)0.2f;
        if (d4 == 0.0 && d6 == 0.0) {
            this.xd *= (double)0.1f;
            this.zd *= (double)0.1f;
        }
        this.quadSize *= 0.75f;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = this.isCloseToScopingPlayer() ? 0.0f : Mth.lerp(0.05f, this.alpha, this.originalAlpha);
    }

    @Override
    protected void setAlpha(float f) {
        super.setAlpha(f);
        this.originalAlpha = f;
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && localPlayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0 && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping();
    }

    public static class InstantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class WitchProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            float f = clientLevel.random.nextFloat() * 0.5f + 0.35f;
            spellParticle.setColor(1.0f * f, 0.0f * f, 1.0f * f);
            return spellParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class MobEffectProvider
    implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public MobEffectProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            spellParticle.setColor(colorParticleOption.getRed(), colorParticleOption.getGreen(), colorParticleOption.getBlue());
            ((Particle)spellParticle).setAlpha(colorParticleOption.getAlpha());
            return spellParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((ColorParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

