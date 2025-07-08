/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CommandTemplate;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.action.StaticAction;

public class ActionTypes {
    public static MapCodec<? extends Action> bootstrap(Registry<MapCodec<? extends Action>> registry) {
        StaticAction.WRAPPED_CODECS.forEach((action, mapCodec) -> Registry.register(registry, ResourceLocation.withDefaultNamespace(action.getSerializedName()), mapCodec));
        Registry.register(registry, ResourceLocation.withDefaultNamespace("dynamic/run_command"), CommandTemplate.MAP_CODEC);
        return Registry.register(registry, ResourceLocation.withDefaultNamespace("dynamic/custom"), CustomAll.MAP_CODEC);
    }
}

