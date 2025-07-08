/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final BuiltInMetadata metadata;
    private final Set<String> namespaces;
    private final List<Path> rootPaths;
    private final Map<PackType, List<Path>> pathsForType;

    VanillaPackResources(PackLocationInfo packLocationInfo, BuiltInMetadata builtInMetadata, Set<String> set, List<Path> list, Map<PackType, List<Path>> map) {
        this.location = packLocationInfo;
        this.metadata = builtInMetadata;
        this.namespaces = set;
        this.rootPaths = list;
        this.pathsForType = map;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... stringArray) {
        FileUtil.validatePath(stringArray);
        List<String> list = List.of(stringArray);
        for (Path path : this.rootPaths) {
            Path path2 = FileUtil.resolvePath(path, list);
            if (!Files.exists(path2, new LinkOption[0]) || !PathPackResources.validatePath(path2)) continue;
            return IoSupplier.create(path2);
        }
        return null;
    }

    public void listRawPaths(PackType packType, ResourceLocation resourceLocation, Consumer<Path> consumer) {
        FileUtil.decomposePath(resourceLocation.getPath()).ifSuccess(list -> {
            String string = resourceLocation.getNamespace();
            for (Path path : this.pathsForType.get((Object)packType)) {
                Path path2 = path.resolve(string);
                consumer.accept(FileUtil.resolvePath(path2, list));
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)resourceLocation, (Object)error.message()));
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        FileUtil.decomposePath(string2).ifSuccess(list -> {
            List<Path> list2 = this.pathsForType.get((Object)packType);
            int n = list2.size();
            if (n == 1) {
                VanillaPackResources.getResources(resourceOutput, string, list2.get(0), list);
            } else if (n > 1) {
                HashMap<ResourceLocation, IoSupplier<InputStream>> hashMap = new HashMap<ResourceLocation, IoSupplier<InputStream>>();
                for (int i = 0; i < n - 1; ++i) {
                    VanillaPackResources.getResources(hashMap::putIfAbsent, string, list2.get(i), list);
                }
                Path path = list2.get(n - 1);
                if (hashMap.isEmpty()) {
                    VanillaPackResources.getResources(resourceOutput, string, path, list);
                } else {
                    VanillaPackResources.getResources(hashMap::putIfAbsent, string, path, list);
                    hashMap.forEach(resourceOutput);
                }
            }
        }).ifError(error -> LOGGER.error("Invalid path {}: {}", (Object)string2, (Object)error.message()));
    }

    private static void getResources(PackResources.ResourceOutput resourceOutput, String string, Path path, List<String> list) {
        Path path2 = path.resolve(string);
        PathPackResources.listPath(string, path2, list, resourceOutput);
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return (IoSupplier)FileUtil.decomposePath(resourceLocation.getPath()).mapOrElse(list -> {
            String string = resourceLocation.getNamespace();
            for (Path path : this.pathsForType.get((Object)packType)) {
                Path path2 = FileUtil.resolvePath(path.resolve(string), list);
                if (!Files.exists(path2, new LinkOption[0]) || !PathPackResources.validatePath(path2)) continue;
                return IoSupplier.create(path2);
            }
            return null;
        }, error -> {
            LOGGER.error("Invalid path {}: {}", (Object)resourceLocation, (Object)error.message());
            return null;
        });
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return this.namespaces;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionType<T> metadataSectionType) {
        IoSupplier<InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
        if (ioSupplier == null) return this.metadata.get(metadataSectionType);
        try (InputStream inputStream = ioSupplier.get();){
            T t2 = AbstractPackResources.getMetadataFromStream(metadataSectionType, inputStream);
            if (t2 == null) return this.metadata.get(metadataSectionType);
            T t = t2;
            return t;
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return this.metadata.get(metadataSectionType);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {
    }

    public ResourceProvider asProvider() {
        return resourceLocation -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, resourceLocation)).map(ioSupplier -> new Resource(this, (IoSupplier<InputStream>)ioSupplier));
    }
}

