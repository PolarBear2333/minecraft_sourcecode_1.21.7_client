/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.resources.model;

import com.mojang.math.Quadrant;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class MissingBlockModel {
    private static final String TEXTURE_SLOT = "missingno";
    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("builtin/missing");

    public static UnbakedModel missingModel() {
        BlockElementFace.UVs uVs = new BlockElementFace.UVs(0.0f, 0.0f, 16.0f, 16.0f);
        Map<Direction, BlockElementFace> map = Util.makeEnumMap(Direction.class, direction -> new BlockElementFace((Direction)direction, -1, TEXTURE_SLOT, uVs, Quadrant.R0));
        BlockElement blockElement = new BlockElement((Vector3fc)new Vector3f(0.0f, 0.0f, 0.0f), (Vector3fc)new Vector3f(16.0f, 16.0f, 16.0f), map);
        return new BlockModel(new SimpleUnbakedGeometry(List.of(blockElement)), null, null, ItemTransforms.NO_TRANSFORMS, new TextureSlots.Data.Builder().addReference("particle", TEXTURE_SLOT).addTexture(TEXTURE_SLOT, new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation())).build(), null);
    }
}

