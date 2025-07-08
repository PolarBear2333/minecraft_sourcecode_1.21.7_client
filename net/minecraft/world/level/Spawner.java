/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;

public interface Spawner {
    public void setEntityId(EntityType<?> var1, RandomSource var2);

    public static void appendHoverText(CustomData customData, Consumer<Component> consumer, String string) {
        Component component = Spawner.getSpawnEntityDisplayName(customData, string);
        if (component != null) {
            consumer.accept(component);
        } else {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
            consumer.accept(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
        }
    }

    @Nullable
    public static Component getSpawnEntityDisplayName(CustomData customData, String string) {
        return customData.getUnsafe().getCompound(string).flatMap(compoundTag -> compoundTag.getCompound("entity")).flatMap(compoundTag -> compoundTag.read("id", EntityType.CODEC)).map(entityType -> Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY)).orElse(null);
    }
}

