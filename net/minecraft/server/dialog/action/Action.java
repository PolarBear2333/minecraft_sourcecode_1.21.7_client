/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.server.dialog.action;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;

public interface Action {
    public static final Codec<Action> CODEC = BuiltInRegistries.DIALOG_ACTION_TYPE.byNameCodec().dispatch(Action::codec, mapCodec -> mapCodec);

    public MapCodec<? extends Action> codec();

    public Optional<ClickEvent> createAction(Map<String, ValueGetter> var1);

    public static interface ValueGetter {
        public String asTemplateSubstitution();

        public Tag asTag();

        public static Map<String, String> getAsTemplateSubstitutions(Map<String, ValueGetter> map) {
            return Maps.transformValues(map, ValueGetter::asTemplateSubstitution);
        }

        public static ValueGetter of(final String string) {
            return new ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return string;
                }

                @Override
                public Tag asTag() {
                    return StringTag.valueOf(string);
                }
            };
        }

        public static ValueGetter of(final Supplier<String> supplier) {
            return new ValueGetter(){

                @Override
                public String asTemplateSubstitution() {
                    return (String)supplier.get();
                }

                @Override
                public Tag asTag() {
                    return StringTag.valueOf((String)supplier.get());
                }
            };
        }
    }
}

