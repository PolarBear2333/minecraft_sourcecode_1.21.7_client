/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record DyedItemColor(int rgb) implements TooltipProvider
{
    public static final Codec<DyedItemColor> CODEC = ExtraCodecs.RGB_COLOR_CODEC.xmap(DyedItemColor::new, DyedItemColor::rgb);
    public static final StreamCodec<ByteBuf, DyedItemColor> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, DyedItemColor::rgb, DyedItemColor::new);
    public static final int LEATHER_COLOR = -6265536;

    public static int getOrDefault(ItemStack itemStack, int n) {
        DyedItemColor dyedItemColor = itemStack.get(DataComponents.DYED_COLOR);
        return dyedItemColor != null ? ARGB.opaque(dyedItemColor.rgb()) : n;
    }

    public static ItemStack applyDyes(ItemStack itemStack, List<DyeItem> list) {
        int n;
        int n2;
        int n3;
        if (!itemStack.is(ItemTags.DYEABLE)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = itemStack.copyWithCount(1);
        int n4 = 0;
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        int n8 = 0;
        DyedItemColor dyedItemColor = itemStack2.get(DataComponents.DYED_COLOR);
        if (dyedItemColor != null) {
            int n9 = ARGB.red(dyedItemColor.rgb());
            n3 = ARGB.green(dyedItemColor.rgb());
            n2 = ARGB.blue(dyedItemColor.rgb());
            n7 += Math.max(n9, Math.max(n3, n2));
            n4 += n9;
            n5 += n3;
            n6 += n2;
            ++n8;
        }
        for (DyeItem dyeItem : list) {
            n2 = dyeItem.getDyeColor().getTextureDiffuseColor();
            int n10 = ARGB.red(n2);
            int n11 = ARGB.green(n2);
            n = ARGB.blue(n2);
            n7 += Math.max(n10, Math.max(n11, n));
            n4 += n10;
            n5 += n11;
            n6 += n;
            ++n8;
        }
        int n12 = n4 / n8;
        n3 = n5 / n8;
        n2 = n6 / n8;
        float f = (float)n7 / (float)n8;
        float f2 = Math.max(n12, Math.max(n3, n2));
        n12 = (int)((float)n12 * f / f2);
        n3 = (int)((float)n3 * f / f2);
        n2 = (int)((float)n2 * f / f2);
        n = ARGB.color(0, n12, n3, n2);
        itemStack2.set(DataComponents.DYED_COLOR, new DyedItemColor(n));
        return itemStack2;
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (tooltipFlag.isAdvanced()) {
            consumer.accept(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).withStyle(ChatFormatting.GRAY));
        } else {
            consumer.accept(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}

