/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.util.Either
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.embedded.EmbeddedChannel
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
    private final GameTestInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHelper(GameTestInfo gameTestInfo) {
        this.testInfo = gameTestInfo;
    }

    public GameTestAssertException assertionException(Component component) {
        return new GameTestAssertException(component, this.testInfo.getTick());
    }

    public GameTestAssertException assertionException(String string, Object ... objectArray) {
        return this.assertionException(Component.translatableEscape(string, objectArray));
    }

    public GameTestAssertPosException assertionException(BlockPos blockPos, Component component) {
        return new GameTestAssertPosException(component, this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
    }

    public GameTestAssertPosException assertionException(BlockPos blockPos, String string, Object ... objectArray) {
        return this.assertionException(blockPos, Component.translatableEscape(string, objectArray));
    }

    public ServerLevel getLevel() {
        return this.testInfo.getLevel();
    }

    public BlockState getBlockState(BlockPos blockPos) {
        return this.getLevel().getBlockState(this.absolutePos(blockPos));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos blockPos, Class<T> clazz) {
        BlockEntity blockEntity = this.getLevel().getBlockEntity(this.absolutePos(blockPos));
        if (blockEntity == null) {
            throw this.assertionException(blockPos, "test.error.missing_block_entity", new Object[0]);
        }
        if (clazz.isInstance(blockEntity)) {
            return (T)((BlockEntity)clazz.cast(blockEntity));
        }
        throw this.assertionException(blockPos, "test.error.wrong_block_entity", blockEntity.getType().builtInRegistryHolder().getRegisteredName());
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class<? extends Entity> clazz) {
        AABB aABB = this.getBounds();
        List<Entity> list = this.getLevel().getEntitiesOfClass(clazz, aABB.inflate(1.0), entity -> !(entity instanceof Player));
        list.forEach(entity -> entity.kill(this.getLevel()));
    }

    public ItemEntity spawnItem(Item item, Vec3 vec3) {
        ServerLevel serverLevel = this.getLevel();
        Vec3 vec32 = this.absoluteVec(vec3);
        ItemEntity itemEntity = new ItemEntity(serverLevel, vec32.x, vec32.y, vec32.z, new ItemStack(item, 1));
        itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
        serverLevel.addFreshEntity(itemEntity);
        return itemEntity;
    }

    public ItemEntity spawnItem(Item item, float f, float f2, float f3) {
        return this.spawnItem(item, new Vec3(f, f2, f3));
    }

    public ItemEntity spawnItem(Item item, BlockPos blockPos) {
        return this.spawnItem(item, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, BlockPos blockPos) {
        return this.spawn(entityType, Vec3.atBottomCenterOf(blockPos));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, Vec3 vec3) {
        Object object;
        ServerLevel serverLevel = this.getLevel();
        E e = entityType.create(serverLevel, EntitySpawnReason.STRUCTURE);
        if (e == null) {
            throw this.assertionException(BlockPos.containing(vec3), "test.error.spawn_failure", entityType.builtInRegistryHolder().getRegisteredName());
        }
        if (e instanceof Mob) {
            object = (Mob)e;
            ((Mob)object).setPersistenceRequired();
        }
        object = this.absoluteVec(vec3);
        ((Entity)e).snapTo(((Vec3)object).x, ((Vec3)object).y, ((Vec3)object).z, ((Entity)e).getYRot(), ((Entity)e).getXRot());
        serverLevel.addFreshEntity((Entity)e);
        return e;
    }

    public void hurt(Entity entity, DamageSource damageSource, float f) {
        entity.hurtServer(this.getLevel(), damageSource, f);
    }

    public void kill(Entity entity) {
        entity.kill(this.getLevel());
    }

    public <E extends Entity> E findOneEntity(EntityType<E> entityType) {
        return this.findClosestEntity(entityType, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E findClosestEntity(EntityType<E> entityType, int n, int n2, int n3, double d) {
        List<E> list = this.findEntities(entityType, n, n2, n3, d);
        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_around", entityType.getDescription(), n, n2, n3);
        }
        if (list.size() > 1) {
            throw this.assertionException("test.error.too_many_entities", entityType.toShortString(), n, n2, n3, list.size());
        }
        Vec3 vec3 = this.absoluteVec(new Vec3(n, n2, n3));
        list.sort((entity, entity2) -> {
            double d = entity.position().distanceTo(vec3);
            double d2 = entity2.position().distanceTo(vec3);
            return Double.compare(d, d2);
        });
        return (E)((Entity)list.get(0));
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> entityType, int n, int n2, int n3, double d) {
        return this.findEntities(entityType, Vec3.atBottomCenterOf(new BlockPos(n, n2, n3)), d);
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> entityType, Vec3 vec3, double d) {
        ServerLevel serverLevel = this.getLevel();
        Vec3 vec32 = this.absoluteVec(vec3);
        AABB aABB = this.testInfo.getStructureBounds();
        AABB aABB2 = new AABB(vec32.add(-d, -d, -d), vec32.add(d, d, d));
        return serverLevel.getEntities(entityType, aABB, entity -> entity.getBoundingBox().intersects(aABB2) && entity.isAlive());
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawn(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawn(entityType, new Vec3(f, f2, f3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, BlockPos blockPos) {
        Mob mob = (Mob)this.spawn(entityType, blockPos);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawnWithNoFreeWill(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, Vec3 vec3) {
        Mob mob = (Mob)this.spawn(entityType, vec3);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawnWithNoFreeWill(entityType, new Vec3(f, f2, f3));
    }

    public void moveTo(Mob mob, float f, float f2, float f3) {
        Vec3 vec3 = this.absoluteVec(new Vec3(f, f2, f3));
        mob.snapTo(vec3.x, vec3.y, vec3.z, mob.getYRot(), mob.getXRot());
    }

    public GameTestSequence walkTo(Mob mob, BlockPos blockPos, float f) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            Path path = mob.getNavigation().createPath(this.absolutePos(blockPos), 0);
            mob.getNavigation().moveTo(path, (double)f);
        });
    }

    public void pressButton(int n, int n2, int n3) {
        this.pressButton(new BlockPos(n, n2, n3));
    }

    public void pressButton(BlockPos blockPos) {
        this.assertBlockTag(BlockTags.BUTTONS, blockPos);
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        ButtonBlock buttonBlock = (ButtonBlock)blockState.getBlock();
        buttonBlock.press(blockState, this.getLevel(), blockPos2, null);
    }

    public void useBlock(BlockPos blockPos) {
        this.useBlock(blockPos, this.makeMockPlayer(GameType.CREATIVE));
    }

    public void useBlock(BlockPos blockPos, Player player) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        this.useBlock(blockPos, player, new BlockHitResult(Vec3.atCenterOf(blockPos2), Direction.NORTH, blockPos2, true));
    }

    public void useBlock(BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        InteractionHand interactionHand;
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        InteractionResult interactionResult = blockState.useItemOn(player.getItemInHand(interactionHand = InteractionHand.MAIN_HAND), this.getLevel(), player, interactionHand, blockHitResult);
        if (interactionResult.consumesAction()) {
            return;
        }
        if (interactionResult instanceof InteractionResult.TryEmptyHandInteraction && blockState.useWithoutItem(this.getLevel(), player, blockHitResult).consumesAction()) {
            return;
        }
        UseOnContext useOnContext = new UseOnContext(player, interactionHand, blockHitResult);
        player.getItemInHand(interactionHand).useOn(useOnContext);
    }

    public LivingEntity makeAboutToDrown(LivingEntity livingEntity) {
        livingEntity.setAirSupply(0);
        livingEntity.setHealth(0.25f);
        return livingEntity;
    }

    public LivingEntity withLowHealth(LivingEntity livingEntity) {
        livingEntity.setHealth(0.25f);
        return livingEntity;
    }

    public Player makeMockPlayer(final GameType gameType) {
        return new Player(this, this.getLevel(), new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            @Nonnull
            public GameType gameMode() {
                return gameType;
            }

            @Override
            public boolean isClientAuthoritative() {
                return false;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer serverPlayer = new ServerPlayer(this, this.getLevel().getServer(), this.getLevel(), commonListenerCookie.gameProfile(), commonListenerCookie.clientInformation()){

            @Override
            public GameType gameMode() {
                return GameType.CREATIVE;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ChannelHandler[]{connection});
        this.getLevel().getServer().getPlayerList().placeNewPlayer(connection, serverPlayer, commonListenerCookie);
        return serverPlayer;
    }

    public void pullLever(int n, int n2, int n3) {
        this.pullLever(new BlockPos(n, n2, n3));
    }

    public void pullLever(BlockPos blockPos) {
        this.assertBlockPresent(Blocks.LEVER, blockPos);
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        LeverBlock leverBlock = (LeverBlock)blockState.getBlock();
        leverBlock.pull(blockState, this.getLevel(), blockPos2, null);
    }

    public void pulseRedstone(BlockPos blockPos, long l) {
        this.setBlock(blockPos, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(l, () -> this.setBlock(blockPos, Blocks.AIR));
    }

    public void destroyBlock(BlockPos blockPos) {
        this.getLevel().destroyBlock(this.absolutePos(blockPos), false, null);
    }

    public void setBlock(int n, int n2, int n3, Block block) {
        this.setBlock(new BlockPos(n, n2, n3), block);
    }

    public void setBlock(int n, int n2, int n3, BlockState blockState) {
        this.setBlock(new BlockPos(n, n2, n3), blockState);
    }

    public void setBlock(BlockPos blockPos, Block block) {
        this.setBlock(blockPos, block.defaultBlockState());
    }

    public void setBlock(BlockPos blockPos, BlockState blockState) {
        this.getLevel().setBlock(this.absolutePos(blockPos), blockState, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int n) {
        this.getLevel().setDayTime(n);
    }

    public void assertBlockPresent(Block block, int n, int n2, int n3) {
        this.assertBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockPresent(Block block, BlockPos blockPos) {
        BlockState blockState = this.getBlockState(blockPos);
        this.assertBlock(blockPos, block2 -> blockState.is(block), block2 -> Component.translatable("test.error.expected_block", block.getName(), block2.getName()));
    }

    public void assertBlockNotPresent(Block block, int n, int n2, int n3) {
        this.assertBlockNotPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockNotPresent(Block block, BlockPos blockPos) {
        this.assertBlock(blockPos, block2 -> !this.getBlockState(blockPos).is(block), block2 -> Component.translatable("test.error.unexpected_block", block.getName()));
    }

    public void assertBlockTag(TagKey<Block> tagKey, BlockPos blockPos) {
        this.assertBlockState(blockPos, blockState -> blockState.is(tagKey), blockState -> Component.translatable("test.error.expected_block_tag", Component.translationArg(tagKey.location()), blockState.getBlock().getName()));
    }

    public void succeedWhenBlockPresent(Block block, int n, int n2, int n3) {
        this.succeedWhenBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void succeedWhenBlockPresent(Block block, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertBlockPresent(block, blockPos));
    }

    public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, Function<Block, Component> function) {
        this.assertBlockState(blockPos, blockState -> predicate.test(blockState.getBlock()), blockState -> (Component)function.apply(blockState.getBlock()));
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, T t) {
        BlockState blockState = this.getBlockState(blockPos);
        boolean bl = blockState.hasProperty(property);
        if (!bl) {
            throw this.assertionException(blockPos, "test.error.block_property_missing", property.getName(), t);
        }
        if (!blockState.getValue(property).equals(t)) {
            throw this.assertionException(blockPos, "test.error.block_property_mismatch", property.getName(), t, blockState.getValue(property));
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, Predicate<T> predicate, Component component) {
        this.assertBlockState(blockPos, blockState -> {
            if (!blockState.hasProperty(property)) {
                return false;
            }
            Object t = blockState.getValue(property);
            return predicate.test(t);
        }, blockState -> component);
    }

    public void assertBlockState(BlockPos blockPos, BlockState blockState) {
        BlockState blockState2 = this.getBlockState(blockPos);
        if (!blockState2.equals(blockState)) {
            throw this.assertionException(blockPos, "test.error.state_not_equal", blockState, blockState2);
        }
    }

    public void assertBlockState(BlockPos blockPos, Predicate<BlockState> predicate, Function<BlockState, Component> function) {
        BlockState blockState = this.getBlockState(blockPos);
        if (!predicate.test(blockState)) {
            throw this.assertionException(blockPos, function.apply(blockState));
        }
    }

    public <T extends BlockEntity> void assertBlockEntityData(BlockPos blockPos, Class<T> clazz, Predicate<T> predicate, Supplier<Component> supplier) {
        T t = this.getBlockEntity(blockPos, clazz);
        if (!predicate.test(t)) {
            throw this.assertionException(blockPos, supplier.get());
        }
    }

    public void assertRedstoneSignal(BlockPos blockPos, Direction direction, IntPredicate intPredicate, Supplier<Component> supplier) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        BlockState blockState = serverLevel.getBlockState(blockPos2);
        int n = blockState.getSignal(serverLevel, blockPos2, direction);
        if (!intPredicate.test(n)) {
            throw this.assertionException(blockPos, supplier.get());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_in_test", entityType.getDescription());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, AABB aABB) {
        AABB aABB2 = this.absoluteAABB(aABB);
        List<Entity> list = this.getLevel().getEntities(entityType, aABB2, Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException(BlockPos.containing(aABB.getCenter()), "test.error.expected_entity", entityType.getDescription());
        }
    }

    public void assertEntitiesPresent(EntityType<?> entityType, int n) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (list.size() != n) {
            throw this.assertionException("test.error.expected_entity_count", n, entityType.getDescription(), list.size());
        }
    }

    public void assertEntitiesPresent(EntityType<?> entityType, BlockPos blockPos, int n, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<?> list = this.getEntities(entityType, blockPos, d);
        if (list.size() != n) {
            throw this.assertionException(blockPos, "test.error.expected_entity_count", n, entityType.getDescription(), list.size());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos, double d) {
        List<?> list = this.getEntities(entityType, blockPos, d);
        if (list.isEmpty()) {
            BlockPos blockPos2 = this.absolutePos(blockPos);
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> entityType, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        return this.getLevel().getEntities(entityType, new AABB(blockPos2).inflate(d), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> entityType) {
        return this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity entity, int n, int n2, int n3) {
        this.assertEntityInstancePresent(entity, new BlockPos(n, n2, n3));
    }

    public void assertEntityInstancePresent(Entity entity, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entity.getType(), new AABB(blockPos2), Entity::isAlive);
        list.stream().filter(entity2 -> entity2 == entity).findFirst().orElseThrow(() -> this.assertionException(blockPos, "test.error.expected_entity", entity.getType().getDescription()));
    }

    public void assertItemEntityCountIs(Item item, BlockPos blockPos, double d, int n) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        int n2 = 0;
        for (ItemEntity itemEntity : list) {
            ItemStack itemStack = itemEntity.getItem();
            if (!itemStack.is(item)) continue;
            n2 += itemStack.getCount();
        }
        if (n2 != n) {
            throw this.assertionException(blockPos, "test.error.expected_items_count", n, item.getName(), n2);
        }
    }

    public void assertItemEntityPresent(Item item, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            return;
        }
        throw this.assertionException(blockPos, "test.error.expected_item", item.getName());
    }

    public void assertItemEntityNotPresent(Item item, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            throw this.assertionException(blockPos, "test.error.unexpected_item", item.getName());
        }
    }

    public void assertItemEntityPresent(Item item) {
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            return;
        }
        throw this.assertionException("test.error.expected_item", item.getName());
    }

    public void assertItemEntityNotPresent(Item item) {
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            throw this.assertionException("test.error.unexpected_item", item.getName());
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.assertionException(list.getFirst().blockPosition(), "test.error.unexpected_entity", entityType.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.unexpected_entity", entityType.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType, AABB aABB) {
        AABB aABB2 = this.absoluteAABB(aABB);
        List<Entity> list = this.getLevel().getEntities(entityType, aABB2, Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.assertionException(list.getFirst().blockPosition(), "test.error.unexpected_entity", entityType.getDescription());
        }
    }

    public void assertEntityTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_touching", entityType.getDescription(), vec32.x(), vec32.y(), vec32.z(), d, d2, d3);
        }
    }

    public void assertEntityNotTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_not_touching", entityType.getDescription(), vec32.x(), vec32.y(), vec32.z(), d, d2, d3);
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos blockPos, EntityType<E> entityType, Predicate<E> predicate) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
        for (Entity entity : list) {
            if (predicate.test(entity)) continue;
            throw this.assertionException(entity.blockPosition(), "test.error.expected_entity_data_predicate", entity.getName());
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos blockPos, EntityType<E> entityType, Function<? super E, T> function, @Nullable T t) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
        for (Entity entity : list) {
            T t2 = function.apply(entity);
            if (Objects.equals(t2, t)) continue;
            throw this.assertionException(blockPos, "test.error.expected_entity_data", t, t2);
        }
    }

    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos blockPos, EntityType<E> entityType, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<LivingEntity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
        for (LivingEntity livingEntity : list) {
            if (!livingEntity.isHolding(item)) continue;
            return;
        }
        throw this.assertionException(blockPos, "test.error.expected_entity_holding", item.getName());
    }

    public <E extends Entity> void assertEntityInventoryContains(BlockPos blockPos, EntityType<E> entityType, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), object -> ((Entity)object).isAlive());
        if (list.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_entity", entityType.getDescription());
        }
        for (Entity entity : list) {
            if (!((InventoryCarrier)((Object)entity)).getInventory().hasAnyMatching(itemStack -> itemStack.is(item))) continue;
            return;
        }
        throw this.assertionException(blockPos, "test.error.expected_entity_having", item.getName());
    }

    public void assertContainerEmpty(BlockPos blockPos) {
        BaseContainerBlockEntity baseContainerBlockEntity = this.getBlockEntity(blockPos, BaseContainerBlockEntity.class);
        if (!baseContainerBlockEntity.isEmpty()) {
            throw this.assertionException(blockPos, "test.error.expected_empty_container", new Object[0]);
        }
    }

    public void assertContainerContainsSingle(BlockPos blockPos, Item item) {
        BaseContainerBlockEntity baseContainerBlockEntity = this.getBlockEntity(blockPos, BaseContainerBlockEntity.class);
        if (baseContainerBlockEntity.countItem(item) != 1) {
            throw this.assertionException(blockPos, "test.error.expected_container_contents_single", item.getName());
        }
    }

    public void assertContainerContains(BlockPos blockPos, Item item) {
        BaseContainerBlockEntity baseContainerBlockEntity = this.getBlockEntity(blockPos, BaseContainerBlockEntity.class);
        if (baseContainerBlockEntity.countItem(item) == 0) {
            throw this.assertionException(blockPos, "test.error.expected_container_contents", item.getName());
        }
    }

    public void assertSameBlockStates(BoundingBox boundingBox, BlockPos blockPos) {
        BlockPos.betweenClosedStream(boundingBox).forEach(blockPos2 -> {
            BlockPos blockPos3 = blockPos.offset(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
            this.assertSameBlockState((BlockPos)blockPos2, blockPos3);
        });
    }

    public void assertSameBlockState(BlockPos blockPos, BlockPos blockPos2) {
        BlockState blockState;
        BlockState blockState2 = this.getBlockState(blockPos);
        if (blockState2 != (blockState = this.getBlockState(blockPos2))) {
            throw this.assertionException(blockPos, "test.error.state_not_equal", blockState, blockState2);
        }
    }

    public void assertAtTickTimeContainerContains(long l, BlockPos blockPos, Item item) {
        this.runAtTickTime(l, () -> this.assertContainerContainsSingle(blockPos, item));
    }

    public void assertAtTickTimeContainerEmpty(long l, BlockPos blockPos) {
        this.runAtTickTime(l, () -> this.assertContainerEmpty(blockPos));
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPos blockPos, EntityType<E> entityType, Function<E, T> function, T t) {
        this.succeedWhen(() -> this.assertEntityData(blockPos, entityType, function, t));
    }

    public void assertEntityPosition(Entity entity, AABB aABB, Component component) {
        if (!aABB.contains(this.relativeVec(entity.position()))) {
            throw this.assertionException(component);
        }
    }

    public <E extends Entity> void assertEntityProperty(E e, Predicate<E> predicate, Component component) {
        if (!predicate.test(e)) {
            throw this.assertionException(e.blockPosition(), "test.error.entity_property", e.getName(), component);
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E e, Function<E, T> function, T t, Component component) {
        T t2 = function.apply(e);
        if (!t2.equals(t)) {
            throw this.assertionException(e.blockPosition(), "test.error.entity_property_details", e.getName(), component, t2, t);
        }
    }

    public void assertLivingEntityHasMobEffect(LivingEntity livingEntity, Holder<MobEffect> holder, int n) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(holder);
        if (mobEffectInstance == null || mobEffectInstance.getAmplifier() != n) {
            throw this.assertionException("test.error.expected_entity_effect", livingEntity.getName(), PotionContents.getPotionDescription(holder, n));
        }
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityPresent(entityType, blockPos));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityNotPresent(entityType, blockPos));
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.finalCheckAdded = true;
    }

    public void succeedIf(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
    }

    public void succeedWhen(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
    }

    public void succeedOnTickWhen(int n, Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(n, runnable).thenSucceed();
    }

    public void runAtTickTime(long l, Runnable runnable) {
        this.testInfo.setRunAtTickTime(l, runnable);
    }

    public void runAfterDelay(long l, Runnable runnable) {
        this.runAtTickTime((long)this.testInfo.getTick() + l, runnable);
    }

    public void randomTick(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.getBlockState(blockPos2).randomTick(serverLevel, blockPos2, serverLevel.random);
    }

    public void tickBlock(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.getBlockState(blockPos2).tick(serverLevel, blockPos2, serverLevel.random);
    }

    public void tickPrecipitation(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.tickPrecipitation(blockPos2);
    }

    public void tickPrecipitation() {
        AABB aABB = this.getRelativeBounds();
        int n = (int)Math.floor(aABB.maxX);
        int n2 = (int)Math.floor(aABB.maxZ);
        int n3 = (int)Math.floor(aABB.maxY);
        for (int i = (int)Math.floor(aABB.minX); i < n; ++i) {
            for (int j = (int)Math.floor(aABB.minZ); j < n2; ++j) {
                this.tickPrecipitation(new BlockPos(i, n3, j));
            }
        }
    }

    public int getHeight(Heightmap.Types types, int n, int n2) {
        BlockPos blockPos = this.absolutePos(new BlockPos(n, 0, n2));
        return this.relativePos(this.getLevel().getHeightmapPos(types, blockPos)).getY();
    }

    public void fail(Component component, BlockPos blockPos) {
        throw this.assertionException(blockPos, component);
    }

    public void fail(Component component, Entity entity) {
        throw this.assertionException(entity.blockPosition(), component);
    }

    public void fail(Component component) {
        throw this.assertionException(component);
    }

    public void failIf(Runnable runnable) {
        this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> this.assertionException("test.error.fail", new Object[0]));
    }

    public void failIfEver(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }

    public GameTestSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPos absolutePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getTestOrigin();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        return StructureTemplate.transform(blockPos3, Mirror.NONE, this.testInfo.getRotation(), blockPos2);
    }

    public BlockPos relativePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getTestOrigin();
        Rotation rotation = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
        BlockPos blockPos3 = StructureTemplate.transform(blockPos, Mirror.NONE, rotation, blockPos2);
        return blockPos3.subtract(blockPos2);
    }

    public AABB absoluteAABB(AABB aABB) {
        Vec3 vec3 = this.absoluteVec(aABB.getMinPosition());
        Vec3 vec32 = this.absoluteVec(aABB.getMaxPosition());
        return new AABB(vec3, vec32);
    }

    public AABB relativeAABB(AABB aABB) {
        Vec3 vec3 = this.relativeVec(aABB.getMinPosition());
        Vec3 vec32 = this.relativeVec(aABB.getMaxPosition());
        return new AABB(vec3, vec32);
    }

    public Vec3 absoluteVec(Vec3 vec3) {
        Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getTestOrigin());
        return StructureTemplate.transform(vec32.add(vec3), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public Vec3 relativeVec(Vec3 vec3) {
        Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getTestOrigin());
        return StructureTemplate.transform(vec3.subtract(vec32), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public Rotation getTestRotation() {
        return this.testInfo.getRotation();
    }

    public void assertTrue(boolean bl, Component component) {
        if (!bl) {
            throw this.assertionException(component);
        }
    }

    public <N> void assertValueEqual(N n, N n2, Component component) {
        if (!n.equals(n2)) {
            throw this.assertionException("test.error.value_not_equal", component, n, n2);
        }
    }

    public void assertFalse(boolean bl, Component component) {
        this.assertTrue(!bl, component);
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    public AABB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AABB getRelativeBounds() {
        AABB aABB = this.testInfo.getStructureBounds();
        Rotation rotation = this.testInfo.getRotation();
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new AABB(0.0, 0.0, 0.0, aABB.getZsize(), aABB.getYsize(), aABB.getXsize());
            }
        }
        return new AABB(0.0, 0.0, 0.0, aABB.getXsize(), aABB.getYsize(), aABB.getZsize());
    }

    public void forEveryBlockInStructure(Consumer<BlockPos> consumer) {
        AABB aABB = this.getRelativeBounds().contract(1.0, 1.0, 1.0);
        BlockPos.MutableBlockPos.betweenClosedStream(aABB).forEach(consumer);
    }

    public void onEachTick(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }

    public void placeAt(Player player, ItemStack itemStack, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = this.absolutePos(blockPos.relative(direction));
        BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(blockPos2), direction, blockPos2, false);
        UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockHitResult);
        itemStack.useOn(useOnContext);
    }

    public void setBiome(ResourceKey<Biome> resourceKey) {
        AABB aABB = this.getBounds();
        BlockPos blockPos = BlockPos.containing(aABB.minX, aABB.minY, aABB.minZ);
        BlockPos blockPos2 = BlockPos.containing(aABB.maxX, aABB.maxY, aABB.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(this.getLevel(), blockPos, blockPos2, this.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(resourceKey));
        if (either.right().isPresent()) {
            throw this.assertionException("test.error.set_biome", new Object[0]);
        }
    }
}

