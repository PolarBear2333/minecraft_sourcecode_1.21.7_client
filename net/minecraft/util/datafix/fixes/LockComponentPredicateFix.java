/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.escape.Escaper
 *  com.google.common.escape.Escapers
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.fixes.DataComponentRemainderFix;

public class LockComponentPredicateFix
extends DataComponentRemainderFix {
    public static final Escaper ESCAPER = Escapers.builder().addEscape('\"', "\\\"").addEscape('\\', "\\\\").build();

    public LockComponentPredicateFix(Schema schema) {
        super(schema, "LockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    @Nullable
    protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return LockComponentPredicateFix.fixLock(dynamic);
    }

    @Nullable
    public static <T> Dynamic<T> fixLock(Dynamic<T> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isEmpty()) {
            return null;
        }
        if (((String)optional.get()).isEmpty()) {
            return null;
        }
        Dynamic dynamic2 = dynamic.createString("\"" + ESCAPER.escape((String)optional.get()) + "\"");
        Dynamic dynamic3 = dynamic.emptyMap().set("minecraft:custom_name", dynamic2);
        return dynamic.emptyMap().set("components", dynamic3);
    }
}

