/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public abstract class VehicleEntity
extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    public VehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        return true;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hurtServer(ServerLevel var1_1, DamageSource var2_2, float var3_3) {
        if (this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableToBase(var2_2)) {
            return false;
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.setDamage(this.getDamage() + var3_3 * 10.0f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, var2_2.getEntity());
        var6_4 = var2_2.getEntity();
        if (!(var6_4 instanceof Player)) ** GOTO lbl-1000
        var5_5 = (Player)var6_4;
        if (var5_5.getAbilities().instabuild) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = var4_6 = false;
        }
        if (var4_6 == false && this.getDamage() > 40.0f || this.shouldSourceDestroy(var2_2)) {
            this.destroy(var1_1, var2_2);
        } else if (var4_6) {
            this.discard();
        }
        return true;
    }

    boolean shouldSourceDestroy(DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return explosion.getIndirectSourceEntity() instanceof Mob && !explosion.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public void destroy(ServerLevel serverLevel, Item item) {
        this.kill(serverLevel);
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        this.spawnAtLocation(serverLevel, itemStack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT, 0);
        builder.define(DATA_ID_HURTDIR, 1);
        builder.define(DATA_ID_DAMAGE, Float.valueOf(0.0f));
    }

    public void setHurtTime(int n) {
        this.entityData.set(DATA_ID_HURT, n);
    }

    public void setHurtDir(int n) {
        this.entityData.set(DATA_ID_HURTDIR, n);
    }

    public void setDamage(float f) {
        this.entityData.set(DATA_ID_DAMAGE, Float.valueOf(f));
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE).floatValue();
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(ServerLevel serverLevel, DamageSource damageSource) {
        this.destroy(serverLevel, this.getDropItem());
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }

    protected abstract Item getDropItem();
}

