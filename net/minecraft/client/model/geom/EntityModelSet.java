/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class EntityModelSet {
    public static final EntityModelSet EMPTY = new EntityModelSet(Map.of());
    private final Map<ModelLayerLocation, LayerDefinition> roots;

    public EntityModelSet(Map<ModelLayerLocation, LayerDefinition> map) {
        this.roots = map;
    }

    public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
        LayerDefinition layerDefinition = this.roots.get(modelLayerLocation);
        if (layerDefinition == null) {
            throw new IllegalArgumentException("No model for layer " + String.valueOf(modelLayerLocation));
        }
        return layerDefinition.bakeRoot();
    }

    public static EntityModelSet vanilla() {
        return new EntityModelSet((Map<ModelLayerLocation, LayerDefinition>)ImmutableMap.copyOf(LayerDefinitions.createRoots()));
    }
}

