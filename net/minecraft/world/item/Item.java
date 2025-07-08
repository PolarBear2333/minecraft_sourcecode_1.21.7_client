/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item
implements FeatureElement,
ItemLike {
    public static final Codec<Holder<Item>> CODEC = BuiltInRegistries.ITEM.holderByNameCodec().validate(holder -> holder.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success((Object)holder));
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
    public static final int DEFAULT_MAX_STACK_SIZE = 64;
    public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
    public static final int MAX_BAR_WIDTH = 13;
    protected static final int APPROXIMATELY_INFINITE_USE_DURATION = 72000;
    private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
    private final DataComponentMap components;
    @Nullable
    private final Item craftingRemainingItem;
    protected final String descriptionId;
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }

    public static Item byId(int n) {
        return BuiltInRegistries.ITEM.byId(n);
    }

    @Deprecated
    public static Item byBlock(Block block) {
        return BY_BLOCK.getOrDefault(block, Items.AIR);
    }

    public Item(Properties properties) {
        String string;
        this.descriptionId = properties.effectiveDescriptionId();
        this.components = properties.buildAndValidateComponents(Component.translatable(this.descriptionId), properties.effectiveModel());
        this.craftingRemainingItem = properties.craftingRemainingItem;
        this.requiredFeatures = properties.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE && !(string = this.getClass().getSimpleName()).endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)string);
        }
    }

    @Deprecated
    public Holder.Reference<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public DataComponentMap components() {
        return this.components;
    }

    public int getDefaultMaxStackSize() {
        return this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int n) {
    }

    public void onDestroyed(ItemEntity itemEntity) {
    }

    public void verifyComponentsAfterLoad(ItemStack itemStack) {
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean canDestroyBlock(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, LivingEntity livingEntity) {
        Tool tool = itemStack.get(DataComponents.TOOL);
        if (tool == null) return true;
        if (tool.canDestroyBlocksInCreative()) return true;
        if (!(livingEntity instanceof Player)) return true;
        Player player = (Player)livingEntity;
        if (player.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        Tool tool = itemStack.get(DataComponents.TOOL);
        return tool != null ? tool.getMiningSpeed(blockState) : 1.0f;
    }

    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.startConsuming(player, itemStack, interactionHand);
        }
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.swappable()) {
            return equippable.swapWithEquipmentSlot(itemStack, player);
        }
        BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null) {
            player.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.onConsume(level, livingEntity, itemStack);
        }
        return itemStack;
    }

    public boolean isBarVisible(ItemStack itemStack) {
        return itemStack.isDamaged();
    }

    public int getBarWidth(ItemStack itemStack) {
        return Mth.clamp(Math.round(13.0f - (float)itemStack.getDamageValue() * 13.0f / (float)itemStack.getMaxDamage()), 0, 13);
    }

    public int getBarColor(ItemStack itemStack) {
        int n = itemStack.getMaxDamage();
        float f = Math.max(0.0f, ((float)n - (float)itemStack.getDamageValue()) / (float)n);
        return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
    }

    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        return false;
    }

    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        return 0.0f;
    }

    @Nullable
    public DamageSource getDamageSource(LivingEntity livingEntity) {
        return null;
    }

    public void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
    }

    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
    }

    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        Tool tool = itemStack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        }
        if (!level.isClientSide && blockState.getDestroySpeed(level, blockPos) != 0.0f && tool.damagePerBlock() > 0) {
            itemStack.hurtAndBreak(tool.damagePerBlock(), livingEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState blockState) {
        Tool tool = itemStack.get(DataComponents.TOOL);
        return tool != null && tool.isCorrectForDrops(blockState);
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    public String toString() {
        return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
    }

    public final ItemStack getCraftingRemainder() {
        return this.craftingRemainingItem == null ? ItemStack.EMPTY : new ItemStack(this.craftingRemainingItem);
    }

    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
    }

    public void onCraftedBy(ItemStack itemStack, Player player) {
        this.onCraftedPostProcess(itemStack, player.level());
    }

    public void onCraftedPostProcess(ItemStack itemStack, Level level) {
    }

    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.animation();
        }
        BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null) {
            return ItemUseAnimation.BLOCK;
        }
        return ItemUseAnimation.NONE;
    }

    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.consumeTicks();
        }
        BlocksAttacks blocksAttacks = itemStack.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks != null) {
            return 72000;
        }
        return 0;
    }

    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        return false;
    }

    @Deprecated
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return Optional.empty();
    }

    @VisibleForTesting
    public final String getDescriptionId() {
        return this.descriptionId;
    }

    public final Component getName() {
        return this.components.getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }

    public Component getName(ItemStack itemStack) {
        return itemStack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }

    public boolean isFoil(ItemStack itemStack) {
        return itemStack.isEnchanted();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        Vec3 vec3 = player.getEyePosition();
        Vec3 vec32 = vec3.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
        return level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluid, player));
    }

    public boolean useOnRelease(ItemStack itemStack) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public boolean shouldPrintOpWarning(ItemStack itemStack, @Nullable Player player) {
        return false;
    }

    public static class Properties {
        private static final DependantName<Item, String> BLOCK_DESCRIPTION_ID = resourceKey -> Util.makeDescriptionId("block", resourceKey.location());
        private static final DependantName<Item, String> ITEM_DESCRIPTION_ID = resourceKey -> Util.makeDescriptionId("item", resourceKey.location());
        private final DataComponentMap.Builder components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
        @Nullable
        Item craftingRemainingItem;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        @Nullable
        private ResourceKey<Item> id;
        private DependantName<Item, String> descriptionId = ITEM_DESCRIPTION_ID;
        private DependantName<Item, ResourceLocation> model = ResourceKey::location;

        public Properties food(FoodProperties foodProperties) {
            return this.food(foodProperties, Consumables.DEFAULT_FOOD);
        }

        public Properties food(FoodProperties foodProperties, Consumable consumable) {
            return this.component(DataComponents.FOOD, foodProperties).component(DataComponents.CONSUMABLE, consumable);
        }

        public Properties usingConvertsTo(Item item) {
            return this.component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStack(item)));
        }

        public Properties useCooldown(float f) {
            return this.component(DataComponents.USE_COOLDOWN, new UseCooldown(f));
        }

        public Properties stacksTo(int n) {
            return this.component(DataComponents.MAX_STACK_SIZE, n);
        }

        public Properties durability(int n) {
            this.component(DataComponents.MAX_DAMAGE, n);
            this.component(DataComponents.MAX_STACK_SIZE, 1);
            this.component(DataComponents.DAMAGE, 0);
            return this;
        }

        public Properties craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            return this.component(DataComponents.RARITY, rarity);
        }

        public Properties fireResistant() {
            return this.component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(DamageTypeTags.IS_FIRE));
        }

        public Properties jukeboxPlayable(ResourceKey<JukeboxSong> resourceKey) {
            return this.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<JukeboxSong>(resourceKey)));
        }

        public Properties enchantable(int n) {
            return this.component(DataComponents.ENCHANTABLE, new Enchantable(n));
        }

        public Properties repairable(Item item) {
            return this.component(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(item.builtInRegistryHolder())));
        }

        public Properties repairable(TagKey<Item> tagKey) {
            HolderGetter<Item> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM);
            return this.component(DataComponents.REPAIRABLE, new Repairable(holderGetter.getOrThrow(tagKey)));
        }

        public Properties equippable(EquipmentSlot equipmentSlot) {
            return this.component(DataComponents.EQUIPPABLE, Equippable.builder(equipmentSlot).build());
        }

        public Properties equippableUnswappable(EquipmentSlot equipmentSlot) {
            return this.component(DataComponents.EQUIPPABLE, Equippable.builder(equipmentSlot).setSwappable(false).build());
        }

        public Properties tool(ToolMaterial toolMaterial, TagKey<Block> tagKey, float f, float f2, float f3) {
            return toolMaterial.applyToolProperties(this, tagKey, f, f2, f3);
        }

        public Properties pickaxe(ToolMaterial toolMaterial, float f, float f2) {
            return this.tool(toolMaterial, BlockTags.MINEABLE_WITH_PICKAXE, f, f2, 0.0f);
        }

        public Properties axe(ToolMaterial toolMaterial, float f, float f2) {
            return this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, f2, 5.0f);
        }

        public Properties hoe(ToolMaterial toolMaterial, float f, float f2) {
            return this.tool(toolMaterial, BlockTags.MINEABLE_WITH_HOE, f, f2, 0.0f);
        }

        public Properties shovel(ToolMaterial toolMaterial, float f, float f2) {
            return this.tool(toolMaterial, BlockTags.MINEABLE_WITH_SHOVEL, f, f2, 0.0f);
        }

        public Properties sword(ToolMaterial toolMaterial, float f, float f2) {
            return toolMaterial.applySwordProperties(this, f, f2);
        }

        public Properties humanoidArmor(ArmorMaterial armorMaterial, ArmorType armorType) {
            return this.durability(armorType.getDurability(armorMaterial.durability())).attributes(armorMaterial.createAttributes(armorType)).enchantable(armorMaterial.enchantmentValue()).component(DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot()).setEquipSound(armorMaterial.equipSound()).setAsset(armorMaterial.assetId()).build()).repairable(armorMaterial.repairIngredient());
        }

        public Properties wolfArmor(ArmorMaterial armorMaterial) {
            return this.durability(ArmorType.BODY.getDurability(armorMaterial.durability())).attributes(armorMaterial.createAttributes(ArmorType.BODY)).repairable(armorMaterial.repairIngredient()).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY).setEquipSound(armorMaterial.equipSound()).setAsset(armorMaterial.assetId()).setAllowedEntities(HolderSet.direct(EntityType.WOLF.builtInRegistryHolder())).setCanBeSheared(true).setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARMOR_UNEQUIP_WOLF)).build()).component(DataComponents.BREAK_SOUND, SoundEvents.WOLF_ARMOR_BREAK).stacksTo(1);
        }

        public Properties horseArmor(ArmorMaterial armorMaterial) {
            HolderGetter<EntityType<?>> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
            return this.attributes(armorMaterial.createAttributes(ArmorType.BODY)).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY).setEquipSound(SoundEvents.HORSE_ARMOR).setAsset(armorMaterial.assetId()).setAllowedEntities(holderGetter.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR)).setDamageOnHurt(false).setCanBeSheared(true).setShearingSound(SoundEvents.HORSE_ARMOR_UNEQUIP).build()).stacksTo(1);
        }

        public Properties trimMaterial(ResourceKey<TrimMaterial> resourceKey) {
            return this.component(DataComponents.PROVIDES_TRIM_MATERIAL, new ProvidesTrimMaterial(resourceKey));
        }

        public Properties requiredFeatures(FeatureFlag ... featureFlagArray) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlagArray);
            return this;
        }

        public Properties setId(ResourceKey<Item> resourceKey) {
            this.id = resourceKey;
            return this;
        }

        public Properties overrideDescription(String string) {
            this.descriptionId = DependantName.fixed(string);
            return this;
        }

        public Properties useBlockDescriptionPrefix() {
            this.descriptionId = BLOCK_DESCRIPTION_ID;
            return this;
        }

        public Properties useItemDescriptionPrefix() {
            this.descriptionId = ITEM_DESCRIPTION_ID;
            return this;
        }

        protected String effectiveDescriptionId() {
            return this.descriptionId.get(Objects.requireNonNull(this.id, "Item id not set"));
        }

        public ResourceLocation effectiveModel() {
            return this.model.get(Objects.requireNonNull(this.id, "Item id not set"));
        }

        public <T> Properties component(DataComponentType<T> dataComponentType, T t) {
            this.components.set(dataComponentType, t);
            return this;
        }

        public Properties attributes(ItemAttributeModifiers itemAttributeModifiers) {
            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
        }

        DataComponentMap buildAndValidateComponents(Component component, ResourceLocation resourceLocation) {
            DataComponentMap dataComponentMap = this.components.set(DataComponents.ITEM_NAME, component).set(DataComponents.ITEM_MODEL, resourceLocation).build();
            if (dataComponentMap.has(DataComponents.DAMAGE) && dataComponentMap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            }
            return dataComponentMap;
        }
    }

    public static interface TooltipContext {
        public static final TooltipContext EMPTY = new TooltipContext(){

            @Override
            @Nullable
            public HolderLookup.Provider registries() {
                return null;
            }

            @Override
            public float tickRate() {
                return 20.0f;
            }

            @Override
            @Nullable
            public MapItemSavedData mapData(MapId mapId) {
                return null;
            }
        };

        @Nullable
        public HolderLookup.Provider registries();

        public float tickRate();

        @Nullable
        public MapItemSavedData mapData(MapId var1);

        public static TooltipContext of(final @Nullable Level level) {
            if (level == null) {
                return EMPTY;
            }
            return new TooltipContext(){

                @Override
                public HolderLookup.Provider registries() {
                    return level.registryAccess();
                }

                @Override
                public float tickRate() {
                    return level.tickRateManager().tickrate();
                }

                @Override
                public MapItemSavedData mapData(MapId mapId) {
                    return level.getMapData(mapId);
                }
            };
        }

        public static TooltipContext of(final HolderLookup.Provider provider) {
            return new TooltipContext(){

                @Override
                public HolderLookup.Provider registries() {
                    return provider;
                }

                @Override
                public float tickRate() {
                    return 20.0f;
                }

                @Override
                @Nullable
                public MapItemSavedData mapData(MapId mapId) {
                    return null;
                }
            };
        }
    }
}

