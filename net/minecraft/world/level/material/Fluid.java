/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid {
    public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper();
    protected final StateDefinition<Fluid, FluidState> stateDefinition;
    private FluidState defaultFluidState;
    private final Holder.Reference<Fluid> builtInRegistryHolder = BuiltInRegistries.FLUID.createIntrusiveHolder(this);

    protected Fluid() {
        StateDefinition.Builder<Fluid, FluidState> builder = new StateDefinition.Builder<Fluid, FluidState>(this);
        this.createFluidStateDefinition(builder);
        this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
    }

    public StateDefinition<Fluid, FluidState> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(FluidState fluidState) {
        this.defaultFluidState = fluidState;
    }

    public final FluidState defaultFluidState() {
        return this.defaultFluidState;
    }

    public abstract Item getBucket();

    protected void animateTick(Level level, BlockPos blockPos, FluidState fluidState, RandomSource randomSource) {
    }

    protected void tick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
    }

    protected void randomTick(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, RandomSource randomSource) {
    }

    protected void entityInside(Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
    }

    @Nullable
    protected ParticleOptions getDripParticle() {
        return null;
    }

    protected abstract boolean canBeReplacedWith(FluidState var1, BlockGetter var2, BlockPos var3, Fluid var4, Direction var5);

    protected abstract Vec3 getFlow(BlockGetter var1, BlockPos var2, FluidState var3);

    public abstract int getTickDelay(LevelReader var1);

    protected boolean isRandomlyTicking() {
        return false;
    }

    protected boolean isEmpty() {
        return false;
    }

    protected abstract float getExplosionResistance();

    public abstract float getHeight(FluidState var1, BlockGetter var2, BlockPos var3);

    public abstract float getOwnHeight(FluidState var1);

    protected abstract BlockState createLegacyBlock(FluidState var1);

    public abstract boolean isSource(FluidState var1);

    public abstract int getAmount(FluidState var1);

    public boolean isSame(Fluid fluid) {
        return fluid == this;
    }

    @Deprecated
    public boolean is(TagKey<Fluid> tagKey) {
        return this.builtInRegistryHolder.is(tagKey);
    }

    public abstract VoxelShape getShape(FluidState var1, BlockGetter var2, BlockPos var3);

    @Nullable
    public AABB getAABB(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (this.isEmpty()) {
            return null;
        }
        float f = fluidState.getHeight(blockGetter, blockPos);
        return new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), (double)blockPos.getX() + 1.0, (float)blockPos.getY() + f, (double)blockPos.getZ() + 1.0);
    }

    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    @Deprecated
    public Holder.Reference<Fluid> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }
}

