/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public class AdventureModePredicate {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<AdventureModePredicate> CODEC = ExtraCodecs.compactListCodec(BlockPredicate.CODEC, ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf())).xmap(AdventureModePredicate::new, adventureModePredicate -> adventureModePredicate.predicates);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()), adventureModePredicate -> adventureModePredicate.predicates, AdventureModePredicate::new);
    public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
    public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
    private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
    private final List<BlockPredicate> predicates;
    @Nullable
    private List<Component> cachedTooltip;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public AdventureModePredicate(List<BlockPredicate> list) {
        this.predicates = list;
    }

    private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2, boolean bl) {
        if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
            return false;
        }
        if (!bl) {
            return true;
        }
        if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
            return true;
        }
        if (blockInWorld.getEntity() == null || blockInWorld2.getEntity() == null) {
            return false;
        }
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER);){
            RegistryAccess registryAccess = blockInWorld.getLevel().registryAccess();
            CompoundTag compoundTag = AdventureModePredicate.saveBlockEntity(blockInWorld.getEntity(), registryAccess, scopedCollector);
            CompoundTag compoundTag2 = AdventureModePredicate.saveBlockEntity(blockInWorld2.getEntity(), registryAccess, scopedCollector);
            boolean bl2 = Objects.equals(compoundTag, compoundTag2);
            return bl2;
        }
    }

    private static CompoundTag saveBlockEntity(BlockEntity blockEntity, RegistryAccess registryAccess, ProblemReporter problemReporter) {
        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(problemReporter.forChild(blockEntity.problemPath()), registryAccess);
        blockEntity.saveWithId(tagValueOutput);
        return tagValueOutput.buildResult();
    }

    public boolean test(BlockInWorld blockInWorld) {
        if (AdventureModePredicate.areSameBlocks(blockInWorld, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        }
        this.lastCheckedBlock = blockInWorld;
        this.checksBlockEntity = false;
        for (BlockPredicate blockPredicate : this.predicates) {
            if (!blockPredicate.matches(blockInWorld)) continue;
            this.checksBlockEntity |= blockPredicate.requiresNbt();
            this.lastResult = true;
            return true;
        }
        this.lastResult = false;
        return false;
    }

    private List<Component> tooltip() {
        if (this.cachedTooltip == null) {
            this.cachedTooltip = AdventureModePredicate.computeTooltip(this.predicates);
        }
        return this.cachedTooltip;
    }

    public void addToTooltip(Consumer<Component> consumer) {
        this.tooltip().forEach(consumer);
    }

    private static List<Component> computeTooltip(List<BlockPredicate> list) {
        for (BlockPredicate blockPredicate2 : list) {
            if (!blockPredicate2.blocks().isEmpty()) continue;
            return List.of(UNKNOWN_USE);
        }
        return list.stream().flatMap(blockPredicate -> blockPredicate.blocks().orElseThrow().stream()).distinct().map(holder -> ((Block)holder.value()).getName().withStyle(ChatFormatting.DARK_GRAY)).toList();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof AdventureModePredicate) {
            AdventureModePredicate adventureModePredicate = (AdventureModePredicate)object;
            return this.predicates.equals(adventureModePredicate.predicates);
        }
        return false;
    }

    public int hashCode() {
        return this.predicates.hashCode();
    }

    public String toString() {
        return "AdventureModePredicate{predicates=" + String.valueOf(this.predicates) + "}";
    }
}

