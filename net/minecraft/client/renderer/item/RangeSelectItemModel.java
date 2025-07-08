/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RangeSelectItemModel
implements ItemModel {
    private static final int LINEAR_SEARCH_THRESHOLD = 16;
    private final RangeSelectItemModelProperty property;
    private final float scale;
    private final float[] thresholds;
    private final ItemModel[] models;
    private final ItemModel fallback;

    RangeSelectItemModel(RangeSelectItemModelProperty rangeSelectItemModelProperty, float f, float[] fArray, ItemModel[] itemModelArray, ItemModel itemModel) {
        this.property = rangeSelectItemModelProperty;
        this.thresholds = fArray;
        this.models = itemModelArray;
        this.fallback = itemModel;
        this.scale = f;
    }

    private static int lastIndexLessOrEqual(float[] fArray, float f) {
        if (fArray.length < 16) {
            for (int i = 0; i < fArray.length; ++i) {
                if (!(fArray[i] > f)) continue;
                return i - 1;
            }
            return fArray.length - 1;
        }
        int n = Arrays.binarySearch(fArray, f);
        if (n < 0) {
            int n2 = ~n;
            return n2 - 1;
        }
        return n;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        int n2;
        itemStackRenderState.appendModelIdentityElement(this);
        float f = this.property.get(itemStack, clientLevel, livingEntity, n) * this.scale;
        ItemModel itemModel = Float.isNaN(f) ? this.fallback : ((n2 = RangeSelectItemModel.lastIndexLessOrEqual(this.thresholds, f)) == -1 ? this.fallback : this.models[n2]);
        itemModel.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, livingEntity, n);
    }

    public static final class Entry
    extends Record {
        final float threshold;
        final ItemModel.Unbaked model;
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("threshold").forGetter(Entry::threshold), (App)ItemModels.CODEC.fieldOf("model").forGetter(Entry::model)).apply((Applicative)instance, Entry::new));
        public static final Comparator<Entry> BY_THRESHOLD = Comparator.comparingDouble(Entry::threshold);

        public Entry(float f, ItemModel.Unbaked unbaked) {
            this.threshold = f;
            this.model = unbaked;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "threshold;model", "threshold", "model"}, this, object);
        }

        public float threshold() {
            return this.threshold;
        }

        public ItemModel.Unbaked model() {
            return this.model;
        }
    }

    public record Unbaked(RangeSelectItemModelProperty property, float scale, List<Entry> entries, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RangeSelectItemModelProperties.MAP_CODEC.forGetter(Unbaked::property), (App)Codec.FLOAT.optionalFieldOf("scale", (Object)Float.valueOf(1.0f)).forGetter(Unbaked::scale), (App)Entry.CODEC.listOf().fieldOf("entries").forGetter(Unbaked::entries), (App)ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext bakingContext) {
            float[] fArray = new float[this.entries.size()];
            ItemModel[] itemModelArray = new ItemModel[this.entries.size()];
            ArrayList<Entry> arrayList = new ArrayList<Entry>(this.entries);
            arrayList.sort(Entry.BY_THRESHOLD);
            for (int i = 0; i < arrayList.size(); ++i) {
                Entry entry = (Entry)arrayList.get(i);
                fArray[i] = entry.threshold;
                itemModelArray[i] = entry.model.bake(bakingContext);
            }
            ItemModel itemModel = this.fallback.map(unbaked -> unbaked.bake(bakingContext)).orElse(bakingContext.missingItemModel());
            return new RangeSelectItemModel(this.property, this.scale, fArray, itemModelArray, itemModel);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.fallback.ifPresent(unbaked -> unbaked.resolveDependencies(resolver));
            this.entries.forEach(entry -> entry.model.resolveDependencies(resolver));
        }
    }
}

