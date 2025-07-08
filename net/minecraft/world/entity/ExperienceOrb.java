/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExperienceOrb
extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_VALUE = SynchedEntityData.defineId(ExperienceOrb.class, EntityDataSerializers.INT);
    private static final int LIFETIME = 6000;
    private static final int ENTITY_SCAN_PERIOD = 20;
    private static final int MAX_FOLLOW_DIST = 8;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double ORB_MERGE_DISTANCE = 0.5;
    private static final short DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_VALUE = 0;
    private static final int DEFAULT_COUNT = 1;
    private int age = 0;
    private int health = 5;
    private int count = 1;
    @Nullable
    private Player followingPlayer;
    private final InterpolationHandler interpolation = new InterpolationHandler(this);

    public ExperienceOrb(Level level, double d, double d2, double d3, int n) {
        this(level, new Vec3(d, d2, d3), Vec3.ZERO, n);
    }

    public ExperienceOrb(Level level, Vec3 vec3, Vec3 vec32, int n) {
        this((EntityType<? extends ExperienceOrb>)EntityType.EXPERIENCE_ORB, level);
        this.setPos(vec3);
        if (!level.isClientSide) {
            this.setYRot(this.random.nextFloat() * 360.0f);
            Vec3 vec33 = new Vec3((this.random.nextDouble() * 0.2 - 0.1) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2 - 0.1) * 2.0);
            if (vec32.lengthSqr() > 0.0 && vec32.dot(vec33) < 0.0) {
                vec33 = vec33.scale(-1.0);
            }
            double d = this.getBoundingBox().getSize();
            this.setPos(vec3.add(vec32.normalize().scale(d * 0.5)));
            this.setDeltaMovement(vec33);
            if (!level.noCollision(this.getBoundingBox())) {
                this.unstuckIfPossible(d);
            }
        }
        this.setValue(n);
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> entityType, Level level) {
        super(entityType, level);
    }

    protected void unstuckIfPossible(double d) {
        Vec3 vec32 = this.position().add(0.0, (double)this.getBbHeight() / 2.0, 0.0);
        VoxelShape voxelShape = Shapes.create(AABB.ofSize(vec32, d, d, d));
        this.level().findFreePosition(this, voxelShape, vec32, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()).ifPresent(vec3 -> this.setPos(vec3.add(0.0, (double)(-this.getBbHeight()) / 2.0, 0.0)));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VALUE, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        boolean bl;
        this.interpolation.interpolate();
        if (this.firstTick && this.level().isClientSide) {
            this.firstTick = false;
            return;
        }
        super.tick();
        boolean bl2 = bl = !this.level().noCollision(this.getBoundingBox());
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!bl) {
            this.applyGravity();
        }
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        if (this.tickCount % 20 == 1) {
            this.scanForMerges();
        }
        this.followNearbyPlayer();
        if (this.followingPlayer == null && !this.level().isClientSide && bl) {
            boolean bl3;
            boolean bl4 = bl3 = !this.level().noCollision(this.getBoundingBox().move(this.getDeltaMovement()));
            if (bl3) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                this.hasImpulse = true;
            }
        }
        double d = this.getDeltaMovement().y;
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        float f = 0.98f;
        if (this.onGround()) {
            f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(f));
        if (this.verticalCollisionBelow && d < -this.getGravity()) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x, -d * 0.4, this.getDeltaMovement().z));
        }
        ++this.age;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    private void followNearbyPlayer() {
        Object object;
        if (this.followingPlayer == null || this.followingPlayer.isSpectator() || this.followingPlayer.distanceToSqr(this) > 64.0) {
            object = this.level().getNearestPlayer(this, 8.0);
            this.followingPlayer = object != null && !((Player)object).isSpectator() && !((LivingEntity)object).isDeadOrDying() ? object : null;
        }
        if (this.followingPlayer != null) {
            object = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(), this.followingPlayer.getZ() - this.getZ());
            double d = ((Vec3)object).lengthSqr();
            double d2 = 1.0 - Math.sqrt(d) / 8.0;
            this.setDeltaMovement(this.getDeltaMovement().add(((Vec3)object).normalize().scale(d2 * d2 * 0.1)));
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999f);
    }

    private void scanForMerges() {
        if (this.level() instanceof ServerLevel) {
            List<ExperienceOrb> list = this.level().getEntities(EntityTypeTest.forClass(ExperienceOrb.class), this.getBoundingBox().inflate(0.5), this::canMerge);
            for (ExperienceOrb experienceOrb : list) {
                this.merge(experienceOrb);
            }
        }
    }

    public static void award(ServerLevel serverLevel, Vec3 vec3, int n) {
        ExperienceOrb.awardWithDirection(serverLevel, vec3, Vec3.ZERO, n);
    }

    public static void awardWithDirection(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, int n) {
        while (n > 0) {
            int n2 = ExperienceOrb.getExperienceValue(n);
            n -= n2;
            if (ExperienceOrb.tryMergeToExisting(serverLevel, vec3, n2)) continue;
            serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, vec3, vec32, n2));
        }
    }

    private static boolean tryMergeToExisting(ServerLevel serverLevel, Vec3 vec3, int n) {
        AABB aABB = AABB.ofSize(vec3, 1.0, 1.0, 1.0);
        int n2 = serverLevel.getRandom().nextInt(40);
        List<ExperienceOrb> list = serverLevel.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), aABB, experienceOrb -> ExperienceOrb.canMerge(experienceOrb, n2, n));
        if (!list.isEmpty()) {
            ExperienceOrb experienceOrb2 = list.get(0);
            ++experienceOrb2.count;
            experienceOrb2.age = 0;
            return true;
        }
        return false;
    }

    private boolean canMerge(ExperienceOrb experienceOrb) {
        return experienceOrb != this && ExperienceOrb.canMerge(experienceOrb, this.getId(), this.getValue());
    }

    private static boolean canMerge(ExperienceOrb experienceOrb, int n, int n2) {
        return !experienceOrb.isRemoved() && (experienceOrb.getId() - n) % 40 == 0 && experienceOrb.getValue() == n2;
    }

    private void merge(ExperienceOrb experienceOrb) {
        this.count += experienceOrb.count;
        this.age = Math.min(this.age, experienceOrb.age);
        experienceOrb.discard();
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.99f, Math.min(vec3.y + (double)5.0E-4f, (double)0.06f), vec3.z * (double)0.99f);
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public final boolean hurtClient(DamageSource damageSource) {
        return !this.isInvulnerableToBase(damageSource);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        this.markHurt();
        this.health = (int)((float)this.health - f);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putShort("Health", (short)this.health);
        valueOutput.putShort("Age", (short)this.age);
        valueOutput.putShort("Value", (short)this.getValue());
        valueOutput.putInt("Count", this.count);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.health = valueInput.getShortOr("Health", (short)5);
        this.age = valueInput.getShortOr("Age", (short)0);
        this.setValue(valueInput.getShortOr("Value", (short)0));
        this.count = valueInput.read("Count", ExtraCodecs.POSITIVE_INT).orElse(1);
    }

    @Override
    public void playerTouch(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (player.takeXpDelay == 0) {
            player.takeXpDelay = 2;
            player.take(this, 1);
            int n = this.repairPlayerItems(serverPlayer, this.getValue());
            if (n > 0) {
                player.giveExperiencePoints(n);
            }
            --this.count;
            if (this.count == 0) {
                this.discard();
            }
        }
    }

    private int repairPlayerItems(ServerPlayer serverPlayer, int n) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, serverPlayer, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int n2;
            ItemStack itemStack = optional.get().itemStack();
            int n3 = EnchantmentHelper.modifyDurabilityToRepairFromXp(serverPlayer.level(), itemStack, n);
            int n4 = Math.min(n3, itemStack.getDamageValue());
            itemStack.setDamageValue(itemStack.getDamageValue() - n4);
            if (n4 > 0 && (n2 = n - n4 * n / n3) > 0) {
                return this.repairPlayerItems(serverPlayer, n2);
            }
            return 0;
        }
        return n;
    }

    public int getValue() {
        return this.entityData.get(DATA_VALUE);
    }

    private void setValue(int n) {
        this.entityData.set(DATA_VALUE, n);
    }

    public int getIcon() {
        int n = this.getValue();
        if (n >= 2477) {
            return 10;
        }
        if (n >= 1237) {
            return 9;
        }
        if (n >= 617) {
            return 8;
        }
        if (n >= 307) {
            return 7;
        }
        if (n >= 149) {
            return 6;
        }
        if (n >= 73) {
            return 5;
        }
        if (n >= 37) {
            return 4;
        }
        if (n >= 17) {
            return 3;
        }
        if (n >= 7) {
            return 2;
        }
        if (n >= 3) {
            return 1;
        }
        return 0;
    }

    public static int getExperienceValue(int n) {
        if (n >= 2477) {
            return 2477;
        }
        if (n >= 1237) {
            return 1237;
        }
        if (n >= 617) {
            return 617;
        }
        if (n >= 307) {
            return 307;
        }
        if (n >= 149) {
            return 149;
        }
        if (n >= 73) {
            return 73;
        }
        if (n >= 37) {
            return 37;
        }
        if (n >= 17) {
            return 17;
        }
        if (n >= 7) {
            return 7;
        }
        if (n >= 3) {
            return 3;
        }
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }
}

