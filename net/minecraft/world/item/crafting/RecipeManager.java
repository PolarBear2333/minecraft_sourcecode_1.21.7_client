/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager
extends SimplePreparableReloadListener<RecipeMap>
implements RecipeAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceKey<RecipePropertySet>, IngredientExtractor> RECIPE_PROPERTY_SETS = Map.of(RecipePropertySet.SMITHING_ADDITION, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = smithingRecipe.additionIngredient();
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.SMITHING_BASE, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = Optional.of(smithingRecipe.baseIngredient());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.SMITHING_TEMPLATE, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = smithingRecipe.templateIngredient();
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.FURNACE_INPUT, RecipeManager.forSingleInput(RecipeType.SMELTING), RecipePropertySet.BLAST_FURNACE_INPUT, RecipeManager.forSingleInput(RecipeType.BLASTING), RecipePropertySet.SMOKER_INPUT, RecipeManager.forSingleInput(RecipeType.SMOKING), RecipePropertySet.CAMPFIRE_INPUT, RecipeManager.forSingleInput(RecipeType.CAMPFIRE_COOKING));
    private static final FileToIdConverter RECIPE_LISTER = FileToIdConverter.registry(Registries.RECIPE);
    private final HolderLookup.Provider registries;
    private RecipeMap recipes = RecipeMap.EMPTY;
    private Map<ResourceKey<RecipePropertySet>, RecipePropertySet> propertySets = Map.of();
    private SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes = SelectableRecipe.SingleInputSet.empty();
    private List<ServerDisplayInfo> allDisplays = List.of();
    private Map<ResourceKey<Recipe<?>>, List<ServerDisplayInfo>> recipeToDisplay = Map.of();

    public RecipeManager(HolderLookup.Provider provider) {
        this.registries = provider;
    }

    @Override
    protected RecipeMap prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        TreeMap<ResourceLocation, Recipe> treeMap = new TreeMap<ResourceLocation, Recipe>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, RECIPE_LISTER, this.registries.createSerializationContext(JsonOps.INSTANCE), Recipe.CODEC, treeMap);
        ArrayList arrayList = new ArrayList(treeMap.size());
        treeMap.forEach((resourceLocation, recipe) -> {
            ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, resourceLocation);
            RecipeHolder<Recipe> recipeHolder = new RecipeHolder<Recipe>(resourceKey, (Recipe)recipe);
            arrayList.add(recipeHolder);
        });
        return RecipeMap.create(arrayList);
    }

    @Override
    protected void apply(RecipeMap recipeMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.recipes = recipeMap;
        LOGGER.info("Loaded {} recipes", (Object)recipeMap.values().size());
    }

    public void finalizeRecipeLoading(FeatureFlagSet featureFlagSet) {
        ArrayList arrayList = new ArrayList();
        List<IngredientCollector> list = RECIPE_PROPERTY_SETS.entrySet().stream().map(entry -> new IngredientCollector((ResourceKey)entry.getKey(), (IngredientExtractor)entry.getValue())).toList();
        this.recipes.values().forEach(recipeHolder -> {
            Object t = recipeHolder.value();
            if (!t.isSpecial() && t.placementInfo().isImpossibleToPlace()) {
                LOGGER.warn("Recipe {} can't be placed due to empty ingredients and will be ignored", (Object)recipeHolder.id().location());
                return;
            }
            list.forEach(ingredientCollector -> ingredientCollector.accept((Recipe<?>)t));
            if (t instanceof StonecutterRecipe) {
                StonecutterRecipe stonecutterRecipe = (StonecutterRecipe)t;
                RecipeHolder recipeHolder2 = recipeHolder;
                if (RecipeManager.isIngredientEnabled(featureFlagSet, stonecutterRecipe.input()) && stonecutterRecipe.resultDisplay().isEnabled(featureFlagSet)) {
                    arrayList.add(new SelectableRecipe.SingleInputEntry(stonecutterRecipe.input(), new SelectableRecipe(stonecutterRecipe.resultDisplay(), Optional.of(recipeHolder2))));
                }
            }
        });
        this.propertySets = list.stream().collect(Collectors.toUnmodifiableMap(ingredientCollector -> ingredientCollector.key, ingredientCollector -> ingredientCollector.asPropertySet(featureFlagSet)));
        this.stonecutterRecipes = new SelectableRecipe.SingleInputSet(arrayList);
        this.allDisplays = RecipeManager.unpackRecipeInfo(this.recipes.values(), featureFlagSet);
        this.recipeToDisplay = this.allDisplays.stream().collect(Collectors.groupingBy(serverDisplayInfo -> serverDisplayInfo.parent.id(), IdentityHashMap::new, Collectors.toList()));
    }

    static List<Ingredient> filterDisabled(FeatureFlagSet featureFlagSet, List<Ingredient> list) {
        list.removeIf(ingredient -> !RecipeManager.isIngredientEnabled(featureFlagSet, ingredient));
        return list;
    }

    private static boolean isIngredientEnabled(FeatureFlagSet featureFlagSet, Ingredient ingredient) {
        return ingredient.items().allMatch(holder -> ((Item)holder.value()).isEnabled(featureFlagSet));
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level, @Nullable ResourceKey<Recipe<?>> resourceKey) {
        RecipeHolder<T> recipeHolder = resourceKey != null ? this.byKeyTyped(recipeType, resourceKey) : null;
        return this.getRecipeFor(recipeType, i, level, recipeHolder);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level, @Nullable RecipeHolder<T> recipeHolder) {
        if (recipeHolder != null && recipeHolder.value().matches(i, level)) {
            return Optional.of(recipeHolder);
        }
        return this.getRecipeFor(recipeType, i, level);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeType, I i, Level level) {
        return this.recipes.getRecipesFor(recipeType, i, level).findFirst();
    }

    public Optional<RecipeHolder<?>> byKey(ResourceKey<Recipe<?>> resourceKey) {
        return Optional.ofNullable(this.recipes.byKey(resourceKey));
    }

    @Nullable
    private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> recipeType, ResourceKey<Recipe<?>> resourceKey) {
        RecipeHolder<?> recipeHolder = this.recipes.byKey(resourceKey);
        if (recipeHolder != null && recipeHolder.value().getType().equals(recipeType)) {
            return recipeHolder;
        }
        return null;
    }

    public Map<ResourceKey<RecipePropertySet>, RecipePropertySet> getSynchronizedItemProperties() {
        return this.propertySets;
    }

    public SelectableRecipe.SingleInputSet<StonecutterRecipe> getSynchronizedStonecutterRecipes() {
        return this.stonecutterRecipes;
    }

    @Override
    public RecipePropertySet propertySet(ResourceKey<RecipePropertySet> resourceKey) {
        return this.propertySets.getOrDefault(resourceKey, RecipePropertySet.EMPTY);
    }

    @Override
    public SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
        return this.stonecutterRecipes;
    }

    public Collection<RecipeHolder<?>> getRecipes() {
        return this.recipes.values();
    }

    @Nullable
    public ServerDisplayInfo getRecipeFromDisplay(RecipeDisplayId recipeDisplayId) {
        int n = recipeDisplayId.index();
        return n >= 0 && n < this.allDisplays.size() ? this.allDisplays.get(n) : null;
    }

    public void listDisplaysForRecipe(ResourceKey<Recipe<?>> resourceKey, Consumer<RecipeDisplayEntry> consumer) {
        List<ServerDisplayInfo> list = this.recipeToDisplay.get(resourceKey);
        if (list != null) {
            list.forEach(serverDisplayInfo -> consumer.accept(serverDisplayInfo.display));
        }
    }

    @VisibleForTesting
    protected static RecipeHolder<?> fromJson(ResourceKey<Recipe<?>> resourceKey, JsonObject jsonObject, HolderLookup.Provider provider) {
        Recipe recipe = (Recipe)Recipe.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), (Object)jsonObject).getOrThrow(JsonParseException::new);
        return new RecipeHolder<Recipe>(resourceKey, recipe);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> CachedCheck<I, T> createCheck(final RecipeType<T> recipeType) {
        return new CachedCheck<I, T>(){
            @Nullable
            private ResourceKey<Recipe<?>> lastRecipe;

            @Override
            public Optional<RecipeHolder<T>> getRecipeFor(I i, ServerLevel serverLevel) {
                RecipeManager recipeManager = serverLevel.recipeAccess();
                Optional optional = recipeManager.getRecipeFor(recipeType, i, (Level)serverLevel, this.lastRecipe);
                if (optional.isPresent()) {
                    RecipeHolder recipeHolder = optional.get();
                    this.lastRecipe = recipeHolder.id();
                    return Optional.of(recipeHolder);
                }
                return Optional.empty();
            }
        };
    }

    private static List<ServerDisplayInfo> unpackRecipeInfo(Iterable<RecipeHolder<?>> iterable, FeatureFlagSet featureFlagSet) {
        ArrayList<ServerDisplayInfo> arrayList = new ArrayList<ServerDisplayInfo>();
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (RecipeHolder<?> recipeHolder : iterable) {
            Object obj = recipeHolder.value();
            OptionalInt optionalInt = obj.group().isEmpty() ? OptionalInt.empty() : OptionalInt.of(object2IntOpenHashMap.computeIfAbsent((Object)obj.group(), arg_0 -> RecipeManager.lambda$unpackRecipeInfo$13((Object2IntMap)object2IntOpenHashMap, arg_0)));
            Optional<Object> optional = obj.isSpecial() ? Optional.empty() : Optional.of(obj.placementInfo().ingredients());
            for (RecipeDisplay recipeDisplay : obj.display()) {
                if (!recipeDisplay.isEnabled(featureFlagSet)) continue;
                int n = arrayList.size();
                RecipeDisplayId recipeDisplayId = new RecipeDisplayId(n);
                RecipeDisplayEntry recipeDisplayEntry = new RecipeDisplayEntry(recipeDisplayId, recipeDisplay, optionalInt, obj.recipeBookCategory(), optional);
                arrayList.add(new ServerDisplayInfo(recipeDisplayEntry, recipeHolder));
            }
        }
        return arrayList;
    }

    private static IngredientExtractor forSingleInput(RecipeType<? extends SingleItemRecipe> recipeType) {
        return recipe -> {
            Optional<Object> optional;
            if (recipe.getType() == recipeType && recipe instanceof SingleItemRecipe) {
                SingleItemRecipe singleItemRecipe = (SingleItemRecipe)recipe;
                optional = Optional.of(singleItemRecipe.input());
            } else {
                optional = Optional.empty();
            }
            return optional;
        };
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    private static /* synthetic */ int lambda$unpackRecipeInfo$13(Object2IntMap object2IntMap, Object object) {
        return object2IntMap.size();
    }

    public static final class ServerDisplayInfo
    extends Record {
        final RecipeDisplayEntry display;
        final RecipeHolder<?> parent;

        public ServerDisplayInfo(RecipeDisplayEntry recipeDisplayEntry, RecipeHolder<?> recipeHolder) {
            this.display = recipeDisplayEntry;
            this.parent = recipeHolder;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerDisplayInfo.class, "display;parent", "display", "parent"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerDisplayInfo.class, "display;parent", "display", "parent"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerDisplayInfo.class, "display;parent", "display", "parent"}, this, object);
        }

        public RecipeDisplayEntry display() {
            return this.display;
        }

        public RecipeHolder<?> parent() {
            return this.parent;
        }
    }

    @FunctionalInterface
    public static interface IngredientExtractor {
        public Optional<Ingredient> apply(Recipe<?> var1);
    }

    public static class IngredientCollector
    implements Consumer<Recipe<?>> {
        final ResourceKey<RecipePropertySet> key;
        private final IngredientExtractor extractor;
        private final List<Ingredient> ingredients = new ArrayList<Ingredient>();

        protected IngredientCollector(ResourceKey<RecipePropertySet> resourceKey, IngredientExtractor ingredientExtractor) {
            this.key = resourceKey;
            this.extractor = ingredientExtractor;
        }

        @Override
        public void accept(Recipe<?> recipe) {
            this.extractor.apply(recipe).ifPresent(this.ingredients::add);
        }

        public RecipePropertySet asPropertySet(FeatureFlagSet featureFlagSet) {
            return RecipePropertySet.create(RecipeManager.filterDisabled(featureFlagSet, this.ingredients));
        }

        @Override
        public /* synthetic */ void accept(Object object) {
            this.accept((Recipe)object);
        }
    }

    public static interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
        public Optional<RecipeHolder<T>> getRecipeFor(I var1, ServerLevel var2);
    }
}

