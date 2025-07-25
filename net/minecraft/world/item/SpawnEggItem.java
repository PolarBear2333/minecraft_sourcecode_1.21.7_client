/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Maps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpawnEggItem
extends Item {
    private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
    private final EntityType<?> defaultType;

    public SpawnEggItem(EntityType<? extends Mob> entityType, Item.Properties properties) {
        super(properties);
        this.defaultType = entityType;
        BY_ID.put(entityType, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);
        EntityType<?> entityType = level.getBlockEntity(blockPos);
        if (entityType instanceof Spawner) {
            Spawner spawner = (Spawner)((Object)entityType);
            entityType = this.getType(level.registryAccess(), itemStack);
            spawner.setEntityId(entityType, level.getRandom());
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        BlockPos blockPos2 = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);
        entityType = this.getType(level.registryAccess(), itemStack);
        if (entityType.spawn((ServerLevel)level, itemStack, useOnContext.getPlayer(), blockPos2, EntitySpawnReason.SPAWN_ITEM_USE, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP) != null) {
            itemStack.shrink(1);
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = SpawnEggItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        BlockHitResult blockHitResult2 = blockHitResult;
        BlockPos blockPos = blockHitResult2.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResult.PASS;
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, blockHitResult2.getDirection(), itemStack)) {
            return InteractionResult.FAIL;
        }
        EntityType<?> entityType = this.getType(serverLevel.registryAccess(), itemStack);
        Object obj = entityType.spawn(serverLevel, itemStack, player, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, false, false);
        if (obj == null) {
            return InteractionResult.PASS;
        }
        itemStack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, ((Entity)obj).position());
        return InteractionResult.SUCCESS;
    }

    public boolean spawnsEntity(HolderLookup.Provider provider, ItemStack itemStack, EntityType<?> entityType) {
        return Objects.equals(this.getType(provider, itemStack), entityType);
    }

    @Nullable
    public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
        return BY_ID.get(entityType);
    }

    public static Iterable<SpawnEggItem> eggs() {
        return Iterables.unmodifiableIterable(BY_ID.values());
    }

    public EntityType<?> getType(HolderLookup.Provider provider, ItemStack itemStack) {
        EntityType<?> entityType;
        CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!customData.isEmpty() && (entityType = customData.parseEntityType(provider, Registries.ENTITY_TYPE)) != null) {
            return entityType;
        }
        return this.defaultType;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.defaultType.requiredFeatures();
    }

    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 vec3, ItemStack itemStack) {
        if (!this.spawnsEntity(serverLevel.registryAccess(), itemStack, entityType)) {
            return Optional.empty();
        }
        Mob mob2 = mob instanceof AgeableMob ? ((AgeableMob)mob).getBreedOffspring(serverLevel, (AgeableMob)mob) : entityType.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (mob2 == null) {
            return Optional.empty();
        }
        mob2.setBaby(true);
        if (!mob2.isBaby()) {
            return Optional.empty();
        }
        mob2.snapTo(vec3.x(), vec3.y(), vec3.z(), 0.0f, 0.0f);
        mob2.applyComponentsFromItemStack(itemStack);
        serverLevel.addFreshEntityWithPassengers(mob2);
        itemStack.consume(1, player);
        return Optional.of(mob2);
    }

    @Override
    public boolean shouldPrintOpWarning(ItemStack itemStack, @Nullable Player player) {
        CustomData customData;
        if (player != null && player.getPermissionLevel() >= 2 && (customData = itemStack.get(DataComponents.ENTITY_DATA)) != null) {
            EntityType<?> entityType = customData.parseEntityType(player.level().registryAccess(), Registries.ENTITY_TYPE);
            return entityType != null && entityType.onlyOpCanSetNbt();
        }
        return false;
    }
}

