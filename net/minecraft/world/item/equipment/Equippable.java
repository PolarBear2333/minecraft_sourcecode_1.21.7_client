/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.equipment;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public record Equippable(EquipmentSlot slot, Holder<SoundEvent> equipSound, Optional<ResourceKey<EquipmentAsset>> assetId, Optional<ResourceLocation> cameraOverlay, Optional<HolderSet<EntityType<?>>> allowedEntities, boolean dispensable, boolean swappable, boolean damageOnHurt, boolean equipOnInteract, boolean canBeSheared, Holder<SoundEvent> shearingSound) {
    public static final Codec<Equippable> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EquipmentSlot.CODEC.fieldOf("slot").forGetter(Equippable::slot), (App)SoundEvent.CODEC.optionalFieldOf("equip_sound", SoundEvents.ARMOR_EQUIP_GENERIC).forGetter(Equippable::equipSound), (App)ResourceKey.codec(EquipmentAssets.ROOT_ID).optionalFieldOf("asset_id").forGetter(Equippable::assetId), (App)ResourceLocation.CODEC.optionalFieldOf("camera_overlay").forGetter(Equippable::cameraOverlay), (App)RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities").forGetter(Equippable::allowedEntities), (App)Codec.BOOL.optionalFieldOf("dispensable", (Object)true).forGetter(Equippable::dispensable), (App)Codec.BOOL.optionalFieldOf("swappable", (Object)true).forGetter(Equippable::swappable), (App)Codec.BOOL.optionalFieldOf("damage_on_hurt", (Object)true).forGetter(Equippable::damageOnHurt), (App)Codec.BOOL.optionalFieldOf("equip_on_interact", (Object)false).forGetter(Equippable::equipOnInteract), (App)Codec.BOOL.optionalFieldOf("can_be_sheared", (Object)false).forGetter(Equippable::canBeSheared), (App)SoundEvent.CODEC.optionalFieldOf("shearing_sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP)).forGetter(Equippable::shearingSound)).apply((Applicative)instance, Equippable::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Equippable> STREAM_CODEC = StreamCodec.composite(EquipmentSlot.STREAM_CODEC, Equippable::slot, SoundEvent.STREAM_CODEC, Equippable::equipSound, ResourceKey.streamCodec(EquipmentAssets.ROOT_ID).apply(ByteBufCodecs::optional), Equippable::assetId, ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), Equippable::cameraOverlay, ByteBufCodecs.holderSet(Registries.ENTITY_TYPE).apply(ByteBufCodecs::optional), Equippable::allowedEntities, ByteBufCodecs.BOOL, Equippable::dispensable, ByteBufCodecs.BOOL, Equippable::swappable, ByteBufCodecs.BOOL, Equippable::damageOnHurt, ByteBufCodecs.BOOL, Equippable::equipOnInteract, ByteBufCodecs.BOOL, Equippable::canBeSheared, SoundEvent.STREAM_CODEC, Equippable::shearingSound, Equippable::new);

    public static Equippable llamaSwag(DyeColor dyeColor) {
        return Equippable.builder(EquipmentSlot.BODY).setEquipSound(SoundEvents.LLAMA_SWAG).setAsset(EquipmentAssets.CARPETS.get(dyeColor)).setAllowedEntities(EntityType.LLAMA, EntityType.TRADER_LLAMA).setCanBeSheared(true).setShearingSound(SoundEvents.LLAMA_CARPET_UNEQUIP).build();
    }

    public static Equippable saddle() {
        HolderGetter<EntityType<?>> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
        return Equippable.builder(EquipmentSlot.SADDLE).setEquipSound(SoundEvents.HORSE_SADDLE).setAsset(EquipmentAssets.SADDLE).setAllowedEntities(holderGetter.getOrThrow(EntityTypeTags.CAN_EQUIP_SADDLE)).setEquipOnInteract(true).setCanBeSheared(true).setShearingSound(SoundEvents.SADDLE_UNEQUIP).build();
    }

    public static Equippable harness(DyeColor dyeColor) {
        HolderGetter<EntityType<?>> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ENTITY_TYPE);
        return Equippable.builder(EquipmentSlot.BODY).setEquipSound(SoundEvents.HARNESS_EQUIP).setAsset(EquipmentAssets.HARNESSES.get(dyeColor)).setAllowedEntities(holderGetter.getOrThrow(EntityTypeTags.CAN_EQUIP_HARNESS)).setEquipOnInteract(true).setCanBeSheared(true).setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.HARNESS_UNEQUIP)).build();
    }

    public static Builder builder(EquipmentSlot equipmentSlot) {
        return new Builder(equipmentSlot);
    }

    public InteractionResult swapWithEquipmentSlot(ItemStack itemStack, Player player) {
        if (!player.canUseSlot(this.slot) || !this.canBeEquippedBy(player.getType())) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack2 = player.getItemBySlot(this.slot);
        if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !player.isCreative() || ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
            return InteractionResult.FAIL;
        }
        if (!player.level().isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        if (itemStack.getCount() <= 1) {
            ItemStack itemStack3 = itemStack2.isEmpty() ? itemStack : itemStack2.copyAndClear();
            ItemStack itemStack4 = player.isCreative() ? itemStack.copy() : itemStack.copyAndClear();
            player.setItemSlot(this.slot, itemStack4);
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack3);
        }
        ItemStack itemStack5 = itemStack2.copyAndClear();
        ItemStack itemStack6 = itemStack.consumeAndReturn(1, player);
        player.setItemSlot(this.slot, itemStack6);
        if (!player.getInventory().add(itemStack5)) {
            player.drop(itemStack5, false);
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(itemStack);
    }

    public InteractionResult equipOnTarget(Player player, LivingEntity livingEntity, ItemStack itemStack) {
        if (!livingEntity.isEquippableInSlot(itemStack, this.slot) || livingEntity.hasItemInSlot(this.slot) || !livingEntity.isAlive()) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            livingEntity.setItemSlot(this.slot, itemStack.split(1));
            if (livingEntity instanceof Mob) {
                Mob mob = (Mob)livingEntity;
                mob.setGuaranteedDrop(this.slot);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public boolean canBeEquippedBy(EntityType<?> entityType) {
        return this.allowedEntities.isEmpty() || this.allowedEntities.get().contains(entityType.builtInRegistryHolder());
    }

    public static class Builder {
        private final EquipmentSlot slot;
        private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
        private Optional<ResourceKey<EquipmentAsset>> assetId = Optional.empty();
        private Optional<ResourceLocation> cameraOverlay = Optional.empty();
        private Optional<HolderSet<EntityType<?>>> allowedEntities = Optional.empty();
        private boolean dispensable = true;
        private boolean swappable = true;
        private boolean damageOnHurt = true;
        private boolean equipOnInteract;
        private boolean canBeSheared;
        private Holder<SoundEvent> shearingSound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP);

        Builder(EquipmentSlot equipmentSlot) {
            this.slot = equipmentSlot;
        }

        public Builder setEquipSound(Holder<SoundEvent> holder) {
            this.equipSound = holder;
            return this;
        }

        public Builder setAsset(ResourceKey<EquipmentAsset> resourceKey) {
            this.assetId = Optional.of(resourceKey);
            return this;
        }

        public Builder setCameraOverlay(ResourceLocation resourceLocation) {
            this.cameraOverlay = Optional.of(resourceLocation);
            return this;
        }

        public Builder setAllowedEntities(EntityType<?> ... entityTypeArray) {
            return this.setAllowedEntities(HolderSet.direct(EntityType::builtInRegistryHolder, entityTypeArray));
        }

        public Builder setAllowedEntities(HolderSet<EntityType<?>> holderSet) {
            this.allowedEntities = Optional.of(holderSet);
            return this;
        }

        public Builder setDispensable(boolean bl) {
            this.dispensable = bl;
            return this;
        }

        public Builder setSwappable(boolean bl) {
            this.swappable = bl;
            return this;
        }

        public Builder setDamageOnHurt(boolean bl) {
            this.damageOnHurt = bl;
            return this;
        }

        public Builder setEquipOnInteract(boolean bl) {
            this.equipOnInteract = bl;
            return this;
        }

        public Builder setCanBeSheared(boolean bl) {
            this.canBeSheared = bl;
            return this;
        }

        public Builder setShearingSound(Holder<SoundEvent> holder) {
            this.shearingSound = holder;
            return this;
        }

        public Equippable build() {
            return new Equippable(this.slot, this.equipSound, this.assetId, this.cameraOverlay, this.allowedEntities, this.dispensable, this.swappable, this.damageOnHurt, this.equipOnInteract, this.canBeSheared, this.shearingSound);
        }
    }
}

