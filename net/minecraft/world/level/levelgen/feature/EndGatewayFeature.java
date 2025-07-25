/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature
extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Codec<EndGatewayConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<EndGatewayConfiguration> featurePlaceContext) {
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        EndGatewayConfiguration endGatewayConfiguration = featurePlaceContext.config();
        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
            boolean bl;
            boolean bl2 = blockPos3.getX() == blockPos.getX();
            boolean bl3 = blockPos3.getY() == blockPos.getY();
            boolean bl4 = blockPos3.getZ() == blockPos.getZ();
            boolean bl5 = bl = Math.abs(blockPos3.getY() - blockPos.getY()) == 2;
            if (bl2 && bl3 && bl4) {
                BlockPos blockPos4 = blockPos3.immutable();
                this.setBlock(worldGenLevel, blockPos4, Blocks.END_GATEWAY.defaultBlockState());
                endGatewayConfiguration.getExit().ifPresent(blockPos2 -> {
                    BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos4);
                    if (blockEntity instanceof TheEndGatewayBlockEntity) {
                        TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
                        theEndGatewayBlockEntity.setExitPosition((BlockPos)blockPos2, endGatewayConfiguration.isExitExact());
                    }
                });
                continue;
            }
            if (bl3) {
                this.setBlock(worldGenLevel, blockPos3, Blocks.AIR.defaultBlockState());
                continue;
            }
            if (bl && bl2 && bl4) {
                this.setBlock(worldGenLevel, blockPos3, Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            if (!bl2 && !bl4 || bl) {
                this.setBlock(worldGenLevel, blockPos3, Blocks.AIR.defaultBlockState());
                continue;
            }
            this.setBlock(worldGenLevel, blockPos3, Blocks.BEDROCK.defaultBlockState());
        }
        return true;
    }
}

