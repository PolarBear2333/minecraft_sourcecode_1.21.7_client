/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record WaypointStyle(int nearDistance, int farDistance, List<ResourceLocation> sprites, List<ResourceLocation> spriteLocations) {
    public static final int DEFAULT_NEAR_DISTANCE = 128;
    public static final int DEFAULT_FAR_DISTANCE = 332;
    private static final Codec<Integer> DISTANCE_CODEC = Codec.intRange((int)0, (int)60000000);
    public static final Codec<WaypointStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DISTANCE_CODEC.optionalFieldOf("near_distance", (Object)128).forGetter(WaypointStyle::nearDistance), (App)DISTANCE_CODEC.optionalFieldOf("far_distance", (Object)332).forGetter(WaypointStyle::farDistance), (App)ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("sprites").forGetter(WaypointStyle::sprites)).apply((Applicative)instance, WaypointStyle::new)).validate(WaypointStyle::validate);

    public WaypointStyle(int n, int n2, List<ResourceLocation> list) {
        this(n, n2, list, list.stream().map(resourceLocation -> resourceLocation.withPrefix("hud/locator_bar_dot/")).toList());
    }

    private DataResult<WaypointStyle> validate() {
        if (this.nearDistance >= this.farDistance) {
            return DataResult.error(() -> "Far distance (" + this.farDistance + ") cannot be closer or equal to near distance (" + this.nearDistance + ")");
        }
        return DataResult.success((Object)this);
    }

    public ResourceLocation sprite(float f) {
        if (f <= (float)this.nearDistance) {
            return this.spriteLocations.getFirst();
        }
        if (f >= (float)this.farDistance) {
            return this.spriteLocations.getLast();
        }
        int n = Mth.lerpInt((f - (float)this.nearDistance) / (float)(this.farDistance - this.nearDistance), 0, this.spriteLocations.size());
        return this.spriteLocations.get(n);
    }
}

