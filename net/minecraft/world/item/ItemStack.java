/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.slf4j.Logger
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.NullOps;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack
implements DataComponentHolder {
    private static final List<Component> OP_NBT_WARNING = List.of(Component.translatable("item.op_warning.line1").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), Component.translatable("item.op_warning.line2").withStyle(ChatFormatting.RED), Component.translatable("item.op_warning.line3").withStyle(ChatFormatting.RED));
    private static final Component UNBREAKABLE_TOOLTIP = Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE);
    public static final MapCodec<ItemStack> MAP_CODEC = MapCodec.recursive((String)"ItemStack", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group((App)Item.CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), (App)ExtraCodecs.intRange(1, 99).fieldOf("count").orElse((Object)1).forGetter(ItemStack::getCount), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(itemStack -> itemStack.components.asPatch())).apply((Applicative)instance, ItemStack::new)));
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> MAP_CODEC.codec());
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group((App)Item.CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(itemStack -> itemStack.components.asPatch())).apply((Applicative)instance, (holder, dataComponentPatch) -> new ItemStack((Holder<Item>)holder, 1, (DataComponentPatch)dataComponentPatch))));
    public static final Codec<ItemStack> STRICT_CODEC = CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> STRICT_SINGLE_ITEM_CODEC = SINGLE_ITEM_CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(optional -> optional.orElse(EMPTY), itemStack -> itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));
    public static final Codec<ItemStack> SIMPLE_ITEM_CODEC = Item.CODEC.xmap(ItemStack::new, ItemStack::getItemHolder);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = ItemStack.createOptionalStreamCodec(DataComponentPatch.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_UNTRUSTED_STREAM_CODEC = ItemStack.createOptionalStreamCodec(DataComponentPatch.DELIMITED_STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>(){

        @Override
        public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            ItemStack itemStack = (ItemStack)OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
            if (itemStack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            }
            return itemStack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            }
            OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, itemStack);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryFriendlyByteBuf)((Object)object), (ItemStack)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryFriendlyByteBuf)((Object)object));
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
    private int count;
    private int popTime;
    @Deprecated
    @Nullable
    private final Item item;
    final PatchedDataComponentMap components;
    @Nullable
    private Entity entityRepresentation;

    public static DataResult<ItemStack> validateStrict(ItemStack itemStack) {
        DataResult<Unit> dataResult = ItemStack.validateComponents(itemStack.getComponents());
        if (dataResult.isError()) {
            return dataResult.map(unit -> itemStack);
        }
        if (itemStack.getCount() > itemStack.getMaxStackSize()) {
            return DataResult.error(() -> "Item stack with stack size of " + itemStack.getCount() + " was larger than maximum: " + itemStack.getMaxStackSize());
        }
        return DataResult.success((Object)itemStack);
    }

    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> createOptionalStreamCodec(final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> streamCodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>(){

            @Override
            public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                int n = registryFriendlyByteBuf.readVarInt();
                if (n <= 0) {
                    return EMPTY;
                }
                Holder holder = (Holder)Item.STREAM_CODEC.decode(registryFriendlyByteBuf);
                DataComponentPatch dataComponentPatch = (DataComponentPatch)streamCodec.decode(registryFriendlyByteBuf);
                return new ItemStack(holder, n, dataComponentPatch);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
                if (itemStack.isEmpty()) {
                    registryFriendlyByteBuf.writeVarInt(0);
                    return;
                }
                registryFriendlyByteBuf.writeVarInt(itemStack.getCount());
                Item.STREAM_CODEC.encode(registryFriendlyByteBuf, itemStack.getItemHolder());
                streamCodec.encode(registryFriendlyByteBuf, itemStack.components.asPatch());
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (ItemStack)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> validatedStreamCodec(final StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamCodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>(){

            @Override
            public ItemStack decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                ItemStack itemStack = (ItemStack)streamCodec.decode(registryFriendlyByteBuf);
                if (!itemStack.isEmpty()) {
                    RegistryOps<Unit> registryOps = registryFriendlyByteBuf.registryAccess().createSerializationContext(NullOps.INSTANCE);
                    CODEC.encodeStart(registryOps, (Object)itemStack).getOrThrow(DecoderException::new);
                }
                return itemStack;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack) {
                streamCodec.encode(registryFriendlyByteBuf, itemStack);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (ItemStack)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    @Override
    public DataComponentMap getComponents() {
        return !this.isEmpty() ? this.components : DataComponentMap.EMPTY;
    }

    public DataComponentMap getPrototype() {
        return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public DataComponentMap immutableComponents() {
        return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
    }

    public boolean hasNonDefault(DataComponentType<?> dataComponentType) {
        return !this.isEmpty() && this.components.hasNonDefault(dataComponentType);
    }

    public ItemStack(ItemLike itemLike) {
        this(itemLike, 1);
    }

    public ItemStack(Holder<Item> holder) {
        this(holder.value(), 1);
    }

    public ItemStack(Holder<Item> holder, int n, DataComponentPatch dataComponentPatch) {
        this(holder.value(), n, PatchedDataComponentMap.fromPatch(holder.value().components(), dataComponentPatch));
    }

    public ItemStack(Holder<Item> holder, int n) {
        this(holder.value(), n);
    }

    public ItemStack(ItemLike itemLike, int n) {
        this(itemLike, n, new PatchedDataComponentMap(itemLike.asItem().components()));
    }

    private ItemStack(ItemLike itemLike, int n, PatchedDataComponentMap patchedDataComponentMap) {
        this.item = itemLike.asItem();
        this.count = n;
        this.components = patchedDataComponentMap;
        this.getItem().verifyComponentsAfterLoad(this);
    }

    private ItemStack(@Nullable Void void_) {
        this.item = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(DataComponentMap dataComponentMap) {
        if (dataComponentMap.has(DataComponents.MAX_DAMAGE) && dataComponentMap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> "Item cannot be both damageable and stackable");
        }
        ItemContainerContents itemContainerContents = dataComponentMap.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        for (ItemStack itemStack : itemContainerContents.nonEmptyItems()) {
            int n;
            int n2 = itemStack.getCount();
            if (n2 <= (n = itemStack.getMaxStackSize())) continue;
            return DataResult.error(() -> "Item stack with count of " + n2 + " was larger than maximum: " + n);
        }
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureFlagSet featureFlagSet) {
        return this.isEmpty() || this.getItem().isEnabled(featureFlagSet);
    }

    public ItemStack split(int n) {
        int n2 = Math.min(n, this.getCount());
        ItemStack itemStack = this.copyWithCount(n2);
        this.shrink(n2);
        return itemStack;
    }

    public ItemStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = this.copy();
        this.setCount(0);
        return itemStack;
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public Holder<Item> getItemHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    public boolean is(TagKey<Item> tagKey) {
        return this.getItem().builtInRegistryHolder().is(tagKey);
    }

    public boolean is(Item item) {
        return this.getItem() == item;
    }

    public boolean is(Predicate<Holder<Item>> predicate) {
        return predicate.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> holder) {
        return this.getItem().builtInRegistryHolder() == holder;
    }

    public boolean is(HolderSet<Item> holderSet) {
        return holderSet.contains(this.getItemHolder());
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        InteractionResult.Success success;
        Player player = useOnContext.getPlayer();
        BlockPos blockPos = useOnContext.getClickedPos();
        if (player != null && !player.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld(useOnContext.getLevel(), blockPos, false))) {
            return InteractionResult.PASS;
        }
        Item item = this.getItem();
        InteractionResult interactionResult = item.useOn(useOnContext);
        if (player != null && interactionResult instanceof InteractionResult.Success && (success = (InteractionResult.Success)interactionResult).wasItemInteraction()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return interactionResult;
    }

    public float getDestroySpeed(BlockState blockState) {
        return this.getItem().getDestroySpeed(this, blockState);
    }

    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = this.copy();
        boolean bl = this.getUseDuration(player) <= 0;
        InteractionResult interactionResult = this.getItem().use(level, player, interactionHand);
        if (bl && interactionResult instanceof InteractionResult.Success) {
            InteractionResult.Success success;
            return success.heldItemTransformedTo((success = (InteractionResult.Success)interactionResult).heldItemTransformedTo() == null ? this.applyAfterUseComponentSideEffects(player, itemStack) : success.heldItemTransformedTo().applyAfterUseComponentSideEffects(player, itemStack));
        }
        return interactionResult;
    }

    public ItemStack finishUsingItem(Level level, LivingEntity livingEntity) {
        ItemStack itemStack = this.copy();
        ItemStack itemStack2 = this.getItem().finishUsingItem(this, level, livingEntity);
        return itemStack2.applyAfterUseComponentSideEffects(livingEntity, itemStack);
    }

    private ItemStack applyAfterUseComponentSideEffects(LivingEntity livingEntity, ItemStack itemStack) {
        UseRemainder useRemainder = itemStack.get(DataComponents.USE_REMAINDER);
        UseCooldown useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
        int n = itemStack.getCount();
        ItemStack itemStack2 = this;
        if (useRemainder != null) {
            itemStack2 = useRemainder.convertIntoRemainder(itemStack2, n, livingEntity.hasInfiniteMaterials(), livingEntity::handleExtraItemsCreatedOnUse);
        }
        if (useCooldown != null) {
            useCooldown.apply(itemStack, livingEntity);
        }
        return itemStack2;
    }

    public int getMaxStackSize() {
        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return Mth.clamp(this.getOrDefault(DataComponents.DAMAGE, 0), 0, this.getMaxDamage());
    }

    public void setDamageValue(int n) {
        this.set(DataComponents.DAMAGE, Mth.clamp(n, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return this.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    public boolean isBroken() {
        return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage();
    }

    public boolean nextDamageWillBreak() {
        return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage() - 1;
    }

    public void hurtAndBreak(int n, ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer, Consumer<Item> consumer) {
        int n2 = this.processDurabilityChange(n, serverLevel, serverPlayer);
        if (n2 != 0) {
            this.applyDamage(this.getDamageValue() + n2, serverPlayer, consumer);
        }
    }

    private int processDurabilityChange(int n, ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer) {
        if (!this.isDamageableItem()) {
            return 0;
        }
        if (serverPlayer != null && serverPlayer.hasInfiniteMaterials()) {
            return 0;
        }
        if (n > 0) {
            return EnchantmentHelper.processDurabilityChange(serverLevel, this, n);
        }
        return n;
    }

    private void applyDamage(int n, @Nullable ServerPlayer serverPlayer, Consumer<Item> consumer) {
        if (serverPlayer != null) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, this, n);
        }
        this.setDamageValue(n);
        if (this.isBroken()) {
            Item item = this.getItem();
            this.shrink(1);
            consumer.accept(item);
        }
    }

    public void hurtWithoutBreaking(int n, Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            int n2 = this.processDurabilityChange(n, serverPlayer.level(), serverPlayer);
            if (n2 == 0) {
                return;
            }
            int n3 = Math.min(this.getDamageValue() + n2, this.getMaxDamage() - 1);
            this.applyDamage(n3, serverPlayer, item -> {});
        }
    }

    public void hurtAndBreak(int n, LivingEntity livingEntity, InteractionHand interactionHand) {
        this.hurtAndBreak(n, livingEntity, LivingEntity.getSlotForHand(interactionHand));
    }

    public void hurtAndBreak(int n, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        Object object = livingEntity.level();
        if (object instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            this.hurtAndBreak(n, serverLevel, (ServerPlayer)(livingEntity instanceof ServerPlayer ? (object = (ServerPlayer)livingEntity) : null), item -> livingEntity.onEquippedItemBroken((Item)item, equipmentSlot));
        }
    }

    public ItemStack hurtAndConvertOnBreak(int n, ItemLike itemLike, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        this.hurtAndBreak(n, livingEntity, equipmentSlot);
        if (this.isEmpty()) {
            ItemStack itemStack = this.transmuteCopyIgnoreEmpty(itemLike, 1);
            if (itemStack.isDamageableItem()) {
                itemStack.setDamageValue(0);
            }
            return itemStack;
        }
        return this;
    }

    public boolean isBarVisible() {
        return this.getItem().isBarVisible(this);
    }

    public int getBarWidth() {
        return this.getItem().getBarWidth(this);
    }

    public int getBarColor() {
        return this.getItem().getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot slot, ClickAction clickAction, Player player) {
        return this.getItem().overrideStackedOnOther(this, slot, clickAction, player);
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        return this.getItem().overrideOtherStackedOnMe(this, itemStack, slot, clickAction, player, slotAccess);
    }

    public boolean hurtEnemy(LivingEntity livingEntity, LivingEntity livingEntity2) {
        Item item = this.getItem();
        item.hurtEnemy(this, livingEntity, livingEntity2);
        if (this.has(DataComponents.WEAPON)) {
            if (livingEntity2 instanceof Player) {
                Player player = (Player)livingEntity2;
                player.awardStat(Stats.ITEM_USED.get(item));
            }
            return true;
        }
        return false;
    }

    public void postHurtEnemy(LivingEntity livingEntity, LivingEntity livingEntity2) {
        this.getItem().postHurtEnemy(this, livingEntity, livingEntity2);
        Weapon weapon = this.get(DataComponents.WEAPON);
        if (weapon != null) {
            this.hurtAndBreak(weapon.itemDamagePerAttack(), livingEntity2, EquipmentSlot.MAINHAND);
        }
    }

    public void mineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player) {
        Item item = this.getItem();
        if (item.mineBlock(this, level, blockState, blockPos, player)) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState blockState) {
        return this.getItem().isCorrectToolForDrops(this, blockState);
    }

    public InteractionResult interactLivingEntity(Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        InteractionResult interactionResult;
        Equippable equippable = this.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.equipOnInteract() && (interactionResult = equippable.equipOnTarget(player, livingEntity, this)) != InteractionResult.PASS) {
            return interactionResult;
        }
        return this.getItem().interactLivingEntity(this, player, livingEntity, interactionHand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = new ItemStack(this.getItem(), this.count, this.components.copy());
        itemStack.setPopTime(this.getPopTime());
        return itemStack;
    }

    public ItemStack copyWithCount(int n) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = this.copy();
        itemStack.setCount(n);
        return itemStack;
    }

    public ItemStack transmuteCopy(ItemLike itemLike) {
        return this.transmuteCopy(itemLike, this.getCount());
    }

    public ItemStack transmuteCopy(ItemLike itemLike, int n) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        return this.transmuteCopyIgnoreEmpty(itemLike, n);
    }

    private ItemStack transmuteCopyIgnoreEmpty(ItemLike itemLike, int n) {
        return new ItemStack(itemLike.asItem().builtInRegistryHolder(), n, this.components.asPatch());
    }

    public static boolean matches(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) {
            return true;
        }
        if (itemStack.getCount() != itemStack2.getCount()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(itemStack, itemStack2);
    }

    @Deprecated
    public static boolean listMatches(List<ItemStack> list, List<ItemStack> list2) {
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); ++i) {
            if (ItemStack.matches(list.get(i), list2.get(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.is(itemStack2.getItem());
    }

    public static boolean isSameItemSameComponents(ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack.is(itemStack2.getItem())) {
            return false;
        }
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        return Objects.equals(itemStack.components, itemStack2.components);
    }

    public static MapCodec<ItemStack> lenientOptionalFieldOf(String string) {
        return CODEC.lenientOptionalFieldOf(string).xmap(optional -> optional.orElse(EMPTY), itemStack -> itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));
    }

    public static int hashItemAndComponents(@Nullable ItemStack itemStack) {
        if (itemStack != null) {
            int n = 31 + itemStack.getItem().hashCode();
            return 31 * n + itemStack.getComponents().hashCode();
        }
        return 0;
    }

    @Deprecated
    public static int hashStackList(List<ItemStack> list) {
        int n = 0;
        for (ItemStack itemStack : list) {
            n = n * 31 + ItemStack.hashItemAndComponents(itemStack);
        }
        return n;
    }

    public String toString() {
        return this.getCount() + " " + String.valueOf(this.getItem());
    }

    public void inventoryTick(Level level, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
        if (this.popTime > 0) {
            --this.popTime;
        }
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.getItem().inventoryTick(this, serverLevel, entity, equipmentSlot);
        }
    }

    public void onCraftedBy(Player player, int n) {
        player.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), n);
        this.getItem().onCraftedBy(this, player);
    }

    public void onCraftedBySystem(Level level) {
        this.getItem().onCraftedPostProcess(this, level);
    }

    public int getUseDuration(LivingEntity livingEntity) {
        return this.getItem().getUseDuration(this, livingEntity);
    }

    public ItemUseAnimation getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level level, LivingEntity livingEntity, int n) {
        ItemStack itemStack;
        ItemStack itemStack2 = this.copy();
        if (this.getItem().releaseUsing(this, level, livingEntity, n) && (itemStack = this.applyAfterUseComponentSideEffects(livingEntity, itemStack2)) != this) {
            livingEntity.setItemInHand(livingEntity.getUsedItemHand(), itemStack);
        }
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    @Nullable
    public <T> T set(DataComponentType<T> dataComponentType, @Nullable T t) {
        return this.components.set(dataComponentType, t);
    }

    public <T> void copyFrom(DataComponentType<T> dataComponentType, DataComponentGetter dataComponentGetter) {
        this.set(dataComponentType, dataComponentGetter.get(dataComponentType));
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> dataComponentType, T t, U u, BiFunction<T, U, T> biFunction) {
        return this.set(dataComponentType, biFunction.apply(this.getOrDefault(dataComponentType, t), u));
    }

    @Nullable
    public <T> T update(DataComponentType<T> dataComponentType, T t, UnaryOperator<T> unaryOperator) {
        T t2 = this.getOrDefault(dataComponentType, t);
        return this.set(dataComponentType, unaryOperator.apply(t2));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> dataComponentType) {
        return this.components.remove(dataComponentType);
    }

    public void applyComponentsAndValidate(DataComponentPatch dataComponentPatch) {
        DataComponentPatch dataComponentPatch2 = this.components.asPatch();
        this.components.applyPatch(dataComponentPatch);
        Optional optional = ItemStack.validateStrict(this).error();
        if (optional.isPresent()) {
            LOGGER.error("Failed to apply component patch '{}' to item: '{}'", (Object)dataComponentPatch, (Object)((DataResult.Error)optional.get()).message());
            this.components.restorePatch(dataComponentPatch2);
            return;
        }
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public void applyComponents(DataComponentPatch dataComponentPatch) {
        this.components.applyPatch(dataComponentPatch);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public void applyComponents(DataComponentMap dataComponentMap) {
        this.components.setAll(dataComponentMap);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public Component getHoverName() {
        Component component = this.getCustomName();
        if (component != null) {
            return component;
        }
        return this.getItemName();
    }

    @Nullable
    public Component getCustomName() {
        String string;
        Component component = this.get(DataComponents.CUSTOM_NAME);
        if (component != null) {
            return component;
        }
        WrittenBookContent writtenBookContent = this.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenBookContent != null && !StringUtil.isBlank(string = writtenBookContent.title().raw())) {
            return Component.literal(string);
        }
        return null;
    }

    public Component getItemName() {
        return this.getItem().getName(this);
    }

    public Component getStyledHoverName() {
        MutableComponent mutableComponent = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color());
        if (this.has(DataComponents.CUSTOM_NAME)) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        return mutableComponent;
    }

    public <T extends TooltipProvider> void addToTooltip(DataComponentType<T> dataComponentType, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        TooltipProvider tooltipProvider = (TooltipProvider)this.get(dataComponentType);
        if (tooltipProvider != null && tooltipDisplay.shows(dataComponentType)) {
            tooltipProvider.addToTooltip(tooltipContext, consumer, tooltipFlag, this.components);
        }
    }

    public List<Component> getTooltipLines(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
        TooltipDisplay tooltipDisplay = this.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!tooltipFlag.isCreative() && tooltipDisplay.hideTooltip()) {
            boolean bl = this.getItem().shouldPrintOpWarning(this, player);
            return bl ? OP_NBT_WARNING : List.of();
        }
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(this.getStyledHoverName());
        this.addDetailsToTooltip(tooltipContext, tooltipDisplay, player, tooltipFlag, arrayList::add);
        return arrayList;
    }

    public void addDetailsToTooltip(Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> consumer) {
        int n;
        AdventureModePredicate adventureModePredicate;
        Object object;
        this.getItem().appendHoverText(this, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.TROPICAL_FISH_PATTERN, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.INSTRUMENT, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.MAP_ID, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.BEES, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.CONTAINER_LOOT, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.CONTAINER, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.BANNER_PATTERNS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.POT_DECORATIONS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.WRITTEN_BOOK_CONTENT, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.CHARGED_PROJECTILES, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.FIREWORKS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.FIREWORK_EXPLOSION, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.POTION_CONTENTS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.TRIM, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.ENCHANTMENTS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.DYED_COLOR, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.LORE, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addAttributeTooltips(consumer, tooltipDisplay, player);
        if (this.has(DataComponents.UNBREAKABLE) && tooltipDisplay.shows(DataComponents.UNBREAKABLE)) {
            consumer.accept(UNBREAKABLE_TOOLTIP);
        }
        this.addToTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.SUSPICIOUS_STEW_EFFECTS, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        this.addToTooltip(DataComponents.BLOCK_STATE, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        if ((this.is(Items.SPAWNER) || this.is(Items.TRIAL_SPAWNER)) && tooltipDisplay.shows(DataComponents.BLOCK_ENTITY_DATA)) {
            object = this.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            Spawner.appendHoverText((CustomData)object, consumer, "SpawnData");
        }
        if ((object = this.get(DataComponents.CAN_BREAK)) != null && tooltipDisplay.shows(DataComponents.CAN_BREAK)) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(AdventureModePredicate.CAN_BREAK_HEADER);
            ((AdventureModePredicate)object).addToTooltip(consumer);
        }
        if ((adventureModePredicate = this.get(DataComponents.CAN_PLACE_ON)) != null && tooltipDisplay.shows(DataComponents.CAN_PLACE_ON)) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(AdventureModePredicate.CAN_PLACE_HEADER);
            adventureModePredicate.addToTooltip(consumer);
        }
        if (tooltipFlag.isAdvanced()) {
            if (this.isDamaged() && tooltipDisplay.shows(DataComponents.DAMAGE)) {
                consumer.accept(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }
            consumer.accept(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            n = this.components.size();
            if (n > 0) {
                consumer.accept(Component.translatable("item.components", n).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (player != null && !this.getItem().isEnabled(player.level().enabledFeatures())) {
            consumer.accept(DISABLED_ITEM_TOOLTIP);
        }
        if ((n = this.getItem().shouldPrintOpWarning(this, player)) != 0) {
            OP_NBT_WARNING.forEach(consumer);
        }
    }

    private void addAttributeTooltips(Consumer<Component> consumer, TooltipDisplay tooltipDisplay, @Nullable Player player) {
        if (!tooltipDisplay.shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
            return;
        }
        for (EquipmentSlotGroup equipmentSlotGroup : EquipmentSlotGroup.values()) {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            this.forEachModifier(equipmentSlotGroup, (TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display>)((TriConsumer)(holder, attributeModifier, display) -> {
                if (display == ItemAttributeModifiers.Display.hidden()) {
                    return;
                }
                if (mutableBoolean.isTrue()) {
                    consumer.accept(CommonComponents.EMPTY);
                    consumer.accept(Component.translatable("item.modifiers." + equipmentSlotGroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
                    mutableBoolean.setFalse();
                }
                display.apply(consumer, player, (Holder<Attribute>)holder, (AttributeModifier)attributeModifier);
            }));
        }
    }

    public boolean hasFoil() {
        Boolean bl = this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        if (bl != null) {
            return bl;
        }
        return this.getItem().isFoil(this);
    }

    public Rarity getRarity() {
        Rarity rarity = this.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
        if (!this.isEnchanted()) {
            return rarity;
        }
        return switch (rarity) {
            case Rarity.COMMON, Rarity.UNCOMMON -> Rarity.RARE;
            case Rarity.RARE -> Rarity.EPIC;
            default -> rarity;
        };
    }

    public boolean isEnchantable() {
        if (!this.has(DataComponents.ENCHANTABLE)) {
            return false;
        }
        ItemEnchantments itemEnchantments = this.get(DataComponents.ENCHANTMENTS);
        return itemEnchantments != null && itemEnchantments.isEmpty();
    }

    public void enchant(Holder<Enchantment> holder, int n) {
        EnchantmentHelper.updateEnchantments(this, mutable -> mutable.upgrade(holder, n));
    }

    public boolean isEnchanted() {
        return !this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public ItemEnchantments getEnchantments() {
        return this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity entity) {
        if (!this.isEmpty()) {
            this.entityRepresentation = entity;
        }
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.isEmpty() ? this.entityRepresentation : null;
    }

    public void forEachModifier(EquipmentSlotGroup equipmentSlotGroup, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> triConsumer) {
        ItemAttributeModifiers itemAttributeModifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        itemAttributeModifiers.forEach(equipmentSlotGroup, triConsumer);
        EnchantmentHelper.forEachModifier(this, equipmentSlotGroup, (holder, attributeModifier) -> triConsumer.accept(holder, attributeModifier, (Object)ItemAttributeModifiers.Display.attributeModifiers()));
    }

    public void forEachModifier(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        ItemAttributeModifiers itemAttributeModifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        itemAttributeModifiers.forEach(equipmentSlot, biConsumer);
        EnchantmentHelper.forEachModifier(this, equipmentSlot, biConsumer);
    }

    public Component getDisplayName() {
        MutableComponent mutableComponent = Component.empty().append(this.getHoverName());
        if (this.has(DataComponents.CUSTOM_NAME)) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        MutableComponent mutableComponent2 = ComponentUtils.wrapInSquareBrackets(mutableComponent);
        if (!this.isEmpty()) {
            mutableComponent2.withStyle(this.getRarity().color()).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowItem(this)));
        }
        return mutableComponent2;
    }

    public boolean canPlaceOnBlockInAdventureMode(BlockInWorld blockInWorld) {
        AdventureModePredicate adventureModePredicate = this.get(DataComponents.CAN_PLACE_ON);
        return adventureModePredicate != null && adventureModePredicate.test(blockInWorld);
    }

    public boolean canBreakBlockInAdventureMode(BlockInWorld blockInWorld) {
        AdventureModePredicate adventureModePredicate = this.get(DataComponents.CAN_BREAK);
        return adventureModePredicate != null && adventureModePredicate.test(blockInWorld);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int n) {
        this.popTime = n;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int n) {
        this.count = n;
    }

    public void limitSize(int n) {
        if (!this.isEmpty() && this.getCount() > n) {
            this.setCount(n);
        }
    }

    public void grow(int n) {
        this.setCount(this.getCount() + n);
    }

    public void shrink(int n) {
        this.grow(-n);
    }

    public void consume(int n, @Nullable LivingEntity livingEntity) {
        if (livingEntity == null || !livingEntity.hasInfiniteMaterials()) {
            this.shrink(n);
        }
    }

    public ItemStack consumeAndReturn(int n, @Nullable LivingEntity livingEntity) {
        ItemStack itemStack = this.copyWithCount(n);
        this.consume(n, livingEntity);
        return itemStack;
    }

    public void onUseTick(Level level, LivingEntity livingEntity, int n) {
        Consumable consumable = this.get(DataComponents.CONSUMABLE);
        if (consumable != null && consumable.shouldEmitParticlesAndSounds(n)) {
            consumable.emitParticlesAndSounds(livingEntity.getRandom(), livingEntity, this, 5);
        }
        this.getItem().onUseTick(level, livingEntity, this, n);
    }

    public void onDestroyed(ItemEntity itemEntity) {
        this.getItem().onDestroyed(itemEntity);
    }

    public boolean canBeHurtBy(DamageSource damageSource) {
        DamageResistant damageResistant = this.get(DataComponents.DAMAGE_RESISTANT);
        return damageResistant == null || !damageResistant.isResistantTo(damageSource);
    }

    public boolean isValidRepairItem(ItemStack itemStack) {
        Repairable repairable = this.get(DataComponents.REPAIRABLE);
        return repairable != null && repairable.isValidRepairItem(itemStack);
    }

    public boolean canDestroyBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return this.getItem().canDestroyBlock(this, blockState, level, blockPos, player);
    }
}

