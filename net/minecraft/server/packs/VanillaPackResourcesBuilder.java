/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.util.FileSystemUtil;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Consumer<VanillaPackResourcesBuilder> developmentConfig = vanillaPackResourcesBuilder -> {};
    private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = (Map)Util.make(() -> {
        Class<VanillaPackResources> clazz = VanillaPackResources.class;
        synchronized (VanillaPackResources.class) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (PackType packType : PackType.values()) {
                String string = "/" + packType.getDirectory() + "/.mcassetsroot";
                URL uRL = VanillaPackResources.class.getResource(string);
                if (uRL == null) {
                    LOGGER.error("File {} does not exist in classpath", (Object)string);
                    continue;
                }
                try {
                    URI uRI = uRL.toURI();
                    String string2 = uRI.getScheme();
                    if (!"jar".equals(string2) && !"file".equals(string2)) {
                        LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uRI);
                    }
                    Path path = FileSystemUtil.safeGetPath(uRI);
                    builder.put((Object)packType, (Object)path.getParent());
                }
                catch (Exception exception) {
                    LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception);
                }
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return builder.build();
        }
    });
    private final Set<Path> rootPaths = new LinkedHashSet<Path>();
    private final Map<PackType, Set<Path>> pathsForType = new EnumMap<PackType, Set<Path>>(PackType.class);
    private BuiltInMetadata metadata = BuiltInMetadata.of();
    private final Set<String> namespaces = new HashSet<String>();

    private boolean validateDirPath(Path path) {
        if (!Files.exists(path, new LinkOption[0])) {
            return false;
        }
        if (!Files.isDirectory(path, new LinkOption[0])) {
            throw new IllegalArgumentException("Path " + String.valueOf(path.toAbsolutePath()) + " is not directory");
        }
        return true;
    }

    private void pushRootPath(Path path) {
        if (this.validateDirPath(path)) {
            this.rootPaths.add(path);
        }
    }

    private void pushPathForType(PackType packType2, Path path) {
        if (this.validateDirPath(path)) {
            this.pathsForType.computeIfAbsent(packType2, packType -> new LinkedHashSet()).add(path);
        }
    }

    public VanillaPackResourcesBuilder pushJarResources() {
        ROOT_DIR_BY_TYPE.forEach((packType, path) -> {
            this.pushRootPath(path.getParent());
            this.pushPathForType((PackType)((Object)packType), (Path)path);
        });
        return this;
    }

    public VanillaPackResourcesBuilder pushClasspathResources(PackType packType, Class<?> clazz) {
        Enumeration<URL> enumeration = null;
        try {
            enumeration = clazz.getClassLoader().getResources(packType.getDirectory() + "/");
        }
        catch (IOException iOException) {
            // empty catch block
        }
        while (enumeration != null && enumeration.hasMoreElements()) {
            URL uRL = enumeration.nextElement();
            try {
                URI uRI = uRL.toURI();
                if (!"file".equals(uRI.getScheme())) continue;
                Path path = Paths.get(uRI);
                this.pushRootPath(path.getParent());
                this.pushPathForType(packType, path);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to extract path from {}", (Object)uRL, (Object)exception);
            }
        }
        return this;
    }

    public VanillaPackResourcesBuilder applyDevelopmentConfig() {
        developmentConfig.accept(this);
        return this;
    }

    public VanillaPackResourcesBuilder pushUniversalPath(Path path) {
        this.pushRootPath(path);
        for (PackType packType : PackType.values()) {
            this.pushPathForType(packType, path.resolve(packType.getDirectory()));
        }
        return this;
    }

    public VanillaPackResourcesBuilder pushAssetPath(PackType packType, Path path) {
        this.pushRootPath(path);
        this.pushPathForType(packType, path);
        return this;
    }

    public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata builtInMetadata) {
        this.metadata = builtInMetadata;
        return this;
    }

    public VanillaPackResourcesBuilder exposeNamespace(String ... stringArray) {
        this.namespaces.addAll(Arrays.asList(stringArray));
        return this;
    }

    public VanillaPackResources build(PackLocationInfo packLocationInfo) {
        return new VanillaPackResources(packLocationInfo, this.metadata, Set.copyOf(this.namespaces), VanillaPackResourcesBuilder.copyAndReverse(this.rootPaths), Util.makeEnumMap(PackType.class, packType -> VanillaPackResourcesBuilder.copyAndReverse(this.pathsForType.getOrDefault(packType, Set.of()))));
    }

    private static List<Path> copyAndReverse(Collection<Path> collection) {
        ArrayList<Path> arrayList = new ArrayList<Path>(collection);
        Collections.reverse(arrayList);
        return List.copyOf(arrayList);
    }
}

