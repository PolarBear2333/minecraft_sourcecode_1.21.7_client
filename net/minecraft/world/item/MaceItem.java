/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MaceItem
extends Item {
    private static final int DEFAULT_ATTACK_DAMAGE = 5;
    private static final float DEFAULT_ATTACK_SPEED = -3.4f;
    public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5f;
    private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0f;
    public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5f;
    private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0f, 2, false);
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (MaceItem.canSmashAttack(livingEntity2)) {
            Object object;
            ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
            livingEntity2.setDeltaMovement(livingEntity2.getDeltaMovement().with(Direction.Axis.Y, 0.01f));
            if (livingEntity2 instanceof ServerPlayer) {
                object = (ServerPlayer)livingEntity2;
                ((ServerPlayer)object).currentImpulseImpactPos = this.calculateImpactPosition((ServerPlayer)object);
                ((Player)object).setIgnoreFallDamageFromCurrentImpulse(true);
                ((ServerPlayer)object).connection.send(new ClientboundSetEntityMotionPacket((Entity)object));
            }
            if (livingEntity.onGround()) {
                if (livingEntity2 instanceof ServerPlayer) {
                    object = (ServerPlayer)livingEntity2;
                    ((ServerPlayer)object).setSpawnExtraParticlesOnFall(true);
                }
                object = livingEntity2.fallDistance > 5.0 ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), (SoundEvent)object, livingEntity2.getSoundSource(), 1.0f, 1.0f);
            } else {
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), SoundEvents.MACE_SMASH_AIR, livingEntity2.getSoundSource(), 1.0f, 1.0f);
            }
            MaceItem.knockback(serverLevel, livingEntity2, livingEntity);
        }
    }

    private Vec3 calculateImpactPosition(ServerPlayer serverPlayer) {
        if (serverPlayer.isIgnoringFallDamageFromCurrentImpulse() && serverPlayer.currentImpulseImpactPos != null && serverPlayer.currentImpulseImpactPos.y <= serverPlayer.position().y) {
            return serverPlayer.currentImpulseImpactPos;
        }
        return serverPlayer.position();
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (MaceItem.canSmashAttack(livingEntity2)) {
            livingEntity2.resetFallDistance();
        }
    }

    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        Entity entity2 = damageSource.getDirectEntity();
        if (!(entity2 instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity livingEntity = (LivingEntity)entity2;
        if (!MaceItem.canSmashAttack(livingEntity)) {
            return 0.0f;
        }
        double d = 3.0;
        double d2 = 8.0;
        double d3 = livingEntity.fallDistance;
        double d4 = d3 <= 3.0 ? 4.0 * d3 : (d3 <= 8.0 ? 12.0 + 2.0 * (d3 - 3.0) : 22.0 + d3 - 8.0);
        Level level = livingEntity.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return (float)(d4 + (double)EnchantmentHelper.modifyFallBasedDamage(serverLevel, livingEntity.getWeaponItem(), entity, damageSource, 0.0f) * d3);
        }
        return (float)d4;
    }

    private static void knockback(Level level, Entity entity, Entity entity2) {
        level.levelEvent(2013, entity2.getOnPos(), 750);
        level.getEntitiesOfClass(LivingEntity.class, entity2.getBoundingBox().inflate(3.5), MaceItem.knockbackPredicate(entity, entity2)).forEach(livingEntity -> {
            Vec3 vec3 = livingEntity.position().subtract(entity2.position());
            double d = MaceItem.getKnockbackPower(entity, livingEntity, vec3);
            Vec3 vec32 = vec3.normalize().scale(d);
            if (d > 0.0) {
                livingEntity.push(vec32.x, 0.7f, vec32.z);
                if (livingEntity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }
        });
    }

    private static Predicate<LivingEntity> knockbackPredicate(Entity entity, Entity entity2) {
        return arg_0 -> MaceItem.lambda$knockbackPredicate$1(entity, entity2, arg_0);
    }

    private static double getKnockbackPower(Entity entity, LivingEntity livingEntity, Vec3 vec3) {
        return (3.5 - vec3.length()) * (double)0.7f * (double)(entity.fallDistance > 5.0 ? 2 : 1) * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean canSmashAttack(LivingEntity livingEntity) {
        return livingEntity.fallDistance > 1.5 && !livingEntity.isFallFlying();
    }

    @Override
    @Nullable
    public DamageSource getDamageSource(LivingEntity livingEntity) {
        if (MaceItem.canSmashAttack(livingEntity)) {
            return livingEntity.damageSources().mace(livingEntity);
        }
        return super.getDamageSource(livingEntity);
    }

    /*
     * Unable to fully structure code
     */
    private static /* synthetic */ boolean lambda$knockbackPredicate$1(Entity var0, Entity var1_1, LivingEntity var2_2) {
        var3_3 = var2_2.isSpectator() == false;
        var4_4 = var2_2 != var0 && var2_2 != var1_1;
        v0 = var5_5 = var0.isAlliedTo(var2_2) == false;
        if (!(var2_2 instanceof TamableAnimal)) ** GOTO lbl-1000
        var8_6 = (TamableAnimal)var2_2;
        if (!(var1_1 instanceof LivingEntity)) ** GOTO lbl-1000
        var7_8 = (LivingEntity)var1_1;
        if (var8_6.isTame() && var8_6.isOwnedBy(var7_8)) {
            v1 = true;
        } else lbl-1000:
        // 3 sources

        {
            v1 = false;
        }
        var6_10 = v1 == false;
        var7_9 = var2_2 instanceof ArmorStand == false || (var8_6 = (ArmorStand)var2_2).isMarker() == false;
        var8_7 = var1_1.distanceToSqr(var2_2) <= Math.pow(3.5, 2.0);
        return var3_3 != false && var4_4 != false && var5_5 != false && var6_10 != false && var7_9 != false && var8_7 != false;
    }
}

