/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList$Builder
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public abstract class RenderStateShard {
    public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = 8.0;
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    protected static final TextureStateShard BLOCK_SHEET_MIPPED = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, true);
    protected static final TextureStateShard BLOCK_SHEET = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false);
    protected static final EmptyTextureStateShard NO_TEXTURE = new EmptyTextureStateShard();
    protected static final TexturingStateShard DEFAULT_TEXTURING = new TexturingStateShard("default_texturing", () -> {}, () -> {});
    protected static final TexturingStateShard GLINT_TEXTURING = new TexturingStateShard("glint_texturing", () -> RenderStateShard.setupGlintTexturing(8.0f), RenderSystem::resetTextureMatrix);
    protected static final TexturingStateShard ENTITY_GLINT_TEXTURING = new TexturingStateShard("entity_glint_texturing", () -> RenderStateShard.setupGlintTexturing(0.5f), RenderSystem::resetTextureMatrix);
    protected static final TexturingStateShard ARMOR_ENTITY_GLINT_TEXTURING = new TexturingStateShard("armor_entity_glint_texturing", () -> RenderStateShard.setupGlintTexturing(0.16f), RenderSystem::resetTextureMatrix);
    protected static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
    protected static final LightmapStateShard NO_LIGHTMAP = new LightmapStateShard(false);
    protected static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
    protected static final OverlayStateShard NO_OVERLAY = new OverlayStateShard(false);
    protected static final LayeringStateShard NO_LAYERING = new LayeringStateShard("no_layering", () -> {}, () -> {});
    protected static final LayeringStateShard VIEW_OFFSET_Z_LAYERING = new LayeringStateShard("view_offset_z_layering", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        RenderSystem.getProjectionType().applyLayeringTransform((Matrix4f)matrix4fStack, 1.0f);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    });
    protected static final LayeringStateShard VIEW_OFFSET_Z_LAYERING_FORWARD = new LayeringStateShard("view_offset_z_layering_forward", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        RenderSystem.getProjectionType().applyLayeringTransform((Matrix4f)matrix4fStack, -1.0f);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    });
    protected static final OutputStateShard MAIN_TARGET = new OutputStateShard("main_target", () -> Minecraft.getInstance().getMainRenderTarget());
    protected static final OutputStateShard OUTLINE_TARGET = new OutputStateShard("outline_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.entityOutlineTarget();
        if (renderTarget != null) {
            return renderTarget;
        }
        return Minecraft.getInstance().getMainRenderTarget();
    });
    protected static final OutputStateShard TRANSLUCENT_TARGET = new OutputStateShard("translucent_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getTranslucentTarget();
        if (renderTarget != null) {
            return renderTarget;
        }
        return Minecraft.getInstance().getMainRenderTarget();
    });
    protected static final OutputStateShard PARTICLES_TARGET = new OutputStateShard("particles_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getParticlesTarget();
        if (renderTarget != null) {
            return renderTarget;
        }
        return Minecraft.getInstance().getMainRenderTarget();
    });
    protected static final OutputStateShard WEATHER_TARGET = new OutputStateShard("weather_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getWeatherTarget();
        if (renderTarget != null) {
            return renderTarget;
        }
        return Minecraft.getInstance().getMainRenderTarget();
    });
    protected static final OutputStateShard ITEM_ENTITY_TARGET = new OutputStateShard("item_entity_target", () -> {
        RenderTarget renderTarget = Minecraft.getInstance().levelRenderer.getItemEntityTarget();
        if (renderTarget != null) {
            return renderTarget;
        }
        return Minecraft.getInstance().getMainRenderTarget();
    });
    protected static final LineStateShard DEFAULT_LINE = new LineStateShard(OptionalDouble.of(1.0));

    public RenderStateShard(String string, Runnable runnable, Runnable runnable2) {
        this.name = string;
        this.setupState = runnable;
        this.clearState = runnable2;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    private static void setupGlintTexturing(float f) {
        long l = (long)((double)Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        float f2 = (float)(l % 110000L) / 110000.0f;
        float f3 = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-f2, f3, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(f);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    protected static class TextureStateShard
    extends EmptyTextureStateShard {
        private final Optional<ResourceLocation> texture;
        private final boolean mipmap;

        public TextureStateShard(ResourceLocation resourceLocation, boolean bl) {
            super(() -> {
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
                abstractTexture.setUseMipmaps(bl);
                RenderSystem.setShaderTexture(0, abstractTexture.getTextureView());
            }, () -> {});
            this.texture = Optional.of(resourceLocation);
            this.mipmap = bl;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.texture) + "(mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.texture;
        }
    }

    protected static class EmptyTextureStateShard
    extends RenderStateShard {
        public EmptyTextureStateShard(Runnable runnable, Runnable runnable2) {
            super("texture", runnable, runnable2);
        }

        EmptyTextureStateShard() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<ResourceLocation> cutoutTexture() {
            return Optional.empty();
        }
    }

    protected static class TexturingStateShard
    extends RenderStateShard {
        public TexturingStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class LightmapStateShard
    extends BooleanStateShard {
        public LightmapStateShard(boolean bl) {
            super("lightmap", () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                }
            }, () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
                }
            }, bl);
        }
    }

    protected static class OverlayStateShard
    extends BooleanStateShard {
        public OverlayStateShard(boolean bl) {
            super("overlay", () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
                }
            }, () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
                }
            }, bl);
        }
    }

    protected static class LayeringStateShard
    extends RenderStateShard {
        public LayeringStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    protected static class OutputStateShard
    extends RenderStateShard {
        private final Supplier<RenderTarget> renderTargetSupplier;

        public OutputStateShard(String string, Supplier<RenderTarget> supplier) {
            super(string, () -> {}, () -> {});
            this.renderTargetSupplier = supplier;
        }

        public RenderTarget getRenderTarget() {
            return this.renderTargetSupplier.get();
        }
    }

    protected static class LineStateShard
    extends RenderStateShard {
        private final OptionalDouble width;

        public LineStateShard(OptionalDouble optionalDouble) {
            super("line_width", () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    if (optionalDouble.isPresent()) {
                        RenderSystem.lineWidth((float)optionalDouble.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5f, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0f);
                }
            });
            this.width = optionalDouble;
        }

        @Override
        public String toString() {
            return this.name + "[" + String.valueOf(this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + "]";
        }
    }

    static class BooleanStateShard
    extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String string, Runnable runnable, Runnable runnable2, boolean bl) {
            super(string, runnable, runnable2);
            this.enabled = bl;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.enabled + "]";
        }
    }

    protected static final class OffsetTexturingStateShard
    extends TexturingStateShard {
        public OffsetTexturingStateShard(float f, float f2) {
            super("offset_texturing", () -> RenderSystem.setTextureMatrix(new Matrix4f().translation(f, f2, 0.0f)), () -> RenderSystem.resetTextureMatrix());
        }
    }

    protected static class MultiTextureStateShard
    extends EmptyTextureStateShard {
        private final Optional<ResourceLocation> cutoutTexture;

        MultiTextureStateShard(List<Entry> list) {
            super(() -> {
                for (int i = 0; i < list.size(); ++i) {
                    Entry entry = (Entry)list.get(i);
                    TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                    AbstractTexture abstractTexture = textureManager.getTexture(entry.id);
                    abstractTexture.setUseMipmaps(entry.mipmap);
                    RenderSystem.setShaderTexture(i, abstractTexture.getTextureView());
                }
            }, () -> {});
            this.cutoutTexture = list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst().id);
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.cutoutTexture;
        }

        public static Builder builder() {
            return new Builder();
        }

        static final class Entry
        extends Record {
            final ResourceLocation id;
            final boolean mipmap;

            Entry(ResourceLocation resourceLocation, boolean bl) {
                this.id = resourceLocation;
                this.mipmap = bl;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "id;mipmap", "id", "mipmap"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "id;mipmap", "id", "mipmap"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "id;mipmap", "id", "mipmap"}, this, object);
            }

            public ResourceLocation id() {
                return this.id;
            }

            public boolean mipmap() {
                return this.mipmap;
            }
        }

        public static final class Builder {
            private final ImmutableList.Builder<Entry> builder = new ImmutableList.Builder();

            public Builder add(ResourceLocation resourceLocation, boolean bl) {
                this.builder.add((Object)new Entry(resourceLocation, bl));
                return this;
            }

            public MultiTextureStateShard build() {
                return new MultiTextureStateShard((List<Entry>)this.builder.build());
            }
        }
    }
}

