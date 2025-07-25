/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public abstract class AbstractSkeleton
extends Monster
implements RangedAttackMob {
    private static final int HARD_ATTACK_INTERVAL = 20;
    private static final int NORMAL_ATTACK_INTERVAL = 40;
    private final RangedBowAttackGoal<AbstractSkeleton> bowGoal = new RangedBowAttackGoal<AbstractSkeleton>(this, 1.0, 20, 15.0f);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2, false){

        @Override
        public void stop() {
            super.stop();
            AbstractSkeleton.this.setAggressive(false);
        }

        @Override
        public void start() {
            super.start();
            AbstractSkeleton.this.setAggressive(true);
        }
    };

    protected AbstractSkeleton(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.reassessWeaponGoal();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Wolf>(this, Wolf.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Turtle>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(this.getStepSound(), 0.15f, 1.0f);
    }

    abstract SoundEvent getStepSound();

    @Override
    public void aiStep() {
        boolean bl = this.isSunBurnTick();
        if (bl) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemStack.isEmpty()) {
                if (itemStack.isDamageableItem()) {
                    Item item = itemStack.getItem();
                    itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
                    if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                        this.onEquippedItemBroken(item, EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
                bl = false;
            }
            if (bl) {
                this.igniteForSeconds(8.0f);
            }
        }
        super.aiStep();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        Entity entity = this.getControlledVehicle();
        if (entity instanceof PathfinderMob) {
            PathfinderMob pathfinderMob = (PathfinderMob)entity;
            this.yBodyRot = pathfinderMob.yBodyRot;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
        RandomSource randomSource = serverLevelAccessor.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance);
        this.reassessWeaponGoal();
        this.setCanPickUpLoot(randomSource.nextFloat() < 0.55f * difficultyInstance.getSpecialMultiplier());
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int n = localDate.get(ChronoField.DAY_OF_MONTH);
            int n2 = localDate.get(ChronoField.MONTH_OF_YEAR);
            if (n2 == 10 && n == 31 && randomSource.nextFloat() < 0.25f) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomSource.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.setDropChance(EquipmentSlot.HEAD, 0.0f);
            }
        }
        return spawnGroupData;
    }

    public void reassessWeaponGoal() {
        if (this.level() == null || this.level().isClientSide) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeGoal);
        this.goalSelector.removeGoal(this.bowGoal);
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        if (itemStack.is(Items.BOW)) {
            int n = this.getHardAttackInterval();
            if (this.level().getDifficulty() != Difficulty.HARD) {
                n = this.getAttackInterval();
            }
            this.bowGoal.setMinAttackInterval(n);
            this.goalSelector.addGoal(4, this.bowGoal);
        } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
        }
    }

    protected int getHardAttackInterval() {
        return 20;
    }

    protected int getAttackInterval() {
        return 40;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        ItemStack itemStack2 = this.getProjectile(itemStack);
        AbstractArrow abstractArrow = this.getArrow(itemStack2, f, itemStack);
        double d = livingEntity.getX() - this.getX();
        double d2 = livingEntity.getY(0.3333333333333333) - abstractArrow.getY();
        double d3 = livingEntity.getZ() - this.getZ();
        double d4 = Math.sqrt(d * d + d3 * d3);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot(abstractArrow, serverLevel, itemStack2, d, d2 + d4 * (double)0.2f, d3, 1.6f, 14 - serverLevel.getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    protected AbstractArrow getArrow(ItemStack itemStack, float f, @Nullable ItemStack itemStack2) {
        return ProjectileUtil.getMobArrow(this, itemStack, f, itemStack2);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return projectileWeaponItem == Items.BOW;
    }

    @Override
    public TagKey<Item> getPreferredWeaponType() {
        return ItemTags.SKELETON_PREFERRED_WEAPONS;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.reassessWeaponGoal();
    }

    @Override
    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        super.onEquipItem(equipmentSlot, itemStack, itemStack2);
        if (!this.level().isClientSide) {
            this.reassessWeaponGoal();
        }
    }

    public boolean isShaking() {
        return this.isFullyFrozen();
    }
}

