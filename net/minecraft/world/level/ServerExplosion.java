/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ServerExplosion
implements Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private static final float LARGE_EXPLOSION_RADIUS = 2.0f;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final ServerLevel level;
    private final Vec3 center;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final Map<Player, Vec3> hitPlayers = new HashMap<Player, Vec3>();

    public ServerExplosion(ServerLevel serverLevel, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, boolean bl, Explosion.BlockInteraction blockInteraction) {
        this.level = serverLevel;
        this.source = entity;
        this.radius = f;
        this.center = vec3;
        this.fire = bl;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? serverLevel.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
        double d2 = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
        double d3 = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
        double d4 = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double d5 = (1.0 - Math.floor(1.0 / d3) * d3) / 2.0;
        if (d < 0.0 || d2 < 0.0 || d3 < 0.0) {
            return 0.0f;
        }
        int n = 0;
        int n2 = 0;
        for (double d6 = 0.0; d6 <= 1.0; d6 += d) {
            for (double d7 = 0.0; d7 <= 1.0; d7 += d2) {
                for (double d8 = 0.0; d8 <= 1.0; d8 += d3) {
                    double d9 = Mth.lerp(d6, aABB.minX, aABB.maxX);
                    double d10 = Mth.lerp(d7, aABB.minY, aABB.maxY);
                    double d11 = Mth.lerp(d8, aABB.minZ, aABB.maxZ);
                    Vec3 vec32 = new Vec3(d9 + d4, d10, d11 + d5);
                    if (entity.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++n;
                    }
                    ++n2;
                }
            }
        }
        return (float)n / (float)n2;
    }

    @Override
    public float radius() {
        return this.radius;
    }

    @Override
    public Vec3 center() {
        return this.center;
    }

    private List<BlockPos> calculateExplodedPositions() {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        int n = 16;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                block2: for (int k = 0; k < 16; ++k) {
                    if (i != 0 && i != 15 && j != 0 && j != 15 && k != 0 && k != 15) continue;
                    double d = (float)i / 15.0f * 2.0f - 1.0f;
                    double d2 = (float)j / 15.0f * 2.0f - 1.0f;
                    double d3 = (float)k / 15.0f * 2.0f - 1.0f;
                    double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
                    d /= d4;
                    d2 /= d4;
                    d3 /= d4;
                    double d5 = this.center.x;
                    double d6 = this.center.y;
                    double d7 = this.center.z;
                    float f = 0.3f;
                    for (float f2 = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); f2 > 0.0f; f2 -= 0.22500001f) {
                        BlockPos blockPos = BlockPos.containing(d5, d6, d7);
                        BlockState blockState = this.level.getBlockState(blockPos);
                        FluidState fluidState = this.level.getFluidState(blockPos);
                        if (!this.level.isInWorldBounds(blockPos)) continue block2;
                        Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
                        if (optional.isPresent()) {
                            f2 -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (f2 > 0.0f && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, f2)) {
                            hashSet.add(blockPos);
                        }
                        d5 += d * (double)0.3f;
                        d6 += d2 * (double)0.3f;
                        d7 += d3 * (double)0.3f;
                    }
                }
            }
        }
        return new ObjectArrayList(hashSet);
    }

    private void hurtEntities() {
        float f = this.radius * 2.0f;
        int n = Mth.floor(this.center.x - (double)f - 1.0);
        int n2 = Mth.floor(this.center.x + (double)f + 1.0);
        int n3 = Mth.floor(this.center.y - (double)f - 1.0);
        int n4 = Mth.floor(this.center.y + (double)f + 1.0);
        int n5 = Mth.floor(this.center.z - (double)f - 1.0);
        int n6 = Mth.floor(this.center.z + (double)f + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB(n, n3, n5, n2, n4, n6));
        for (Entity entity : list) {
            Player player;
            double d;
            Object object;
            float f2;
            double d2;
            double d3;
            double d4;
            double d5;
            double d6;
            if (entity.ignoreExplosion(this) || !((d6 = Math.sqrt(entity.distanceToSqr(this.center)) / (double)f) <= 1.0) || (d5 = Math.sqrt((d4 = entity.getX() - this.center.x) * d4 + (d3 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.center.y) * d3 + (d2 = entity.getZ() - this.center.z) * d2)) == 0.0) continue;
            d4 /= d5;
            d3 /= d5;
            d2 /= d5;
            boolean bl = this.damageCalculator.shouldDamageEntity(this, entity);
            float f3 = this.damageCalculator.getKnockbackMultiplier(entity);
            float f4 = f2 = bl || f3 != 0.0f ? ServerExplosion.getSeenPercent(this.center, entity) : 0.0f;
            if (bl) {
                entity.hurtServer(this.level, this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity, f2));
            }
            double d7 = (1.0 - d6) * (double)f2 * (double)f3;
            if (entity instanceof LivingEntity) {
                object = (LivingEntity)entity;
                d = d7 * (1.0 - ((LivingEntity)object).getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
            } else {
                d = d7;
            }
            object = new Vec3(d4 *= d, d3 *= d, d2 *= d);
            entity.push((Vec3)object);
            if (!(!(entity instanceof Player) || (player = (Player)entity).isSpectator() || player.isCreative() && player.getAbilities().flying)) {
                this.hitPlayers.put(player, (Vec3)object);
            }
            entity.onExplosionHit(this.source);
        }
    }

    private void interactWithBlocks(List<BlockPos> list) {
        ArrayList arrayList = new ArrayList();
        Util.shuffle(list, this.level.random);
        for (BlockPos object : list) {
            this.level.getBlockState(object).onExplosionHit(this.level, object, this, (itemStack, blockPos) -> ServerExplosion.addOrAppendStack(arrayList, itemStack, blockPos));
        }
        for (StackCollector stackCollector : arrayList) {
            Block.popResource((Level)this.level, stackCollector.pos, stackCollector.stack);
        }
    }

    private void createFire(List<BlockPos> list) {
        for (BlockPos blockPos : list) {
            if (this.level.random.nextInt(3) != 0 || !this.level.getBlockState(blockPos).isAir() || !this.level.getBlockState(blockPos.below()).isSolidRender()) continue;
            this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
        }
    }

    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, this.center);
        List<BlockPos> list = this.calculateExplodedPositions();
        this.hurtEntities();
        if (this.interactsWithBlocks()) {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("explosion_blocks");
            this.interactWithBlocks(list);
            profilerFiller.pop();
        }
        if (this.fire) {
            this.createFire(list);
        }
    }

    private static void addOrAppendStack(List<StackCollector> list, ItemStack itemStack, BlockPos blockPos) {
        for (StackCollector stackCollector : list) {
            stackCollector.tryMerge(itemStack);
            if (!itemStack.isEmpty()) continue;
            return;
        }
        list.add(new StackCollector(blockPos, itemStack));
    }

    private boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.BlockInteraction.KEEP;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Override
    public ServerLevel level() {
        return this.level;
    }

    @Override
    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntity(this.source);
    }

    @Override
    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    @Override
    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    @Override
    public boolean canTriggerBlocks() {
        if (this.blockInteraction != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            return false;
        }
        if (this.source != null && this.source.getType() == EntityType.BREEZE_WIND_CHARGE) {
            return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        return true;
    }

    @Override
    public boolean shouldAffectBlocklikeEntities() {
        boolean bl;
        boolean bl2 = this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        boolean bl3 = bl = this.source == null || this.source.getType() != EntityType.BREEZE_WIND_CHARGE && this.source.getType() != EntityType.WIND_CHARGE;
        if (bl2) {
            return bl;
        }
        return this.blockInteraction.shouldAffectBlocklikeEntities() && bl;
    }

    public boolean isSmall() {
        return this.radius < 2.0f || !this.interactsWithBlocks();
    }

    static class StackCollector {
        final BlockPos pos;
        ItemStack stack;

        StackCollector(BlockPos blockPos, ItemStack itemStack) {
            this.pos = blockPos;
            this.stack = itemStack;
        }

        public void tryMerge(ItemStack itemStack) {
            if (ItemEntity.areMergable(this.stack, itemStack)) {
                this.stack = ItemEntity.merge(this.stack, itemStack, 16);
            }
        }
    }
}

