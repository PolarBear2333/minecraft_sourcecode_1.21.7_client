/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.ContextDimension;
import net.minecraft.client.renderer.item.properties.select.ItemBlockState;
import net.minecraft.client.renderer.item.properties.select.LocalTime;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemModelUtils {
    public static ItemModel.Unbaked plainModel(ResourceLocation resourceLocation) {
        return new BlockModelWrapper.Unbaked(resourceLocation, List.of());
    }

    public static ItemModel.Unbaked tintedModel(ResourceLocation resourceLocation, ItemTintSource ... itemTintSourceArray) {
        return new BlockModelWrapper.Unbaked(resourceLocation, List.of(itemTintSourceArray));
    }

    public static ItemTintSource constantTint(int n) {
        return new Constant(n);
    }

    public static ItemModel.Unbaked composite(ItemModel.Unbaked ... unbakedArray) {
        return new CompositeModel.Unbaked(List.of(unbakedArray));
    }

    public static ItemModel.Unbaked specialModel(ResourceLocation resourceLocation, SpecialModelRenderer.Unbaked unbaked) {
        return new SpecialModelWrapper.Unbaked(resourceLocation, unbaked);
    }

    public static RangeSelectItemModel.Entry override(ItemModel.Unbaked unbaked, float f) {
        return new RangeSelectItemModel.Entry(f, unbaked);
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, ItemModel.Unbaked unbaked, RangeSelectItemModel.Entry ... entryArray) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, List.of(entryArray), Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, ItemModel.Unbaked unbaked, RangeSelectItemModel.Entry ... entryArray) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, f, List.of(entryArray), Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, ItemModel.Unbaked unbaked, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, list, Optional.of(unbaked));
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, 1.0f, list, Optional.empty());
    }

    public static ItemModel.Unbaked rangeSelect(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, List<RangeSelectItemModel.Entry> list) {
        return new RangeSelectItemModel.Unbaked(rangeSelectItemModelProperty, f, list, Optional.empty());
    }

    public static ItemModel.Unbaked conditional(ConditionalItemModelProperty conditionalItemModelProperty, ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        return new ConditionalItemModel.Unbaked(conditionalItemModelProperty, unbaked, unbaked2);
    }

    public static <T> SelectItemModel.SwitchCase<T> when(T t, ItemModel.Unbaked unbaked) {
        return new SelectItemModel.SwitchCase<T>(List.of(t), unbaked);
    }

    public static <T> SelectItemModel.SwitchCase<T> when(List<T> list, ItemModel.Unbaked unbaked) {
        return new SelectItemModel.SwitchCase<T>(list, unbaked);
    }

    @SafeVarargs
    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, ItemModel.Unbaked unbaked, SelectItemModel.SwitchCase<T> ... switchCaseArray) {
        return ItemModelUtils.select(selectItemModelProperty, unbaked, List.of(switchCaseArray));
    }

    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, ItemModel.Unbaked unbaked, List<SelectItemModel.SwitchCase<T>> list) {
        return new SelectItemModel.Unbaked(new SelectItemModel.UnbakedSwitch<SelectItemModelProperty<T>, T>(selectItemModelProperty, list), Optional.of(unbaked));
    }

    @SafeVarargs
    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, SelectItemModel.SwitchCase<T> ... switchCaseArray) {
        return ItemModelUtils.select(selectItemModelProperty, List.of(switchCaseArray));
    }

    public static <T> ItemModel.Unbaked select(SelectItemModelProperty<T> selectItemModelProperty, List<SelectItemModel.SwitchCase<T>> list) {
        return new SelectItemModel.Unbaked(new SelectItemModel.UnbakedSwitch<SelectItemModelProperty<T>, T>(selectItemModelProperty, list), Optional.empty());
    }

    public static ConditionalItemModelProperty isUsingItem() {
        return new IsUsingItem();
    }

    public static ConditionalItemModelProperty hasComponent(DataComponentType<?> dataComponentType) {
        return new HasComponent(dataComponentType, false);
    }

    public static ItemModel.Unbaked inOverworld(ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        return ItemModelUtils.select(new ContextDimension(), unbaked2, ItemModelUtils.when(Level.OVERWORLD, unbaked));
    }

    public static <T extends Comparable<T>> ItemModel.Unbaked selectBlockItemProperty(Property<T> property, ItemModel.Unbaked unbaked, Map<T, ItemModel.Unbaked> map) {
        List<SelectItemModel.SwitchCase<T>> list = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            String string = property.getName((Comparable)entry.getKey());
            return new SelectItemModel.SwitchCase<String>(List.of(string), (ItemModel.Unbaked)entry.getValue());
        }).toList();
        return ItemModelUtils.select(new ItemBlockState(property.getName()), unbaked, list);
    }

    public static ItemModel.Unbaked isXmas(ItemModel.Unbaked unbaked, ItemModel.Unbaked unbaked2) {
        return ItemModelUtils.select(LocalTime.create("MM-dd", "", Optional.empty()), unbaked2, List.of(ItemModelUtils.when(List.of("12-24", "12-25", "12-26"), unbaked)));
    }
}

