/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public final class OverworldBiomeBuilder {
    private static final float VALLEY_SIZE = 0.05f;
    private static final float LOW_START = 0.26666668f;
    public static final float HIGH_START = 0.4f;
    private static final float HIGH_END = 0.93333334f;
    private static final float PEAK_SIZE = 0.1f;
    public static final float PEAK_START = 0.56666666f;
    private static final float PEAK_END = 0.7666667f;
    public static final float NEAR_INLAND_START = -0.11f;
    public static final float MID_INLAND_START = 0.03f;
    public static final float FAR_INLAND_START = 0.3f;
    public static final float EROSION_INDEX_1_START = -0.78f;
    public static final float EROSION_INDEX_2_START = -0.375f;
    private static final float EROSION_DEEP_DARK_DRYNESS_THRESHOLD = -0.225f;
    private static final float DEPTH_DEEP_DARK_DRYNESS_THRESHOLD = 0.9f;
    private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0f, 1.0f);
    private final Climate.Parameter[] temperatures = new Climate.Parameter[]{Climate.Parameter.span(-1.0f, -0.45f), Climate.Parameter.span(-0.45f, -0.15f), Climate.Parameter.span(-0.15f, 0.2f), Climate.Parameter.span(0.2f, 0.55f), Climate.Parameter.span(0.55f, 1.0f)};
    private final Climate.Parameter[] humidities = new Climate.Parameter[]{Climate.Parameter.span(-1.0f, -0.35f), Climate.Parameter.span(-0.35f, -0.1f), Climate.Parameter.span(-0.1f, 0.1f), Climate.Parameter.span(0.1f, 0.3f), Climate.Parameter.span(0.3f, 1.0f)};
    private final Climate.Parameter[] erosions = new Climate.Parameter[]{Climate.Parameter.span(-1.0f, -0.78f), Climate.Parameter.span(-0.78f, -0.375f), Climate.Parameter.span(-0.375f, -0.2225f), Climate.Parameter.span(-0.2225f, 0.05f), Climate.Parameter.span(0.05f, 0.45f), Climate.Parameter.span(0.45f, 0.55f), Climate.Parameter.span(0.55f, 1.0f)};
    private final Climate.Parameter FROZEN_RANGE = this.temperatures[0];
    private final Climate.Parameter UNFROZEN_RANGE = Climate.Parameter.span(this.temperatures[1], this.temperatures[4]);
    private final Climate.Parameter mushroomFieldsContinentalness = Climate.Parameter.span(-1.2f, -1.05f);
    private final Climate.Parameter deepOceanContinentalness = Climate.Parameter.span(-1.05f, -0.455f);
    private final Climate.Parameter oceanContinentalness = Climate.Parameter.span(-0.455f, -0.19f);
    private final Climate.Parameter coastContinentalness = Climate.Parameter.span(-0.19f, -0.11f);
    private final Climate.Parameter inlandContinentalness = Climate.Parameter.span(-0.11f, 0.55f);
    private final Climate.Parameter nearInlandContinentalness = Climate.Parameter.span(-0.11f, 0.03f);
    private final Climate.Parameter midInlandContinentalness = Climate.Parameter.span(0.03f, 0.3f);
    private final Climate.Parameter farInlandContinentalness = Climate.Parameter.span(0.3f, 1.0f);
    private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{{Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN}, {Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}};
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA}, {Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA}, {Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST}, {Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE}, {Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT}};
    private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{{Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null}, {null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA}, {Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null}, {null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE}, {null, null, null, null, null}};
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA}, {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA}, {Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.PALE_GARDEN}, {Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE}, {Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS}};
    private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{{Biomes.ICE_SPIKES, null, null, null, null}, {Biomes.CHERRY_GROVE, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA}, {Biomes.CHERRY_GROVE, Biomes.CHERRY_GROVE, Biomes.FOREST, Biomes.BIRCH_FOREST, null}, {null, null, null, null, null}, {Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}};
    private final ResourceKey<Biome>[][] SHATTERED_BIOMES = new ResourceKey[][]{{Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {null, null, null, null, null}, {null, null, null, null, null}};

    public List<Climate.ParameterPoint> spawnTarget() {
        Climate.Parameter parameter = Climate.Parameter.point(0.0f);
        float f = 0.16f;
        return List.of(new Climate.ParameterPoint(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE), this.FULL_RANGE, parameter, Climate.Parameter.span(-1.0f, -0.16f), 0L), new Climate.ParameterPoint(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE), this.FULL_RANGE, parameter, Climate.Parameter.span(0.16f, 1.0f), 0L));
    }

    protected void addBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
            this.addDebugBiomes(consumer);
            return;
        }
        this.addOffCoastBiomes(consumer);
        this.addInlandBiomes(consumer);
        this.addUndergroundBiomes(consumer);
    }

    private void addDebugBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        ResourceKey<Biome> resourceKey;
        CubicSpline cubicSpline;
        HolderLookup.Provider provider = VanillaRegistries.createLookup();
        HolderGetter holderGetter = provider.lookupOrThrow(Registries.DENSITY_FUNCTION);
        DensityFunctions.Spline.Coordinate coordinate = new DensityFunctions.Spline.Coordinate(holderGetter.getOrThrow(NoiseRouterData.CONTINENTS));
        DensityFunctions.Spline.Coordinate coordinate2 = new DensityFunctions.Spline.Coordinate(holderGetter.getOrThrow(NoiseRouterData.EROSION));
        DensityFunctions.Spline.Coordinate coordinate3 = new DensityFunctions.Spline.Coordinate(holderGetter.getOrThrow(NoiseRouterData.RIDGES_FOLDED));
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(0.0f), this.FULL_RANGE, 0.01f), Biomes.PLAINS));
        CubicSpline cubicSpline2 = TerrainProvider.buildErosionOffsetSpline(coordinate2, coordinate3, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, ToFloatFunction.IDENTITY);
        if (cubicSpline2 instanceof CubicSpline.Multipoint) {
            cubicSpline = (CubicSpline.Multipoint)cubicSpline2;
            resourceKey = Biomes.DESERT;
            for (float f : cubicSpline.locations()) {
                consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(f), Climate.Parameter.point(0.0f), this.FULL_RANGE, 0.0f), (Object)resourceKey));
                resourceKey = resourceKey == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
            }
        }
        if ((cubicSpline = TerrainProvider.overworldOffset(coordinate, coordinate2, coordinate3, false)) instanceof CubicSpline.Multipoint) {
            resourceKey = cubicSpline;
            for (float f : ((CubicSpline.Multipoint)((Object)resourceKey)).locations()) {
                consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(f), this.FULL_RANGE, Climate.Parameter.point(0.0f), this.FULL_RANGE, 0.0f), Biomes.SNOWY_TAIGA));
            }
        }
    }

    private void addOffCoastBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        this.addSurfaceBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, this.mushroomFieldsContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0f, Biomes.MUSHROOM_FIELDS);
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter = this.temperatures[i];
            this.addSurfaceBiome(consumer, parameter, this.FULL_RANGE, this.deepOceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0f, this.OCEANS[0][i]);
            this.addSurfaceBiome(consumer, parameter, this.FULL_RANGE, this.oceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0f, this.OCEANS[1][i]);
        }
    }

    private void addInlandBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        this.addMidSlice(consumer, Climate.Parameter.span(-1.0f, -0.93333334f));
        this.addHighSlice(consumer, Climate.Parameter.span(-0.93333334f, -0.7666667f));
        this.addPeaks(consumer, Climate.Parameter.span(-0.7666667f, -0.56666666f));
        this.addHighSlice(consumer, Climate.Parameter.span(-0.56666666f, -0.4f));
        this.addMidSlice(consumer, Climate.Parameter.span(-0.4f, -0.26666668f));
        this.addLowSlice(consumer, Climate.Parameter.span(-0.26666668f, -0.05f));
        this.addValleys(consumer, Climate.Parameter.span(-0.05f, 0.05f));
        this.addLowSlice(consumer, Climate.Parameter.span(0.05f, 0.26666668f));
        this.addMidSlice(consumer, Climate.Parameter.span(0.26666668f, 0.4f));
        this.addHighSlice(consumer, Climate.Parameter.span(0.4f, 0.56666666f));
        this.addPeaks(consumer, Climate.Parameter.span(0.56666666f, 0.7666667f));
        this.addHighSlice(consumer, Climate.Parameter.span(0.7666667f, 0.93333334f));
        this.addMidSlice(consumer, Climate.Parameter.span(0.93333334f, 1.0f));
    }

    private void addPeaks(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter2 = this.temperatures[i];
            for (int j = 0; j < this.humidities.length; ++j) {
                Climate.Parameter parameter3 = this.humidities[j];
                ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
                ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
                ResourceKey<Biome> resourceKey4 = this.pickPlateauBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey5 = this.pickShatteredBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey6 = this.maybePickWindsweptSavannaBiome(i, j, parameter, resourceKey5);
                ResourceKey<Biome> resourceKey7 = this.pickPeakBiome(i, j, parameter);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[0], parameter, 0.0f, resourceKey7);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[1], parameter, 0.0f, resourceKey3);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[1], parameter, 0.0f, resourceKey7);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[2], parameter, 0.0f, resourceKey4);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[3], parameter, 0.0f, resourceKey2);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[3], parameter, 0.0f, resourceKey4);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey6);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey5);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, resourceKey);
            }
        }
    }

    private void addHighSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter2 = this.temperatures[i];
            for (int j = 0; j < this.humidities.length; ++j) {
                Climate.Parameter parameter3 = this.humidities[j];
                ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
                ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
                ResourceKey<Biome> resourceKey4 = this.pickPlateauBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey5 = this.pickShatteredBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey6 = this.maybePickWindsweptSavannaBiome(i, j, parameter, resourceKey);
                ResourceKey<Biome> resourceKey7 = this.pickSlopeBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey8 = this.pickPeakBiome(i, j, parameter);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0f, resourceKey7);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[0], parameter, 0.0f, resourceKey8);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[1], parameter, 0.0f, resourceKey3);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[1], parameter, 0.0f, resourceKey7);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[2], parameter, 0.0f, resourceKey4);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[3], parameter, 0.0f, resourceKey2);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[3], parameter, 0.0f, resourceKey4);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey6);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey5);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, resourceKey);
            }
        }
    }

    private void addMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
        this.addSurfaceBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[2]), parameter, 0.0f, Biomes.STONY_SHORE);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.SWAMP);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.MANGROVE_SWAMP);
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter2 = this.temperatures[i];
            for (int j = 0; j < this.humidities.length; ++j) {
                Climate.Parameter parameter3 = this.humidities[j];
                ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
                ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
                ResourceKey<Biome> resourceKey4 = this.pickShatteredBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey5 = this.pickPlateauBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey6 = this.pickBeachBiome(i, j);
                ResourceKey<Biome> resourceKey7 = this.maybePickWindsweptSavannaBiome(i, j, parameter, resourceKey);
                ResourceKey<Biome> resourceKey8 = this.pickShatteredCoastBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey9 = this.pickSlopeBiome(i, j, parameter);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[0], parameter, 0.0f, resourceKey9);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[1], parameter, 0.0f, resourceKey3);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[1], parameter, 0.0f, i == 0 ? resourceKey9 : resourceKey5);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[2], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[2], parameter, 0.0f, resourceKey2);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[2], parameter, 0.0f, resourceKey5);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[3], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[3], parameter, 0.0f, resourceKey2);
                if (parameter.max() < 0L) {
                    this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[4], parameter, 0.0f, resourceKey6);
                    this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[4], parameter, 0.0f, resourceKey);
                } else {
                    this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], parameter, 0.0f, resourceKey);
                }
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0f, resourceKey8);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[5], parameter, 0.0f, resourceKey7);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey4);
                if (parameter.max() < 0L) {
                    this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0f, resourceKey6);
                } else {
                    this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0f, resourceKey);
                }
                if (i != 0) continue;
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, resourceKey);
            }
        }
    }

    private void addLowSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
        this.addSurfaceBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[2]), parameter, 0.0f, Biomes.STONY_SHORE);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.SWAMP);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.MANGROVE_SWAMP);
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter2 = this.temperatures[i];
            for (int j = 0; j < this.humidities.length; ++j) {
                Climate.Parameter parameter3 = this.humidities[j];
                ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
                ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
                ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
                ResourceKey<Biome> resourceKey4 = this.pickBeachBiome(i, j);
                ResourceKey<Biome> resourceKey5 = this.maybePickWindsweptSavannaBiome(i, j, parameter, resourceKey);
                ResourceKey<Biome> resourceKey6 = this.pickShatteredCoastBiome(i, j, parameter);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, resourceKey2);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, resourceKey3);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0f, resourceKey2);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, Climate.Parameter.span(this.erosions[3], this.erosions[4]), parameter, 0.0f, resourceKey4);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[4], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0f, resourceKey6);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[5], parameter, 0.0f, resourceKey5);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0f, resourceKey);
                this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0f, resourceKey4);
                if (i != 0) continue;
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, resourceKey);
            }
        }
    }

    private void addValleys(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
        this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, parameter.max() < 0L ? Biomes.STONY_SHORE : Biomes.FROZEN_RIVER);
        this.addSurfaceBiome(consumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, parameter.max() < 0L ? Biomes.STONY_SHORE : Biomes.RIVER);
        this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, Biomes.FROZEN_RIVER);
        this.addSurfaceBiome(consumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, Biomes.RIVER);
        this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[5]), parameter, 0.0f, Biomes.FROZEN_RIVER);
        this.addSurfaceBiome(consumer, this.UNFROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[5]), parameter, 0.0f, Biomes.RIVER);
        this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], parameter, 0.0f, Biomes.FROZEN_RIVER);
        this.addSurfaceBiome(consumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], parameter, 0.0f, Biomes.RIVER);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.SWAMP);
        this.addSurfaceBiome(consumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.MANGROVE_SWAMP);
        this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], parameter, 0.0f, Biomes.FROZEN_RIVER);
        for (int i = 0; i < this.temperatures.length; ++i) {
            Climate.Parameter parameter2 = this.temperatures[i];
            for (int j = 0; j < this.humidities.length; ++j) {
                Climate.Parameter parameter3 = this.humidities[j];
                ResourceKey<Biome> resourceKey = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
                this.addSurfaceBiome(consumer, parameter2, parameter3, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0f, resourceKey);
            }
        }
    }

    private void addUndergroundBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        this.addUndergroundBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(0.8f, 1.0f), this.FULL_RANGE, this.FULL_RANGE, 0.0f, Biomes.DRIPSTONE_CAVES);
        this.addUndergroundBiome(consumer, this.FULL_RANGE, Climate.Parameter.span(0.7f, 1.0f), this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.0f, Biomes.LUSH_CAVES);
        this.addBottomBiome(consumer, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.erosions[0], this.erosions[1]), this.FULL_RANGE, 0.0f, Biomes.DEEP_DARK);
    }

    private ResourceKey<Biome> pickMiddleBiome(int n, int n2, Climate.Parameter parameter) {
        if (parameter.max() < 0L) {
            return this.MIDDLE_BIOMES[n][n2];
        }
        ResourceKey<Biome> resourceKey = this.MIDDLE_BIOMES_VARIANT[n][n2];
        return resourceKey == null ? this.MIDDLE_BIOMES[n][n2] : resourceKey;
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(int n, int n2, Climate.Parameter parameter) {
        return n == 4 ? this.pickBadlandsBiome(n2, parameter) : this.pickMiddleBiome(n, n2, parameter);
    }

    private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(int n, int n2, Climate.Parameter parameter) {
        return n == 0 ? this.pickSlopeBiome(n, n2, parameter) : this.pickMiddleBiomeOrBadlandsIfHot(n, n2, parameter);
    }

    private ResourceKey<Biome> maybePickWindsweptSavannaBiome(int n, int n2, Climate.Parameter parameter, ResourceKey<Biome> resourceKey) {
        if (n > 1 && n2 < 4 && parameter.max() >= 0L) {
            return Biomes.WINDSWEPT_SAVANNA;
        }
        return resourceKey;
    }

    private ResourceKey<Biome> pickShatteredCoastBiome(int n, int n2, Climate.Parameter parameter) {
        ResourceKey<Biome> resourceKey = parameter.max() >= 0L ? this.pickMiddleBiome(n, n2, parameter) : this.pickBeachBiome(n, n2);
        return this.maybePickWindsweptSavannaBiome(n, n2, parameter, resourceKey);
    }

    private ResourceKey<Biome> pickBeachBiome(int n, int n2) {
        if (n == 0) {
            return Biomes.SNOWY_BEACH;
        }
        if (n == 4) {
            return Biomes.DESERT;
        }
        return Biomes.BEACH;
    }

    private ResourceKey<Biome> pickBadlandsBiome(int n, Climate.Parameter parameter) {
        if (n < 2) {
            return parameter.max() < 0L ? Biomes.BADLANDS : Biomes.ERODED_BADLANDS;
        }
        if (n < 3) {
            return Biomes.BADLANDS;
        }
        return Biomes.WOODED_BADLANDS;
    }

    private ResourceKey<Biome> pickPlateauBiome(int n, int n2, Climate.Parameter parameter) {
        ResourceKey<Biome> resourceKey;
        if (parameter.max() >= 0L && (resourceKey = this.PLATEAU_BIOMES_VARIANT[n][n2]) != null) {
            return resourceKey;
        }
        return this.PLATEAU_BIOMES[n][n2];
    }

    private ResourceKey<Biome> pickPeakBiome(int n, int n2, Climate.Parameter parameter) {
        if (n <= 2) {
            return parameter.max() < 0L ? Biomes.JAGGED_PEAKS : Biomes.FROZEN_PEAKS;
        }
        if (n == 3) {
            return Biomes.STONY_PEAKS;
        }
        return this.pickBadlandsBiome(n2, parameter);
    }

    private ResourceKey<Biome> pickSlopeBiome(int n, int n2, Climate.Parameter parameter) {
        if (n >= 3) {
            return this.pickPlateauBiome(n, n2, parameter);
        }
        if (n2 <= 1) {
            return Biomes.SNOWY_SLOPES;
        }
        return Biomes.GROVE;
    }

    private ResourceKey<Biome> pickShatteredBiome(int n, int n2, Climate.Parameter parameter) {
        ResourceKey<Biome> resourceKey = this.SHATTERED_BIOMES[n][n2];
        return resourceKey == null ? this.pickMiddleBiome(n, n2, parameter) : resourceKey;
    }

    private void addSurfaceBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter, Climate.Parameter parameter2, Climate.Parameter parameter3, Climate.Parameter parameter4, Climate.Parameter parameter5, float f, ResourceKey<Biome> resourceKey) {
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.point(0.0f), parameter5, f), resourceKey));
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.point(1.0f), parameter5, f), resourceKey));
    }

    private void addUndergroundBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter, Climate.Parameter parameter2, Climate.Parameter parameter3, Climate.Parameter parameter4, Climate.Parameter parameter5, float f, ResourceKey<Biome> resourceKey) {
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.span(0.2f, 0.9f), parameter5, f), resourceKey));
    }

    private void addBottomBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter, Climate.Parameter parameter2, Climate.Parameter parameter3, Climate.Parameter parameter4, Climate.Parameter parameter5, float f, ResourceKey<Biome> resourceKey) {
        consumer.accept((Pair<Climate.ParameterPoint, ResourceKey<Biome>>)Pair.of((Object)Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.point(1.1f), parameter5, f), resourceKey));
    }

    public static boolean isDeepDarkRegion(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction.FunctionContext functionContext) {
        return densityFunction.compute(functionContext) < (double)-0.225f && densityFunction2.compute(functionContext) > (double)0.9f;
    }

    public static String getDebugStringForPeaksAndValleys(double d) {
        if (d < (double)NoiseRouterData.peaksAndValleys(0.05f)) {
            return "Valley";
        }
        if (d < (double)NoiseRouterData.peaksAndValleys(0.26666668f)) {
            return "Low";
        }
        if (d < (double)NoiseRouterData.peaksAndValleys(0.4f)) {
            return "Mid";
        }
        if (d < (double)NoiseRouterData.peaksAndValleys(0.56666666f)) {
            return "High";
        }
        return "Peak";
    }

    public String getDebugStringForContinentalness(double d) {
        double d2 = Climate.quantizeCoord((float)d);
        if (d2 < (double)this.mushroomFieldsContinentalness.max()) {
            return "Mushroom fields";
        }
        if (d2 < (double)this.deepOceanContinentalness.max()) {
            return "Deep ocean";
        }
        if (d2 < (double)this.oceanContinentalness.max()) {
            return "Ocean";
        }
        if (d2 < (double)this.coastContinentalness.max()) {
            return "Coast";
        }
        if (d2 < (double)this.nearInlandContinentalness.max()) {
            return "Near inland";
        }
        if (d2 < (double)this.midInlandContinentalness.max()) {
            return "Mid inland";
        }
        return "Far inland";
    }

    public String getDebugStringForErosion(double d) {
        return OverworldBiomeBuilder.getDebugStringForNoiseValue(d, this.erosions);
    }

    public String getDebugStringForTemperature(double d) {
        return OverworldBiomeBuilder.getDebugStringForNoiseValue(d, this.temperatures);
    }

    public String getDebugStringForHumidity(double d) {
        return OverworldBiomeBuilder.getDebugStringForNoiseValue(d, this.humidities);
    }

    private static String getDebugStringForNoiseValue(double d, Climate.Parameter[] parameterArray) {
        double d2 = Climate.quantizeCoord((float)d);
        for (int i = 0; i < parameterArray.length; ++i) {
            if (!(d2 < (double)parameterArray[i].max())) continue;
            return "" + i;
        }
        return "?";
    }

    @VisibleForDebug
    public Climate.Parameter[] getTemperatureThresholds() {
        return this.temperatures;
    }

    @VisibleForDebug
    public Climate.Parameter[] getHumidityThresholds() {
        return this.humidities;
    }

    @VisibleForDebug
    public Climate.Parameter[] getErosionThresholds() {
        return this.erosions;
    }

    @VisibleForDebug
    public Climate.Parameter[] getContinentalnessThresholds() {
        return new Climate.Parameter[]{this.mushroomFieldsContinentalness, this.deepOceanContinentalness, this.oceanContinentalness, this.coastContinentalness, this.nearInlandContinentalness, this.midInlandContinentalness, this.farInlandContinentalness};
    }

    @VisibleForDebug
    public Climate.Parameter[] getPeaksAndValleysThresholds() {
        return new Climate.Parameter[]{Climate.Parameter.span(-2.0f, NoiseRouterData.peaksAndValleys(0.05f)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.05f), NoiseRouterData.peaksAndValleys(0.26666668f)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.26666668f), NoiseRouterData.peaksAndValleys(0.4f)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.4f), NoiseRouterData.peaksAndValleys(0.56666666f)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.56666666f), 2.0f)};
    }

    @VisibleForDebug
    public Climate.Parameter[] getWeirdnessThresholds() {
        return new Climate.Parameter[]{Climate.Parameter.span(-2.0f, 0.0f), Climate.Parameter.span(0.0f, 2.0f)};
    }
}

