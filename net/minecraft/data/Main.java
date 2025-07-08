/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 */
package net.minecraft.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.DatapackStructureReport;
import net.minecraft.data.info.ItemListReport;
import net.minecraft.data.info.PacketReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.TradeRebalanceLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.DialogTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TradeRebalanceEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class Main {
    @SuppressForbidden(a="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] stringArray) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec abstractOptionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpecBuilder = optionParser.accepts("server", "Include server generators");
        OptionSpecBuilder optionSpecBuilder2 = optionParser.accepts("dev", "Include development tools");
        OptionSpecBuilder optionSpecBuilder3 = optionParser.accepts("reports", "Include data reports");
        optionParser.accepts("validate", "Validate inputs");
        OptionSpecBuilder optionSpecBuilder4 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo((Object)"generated", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec2 = optionParser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionSet = optionParser.parse(stringArray);
        if (optionSet.has((OptionSpec)abstractOptionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn((OutputStream)System.out);
            return;
        }
        Path path = Paths.get((String)argumentAcceptingOptionSpec.value(optionSet), new String[0]);
        boolean bl = optionSet.has((OptionSpec)optionSpecBuilder4);
        boolean bl2 = bl || optionSet.has((OptionSpec)optionSpecBuilder);
        boolean bl3 = bl || optionSet.has((OptionSpec)optionSpecBuilder2);
        boolean bl4 = bl || optionSet.has((OptionSpec)optionSpecBuilder3);
        List<Path> list = optionSet.valuesOf((OptionSpec)argumentAcceptingOptionSpec2).stream().map(string -> Paths.get(string, new String[0])).toList();
        DataGenerator dataGenerator = new DataGenerator(path, SharedConstants.getCurrentVersion(), true);
        Main.addServerProviders(dataGenerator, list, bl2, bl3, bl4);
        dataGenerator.run();
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> biFunction, CompletableFuture<HolderLookup.Provider> completableFuture) {
        return packOutput -> (DataProvider)biFunction.apply(packOutput, completableFuture);
    }

    public static void addServerProviders(DataGenerator dataGenerator, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3) {
        Object object = dataGenerator.getVanillaPack(bl);
        ((DataGenerator.PackGenerator)object).addProvider(packOutput -> new SnbtToNbt(packOutput, collection).addFilter(new StructureUpdater()));
        object = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        Object object2 = dataGenerator.getVanillaPack(bl);
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaAdvancementProvider::create, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaLootTableProvider::create, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaRecipeProvider.Runner::new, (CompletableFuture<HolderLookup.Provider>)object));
        TagsProvider tagsProvider = ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaBlockTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        TagsProvider tagsProvider2 = ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaItemTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        TagsProvider tagsProvider3 = ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(BiomeTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        TagsProvider tagsProvider4 = ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(BannerPatternTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        TagsProvider tagsProvider5 = ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(StructureTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(DamageTypeTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(DialogTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(EntityTypeTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(FluidTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(GameEventTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(InstrumentTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(PaintingVariantTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(PoiTypeTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(WorldPresetTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(VanillaEnchantmentTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        object2 = dataGenerator.getVanillaPack(bl2);
        ((DataGenerator.PackGenerator)object2).addProvider(packOutput -> new NbtToSnbt(packOutput, collection));
        object2 = dataGenerator.getVanillaPack(bl3);
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(BiomeParametersDumpReport::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(ItemListReport::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(BlockListReport::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(Main.bindRegistries(CommandsReport::new, (CompletableFuture<HolderLookup.Provider>)object));
        ((DataGenerator.PackGenerator)object2).addProvider(RegistryDumpReport::new);
        ((DataGenerator.PackGenerator)object2).addProvider(PacketReport::new);
        ((DataGenerator.PackGenerator)object2).addProvider(DatapackStructureReport::new);
        object2 = TradeRebalanceRegistries.createLookup((CompletableFuture<HolderLookup.Provider>)object);
        CompletionStage completionStage = ((CompletableFuture)object2).thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        DataGenerator.PackGenerator packGenerator = dataGenerator.getBuiltinDatapack(bl, "trade_rebalance");
        packGenerator.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, (CompletableFuture<HolderLookup.Provider>)completionStage));
        packGenerator.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.trade_rebalance.description"), FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE)));
        packGenerator.addProvider(Main.bindRegistries(TradeRebalanceLootTableProvider::create, (CompletableFuture<HolderLookup.Provider>)object));
        packGenerator.addProvider(Main.bindRegistries(TradeRebalanceEnchantmentTagsProvider::new, (CompletableFuture<HolderLookup.Provider>)object));
        object2 = dataGenerator.getBuiltinDatapack(bl, "redstone_experiments");
        ((DataGenerator.PackGenerator)object2).addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.redstone_experiments.description"), FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)));
        object2 = dataGenerator.getBuiltinDatapack(bl, "minecart_improvements");
        ((DataGenerator.PackGenerator)object2).addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.minecart_improvements.description"), FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS)));
    }
}

