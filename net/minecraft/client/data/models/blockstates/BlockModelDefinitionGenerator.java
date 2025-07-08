/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models.blockstates;

import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.world.level.block.Block;

public interface BlockModelDefinitionGenerator {
    public Block block();

    public BlockModelDefinition create();
}

