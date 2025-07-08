/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.chars.CharArraySet
 *  it.unimi.dsi.fastutil.chars.CharSet
 */
package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;

public final class ShapedRecipePattern {
    private static final int MAX_SIZE = 3;
    public static final char EMPTY_SLOT = ' ';
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, shapedRecipePattern -> shapedRecipePattern.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, shapedRecipePattern -> shapedRecipePattern.width, ByteBufCodecs.VAR_INT, shapedRecipePattern -> shapedRecipePattern.height, Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), shapedRecipePattern -> shapedRecipePattern.ingredients, ShapedRecipePattern::createFromNetwork);
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> ingredients;
    private final Optional<Data> data;
    private final int ingredientCount;
    private final boolean symmetrical;

    public ShapedRecipePattern(int n, int n2, List<Optional<Ingredient>> list, Optional<Data> optional) {
        this.width = n;
        this.height = n2;
        this.ingredients = list;
        this.data = optional;
        this.ingredientCount = (int)list.stream().flatMap(Optional::stream).count();
        this.symmetrical = Util.isSymmetrical(n, n2, list);
    }

    private static ShapedRecipePattern createFromNetwork(Integer n, Integer n2, List<Optional<Ingredient>> list) {
        return new ShapedRecipePattern(n, n2, list, Optional.empty());
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> map, String ... stringArray) {
        return ShapedRecipePattern.of(map, List.of(stringArray));
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> map, List<String> list) {
        Data data = new Data(map, list);
        return (ShapedRecipePattern)ShapedRecipePattern.unpack(data).getOrThrow();
    }

    private static DataResult<ShapedRecipePattern> unpack(Data data) {
        String[] stringArray = ShapedRecipePattern.shrink(data.pattern);
        int n = stringArray[0].length();
        int n2 = stringArray.length;
        ArrayList<Optional<Ingredient>> arrayList = new ArrayList<Optional<Ingredient>>(n * n2);
        CharArraySet charArraySet = new CharArraySet(data.key.keySet());
        for (String string : stringArray) {
            for (int i = 0; i < string.length(); ++i) {
                Optional<Object> optional;
                char c = string.charAt(i);
                if (c == ' ') {
                    optional = Optional.empty();
                } else {
                    Ingredient ingredient = data.key.get(Character.valueOf(c));
                    if (ingredient == null) {
                        return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
                    }
                    optional = Optional.of(ingredient);
                }
                charArraySet.remove(c);
                arrayList.add(optional);
            }
        }
        if (!charArraySet.isEmpty()) {
            return DataResult.error(() -> ShapedRecipePattern.lambda$unpack$7((CharSet)charArraySet));
        }
        return DataResult.success((Object)new ShapedRecipePattern(n, n2, arrayList, Optional.of(data)));
    }

    @VisibleForTesting
    static String[] shrink(List<String> list) {
        int n = Integer.MAX_VALUE;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (int i = 0; i < list.size(); ++i) {
            String string = list.get(i);
            n = Math.min(n, ShapedRecipePattern.firstNonEmpty(string));
            int n5 = ShapedRecipePattern.lastNonEmpty(string);
            n2 = Math.max(n2, n5);
            if (n5 < 0) {
                if (n3 == i) {
                    ++n3;
                }
                ++n4;
                continue;
            }
            n4 = 0;
        }
        if (list.size() == n4) {
            return new String[0];
        }
        String[] stringArray = new String[list.size() - n4 - n3];
        for (int i = 0; i < stringArray.length; ++i) {
            stringArray[i] = list.get(i + n3).substring(n, n2 + 1);
        }
        return stringArray;
    }

    private static int firstNonEmpty(String string) {
        int n;
        for (n = 0; n < string.length() && string.charAt(n) == ' '; ++n) {
        }
        return n;
    }

    private static int lastNonEmpty(String string) {
        int n;
        for (n = string.length() - 1; n >= 0 && string.charAt(n) == ' '; --n) {
        }
        return n;
    }

    public boolean matches(CraftingInput craftingInput) {
        if (craftingInput.ingredientCount() != this.ingredientCount) {
            return false;
        }
        if (craftingInput.width() == this.width && craftingInput.height() == this.height) {
            if (!this.symmetrical && this.matches(craftingInput, true)) {
                return true;
            }
            if (this.matches(craftingInput, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(CraftingInput craftingInput, boolean bl) {
        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                ItemStack itemStack;
                Optional<Ingredient> optional = bl ? this.ingredients.get(this.width - j - 1 + i * this.width) : this.ingredients.get(j + i * this.width);
                if (Ingredient.testOptionalIngredient(optional, itemStack = craftingInput.getItem(j, i))) continue;
                return false;
            }
        }
        return true;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public List<Optional<Ingredient>> ingredients() {
        return this.ingredients;
    }

    private static /* synthetic */ String lambda$unpack$7(CharSet charSet) {
        return "Key defines symbols that aren't used in pattern: " + String.valueOf(charSet);
    }

    public static final class Data
    extends Record {
        final Map<Character, Ingredient> key;
        final List<String> pattern;
        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(list -> {
            if (list.size() > 3) {
                return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
            }
            if (list.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            }
            int n = ((String)list.getFirst()).length();
            for (String string : list) {
                if (string.length() > 3) {
                    return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
                }
                if (n == string.length()) continue;
                return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
            return DataResult.success((Object)list);
        }, Function.identity());
        private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(string -> {
            if (string.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + string + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(string)) {
                return DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.");
            }
            return DataResult.success((Object)Character.valueOf(string.charAt(0)));
        }, String::valueOf);
        public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC).fieldOf("key").forGetter(data -> data.key), (App)PATTERN_CODEC.fieldOf("pattern").forGetter(data -> data.pattern)).apply((Applicative)instance, Data::new));

        public Data(Map<Character, Ingredient> map, List<String> list) {
            this.key = map;
            this.pattern = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this, object);
        }

        public Map<Character, Ingredient> key() {
            return this.key;
        }

        public List<String> pattern() {
            return this.pattern;
        }
    }
}

