/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.gametest.framework;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;

public class FunctionGameTestInstance
extends GameTestInstance {
    public static final MapCodec<FunctionGameTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.TEST_FUNCTION).fieldOf("function").forGetter(FunctionGameTestInstance::function), (App)TestData.CODEC.forGetter(GameTestInstance::info)).apply((Applicative)instance, FunctionGameTestInstance::new));
    private final ResourceKey<Consumer<GameTestHelper>> function;

    public FunctionGameTestInstance(ResourceKey<Consumer<GameTestHelper>> resourceKey, TestData<Holder<TestEnvironmentDefinition>> testData) {
        super(testData);
        this.function = resourceKey;
    }

    @Override
    public void run(GameTestHelper gameTestHelper) {
        gameTestHelper.getLevel().registryAccess().get(this.function).map(Holder.Reference::value).orElseThrow(() -> new IllegalStateException("Trying to access missing test function: " + String.valueOf(this.function.location()))).accept(gameTestHelper);
    }

    private ResourceKey<Consumer<GameTestHelper>> function() {
        return this.function;
    }

    public MapCodec<FunctionGameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.translatable("test_instance.type.function");
    }

    @Override
    public Component describe() {
        return this.describeType().append(this.descriptionRow("test_instance.description.function", this.function.location().toString())).append(this.describeInfo());
    }
}

