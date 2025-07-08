/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

public class KnownPacksManager {
    private final PackRepository repository = ServerPacksSource.createVanillaTrustedRepository();
    private final Map<KnownPack, String> knownPackToId;

    public KnownPacksManager() {
        this.repository.reload();
        ImmutableMap.Builder builder = ImmutableMap.builder();
        this.repository.getAvailablePacks().forEach(pack -> {
            PackLocationInfo packLocationInfo = pack.location();
            packLocationInfo.knownPackInfo().ifPresent(knownPack -> builder.put(knownPack, (Object)packLocationInfo.id()));
        });
        this.knownPackToId = builder.build();
    }

    public List<KnownPack> trySelectingPacks(List<KnownPack> list) {
        ArrayList<KnownPack> arrayList = new ArrayList<KnownPack>(list.size());
        ArrayList<String> arrayList2 = new ArrayList<String>(list.size());
        for (KnownPack knownPack : list) {
            String string = this.knownPackToId.get(knownPack);
            if (string == null) continue;
            arrayList2.add(string);
            arrayList.add(knownPack);
        }
        this.repository.setSelected(arrayList2);
        return arrayList;
    }

    public CloseableResourceManager createResourceManager() {
        List<PackResources> list = this.repository.openAllSelected();
        return new MultiPackResourceManager(PackType.SERVER_DATA, list);
    }
}

