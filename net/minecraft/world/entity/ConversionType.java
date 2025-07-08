/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;

public enum ConversionType {
    SINGLE(true){

        @Override
        void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
            Object object;
            Object object2;
            Entity entity = mob.getFirstPassenger();
            mob2.copyPosition(mob);
            mob2.setDeltaMovement(mob.getDeltaMovement());
            if (entity != null) {
                entity.stopRiding();
                entity.boardingCooldown = 0;
                object2 = mob2.getPassengers().iterator();
                while (object2.hasNext()) {
                    object = (Entity)object2.next();
                    ((Entity)object).stopRiding();
                    ((Entity)object).remove(Entity.RemovalReason.DISCARDED);
                }
                entity.startRiding(mob2);
            }
            if ((object2 = mob.getVehicle()) != null) {
                mob.stopRiding();
                mob2.startRiding((Entity)object2);
            }
            if (conversionParams.keepEquipment()) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                    ItemStack itemStack = mob.getItemBySlot(equipmentSlot);
                    if (itemStack.isEmpty()) continue;
                    mob2.setItemSlot(equipmentSlot, itemStack.copyAndClear());
                    mob2.setDropChance(equipmentSlot, mob.getDropChances().byEquipment(equipmentSlot));
                }
            }
            mob2.fallDistance = mob.fallDistance;
            mob2.setSharedFlag(7, mob.isFallFlying());
            mob2.lastHurtByPlayerMemoryTime = mob.lastHurtByPlayerMemoryTime;
            mob2.hurtTime = mob.hurtTime;
            mob2.yBodyRot = mob.yBodyRot;
            mob2.setOnGround(mob.onGround());
            mob.getSleepingPos().ifPresent(mob2::setSleepingPos);
            object = mob.getLeashHolder();
            if (object != null) {
                mob2.setLeashedTo((Entity)object, true);
            }
            this.convertCommon(mob, mob2, conversionParams);
        }
    }
    ,
    SPLIT_ON_DEATH(false){

        @Override
        void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
            Entity entity;
            Entity entity2 = mob.getFirstPassenger();
            if (entity2 != null) {
                entity2.stopRiding();
            }
            if ((entity = mob.getLeashHolder()) != null) {
                mob.dropLeash();
            }
            this.convertCommon(mob, mob2, conversionParams);
        }
    };

    private static final Set<DataComponentType<?>> COMPONENTS_TO_COPY;
    private final boolean discardAfterConversion;

    ConversionType(boolean bl) {
        this.discardAfterConversion = bl;
    }

    public boolean shouldDiscardAfterConversion() {
        return this.discardAfterConversion;
    }

    abstract void convert(Mob var1, Mob var2, ConversionParams var3);

    void convertCommon(Mob mob, Mob mob2, ConversionParams conversionParams) {
        Object object;
        Brain<UUID> brain;
        mob2.setAbsorptionAmount(mob.getAbsorptionAmount());
        for (MobEffectInstance object22 : mob.getActiveEffects()) {
            mob2.addEffect(new MobEffectInstance(object22));
        }
        if (mob.isBaby()) {
            mob2.setBaby(true);
        }
        if (mob instanceof AgeableMob) {
            brain = (AgeableMob)mob;
            if (mob2 instanceof AgeableMob) {
                AgeableMob ageableMob = (AgeableMob)mob2;
                ageableMob.setAge(((AgeableMob)((Object)brain)).getAge());
                ageableMob.forcedAge = ((AgeableMob)((Object)brain)).forcedAge;
                ageableMob.forcedAgeTimer = ((AgeableMob)((Object)brain)).forcedAgeTimer;
            }
        }
        brain = mob.getBrain();
        Brain<?> brain2 = mob2.getBrain();
        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED) && brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)) {
            brain2.setMemory(MemoryModuleType.ANGRY_AT, brain.getMemory(MemoryModuleType.ANGRY_AT));
        }
        if (conversionParams.preserveCanPickUpLoot()) {
            mob2.setCanPickUpLoot(mob.canPickUpLoot());
        }
        mob2.setLeftHanded(mob.isLeftHanded());
        mob2.setNoAi(mob.isNoAi());
        if (mob.isPersistenceRequired()) {
            mob2.setPersistenceRequired();
        }
        mob2.setCustomNameVisible(mob.isCustomNameVisible());
        mob2.setSharedFlagOnFire(mob.isOnFire());
        mob2.setInvulnerable(mob.isInvulnerable());
        mob2.setNoGravity(mob.isNoGravity());
        mob2.setPortalCooldown(mob.getPortalCooldown());
        mob2.setSilent(mob.isSilent());
        mob.getTags().forEach(mob2::addTag);
        Object object2 = COMPONENTS_TO_COPY.iterator();
        while (object2.hasNext()) {
            object = object2.next();
            ConversionType.copyComponent(mob, mob2, object);
        }
        if (conversionParams.team() != null) {
            object2 = mob2.level().getScoreboard();
            ((Scoreboard)object2).addPlayerToTeam(mob2.getStringUUID(), conversionParams.team());
            if (mob.getTeam() != null && mob.getTeam() == conversionParams.team()) {
                ((Scoreboard)object2).removePlayerFromTeam(mob.getStringUUID(), mob.getTeam());
            }
        }
        if (mob instanceof Zombie && ((Zombie)(object2 = (Zombie)mob)).canBreakDoors() && mob2 instanceof Zombie) {
            object = (Zombie)mob2;
            ((Zombie)object).setCanBreakDoors(true);
        }
    }

    private static <T> void copyComponent(Mob mob, Mob mob2, DataComponentType<T> dataComponentType) {
        T t = mob.get(dataComponentType);
        if (t != null) {
            mob2.setComponent(dataComponentType, t);
        }
    }

    static {
        COMPONENTS_TO_COPY = Set.of(DataComponents.CUSTOM_NAME, DataComponents.CUSTOM_DATA);
    }
}

