/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class FontManager
implements PreparableReloadListener,
AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FONTS_PATH = "fonts.json";
    public static final ResourceLocation MISSING_FONT = ResourceLocation.withDefaultNamespace("missing");
    private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final FontSet missingFontSet;
    private final List<GlyphProvider> providersToClose = new ArrayList<GlyphProvider>();
    private final Map<ResourceLocation, FontSet> fontSets = new HashMap<ResourceLocation, FontSet>();
    private final TextureManager textureManager;
    @Nullable
    private volatile FontSet lastFontSetCache;

    public FontManager(TextureManager textureManager) {
        this.textureManager = textureManager;
        this.missingFontSet = Util.make(new FontSet(textureManager, MISSING_FONT), fontSet -> fontSet.reload(List.of(FontManager.createFallbackProvider()), Set.of()));
    }

    private static GlyphProvider.Conditional createFallbackProvider() {
        return new GlyphProvider.Conditional(new AllMissingGlyphProvider(), FontOption.Filter.ALWAYS_PASS);
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        return ((CompletableFuture)this.prepare(resourceManager, executor).thenCompose(preparationBarrier::wait)).thenAcceptAsync(preparation -> this.apply((Preparation)preparation, Profiler.get()), executor2);
    }

    private CompletableFuture<Preparation> prepare(ResourceManager resourceManager, Executor executor) {
        ArrayList<CompletableFuture<UnresolvedBuilderBundle>> arrayList = new ArrayList<CompletableFuture<UnresolvedBuilderBundle>>();
        for (Map.Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation resourceLocation = FONT_DEFINITIONS.fileToId(entry.getKey());
            arrayList.add(CompletableFuture.supplyAsync(() -> {
                List<Pair<BuilderId, GlyphProviderDefinition.Conditional>> list = FontManager.loadResourceStack((List)entry.getValue(), resourceLocation);
                UnresolvedBuilderBundle unresolvedBuilderBundle = new UnresolvedBuilderBundle(resourceLocation);
                for (Pair<BuilderId, GlyphProviderDefinition.Conditional> pair : list) {
                    BuilderId builderId = (BuilderId)pair.getFirst();
                    FontOption.Filter filter = ((GlyphProviderDefinition.Conditional)pair.getSecond()).filter();
                    ((GlyphProviderDefinition.Conditional)pair.getSecond()).definition().unpack().ifLeft(loader -> {
                        CompletableFuture<Optional<GlyphProvider>> completableFuture = this.safeLoad(builderId, (GlyphProviderDefinition.Loader)loader, resourceManager, executor);
                        unresolvedBuilderBundle.add(builderId, filter, completableFuture);
                    }).ifRight(reference -> unresolvedBuilderBundle.add(builderId, filter, (GlyphProviderDefinition.Reference)reference));
                }
                return unresolvedBuilderBundle;
            }, executor));
        }
        return Util.sequence(arrayList).thenCompose(list -> {
            List list2 = list.stream().flatMap(UnresolvedBuilderBundle::listBuilders).collect(Util.toMutableList());
            GlyphProvider.Conditional conditional = FontManager.createFallbackProvider();
            list2.add(CompletableFuture.completedFuture(Optional.of(conditional.provider())));
            return Util.sequence(list2).thenCompose(list3 -> {
                Map<ResourceLocation, List<GlyphProvider.Conditional>> map = this.resolveProviders((List<UnresolvedBuilderBundle>)list);
                CompletableFuture[] completableFutureArray = (CompletableFuture[])map.values().stream().map(list -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading((List<GlyphProvider.Conditional>)list, conditional), executor)).toArray(CompletableFuture[]::new);
                return CompletableFuture.allOf(completableFutureArray).thenApply(void_ -> {
                    List<GlyphProvider> list2 = list3.stream().flatMap(Optional::stream).toList();
                    return new Preparation(map, list2);
                });
            });
        });
    }

    private CompletableFuture<Optional<GlyphProvider>> safeLoad(BuilderId builderId, GlyphProviderDefinition.Loader loader, ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(loader.load(resourceManager));
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load builder {}, rejecting", (Object)builderId, (Object)exception);
                return Optional.empty();
            }
        }, executor);
    }

    private Map<ResourceLocation, List<GlyphProvider.Conditional>> resolveProviders(List<UnresolvedBuilderBundle> list) {
        HashMap<ResourceLocation, List<GlyphProvider.Conditional>> hashMap = new HashMap<ResourceLocation, List<GlyphProvider.Conditional>>();
        DependencySorter<ResourceLocation, UnresolvedBuilderBundle> dependencySorter = new DependencySorter<ResourceLocation, UnresolvedBuilderBundle>();
        list.forEach(unresolvedBuilderBundle -> dependencySorter.addEntry(unresolvedBuilderBundle.fontId, (UnresolvedBuilderBundle)unresolvedBuilderBundle));
        dependencySorter.orderByDependencies((resourceLocation, unresolvedBuilderBundle) -> unresolvedBuilderBundle.resolve(hashMap::get).ifPresent(list -> hashMap.put((ResourceLocation)resourceLocation, (List<GlyphProvider.Conditional>)list)));
        return hashMap;
    }

    private void finalizeProviderLoading(List<GlyphProvider.Conditional> list, GlyphProvider.Conditional conditional) {
        list.add(0, conditional);
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        for (GlyphProvider.Conditional conditional2 : list) {
            intOpenHashSet.addAll((IntCollection)conditional2.provider().getSupportedGlyphs());
        }
        intOpenHashSet.forEach(n -> {
            GlyphProvider.Conditional conditional;
            if (n == 32) {
                return;
            }
            Iterator iterator = Lists.reverse((List)list).iterator();
            while (iterator.hasNext() && (conditional = (GlyphProvider.Conditional)iterator.next()).provider().getGlyph(n) == null) {
            }
        });
    }

    private static Set<FontOption> getFontOptions(Options options) {
        EnumSet<FontOption> enumSet = EnumSet.noneOf(FontOption.class);
        if (options.forceUnicodeFont().get().booleanValue()) {
            enumSet.add(FontOption.UNIFORM);
        }
        if (options.japaneseGlyphVariants().get().booleanValue()) {
            enumSet.add(FontOption.JAPANESE_VARIANTS);
        }
        return enumSet;
    }

    private void apply(Preparation preparation, ProfilerFiller profilerFiller) {
        profilerFiller.push("closing");
        this.lastFontSetCache = null;
        this.fontSets.values().forEach(FontSet::close);
        this.fontSets.clear();
        this.providersToClose.forEach(GlyphProvider::close);
        this.providersToClose.clear();
        Set<FontOption> set = FontManager.getFontOptions(Minecraft.getInstance().options);
        profilerFiller.popPush("reloading");
        preparation.fontSets().forEach((resourceLocation, list) -> {
            FontSet fontSet = new FontSet(this.textureManager, (ResourceLocation)resourceLocation);
            fontSet.reload(Lists.reverse((List)list), set);
            this.fontSets.put((ResourceLocation)resourceLocation, fontSet);
        });
        this.providersToClose.addAll(preparation.allProviders);
        profilerFiller.pop();
        if (!this.fontSets.containsKey(Minecraft.DEFAULT_FONT)) {
            throw new IllegalStateException("Default font failed to load");
        }
    }

    public void updateOptions(Options options) {
        Set<FontOption> set = FontManager.getFontOptions(options);
        for (FontSet fontSet : this.fontSets.values()) {
            fontSet.reload(set);
        }
    }

    private static List<Pair<BuilderId, GlyphProviderDefinition.Conditional>> loadResourceStack(List<Resource> list, ResourceLocation resourceLocation) {
        ArrayList<Pair<BuilderId, GlyphProviderDefinition.Conditional>> arrayList = new ArrayList<Pair<BuilderId, GlyphProviderDefinition.Conditional>>();
        for (Resource resource : list) {
            try {
                BufferedReader bufferedReader = resource.openAsReader();
                try {
                    JsonElement jsonElement = (JsonElement)GSON.fromJson((Reader)bufferedReader, JsonElement.class);
                    FontDefinitionFile fontDefinitionFile = (FontDefinitionFile)FontDefinitionFile.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonParseException::new);
                    List<GlyphProviderDefinition.Conditional> list2 = fontDefinitionFile.providers;
                    for (int i = list2.size() - 1; i >= 0; --i) {
                        BuilderId builderId = new BuilderId(resourceLocation, resource.sourcePackId(), i);
                        arrayList.add((Pair<BuilderId, GlyphProviderDefinition.Conditional>)Pair.of((Object)builderId, (Object)list2.get(i)));
                    }
                }
                finally {
                    if (bufferedReader == null) continue;
                    ((Reader)bufferedReader).close();
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", new Object[]{resourceLocation, FONTS_PATH, resource.sourcePackId(), exception});
            }
        }
        return arrayList;
    }

    public Font createFont() {
        return new Font(this::getFontSetCached, false);
    }

    public Font createFontFilterFishy() {
        return new Font(this::getFontSetCached, true);
    }

    private FontSet getFontSetRaw(ResourceLocation resourceLocation) {
        return this.fontSets.getOrDefault(resourceLocation, this.missingFontSet);
    }

    private FontSet getFontSetCached(ResourceLocation resourceLocation) {
        FontSet fontSet;
        FontSet fontSet2 = this.lastFontSetCache;
        if (fontSet2 != null && resourceLocation.equals(fontSet2.name())) {
            return fontSet2;
        }
        this.lastFontSetCache = fontSet = this.getFontSetRaw(resourceLocation);
        return fontSet;
    }

    @Override
    public void close() {
        this.fontSets.values().forEach(FontSet::close);
        this.providersToClose.forEach(GlyphProvider::close);
        this.missingFontSet.close();
    }

    record BuilderId(ResourceLocation fontId, String pack, int index) {
        @Override
        public String toString() {
            return "(" + String.valueOf(this.fontId) + ": builder #" + this.index + " from pack " + this.pack + ")";
        }
    }

    static final class Preparation
    extends Record {
        private final Map<ResourceLocation, List<GlyphProvider.Conditional>> fontSets;
        final List<GlyphProvider> allProviders;

        Preparation(Map<ResourceLocation, List<GlyphProvider.Conditional>> map, List<GlyphProvider> list) {
            this.fontSets = map;
            this.allProviders = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Preparation.class, "fontSets;allProviders", "fontSets", "allProviders"}, this, object);
        }

        public Map<ResourceLocation, List<GlyphProvider.Conditional>> fontSets() {
            return this.fontSets;
        }

        public List<GlyphProvider> allProviders() {
            return this.allProviders;
        }
    }

    static final class FontDefinitionFile
    extends Record {
        final List<GlyphProviderDefinition.Conditional> providers;
        public static final Codec<FontDefinitionFile> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GlyphProviderDefinition.Conditional.CODEC.listOf().fieldOf("providers").forGetter(FontDefinitionFile::providers)).apply((Applicative)instance, FontDefinitionFile::new));

        private FontDefinitionFile(List<GlyphProviderDefinition.Conditional> list) {
            this.providers = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FontDefinitionFile.class, "providers", "providers"}, this, object);
        }

        public List<GlyphProviderDefinition.Conditional> providers() {
            return this.providers;
        }
    }

    static final class UnresolvedBuilderBundle
    extends Record
    implements DependencySorter.Entry<ResourceLocation> {
        final ResourceLocation fontId;
        private final List<BuilderResult> builders;
        private final Set<ResourceLocation> dependencies;

        public UnresolvedBuilderBundle(ResourceLocation resourceLocation) {
            this(resourceLocation, new ArrayList<BuilderResult>(), new HashSet<ResourceLocation>());
        }

        private UnresolvedBuilderBundle(ResourceLocation resourceLocation, List<BuilderResult> list, Set<ResourceLocation> set) {
            this.fontId = resourceLocation;
            this.builders = list;
            this.dependencies = set;
        }

        public void add(BuilderId builderId, FontOption.Filter filter, GlyphProviderDefinition.Reference reference) {
            this.builders.add(new BuilderResult(builderId, filter, (Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation>)Either.right((Object)reference.id())));
            this.dependencies.add(reference.id());
        }

        public void add(BuilderId builderId, FontOption.Filter filter, CompletableFuture<Optional<GlyphProvider>> completableFuture) {
            this.builders.add(new BuilderResult(builderId, filter, (Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation>)Either.left(completableFuture)));
        }

        private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
            return this.builders.stream().flatMap(builderResult -> builderResult.result.left().stream());
        }

        public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> function) {
            ArrayList arrayList = new ArrayList();
            for (BuilderResult builderResult : this.builders) {
                Optional<List<GlyphProvider.Conditional>> optional = builderResult.resolve(function);
                if (optional.isPresent()) {
                    arrayList.addAll(optional.get());
                    continue;
                }
                return Optional.empty();
            }
            return Optional.of(arrayList);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            this.dependencies.forEach(consumer);
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnresolvedBuilderBundle.class, "fontId;builders;dependencies", "fontId", "builders", "dependencies"}, this, object);
        }

        public ResourceLocation fontId() {
            return this.fontId;
        }

        public List<BuilderResult> builders() {
            return this.builders;
        }

        public Set<ResourceLocation> dependencies() {
            return this.dependencies;
        }
    }

    static final class BuilderResult
    extends Record {
        private final BuilderId id;
        private final FontOption.Filter filter;
        final Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result;

        BuilderResult(BuilderId builderId, FontOption.Filter filter, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> either) {
            this.id = builderId;
            this.filter = filter;
            this.result = either;
        }

        public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> function) {
            return (Optional)this.result.map(completableFuture -> ((Optional)completableFuture.join()).map(glyphProvider -> List.of(new GlyphProvider.Conditional((GlyphProvider)glyphProvider, this.filter))), resourceLocation -> {
                List list = (List)function.apply((ResourceLocation)resourceLocation);
                if (list == null) {
                    LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", resourceLocation, (Object)this.id);
                    return Optional.empty();
                }
                return Optional.of(list.stream().map(this::mergeFilters).toList());
            });
        }

        private GlyphProvider.Conditional mergeFilters(GlyphProvider.Conditional conditional) {
            return new GlyphProvider.Conditional(conditional.provider(), this.filter.merge(conditional.filter()));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BuilderResult.class, "id;filter;result", "id", "filter", "result"}, this, object);
        }

        public BuilderId id() {
            return this.id;
        }

        public FontOption.Filter filter() {
            return this.filter;
        }

        public Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result() {
            return this.result;
        }
    }
}

