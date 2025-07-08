/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ServerAdvancementManager
extends SimpleJsonResourceReloadListener<Advancement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<ResourceLocation, AdvancementHolder> advancements = Map.of();
    private AdvancementTree tree = new AdvancementTree();
    private final HolderLookup.Provider registries;

    public ServerAdvancementManager(HolderLookup.Provider provider) {
        super(provider, Advancement.CODEC, Registries.ADVANCEMENT);
        this.registries = provider;
    }

    @Override
    protected void apply(Map<ResourceLocation, Advancement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        map.forEach((resourceLocation, advancement) -> {
            this.validate((ResourceLocation)resourceLocation, (Advancement)advancement);
            builder.put(resourceLocation, (Object)new AdvancementHolder((ResourceLocation)resourceLocation, (Advancement)advancement));
        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementTree = new AdvancementTree();
        advancementTree.addAll(this.advancements.values());
        for (AdvancementNode advancementNode : advancementTree.roots()) {
            if (!advancementNode.holder().value().display().isPresent()) continue;
            TreeNodePosition.run(advancementNode);
        }
        this.tree = advancementTree;
    }

    private void validate(ResourceLocation resourceLocation, Advancement advancement) {
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        advancement.validate(collector, this.registries);
        if (!collector.isEmpty()) {
            LOGGER.warn("Found validation problems in advancement {}: \n{}", (Object)resourceLocation, (Object)collector.getReport());
        }
    }

    @Nullable
    public AdvancementHolder get(ResourceLocation resourceLocation) {
        return this.advancements.get(resourceLocation);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}

