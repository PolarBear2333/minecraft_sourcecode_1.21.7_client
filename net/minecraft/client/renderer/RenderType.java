/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  org.joml.Matrix4fc
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public abstract class RenderType
extends RenderStateShard {
    private static final int MEGABYTE = 0x100000;
    public static final int BIG_BUFFER_SIZE = 0x400000;
    public static final int SMALL_BUFFER_SIZE = 786432;
    public static final int TRANSIENT_BUFFER_SIZE = 1536;
    private static final RenderType SOLID = RenderType.create("solid", 1536, true, false, RenderPipelines.SOLID, CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT_MIPPED = RenderType.create("cutout_mipped", 1536, true, false, RenderPipelines.CUTOUT_MIPPED, CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT = RenderType.create("cutout", 1536, true, false, RenderPipelines.CUTOUT, CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET).createCompositeState(true));
    private static final RenderType TRANSLUCENT_MOVING_BLOCK = RenderType.create("translucent_moving_block", 786432, false, true, RenderPipelines.TRANSLUCENT_MOVING_BLOCK, CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(true));
    private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
        return RenderType.create("armor_cutout_no_cull", 1536, true, false, RenderPipelines.ARMOR_CUTOUT_NO_CULL, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ARMOR_TRANSLUCENT = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
        return RenderType.create("armor_translucent", 1536, true, true, RenderPipelines.ARMOR_TRANSLUCENT, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return RenderType.create("entity_solid", 1536, true, false, RenderPipelines.ENTITY_SOLID, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID_Z_OFFSET_FORWARD = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING_FORWARD).createCompositeState(true);
        return RenderType.create("entity_solid_z_offset_forward", 1536, true, false, RenderPipelines.ENTITY_SOLID_Z_OFFSET_FORWARD, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return RenderType.create("entity_cutout", 1536, true, false, RenderPipelines.ENTITY_CUTOUT, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize((resourceLocation, bl) -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState((boolean)bl);
        return RenderType.create("entity_cutout_no_cull", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((resourceLocation, bl) -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState((boolean)bl);
        return RenderType.create("entity_cutout_no_cull_z_offset", 1536, true, false, RenderPipelines.ENTITY_CUTOUT_NO_CULL_Z_OFFSET, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setOutputState(ITEM_ENTITY_TARGET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return RenderType.create("item_entity_translucent_cull", 1536, true, true, RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT = Util.memoize((resourceLocation, bl) -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState((boolean)bl);
        return RenderType.create("entity_translucent", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT_EMISSIVE = Util.memoize((resourceLocation, bl) -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setOverlayState(OVERLAY).createCompositeState((boolean)bl);
        return RenderType.create("entity_translucent_emissive", 1536, true, true, RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return RenderType.create("entity_smooth_cutout", 1536, RenderPipelines.ENTITY_SMOOTH_CUTOUT, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize((resourceLocation, bl) -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).createCompositeState(false);
        return RenderType.create("beacon_beam", 1536, false, true, bl != false ? RenderPipelines.BEACON_BEAM_TRANSLUCENT : RenderPipelines.BEACON_BEAM_OPAQUE, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_DECAL = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
        return RenderType.create("entity_decal", 1536, RenderPipelines.ENTITY_DECAL, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
        return RenderType.create("entity_no_outline", 1536, false, true, RenderPipelines.ENTITY_NO_OUTLINE, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false);
        return RenderType.create("entity_shadow", 1536, false, false, RenderPipelines.ENTITY_SHADOW, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA = Util.memoize(resourceLocation -> {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).createCompositeState(true);
        return RenderType.create("entity_alpha", 1536, RenderPipelines.DRAGON_EXPLOSION_ALPHA, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> EYES = Util.memoize(resourceLocation -> {
        RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false);
        return RenderType.create("eyes", 1536, false, true, RenderPipelines.EYES, CompositeState.builder().setTextureState(textureStateShard).createCompositeState(false));
    });
    private static final RenderType LEASH = RenderType.create("leash", 1536, RenderPipelines.LEASH, CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
    private static final RenderType WATER_MASK = RenderType.create("water_mask", 1536, RenderPipelines.WATER_MASK, CompositeState.builder().setTextureState(NO_TEXTURE).createCompositeState(false));
    private static final RenderType ARMOR_ENTITY_GLINT = RenderType.create("armor_entity_glint", 1536, RenderPipelines.GLINT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ARMOR, false)).setTexturingState(ARMOR_ENTITY_GLINT_TEXTURING).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    private static final RenderType GLINT_TRANSLUCENT = RenderType.create("glint_translucent", 1536, RenderPipelines.GLINT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(GLINT_TEXTURING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    private static final RenderType GLINT = RenderType.create("glint", 1536, RenderPipelines.GLINT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType ENTITY_GLINT = RenderType.create("entity_glint", 1536, RenderPipelines.GLINT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, false)).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> CRUMBLING = Util.memoize(resourceLocation -> {
        RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false);
        return RenderType.create("crumbling", 1536, false, true, RenderPipelines.CRUMBLING, CompositeState.builder().setTextureState(textureStateShard).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize(resourceLocation -> RenderType.create("text", 786432, false, false, RenderPipelines.TEXT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final RenderType TEXT_BACKGROUND = RenderType.create("text_background", 1536, false, true, RenderPipelines.TEXT_BACKGROUND, CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize(resourceLocation -> RenderType.create("text_intensity", 786432, false, false, RenderPipelines.TEXT_INTENSITY, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize(resourceLocation -> RenderType.create("text_polygon_offset", 1536, false, true, RenderPipelines.TEXT_POLYGON_OFFSET, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(resourceLocation -> RenderType.create("text_intensity_polygon_offset", 1536, false, true, RenderPipelines.TEXT_INTENSITY, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize(resourceLocation -> RenderType.create("text_see_through", 1536, false, false, RenderPipelines.TEXT_SEE_THROUGH, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH = RenderType.create("text_background_see_through", 1536, false, true, RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH, CompositeState.builder().setTextureState(NO_TEXTURE).setLightmapState(LIGHTMAP).createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(resourceLocation -> RenderType.create("text_intensity_see_through", 1536, false, true, RenderPipelines.TEXT_INTENSITY_SEE_THROUGH, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final RenderType LIGHTNING = RenderType.create("lightning", 1536, false, true, RenderPipelines.LIGHTNING, CompositeState.builder().setOutputState(WEATHER_TARGET).createCompositeState(false));
    private static final RenderType DRAGON_RAYS = RenderType.create("dragon_rays", 1536, false, false, RenderPipelines.DRAGON_RAYS, CompositeState.builder().createCompositeState(false));
    private static final RenderType DRAGON_RAYS_DEPTH = RenderType.create("dragon_rays_depth", 1536, false, false, RenderPipelines.DRAGON_RAYS_DEPTH, CompositeState.builder().createCompositeState(false));
    private static final RenderType TRIPWIRE = RenderType.create("tripwire", 1536, true, true, RenderPipelines.TRIPWIRE, CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setOutputState(WEATHER_TARGET).createCompositeState(true));
    private static final RenderType END_PORTAL = RenderType.create("end_portal", 1536, false, false, RenderPipelines.END_PORTAL, CompositeState.builder().setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false).build()).createCompositeState(false));
    private static final RenderType END_GATEWAY = RenderType.create("end_gateway", 1536, false, false, RenderPipelines.END_GATEWAY, CompositeState.builder().setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false).build()).createCompositeState(false));
    public static final CompositeRenderType LINES = RenderType.create("lines", 1536, RenderPipelines.LINES, CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    public static final CompositeRenderType SECONDARY_BLOCK_OUTLINE = RenderType.create("secondary_block_outline", 1536, RenderPipelines.SECONDARY_BLOCK_OUTLINE, CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(7.0))).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    public static final CompositeRenderType LINE_STRIP = RenderType.create("line_strip", 1536, RenderPipelines.LINE_STRIP, CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    private static final Function<Double, CompositeRenderType> DEBUG_LINE_STRIP = Util.memoize(d -> RenderType.create("debug_line_strip", 1536, RenderPipelines.DEBUG_LINE_STRIP, CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(d))).createCompositeState(false)));
    private static final CompositeRenderType DEBUG_FILLED_BOX = RenderType.create("debug_filled_box", 1536, false, true, RenderPipelines.DEBUG_FILLED_BOX, CompositeState.builder().setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    private static final CompositeRenderType DEBUG_QUADS = RenderType.create("debug_quads", 1536, false, true, RenderPipelines.DEBUG_QUADS, CompositeState.builder().createCompositeState(false));
    private static final CompositeRenderType DEBUG_TRIANGLE_FAN = RenderType.create("debug_triangle_fan", 1536, false, true, RenderPipelines.DEBUG_TRIANGLE_FAN, CompositeState.builder().createCompositeState(false));
    private static final CompositeRenderType DEBUG_STRUCTURE_QUADS = RenderType.create("debug_structure_quads", 1536, false, true, RenderPipelines.DEBUG_STRUCTURE_QUADS, CompositeState.builder().createCompositeState(false));
    private static final CompositeRenderType DEBUG_SECTION_QUADS = RenderType.create("debug_section_quads", 1536, false, true, RenderPipelines.DEBUG_SECTION_QUADS, CompositeState.builder().setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> OPAQUE_PARTICLE = Util.memoize(resourceLocation -> RenderType.create("opaque_particle", 1536, false, false, RenderPipelines.OPAQUE_PARTICLE, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> TRANSLUCENT_PARTICLE = Util.memoize(resourceLocation -> RenderType.create("translucent_particle", 1536, false, false, RenderPipelines.TRANSLUCENT_PARTICLE, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setOutputState(PARTICLES_TARGET).setLightmapState(LIGHTMAP).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> WEATHER_DEPTH_WRITE = RenderType.createWeather(RenderPipelines.WEATHER_DEPTH_WRITE);
    private static final Function<ResourceLocation, RenderType> WEATHER_NO_DEPTH_WRITE = RenderType.createWeather(RenderPipelines.WEATHER_NO_DEPTH_WRITE);
    private static final RenderType SUNRISE_SUNSET = RenderType.create("sunrise_sunset", 1536, false, false, RenderPipelines.SUNRISE_SUNSET, CompositeState.builder().createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> CELESTIAL = Util.memoize(resourceLocation -> RenderType.create("celestial", 1536, false, false, RenderPipelines.CELESTIAL, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> BLOCK_SCREEN_EFFECT = Util.memoize(resourceLocation -> RenderType.create("block_screen_effect", 1536, false, false, RenderPipelines.BLOCK_SCREEN_EFFECT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).createCompositeState(false)));
    private static final Function<ResourceLocation, RenderType> FIRE_SCREEN_EFFECT = Util.memoize(resourceLocation -> RenderType.create("fire_screen_effect", 1536, false, false, RenderPipelines.FIRE_SCREEN_EFFECT, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).createCompositeState(false)));
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    public static RenderType solid() {
        return SOLID;
    }

    public static RenderType cutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static RenderType cutout() {
        return CUTOUT;
    }

    public static RenderType translucentMovingBlock() {
        return TRANSLUCENT_MOVING_BLOCK;
    }

    public static RenderType armorCutoutNoCull(ResourceLocation resourceLocation) {
        return ARMOR_CUTOUT_NO_CULL.apply(resourceLocation);
    }

    public static RenderType createArmorDecalCutoutNoCull(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
        return RenderType.create("armor_decal_cutout_no_cull", 1536, true, false, RenderPipelines.ARMOR_DECAL_CUTOUT_NO_CULL, compositeState);
    }

    public static RenderType armorTranslucent(ResourceLocation resourceLocation) {
        return ARMOR_TRANSLUCENT.apply(resourceLocation);
    }

    public static RenderType entitySolid(ResourceLocation resourceLocation) {
        return ENTITY_SOLID.apply(resourceLocation);
    }

    public static RenderType entitySolidZOffsetForward(ResourceLocation resourceLocation) {
        return ENTITY_SOLID_Z_OFFSET_FORWARD.apply(resourceLocation);
    }

    public static RenderType entityCutout(ResourceLocation resourceLocation) {
        return ENTITY_CUTOUT.apply(resourceLocation);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation, boolean bl) {
        return ENTITY_CUTOUT_NO_CULL.apply(resourceLocation, bl);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation) {
        return RenderType.entityCutoutNoCull(resourceLocation, true);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation resourceLocation, boolean bl) {
        return ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(resourceLocation, bl);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation resourceLocation) {
        return RenderType.entityCutoutNoCullZOffset(resourceLocation, true);
    }

    public static RenderType itemEntityTranslucentCull(ResourceLocation resourceLocation) {
        return ITEM_ENTITY_TRANSLUCENT_CULL.apply(resourceLocation);
    }

    public static RenderType entityTranslucent(ResourceLocation resourceLocation, boolean bl) {
        return ENTITY_TRANSLUCENT.apply(resourceLocation, bl);
    }

    public static RenderType entityTranslucent(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucent(resourceLocation, true);
    }

    public static RenderType entityTranslucentEmissive(ResourceLocation resourceLocation, boolean bl) {
        return ENTITY_TRANSLUCENT_EMISSIVE.apply(resourceLocation, bl);
    }

    public static RenderType entityTranslucentEmissive(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucentEmissive(resourceLocation, true);
    }

    public static RenderType entitySmoothCutout(ResourceLocation resourceLocation) {
        return ENTITY_SMOOTH_CUTOUT.apply(resourceLocation);
    }

    public static RenderType beaconBeam(ResourceLocation resourceLocation, boolean bl) {
        return BEACON_BEAM.apply(resourceLocation, bl);
    }

    public static RenderType entityDecal(ResourceLocation resourceLocation) {
        return ENTITY_DECAL.apply(resourceLocation);
    }

    public static RenderType entityNoOutline(ResourceLocation resourceLocation) {
        return ENTITY_NO_OUTLINE.apply(resourceLocation);
    }

    public static RenderType entityShadow(ResourceLocation resourceLocation) {
        return ENTITY_SHADOW.apply(resourceLocation);
    }

    public static RenderType dragonExplosionAlpha(ResourceLocation resourceLocation) {
        return DRAGON_EXPLOSION_ALPHA.apply(resourceLocation);
    }

    public static RenderType eyes(ResourceLocation resourceLocation) {
        return EYES.apply(resourceLocation);
    }

    public static RenderType breezeEyes(ResourceLocation resourceLocation) {
        return ENTITY_TRANSLUCENT_EMISSIVE.apply(resourceLocation, false);
    }

    public static RenderType breezeWind(ResourceLocation resourceLocation, float f, float f2) {
        return RenderType.create("breeze_wind", 1536, false, true, RenderPipelines.BREEZE_WIND, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setTexturingState(new RenderStateShard.OffsetTexturingStateShard(f, f2)).setLightmapState(LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false));
    }

    public static RenderType energySwirl(ResourceLocation resourceLocation, float f, float f2) {
        return RenderType.create("energy_swirl", 1536, false, true, RenderPipelines.ENERGY_SWIRL, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false)).setTexturingState(new RenderStateShard.OffsetTexturingStateShard(f, f2)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false));
    }

    public static RenderType leash() {
        return LEASH;
    }

    public static RenderType waterMask() {
        return WATER_MASK;
    }

    public static RenderType outline(ResourceLocation resourceLocation) {
        return CompositeRenderType.OUTLINE.apply(resourceLocation, false);
    }

    public static RenderType armorEntityGlint() {
        return ARMOR_ENTITY_GLINT;
    }

    public static RenderType glintTranslucent() {
        return GLINT_TRANSLUCENT;
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType crumbling(ResourceLocation resourceLocation) {
        return CRUMBLING.apply(resourceLocation);
    }

    public static RenderType text(ResourceLocation resourceLocation) {
        return TEXT.apply(resourceLocation);
    }

    public static RenderType textBackground() {
        return TEXT_BACKGROUND;
    }

    public static RenderType textIntensity(ResourceLocation resourceLocation) {
        return TEXT_INTENSITY.apply(resourceLocation);
    }

    public static RenderType textPolygonOffset(ResourceLocation resourceLocation) {
        return TEXT_POLYGON_OFFSET.apply(resourceLocation);
    }

    public static RenderType textIntensityPolygonOffset(ResourceLocation resourceLocation) {
        return TEXT_INTENSITY_POLYGON_OFFSET.apply(resourceLocation);
    }

    public static RenderType textSeeThrough(ResourceLocation resourceLocation) {
        return TEXT_SEE_THROUGH.apply(resourceLocation);
    }

    public static RenderType textBackgroundSeeThrough() {
        return TEXT_BACKGROUND_SEE_THROUGH;
    }

    public static RenderType textIntensitySeeThrough(ResourceLocation resourceLocation) {
        return TEXT_INTENSITY_SEE_THROUGH.apply(resourceLocation);
    }

    public static RenderType lightning() {
        return LIGHTNING;
    }

    public static RenderType dragonRays() {
        return DRAGON_RAYS;
    }

    public static RenderType dragonRaysDepth() {
        return DRAGON_RAYS_DEPTH;
    }

    public static RenderType tripwire() {
        return TRIPWIRE;
    }

    public static RenderType endPortal() {
        return END_PORTAL;
    }

    public static RenderType endGateway() {
        return END_GATEWAY;
    }

    public static RenderType lines() {
        return LINES;
    }

    public static RenderType secondaryBlockOutline() {
        return SECONDARY_BLOCK_OUTLINE;
    }

    public static RenderType lineStrip() {
        return LINE_STRIP;
    }

    public static RenderType debugLineStrip(double d) {
        return DEBUG_LINE_STRIP.apply(d);
    }

    public static RenderType debugFilledBox() {
        return DEBUG_FILLED_BOX;
    }

    public static RenderType debugQuads() {
        return DEBUG_QUADS;
    }

    public static RenderType debugTriangleFan() {
        return DEBUG_TRIANGLE_FAN;
    }

    public static RenderType debugStructureQuads() {
        return DEBUG_STRUCTURE_QUADS;
    }

    public static RenderType debugSectionQuads() {
        return DEBUG_SECTION_QUADS;
    }

    public static RenderType opaqueParticle(ResourceLocation resourceLocation) {
        return OPAQUE_PARTICLE.apply(resourceLocation);
    }

    public static RenderType translucentParticle(ResourceLocation resourceLocation) {
        return TRANSLUCENT_PARTICLE.apply(resourceLocation);
    }

    private static Function<ResourceLocation, RenderType> createWeather(RenderPipeline renderPipeline) {
        return Util.memoize(resourceLocation -> RenderType.create("weather", 1536, false, false, renderPipeline, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setOutputState(WEATHER_TARGET).setLightmapState(LIGHTMAP).createCompositeState(false)));
    }

    public static RenderType weather(ResourceLocation resourceLocation, boolean bl) {
        return (bl ? WEATHER_DEPTH_WRITE : WEATHER_NO_DEPTH_WRITE).apply(resourceLocation);
    }

    public static RenderType sunriseSunset() {
        return SUNRISE_SUNSET;
    }

    public static RenderType celestial(ResourceLocation resourceLocation) {
        return CELESTIAL.apply(resourceLocation);
    }

    public static RenderType blockScreenEffect(ResourceLocation resourceLocation) {
        return BLOCK_SCREEN_EFFECT.apply(resourceLocation);
    }

    public static RenderType fireScreenEffect(ResourceLocation resourceLocation) {
        return FIRE_SCREEN_EFFECT.apply(resourceLocation);
    }

    public RenderType(String string, int n, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
        this.bufferSize = n;
        this.affectsCrumbling = bl;
        this.sortOnUpload = bl2;
    }

    static CompositeRenderType create(String string, int n, RenderPipeline renderPipeline, CompositeState compositeState) {
        return RenderType.create(string, n, false, false, renderPipeline, compositeState);
    }

    private static CompositeRenderType create(String string, int n, boolean bl, boolean bl2, RenderPipeline renderPipeline, CompositeState compositeState) {
        return new CompositeRenderType(string, n, bl, bl2, renderPipeline, compositeState);
    }

    public abstract void draw(MeshData var1);

    public int bufferSize() {
        return this.bufferSize;
    }

    public abstract VertexFormat format();

    public abstract VertexFormat.Mode mode();

    public Optional<RenderType> outline() {
        return Optional.empty();
    }

    public boolean isOutline() {
        return false;
    }

    public boolean affectsCrumbling() {
        return this.affectsCrumbling;
    }

    public boolean canConsolidateConsecutiveGeometry() {
        return !this.mode().connectedPrimitives;
    }

    public boolean sortOnUpload() {
        return this.sortOnUpload;
    }

    protected static final class CompositeState {
        final RenderStateShard.EmptyTextureStateShard textureState;
        final RenderStateShard.OutputStateShard outputState;
        final OutlineProperty outlineProperty;
        final ImmutableList<RenderStateShard> states;

        CompositeState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard, RenderStateShard.LightmapStateShard lightmapStateShard, RenderStateShard.OverlayStateShard overlayStateShard, RenderStateShard.LayeringStateShard layeringStateShard, RenderStateShard.OutputStateShard outputStateShard, RenderStateShard.TexturingStateShard texturingStateShard, RenderStateShard.LineStateShard lineStateShard, OutlineProperty outlineProperty) {
            this.textureState = emptyTextureStateShard;
            this.outputState = outputStateShard;
            this.outlineProperty = outlineProperty;
            this.states = ImmutableList.of((Object)emptyTextureStateShard, (Object)lightmapStateShard, (Object)overlayStateShard, (Object)layeringStateShard, (Object)outputStateShard, (Object)texturingStateShard, (Object)lineStateShard);
        }

        public String toString() {
            return "CompositeState[" + String.valueOf(this.states) + ", outlineProperty=" + String.valueOf((Object)this.outlineProperty) + "]";
        }

        public static CompositeStateBuilder builder() {
            return new CompositeStateBuilder();
        }

        public static class CompositeStateBuilder {
            private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
            private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
            private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
            private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
            private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
            private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
            private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;

            CompositeStateBuilder() {
            }

            protected CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard) {
                this.textureState = emptyTextureStateShard;
                return this;
            }

            protected CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard lightmapStateShard) {
                this.lightmapState = lightmapStateShard;
                return this;
            }

            protected CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard overlayStateShard) {
                this.overlayState = overlayStateShard;
                return this;
            }

            protected CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard layeringStateShard) {
                this.layeringState = layeringStateShard;
                return this;
            }

            protected CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard outputStateShard) {
                this.outputState = outputStateShard;
                return this;
            }

            protected CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard texturingStateShard) {
                this.texturingState = texturingStateShard;
                return this;
            }

            protected CompositeStateBuilder setLineState(RenderStateShard.LineStateShard lineStateShard) {
                this.lineState = lineStateShard;
                return this;
            }

            protected CompositeState createCompositeState(boolean bl) {
                return this.createCompositeState(bl ? OutlineProperty.AFFECTS_OUTLINE : OutlineProperty.NONE);
            }

            protected CompositeState createCompositeState(OutlineProperty outlineProperty) {
                return new CompositeState(this.textureState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.lineState, outlineProperty);
            }
        }
    }

    static final class CompositeRenderType
    extends RenderType {
        static final BiFunction<ResourceLocation, Boolean, RenderType> OUTLINE = Util.memoize((resourceLocation, bl) -> RenderType.create("outline", 1536, bl != false ? RenderPipelines.OUTLINE_CULL : RenderPipelines.OUTLINE_NO_CULL, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false)).setOutputState(OUTLINE_TARGET).createCompositeState(OutlineProperty.IS_OUTLINE)));
        private final CompositeState state;
        private final RenderPipeline renderPipeline;
        private final Optional<RenderType> outline;
        private final boolean isOutline;

        CompositeRenderType(String string, int n, boolean bl, boolean bl2, RenderPipeline renderPipeline, CompositeState compositeState) {
            super(string, n, bl, bl2, () -> compositeState.states.forEach(RenderStateShard::setupRenderState), () -> compositeState.states.forEach(RenderStateShard::clearRenderState));
            this.state = compositeState;
            this.renderPipeline = renderPipeline;
            this.outline = compositeState.outlineProperty == OutlineProperty.AFFECTS_OUTLINE ? compositeState.textureState.cutoutTexture().map(resourceLocation -> OUTLINE.apply((ResourceLocation)resourceLocation, renderPipeline.isCull())) : Optional.empty();
            this.isOutline = compositeState.outlineProperty == OutlineProperty.IS_OUTLINE;
        }

        @Override
        public Optional<RenderType> outline() {
            return this.outline;
        }

        @Override
        public boolean isOutline() {
            return this.isOutline;
        }

        @Override
        public VertexFormat format() {
            return this.renderPipeline.getVertexFormat();
        }

        @Override
        public VertexFormat.Mode mode() {
            return this.renderPipeline.getVertexFormatMode();
        }

        @Override
        public void draw(MeshData meshData) {
            this.setupRenderState();
            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)RenderSystem.getModelOffset(), (Matrix4fc)RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());
            try (MeshData meshData2 = meshData;){
                GpuTextureView gpuTextureView;
                VertexFormat.IndexType indexType;
                GpuBuffer gpuBuffer;
                Object object;
                GpuBuffer gpuBuffer2 = this.renderPipeline.getVertexFormat().uploadImmediateVertexBuffer(meshData.vertexBuffer());
                if (meshData.indexBuffer() == null) {
                    object = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
                    gpuBuffer = ((RenderSystem.AutoStorageIndexBuffer)object).getBuffer(meshData.drawState().indexCount());
                    indexType = ((RenderSystem.AutoStorageIndexBuffer)object).type();
                } else {
                    gpuBuffer = this.renderPipeline.getVertexFormat().uploadImmediateIndexBuffer(meshData.indexBuffer());
                    indexType = meshData.drawState().indexType();
                }
                object = this.state.outputState.getRenderTarget();
                GpuTextureView gpuTextureView2 = gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : ((RenderTarget)object).getColorTextureView();
                GpuTextureView gpuTextureView3 = ((RenderTarget)object).useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : ((RenderTarget)object).getDepthTextureView()) : null;
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + this.getName(), gpuTextureView, OptionalInt.empty(), gpuTextureView3, OptionalDouble.empty());){
                    renderPass.setPipeline(this.renderPipeline);
                    ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                    if (scissorState.enabled()) {
                        renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
                    }
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, gpuBuffer2);
                    for (int i = 0; i < 12; ++i) {
                        GpuTextureView gpuTextureView4 = RenderSystem.getShaderTexture(i);
                        if (gpuTextureView4 == null) continue;
                        renderPass.bindSampler("Sampler" + i, gpuTextureView4);
                    }
                    renderPass.setIndexBuffer(gpuBuffer, indexType);
                    renderPass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);
                }
            }
            this.clearRenderState();
        }

        @Override
        public String toString() {
            return "RenderType[" + this.name + ":" + String.valueOf(this.state) + "]";
        }
    }

    protected static enum OutlineProperty {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String string2) {
            this.name = string2;
        }

        public String toString() {
            return this.name;
        }
    }
}

