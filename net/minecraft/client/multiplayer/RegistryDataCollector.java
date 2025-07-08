/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;

public class RegistryDataCollector {
    @Nullable
    private ContentsCollector contentsCollector;
    @Nullable
    private TagCollector tagCollector;

    public void appendContents(ResourceKey<? extends Registry<?>> resourceKey, List<RegistrySynchronization.PackedRegistryEntry> list) {
        if (this.contentsCollector == null) {
            this.contentsCollector = new ContentsCollector();
        }
        this.contentsCollector.append(resourceKey, list);
    }

    public void appendTags(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
        if (this.tagCollector == null) {
            this.tagCollector = new TagCollector();
        }
        map.forEach(this.tagCollector::append);
    }

    private static <T> Registry.PendingTags<T> resolveRegistryTags(RegistryAccess.Frozen frozen, ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
        HolderLookup.RegistryLookup registryLookup = frozen.lookupOrThrow((ResourceKey)resourceKey);
        return registryLookup.prepareTagReload(networkPayload.resolve(registryLookup));
    }

    private RegistryAccess loadNewElementsAndTags(ResourceProvider resourceProvider, ContentsCollector contentsCollector, boolean bl) {
        RegistryAccess.Frozen frozen;
        LayeredRegistryAccess<ClientRegistryLayer> layeredRegistryAccess = ClientRegistryLayer.createRegistryAccess();
        RegistryAccess.Frozen frozen2 = layeredRegistryAccess.getAccessForLoading(ClientRegistryLayer.REMOTE);
        HashMap hashMap = new HashMap();
        contentsCollector.elements.forEach((resourceKey, list) -> hashMap.put((ResourceKey<? extends Registry<?>>)resourceKey, new RegistryDataLoader.NetworkedRegistryData((List<RegistrySynchronization.PackedRegistryEntry>)list, TagNetworkSerialization.NetworkPayload.EMPTY)));
        ArrayList arrayList = new ArrayList();
        if (this.tagCollector != null) {
            this.tagCollector.forEach((resourceKey2, networkPayload) -> {
                if (networkPayload.isEmpty()) {
                    return;
                }
                if (RegistrySynchronization.isNetworkable(resourceKey2)) {
                    hashMap.compute((ResourceKey<? extends Registry<?>>)resourceKey2, (resourceKey, networkedRegistryData) -> {
                        List<RegistrySynchronization.PackedRegistryEntry> list = networkedRegistryData != null ? networkedRegistryData.elements() : List.of();
                        return new RegistryDataLoader.NetworkedRegistryData(list, (TagNetworkSerialization.NetworkPayload)networkPayload);
                    });
                } else if (!bl) {
                    arrayList.add(RegistryDataCollector.resolveRegistryTags(frozen2, resourceKey2, networkPayload));
                }
            });
        }
        List<HolderLookup.RegistryLookup<?>> list2 = TagLoader.buildUpdatedLookups(frozen2, arrayList);
        try {
            frozen = RegistryDataLoader.load(hashMap, resourceProvider, list2, RegistryDataLoader.SYNCHRONIZED_REGISTRIES).freeze();
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Network Registry Load");
            RegistryDataCollector.addCrashDetails(crashReport, hashMap, arrayList);
            throw new ReportedException(crashReport);
        }
        RegistryAccess.Frozen frozen3 = layeredRegistryAccess.replaceFrom(ClientRegistryLayer.REMOTE, frozen).compositeAccess();
        arrayList.forEach(Registry.PendingTags::apply);
        return frozen3;
    }

    private static void addCrashDetails(CrashReport crashReport, Map<ResourceKey<? extends Registry<?>>, RegistryDataLoader.NetworkedRegistryData> map, List<Registry.PendingTags<?>> list) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Received Elements and Tags");
        crashReportCategory.setDetail("Dynamic Registries", () -> map.entrySet().stream().sorted(Comparator.comparing(entry -> ((ResourceKey)entry.getKey()).location())).map(entry -> String.format(Locale.ROOT, "\n\t\t%s: elements=%d tags=%d", ((ResourceKey)entry.getKey()).location(), ((RegistryDataLoader.NetworkedRegistryData)entry.getValue()).elements().size(), ((RegistryDataLoader.NetworkedRegistryData)entry.getValue()).tags().size())).collect(Collectors.joining()));
        crashReportCategory.setDetail("Static Registries", () -> list.stream().sorted(Comparator.comparing(pendingTags -> pendingTags.key().location())).map(pendingTags -> String.format(Locale.ROOT, "\n\t\t%s: tags=%d", pendingTags.key().location(), pendingTags.size())).collect(Collectors.joining()));
    }

    private void loadOnlyTags(TagCollector tagCollector, RegistryAccess.Frozen frozen, boolean bl) {
        tagCollector.forEach((resourceKey, networkPayload) -> {
            if (bl || RegistrySynchronization.isNetworkable(resourceKey)) {
                RegistryDataCollector.resolveRegistryTags(frozen, resourceKey, networkPayload).apply();
            }
        });
    }

    public RegistryAccess.Frozen collectGameRegistries(ResourceProvider resourceProvider, RegistryAccess.Frozen frozen, boolean bl) {
        RegistryAccess registryAccess;
        if (this.contentsCollector != null) {
            registryAccess = this.loadNewElementsAndTags(resourceProvider, this.contentsCollector, bl);
        } else {
            if (this.tagCollector != null) {
                this.loadOnlyTags(this.tagCollector, frozen, !bl);
            }
            registryAccess = frozen;
        }
        return registryAccess.freeze();
    }

    static class ContentsCollector {
        final Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap();

        ContentsCollector() {
        }

        public void append(ResourceKey<? extends Registry<?>> resourceKey2, List<RegistrySynchronization.PackedRegistryEntry> list) {
            this.elements.computeIfAbsent(resourceKey2, resourceKey -> new ArrayList()).addAll(list);
        }
    }

    static class TagCollector {
        private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap();

        TagCollector() {
        }

        public void append(ResourceKey<? extends Registry<?>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
            this.tags.put(resourceKey, networkPayload);
        }

        public void forEach(BiConsumer<? super ResourceKey<? extends Registry<?>>, ? super TagNetworkSerialization.NetworkPayload> biConsumer) {
            this.tags.forEach(biConsumer);
        }
    }
}

