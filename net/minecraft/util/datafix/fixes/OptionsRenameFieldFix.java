/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class OptionsRenameFieldFix
extends DataFix {
    private final String fixName;
    private final String fieldFrom;
    private final String fieldTo;

    public OptionsRenameFieldFix(Schema schema, boolean bl, String string, String string2, String string3) {
        super(schema, bl);
        this.fixName = string;
        this.fieldFrom = string2;
        this.fieldTo = string3;
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.fixName, this.getInputSchema().getType(References.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.renameField(this.fieldFrom, this.fieldTo)));
    }
}

