/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetInstrumentFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetInstrumentFunction.commonFields(instance).and((App)TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter(setInstrumentFunction -> setInstrumentFunction.options)).apply((Applicative)instance, SetInstrumentFunction::new));
    private final TagKey<Instrument> options;

    private SetInstrumentFunction(List<LootItemCondition> list, TagKey<Instrument> tagKey) {
        super(list);
        this.options = tagKey;
    }

    public LootItemFunctionType<SetInstrumentFunction> getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        HolderLookup.RegistryLookup registryLookup = lootContext.getLevel().registryAccess().lookupOrThrow(Registries.INSTRUMENT);
        Optional<Holder<Instrument>> optional = registryLookup.getRandomElementOf(this.options, lootContext.getRandom());
        if (optional.isPresent()) {
            itemStack.set(DataComponents.INSTRUMENT, new InstrumentComponent(optional.get()));
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagKey) {
        return SetInstrumentFunction.simpleBuilder(list -> new SetInstrumentFunction((List<LootItemCondition>)list, tagKey));
    }
}

