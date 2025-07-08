/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TridentItem
extends Item
implements ProjectileItem {
    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 8.0f;
    public static final float PROJECTILE_SHOOT_POWER = 2.5f;

    public TridentItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0f, 2, false);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        if (!(livingEntity instanceof Player)) {
            return false;
        }
        Player player = (Player)livingEntity;
        int n2 = this.getUseDuration(itemStack, livingEntity) - n;
        if (n2 < 10) {
            return false;
        }
        float f = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
        if (f > 0.0f && !player.isInWaterOrRain()) {
            return false;
        }
        if (itemStack.nextDamageWillBreak()) {
            return false;
        }
        Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            itemStack.hurtWithoutBreaking(1, player);
            if (f == 0.0f) {
                ItemStack itemStack2 = itemStack.consumeAndReturn(1, player);
                ThrownTrident thrownTrident = Projectile.spawnProjectileFromRotation(ThrownTrident::new, serverLevel, itemStack2, player, 0.0f, 2.5f, 1.0f);
                if (player.hasInfiniteMaterials()) {
                    thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.playSound(null, thrownTrident, holder.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        if (f > 0.0f) {
            float f2 = player.getYRot();
            float f3 = player.getXRot();
            float f4 = -Mth.sin(f2 * ((float)Math.PI / 180)) * Mth.cos(f3 * ((float)Math.PI / 180));
            float f5 = -Mth.sin(f3 * ((float)Math.PI / 180));
            float f6 = Mth.cos(f2 * ((float)Math.PI / 180)) * Mth.cos(f3 * ((float)Math.PI / 180));
            float f7 = Mth.sqrt(f4 * f4 + f5 * f5 + f6 * f6);
            player.push(f4 *= f / f7, f5 *= f / f7, f6 *= f / f7);
            player.startAutoSpinAttack(20, 8.0f, itemStack);
            if (player.onGround()) {
                float f8 = 1.1999999f;
                player.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
            }
            level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        }
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) > 0.0f && !player.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(interactionHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownTrident thrownTrident = new ThrownTrident(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        thrownTrident.pickup = AbstractArrow.Pickup.ALLOWED;
        return thrownTrident;
    }
}

