/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class CarvingStepRemoveFix
extends DataFix {
    public CarvingStepRemoveFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("CarvingStepRemoveFix", this.getInputSchema().getType(References.CHUNK), CarvingStepRemoveFix::fixChunk);
    }

    private static Typed<?> fixChunk(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional;
            Dynamic dynamic2 = dynamic;
            Optional optional2 = dynamic2.get("CarvingMasks").result();
            if (optional2.isPresent() && (optional = ((Dynamic)optional2.get()).get("AIR").result()).isPresent()) {
                dynamic2 = dynamic2.set("carving_mask", (Dynamic)optional.get());
            }
            return dynamic2.remove("CarvingMasks");
        });
    }
}

