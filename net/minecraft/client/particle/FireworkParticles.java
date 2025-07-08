/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.FireworkExplosion;

public class FireworkParticles {

    public static class SparkProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SparkProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SparkParticle sparkParticle = new SparkParticle(clientLevel, d, d2, d3, d4, d5, d6, Minecraft.getInstance().particleEngine, this.sprites);
            sparkParticle.setAlpha(0.99f);
            return sparkParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class FlashProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public FlashProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            OverlayParticle overlayParticle = new OverlayParticle(clientLevel, d, d2, d3);
            overlayParticle.pickSprite(this.sprite);
            return overlayParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class OverlayParticle
    extends TextureSheetParticle {
        OverlayParticle(ClientLevel clientLevel, double d, double d2, double d3) {
            super(clientLevel, d, d2, d3);
            this.lifetime = 4;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
            this.setAlpha(0.6f - ((float)this.age + f - 1.0f) * 0.25f * 0.5f);
            super.render(vertexConsumer, camera, f);
        }

        @Override
        public float getQuadSize(float f) {
            return 7.1f * Mth.sin(((float)this.age + f - 1.0f) * 0.25f * (float)Math.PI);
        }
    }

    static class SparkParticle
    extends SimpleAnimatedParticle {
        private boolean trail;
        private boolean twinkle;
        private final ParticleEngine engine;
        private float fadeR;
        private float fadeG;
        private float fadeB;
        private boolean hasFade;

        SparkParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, ParticleEngine particleEngine, SpriteSet spriteSet) {
            super(clientLevel, d, d2, d3, spriteSet, 0.1f);
            this.xd = d4;
            this.yd = d5;
            this.zd = d6;
            this.engine = particleEngine;
            this.quadSize *= 0.75f;
            this.lifetime = 48 + this.random.nextInt(12);
            this.setSpriteFromAge(spriteSet);
        }

        public void setTrail(boolean bl) {
            this.trail = bl;
        }

        public void setTwinkle(boolean bl) {
            this.twinkle = bl;
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
            if (!this.twinkle || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
                super.render(vertexConsumer, camera, f);
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
                SparkParticle sparkParticle = new SparkParticle(this.level, this.x, this.y, this.z, 0.0, 0.0, 0.0, this.engine, this.sprites);
                sparkParticle.setAlpha(0.99f);
                sparkParticle.setColor(this.rCol, this.gCol, this.bCol);
                sparkParticle.age = sparkParticle.lifetime / 2;
                if (this.hasFade) {
                    sparkParticle.hasFade = true;
                    sparkParticle.fadeR = this.fadeR;
                    sparkParticle.fadeG = this.fadeG;
                    sparkParticle.fadeB = this.fadeB;
                }
                sparkParticle.twinkle = this.twinkle;
                this.engine.add(sparkParticle);
            }
        }
    }

    public static class Starter
    extends NoRenderParticle {
        private static final double[][] CREEPER_PARTICLE_COORDS = new double[][]{{0.0, 0.2}, {0.2, 0.2}, {0.2, 0.6}, {0.6, 0.6}, {0.6, 0.2}, {0.2, 0.2}, {0.2, 0.0}, {0.4, 0.0}, {0.4, -0.6}, {0.2, -0.6}, {0.2, -0.4}, {0.0, -0.4}};
        private static final double[][] STAR_PARTICLE_COORDS = new double[][]{{0.0, 1.0}, {0.3455, 0.309}, {0.9511, 0.309}, {0.3795918367346939, -0.12653061224489795}, {0.6122448979591837, -0.8040816326530612}, {0.0, -0.35918367346938773}};
        private int life;
        private final ParticleEngine engine;
        private final List<FireworkExplosion> explosions;
        private boolean twinkleDelay;

        public Starter(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, ParticleEngine particleEngine, List<FireworkExplosion> list) {
            super(clientLevel, d, d2, d3);
            this.xd = d4;
            this.yd = d5;
            this.zd = d6;
            this.engine = particleEngine;
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Cannot create firework starter with no explosions");
            }
            this.explosions = list;
            this.lifetime = list.size() * 2 - 1;
            for (FireworkExplosion fireworkExplosion : list) {
                if (!fireworkExplosion.hasTwinkle()) continue;
                this.twinkleDelay = true;
                this.lifetime += 15;
                break;
            }
        }

        @Override
        public void tick() {
            int n;
            if (this.life == 0) {
                n = this.isFarAwayFromCamera();
                boolean bl = false;
                if (this.explosions.size() >= 3) {
                    bl = true;
                } else {
                    for (FireworkExplosion fireworkExplosion : this.explosions) {
                        if (fireworkExplosion.shape() != FireworkExplosion.Shape.LARGE_BALL) continue;
                        bl = true;
                        break;
                    }
                }
                SoundEvent soundEvent = bl ? (n != 0 ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST) : (n != 0 ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST);
                this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.AMBIENT, 20.0f, 0.95f + this.random.nextFloat() * 0.1f, true);
            }
            if (this.life % 2 == 0 && this.life / 2 < this.explosions.size()) {
                n = this.life / 2;
                FireworkExplosion fireworkExplosion = this.explosions.get(n);
                boolean bl = fireworkExplosion.hasTrail();
                boolean bl2 = fireworkExplosion.hasTwinkle();
                IntList intList = fireworkExplosion.colors();
                IntList intList2 = fireworkExplosion.fadeColors();
                if (intList.isEmpty()) {
                    intList = IntList.of((int)DyeColor.BLACK.getFireworkColor());
                }
                switch (fireworkExplosion.shape()) {
                    case SMALL_BALL: {
                        this.createParticleBall(0.25, 2, intList, intList2, bl, bl2);
                        break;
                    }
                    case LARGE_BALL: {
                        this.createParticleBall(0.5, 4, intList, intList2, bl, bl2);
                        break;
                    }
                    case STAR: {
                        this.createParticleShape(0.5, STAR_PARTICLE_COORDS, intList, intList2, bl, bl2, false);
                        break;
                    }
                    case CREEPER: {
                        this.createParticleShape(0.5, CREEPER_PARTICLE_COORDS, intList, intList2, bl, bl2, true);
                        break;
                    }
                    case BURST: {
                        this.createParticleBurst(intList, intList2, bl, bl2);
                    }
                }
                int n2 = intList.getInt(0);
                Particle particle = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                particle.setColor((float)ARGB.red(n2) / 255.0f, (float)ARGB.green(n2) / 255.0f, (float)ARGB.blue(n2) / 255.0f);
            }
            ++this.life;
            if (this.life > this.lifetime) {
                if (this.twinkleDelay) {
                    n = this.isFarAwayFromCamera() ? 1 : 0;
                    SoundEvent soundEvent = n != 0 ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
                    this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.AMBIENT, 20.0f, 0.9f + this.random.nextFloat() * 0.15f, true);
                }
                this.remove();
            }
        }

        private boolean isFarAwayFromCamera() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0;
        }

        private void createParticle(double d, double d2, double d3, double d4, double d5, double d6, IntList intList, IntList intList2, boolean bl, boolean bl2) {
            SparkParticle sparkParticle = (SparkParticle)this.engine.createParticle(ParticleTypes.FIREWORK, d, d2, d3, d4, d5, d6);
            sparkParticle.setTrail(bl);
            sparkParticle.setTwinkle(bl2);
            sparkParticle.setAlpha(0.99f);
            sparkParticle.setColor((Integer)Util.getRandom(intList, this.random));
            if (!intList2.isEmpty()) {
                sparkParticle.setFadeColor((Integer)Util.getRandom(intList2, this.random));
            }
        }

        private void createParticleBall(double d, int n, IntList intList, IntList intList2, boolean bl, boolean bl2) {
            double d2 = this.x;
            double d3 = this.y;
            double d4 = this.z;
            for (int i = -n; i <= n; ++i) {
                for (int j = -n; j <= n; ++j) {
                    for (int k = -n; k <= n; ++k) {
                        double d5 = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double d6 = (double)i + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double d7 = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5;
                        double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7) / d + this.random.nextGaussian() * 0.05;
                        this.createParticle(d2, d3, d4, d5 / d8, d6 / d8, d7 / d8, intList, intList2, bl, bl2);
                        if (i == -n || i == n || j == -n || j == n) continue;
                        k += n * 2 - 1;
                    }
                }
            }
        }

        private void createParticleShape(double d, double[][] dArray, IntList intList, IntList intList2, boolean bl, boolean bl2, boolean bl3) {
            double d2 = dArray[0][0];
            double d3 = dArray[0][1];
            this.createParticle(this.x, this.y, this.z, d2 * d, d3 * d, 0.0, intList, intList2, bl, bl2);
            float f = this.random.nextFloat() * (float)Math.PI;
            double d4 = bl3 ? 0.034 : 0.34;
            for (int i = 0; i < 3; ++i) {
                double d5 = (double)f + (double)((float)i * (float)Math.PI) * d4;
                double d6 = d2;
                double d7 = d3;
                for (int j = 1; j < dArray.length; ++j) {
                    double d8 = dArray[j][0];
                    double d9 = dArray[j][1];
                    for (double d10 = 0.25; d10 <= 1.0; d10 += 0.25) {
                        double d11 = Mth.lerp(d10, d6, d8) * d;
                        double d12 = Mth.lerp(d10, d7, d9) * d;
                        double d13 = d11 * Math.sin(d5);
                        d11 *= Math.cos(d5);
                        for (double d14 = -1.0; d14 <= 1.0; d14 += 2.0) {
                            this.createParticle(this.x, this.y, this.z, d11 * d14, d12, d13 * d14, intList, intList2, bl, bl2);
                        }
                    }
                    d6 = d8;
                    d7 = d9;
                }
            }
        }

        private void createParticleBurst(IntList intList, IntList intList2, boolean bl, boolean bl2) {
            double d = this.random.nextGaussian() * 0.05;
            double d2 = this.random.nextGaussian() * 0.05;
            for (int i = 0; i < 70; ++i) {
                double d3 = this.xd * 0.5 + this.random.nextGaussian() * 0.15 + d;
                double d4 = this.zd * 0.5 + this.random.nextGaussian() * 0.15 + d2;
                double d5 = this.yd * 0.5 + this.random.nextDouble() * 0.5;
                this.createParticle(this.x, this.y, this.z, d3, d5, d4, intList, intList2, bl, bl2);
            }
        }
    }
}

