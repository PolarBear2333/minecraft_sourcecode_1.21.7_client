/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;

public class MinecartCommandBlockEditScreen
extends AbstractCommandBlockEditScreen {
    private final BaseCommandBlock commandBlock;

    public MinecartCommandBlockEditScreen(BaseCommandBlock baseCommandBlock) {
        this.commandBlock = baseCommandBlock;
    }

    @Override
    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override
    int getPreviousY() {
        return 150;
    }

    @Override
    protected void init() {
        super.init();
        this.commandEdit.setValue(this.getCommandBlock().getCommand());
    }

    @Override
    protected void populateAndSendPacket(BaseCommandBlock baseCommandBlock) {
        if (baseCommandBlock instanceof MinecartCommandBlock.MinecartCommandBase) {
            MinecartCommandBlock.MinecartCommandBase minecartCommandBase = (MinecartCommandBlock.MinecartCommandBase)baseCommandBlock;
            this.minecraft.getConnection().send(new ServerboundSetCommandMinecartPacket(minecartCommandBase.getMinecart().getId(), this.commandEdit.getValue(), baseCommandBlock.isTrackOutput()));
        }
    }
}

