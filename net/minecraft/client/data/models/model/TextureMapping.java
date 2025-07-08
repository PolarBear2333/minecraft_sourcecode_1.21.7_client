/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 */
package net.minecraft.client.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TextureMapping {
    private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
    private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

    public TextureMapping put(TextureSlot textureSlot, ResourceLocation resourceLocation) {
        this.slots.put(textureSlot, resourceLocation);
        return this;
    }

    public TextureMapping putForced(TextureSlot textureSlot, ResourceLocation resourceLocation) {
        this.slots.put(textureSlot, resourceLocation);
        this.forcedSlots.add(textureSlot);
        return this;
    }

    public Stream<TextureSlot> getForced() {
        return this.forcedSlots.stream();
    }

    public TextureMapping copySlot(TextureSlot textureSlot, TextureSlot textureSlot2) {
        this.slots.put(textureSlot2, this.slots.get(textureSlot));
        return this;
    }

    public TextureMapping copyForced(TextureSlot textureSlot, TextureSlot textureSlot2) {
        this.slots.put(textureSlot2, this.slots.get(textureSlot));
        this.forcedSlots.add(textureSlot2);
        return this;
    }

    public ResourceLocation get(TextureSlot textureSlot) {
        for (TextureSlot textureSlot2 = textureSlot; textureSlot2 != null; textureSlot2 = textureSlot2.getParent()) {
            ResourceLocation resourceLocation = this.slots.get(textureSlot2);
            if (resourceLocation == null) continue;
            return resourceLocation;
        }
        throw new IllegalStateException("Can't find texture for slot " + String.valueOf(textureSlot));
    }

    public TextureMapping copyAndUpdate(TextureSlot textureSlot, ResourceLocation resourceLocation) {
        TextureMapping textureMapping = new TextureMapping();
        textureMapping.slots.putAll(this.slots);
        textureMapping.forcedSlots.addAll(this.forcedSlots);
        textureMapping.put(textureSlot, resourceLocation);
        return textureMapping;
    }

    public static TextureMapping cube(Block block) {
        ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block);
        return TextureMapping.cube(resourceLocation);
    }

    public static TextureMapping defaultTexture(Block block) {
        ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block);
        return TextureMapping.defaultTexture(resourceLocation);
    }

    public static TextureMapping defaultTexture(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.TEXTURE, resourceLocation);
    }

    public static TextureMapping cube(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.ALL, resourceLocation);
    }

    public static TextureMapping cross(Block block) {
        return TextureMapping.singleSlot(TextureSlot.CROSS, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping side(Block block) {
        return TextureMapping.singleSlot(TextureSlot.SIDE, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping crossEmissive(Block block) {
        return new TextureMapping().put(TextureSlot.CROSS, TextureMapping.getBlockTexture(block)).put(TextureSlot.CROSS_EMISSIVE, TextureMapping.getBlockTexture(block, "_emissive"));
    }

    public static TextureMapping cross(ResourceLocation resourceLocation) {
        return TextureMapping.singleSlot(TextureSlot.CROSS, resourceLocation);
    }

    public static TextureMapping plant(Block block) {
        return TextureMapping.singleSlot(TextureSlot.PLANT, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping plantEmissive(Block block) {
        return new TextureMapping().put(TextureSlot.PLANT, TextureMapping.getBlockTexture(block)).put(TextureSlot.CROSS_EMISSIVE, TextureMapping.getBlockTexture(block, "_emissive"));
    }

    public static TextureMapping plant(ResourceLocation resourceLocation) {
        return TextureMapping.singleSlot(TextureSlot.PLANT, resourceLocation);
    }

    public static TextureMapping rail(Block block) {
        return TextureMapping.singleSlot(TextureSlot.RAIL, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping rail(ResourceLocation resourceLocation) {
        return TextureMapping.singleSlot(TextureSlot.RAIL, resourceLocation);
    }

    public static TextureMapping wool(Block block) {
        return TextureMapping.singleSlot(TextureSlot.WOOL, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping flowerbed(Block block) {
        return new TextureMapping().put(TextureSlot.FLOWERBED, TextureMapping.getBlockTexture(block)).put(TextureSlot.STEM, TextureMapping.getBlockTexture(block, "_stem"));
    }

    public static TextureMapping wool(ResourceLocation resourceLocation) {
        return TextureMapping.singleSlot(TextureSlot.WOOL, resourceLocation);
    }

    public static TextureMapping stem(Block block) {
        return TextureMapping.singleSlot(TextureSlot.STEM, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping attachedStem(Block block, Block block2) {
        return new TextureMapping().put(TextureSlot.STEM, TextureMapping.getBlockTexture(block)).put(TextureSlot.UPPER_STEM, TextureMapping.getBlockTexture(block2));
    }

    public static TextureMapping pattern(Block block) {
        return TextureMapping.singleSlot(TextureSlot.PATTERN, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping fan(Block block) {
        return TextureMapping.singleSlot(TextureSlot.FAN, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping crop(ResourceLocation resourceLocation) {
        return TextureMapping.singleSlot(TextureSlot.CROP, resourceLocation);
    }

    public static TextureMapping pane(Block block, Block block2) {
        return new TextureMapping().put(TextureSlot.PANE, TextureMapping.getBlockTexture(block)).put(TextureSlot.EDGE, TextureMapping.getBlockTexture(block2, "_top"));
    }

    public static TextureMapping singleSlot(TextureSlot textureSlot, ResourceLocation resourceLocation) {
        return new TextureMapping().put(textureSlot, resourceLocation);
    }

    public static TextureMapping column(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping cubeTop(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping pottedAzalea(Block block) {
        return new TextureMapping().put(TextureSlot.PLANT, TextureMapping.getBlockTexture(block, "_plant")).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping logColumn(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block)).put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping column(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return new TextureMapping().put(TextureSlot.SIDE, resourceLocation).put(TextureSlot.END, resourceLocation2);
    }

    public static TextureMapping fence(Block block) {
        return new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block)).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping customParticle(Block block) {
        return new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block)).put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_particle"));
    }

    public static TextureMapping cubeBottomTop(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping cubeBottomTopWithWall(Block block) {
        ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block);
        return new TextureMapping().put(TextureSlot.WALL, resourceLocation).put(TextureSlot.SIDE, resourceLocation).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping columnWithWall(Block block) {
        ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block);
        return new TextureMapping().put(TextureSlot.TEXTURE, resourceLocation).put(TextureSlot.WALL, resourceLocation).put(TextureSlot.SIDE, resourceLocation).put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping door(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return new TextureMapping().put(TextureSlot.TOP, resourceLocation).put(TextureSlot.BOTTOM, resourceLocation2);
    }

    public static TextureMapping door(Block block) {
        return new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping particle(Block block) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping particle(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.PARTICLE, resourceLocation);
    }

    public static TextureMapping fire0(Block block) {
        return new TextureMapping().put(TextureSlot.FIRE, TextureMapping.getBlockTexture(block, "_0"));
    }

    public static TextureMapping fire1(Block block) {
        return new TextureMapping().put(TextureSlot.FIRE, TextureMapping.getBlockTexture(block, "_1"));
    }

    public static TextureMapping lantern(Block block) {
        return new TextureMapping().put(TextureSlot.LANTERN, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping torch(Block block) {
        return new TextureMapping().put(TextureSlot.TORCH, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping torch(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.TORCH, resourceLocation);
    }

    public static TextureMapping trialSpawner(Block block, String string, String string2) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, string)).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, string2)).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping vault(Block block, String string, String string2, String string3, String string4) {
        return new TextureMapping().put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, string)).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, string2)).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, string3)).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, string4));
    }

    public static TextureMapping particleFromItem(Item item) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getItemTexture(item));
    }

    public static TextureMapping commandBlock(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.BACK, TextureMapping.getBlockTexture(block, "_back"));
    }

    public static TextureMapping orientableCube(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping orientableCubeOnlyTop(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping orientableCubeSameEnds(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_end"));
    }

    public static TextureMapping top(Block block) {
        return new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"));
    }

    public static TextureMapping craftingTable(Block block, Block block2) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block2)).put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_front"));
    }

    public static TextureMapping fletchingTable(Block block, Block block2) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block2)).put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top")).put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_front")).put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side")).put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
    }

    public static TextureMapping snifferEgg(String string) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_north")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_bottom")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_top")).put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_north")).put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_south")).put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_east")).put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SNIFFER_EGG, string + "_west"));
    }

    public static TextureMapping driedGhast(String string) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_north")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_bottom")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_top")).put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_north")).put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_south")).put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_east")).put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_west")).put(TextureSlot.TENTACLES, TextureMapping.getBlockTexture(Blocks.DRIED_GHAST, string + "_tentacles"));
    }

    public static TextureMapping campfire(Block block) {
        return new TextureMapping().put(TextureSlot.LIT_LOG, TextureMapping.getBlockTexture(block, "_log_lit")).put(TextureSlot.FIRE, TextureMapping.getBlockTexture(block, "_fire"));
    }

    public static TextureMapping candleCake(Block block, boolean bl) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.CAKE, "_bottom")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.CAKE, "_top")).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.CANDLE, TextureMapping.getBlockTexture(block, bl ? "_lit" : ""));
    }

    public static TextureMapping cauldron(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.CAULDRON, "_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.CAULDRON, "_bottom")).put(TextureSlot.INSIDE, TextureMapping.getBlockTexture(Blocks.CAULDRON, "_inner")).put(TextureSlot.CONTENT, resourceLocation);
    }

    public static TextureMapping sculkShrieker(boolean bl) {
        String string = bl ? "_can_summon" : "";
        return new TextureMapping().put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom")).put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_SHRIEKER, "_side")).put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_SHRIEKER, "_top")).put(TextureSlot.INNER_TOP, TextureMapping.getBlockTexture(Blocks.SCULK_SHRIEKER, string + "_inner_top")).put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
    }

    public static TextureMapping layer0(Item item) {
        return new TextureMapping().put(TextureSlot.LAYER0, TextureMapping.getItemTexture(item));
    }

    public static TextureMapping layer0(Block block) {
        return new TextureMapping().put(TextureSlot.LAYER0, TextureMapping.getBlockTexture(block));
    }

    public static TextureMapping layer0(ResourceLocation resourceLocation) {
        return new TextureMapping().put(TextureSlot.LAYER0, resourceLocation);
    }

    public static TextureMapping layered(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return new TextureMapping().put(TextureSlot.LAYER0, resourceLocation).put(TextureSlot.LAYER1, resourceLocation2);
    }

    public static TextureMapping layered(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
        return new TextureMapping().put(TextureSlot.LAYER0, resourceLocation).put(TextureSlot.LAYER1, resourceLocation2).put(TextureSlot.LAYER2, resourceLocation3);
    }

    public static ResourceLocation getBlockTexture(Block block) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return resourceLocation.withPrefix("block/");
    }

    public static ResourceLocation getBlockTexture(Block block, String string) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return resourceLocation.withPath(string2 -> "block/" + string2 + string);
    }

    public static ResourceLocation getItemTexture(Item item) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(item);
        return resourceLocation.withPrefix("item/");
    }

    public static ResourceLocation getItemTexture(Item item, String string) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(item);
        return resourceLocation.withPath(string2 -> "item/" + string2 + string);
    }
}

