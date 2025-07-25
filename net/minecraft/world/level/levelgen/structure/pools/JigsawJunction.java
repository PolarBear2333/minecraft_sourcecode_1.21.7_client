/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawJunction {
    private final int sourceX;
    private final int sourceGroundY;
    private final int sourceZ;
    private final int deltaY;
    private final StructureTemplatePool.Projection destProjection;

    public JigsawJunction(int n, int n2, int n3, int n4, StructureTemplatePool.Projection projection) {
        this.sourceX = n;
        this.sourceGroundY = n2;
        this.sourceZ = n3;
        this.deltaY = n4;
        this.destProjection = projection;
    }

    public int getSourceX() {
        return this.sourceX;
    }

    public int getSourceGroundY() {
        return this.sourceGroundY;
    }

    public int getSourceZ() {
        return this.sourceZ;
    }

    public int getDeltaY() {
        return this.deltaY;
    }

    public StructureTemplatePool.Projection getDestProjection() {
        return this.destProjection;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("source_x"), dynamicOps.createInt(this.sourceX)).put(dynamicOps.createString("source_ground_y"), dynamicOps.createInt(this.sourceGroundY)).put(dynamicOps.createString("source_z"), dynamicOps.createInt(this.sourceZ)).put(dynamicOps.createString("delta_y"), dynamicOps.createInt(this.deltaY)).put(dynamicOps.createString("dest_proj"), dynamicOps.createString(this.destProjection.getName()));
        return new Dynamic(dynamicOps, dynamicOps.createMap((Map)builder.build()));
    }

    public static <T> JigsawJunction deserialize(Dynamic<T> dynamic) {
        return new JigsawJunction(dynamic.get("source_x").asInt(0), dynamic.get("source_ground_y").asInt(0), dynamic.get("source_z").asInt(0), dynamic.get("delta_y").asInt(0), StructureTemplatePool.Projection.byName(dynamic.get("dest_proj").asString("")));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        JigsawJunction jigsawJunction = (JigsawJunction)object;
        if (this.sourceX != jigsawJunction.sourceX) {
            return false;
        }
        if (this.sourceZ != jigsawJunction.sourceZ) {
            return false;
        }
        if (this.deltaY != jigsawJunction.deltaY) {
            return false;
        }
        return this.destProjection == jigsawJunction.destProjection;
    }

    public int hashCode() {
        int n = this.sourceX;
        n = 31 * n + this.sourceGroundY;
        n = 31 * n + this.sourceZ;
        n = 31 * n + this.deltaY;
        n = 31 * n + this.destProjection.hashCode();
        return n;
    }

    public String toString() {
        return "JigsawJunction{sourceX=" + this.sourceX + ", sourceGroundY=" + this.sourceGroundY + ", sourceZ=" + this.sourceZ + ", deltaY=" + this.deltaY + ", destProjection=" + String.valueOf(this.destProjection) + "}";
    }
}

