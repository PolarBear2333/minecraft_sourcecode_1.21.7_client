/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class AddFlagIfNotPresentFix
extends DataFix {
    private final String name;
    private final boolean flagValue;
    private final String flagKey;
    private final DSL.TypeReference typeReference;

    public AddFlagIfNotPresentFix(Schema schema, DSL.TypeReference typeReference, String string, boolean bl) {
        super(schema, true);
        this.flagValue = bl;
        this.flagKey = string;
        this.name = "AddFlagIfNotPresentFix_" + this.flagKey + "=" + this.flagValue + " for " + schema.getVersionKey();
        this.typeReference = typeReference;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(this.typeReference);
        return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.set(this.flagKey, (Dynamic)DataFixUtils.orElseGet((Optional)dynamic.get(this.flagKey).result(), () -> dynamic.createBoolean(this.flagValue)))));
    }
}

