/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MeshDefinition {
    private final PartDefinition root;

    public MeshDefinition() {
        this(new PartDefinition((List<CubeDefinition>)ImmutableList.of(), PartPose.ZERO));
    }

    private MeshDefinition(PartDefinition partDefinition) {
        this.root = partDefinition;
    }

    public PartDefinition getRoot() {
        return this.root;
    }

    public MeshDefinition transformed(UnaryOperator<PartPose> unaryOperator) {
        return new MeshDefinition(this.root.transformed(unaryOperator));
    }

    public MeshDefinition apply(MeshTransformer meshTransformer) {
        return meshTransformer.apply(this);
    }
}

