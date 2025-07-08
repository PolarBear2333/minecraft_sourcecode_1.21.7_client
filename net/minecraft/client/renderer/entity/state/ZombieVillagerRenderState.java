/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.npc.VillagerData;

public class ZombieVillagerRenderState
extends ZombieRenderState
implements VillagerDataHolderRenderState {
    @Nullable
    public VillagerData villagerData;

    @Override
    @Nullable
    public VillagerData getVillagerData() {
        return this.villagerData;
    }
}

