/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BuiltInPackSource
implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_ID = "vanilla";
    public static final String TESTS_ID = "tests";
    public static final KnownPack CORE_PACK_INFO = KnownPack.vanilla("core");
    private final PackType packType;
    private final VanillaPackResources vanillaPack;
    private final ResourceLocation packDir;
    private final DirectoryValidator validator;

    public BuiltInPackSource(PackType packType, VanillaPackResources vanillaPackResources, ResourceLocation resourceLocation, DirectoryValidator directoryValidator) {
        this.packType = packType;
        this.vanillaPack = vanillaPackResources;
        this.packDir = resourceLocation;
        this.validator = directoryValidator;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        Pack pack = this.createVanillaPack(this.vanillaPack);
        if (pack != null) {
            consumer.accept(pack);
        }
        this.listBundledPacks(consumer);
    }

    @Nullable
    protected abstract Pack createVanillaPack(PackResources var1);

    protected abstract Component getPackTitle(String var1);

    public VanillaPackResources getVanillaPack() {
        return this.vanillaPack;
    }

    private void listBundledPacks(Consumer<Pack> consumer) {
        HashMap<String, Function> hashMap = new HashMap<String, Function>();
        this.populatePackList(hashMap::put);
        hashMap.forEach((string, function) -> {
            Pack pack = (Pack)function.apply(string);
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    protected void populatePackList(BiConsumer<String, Function<String, Pack>> biConsumer) {
        this.vanillaPack.listRawPaths(this.packType, this.packDir, path -> this.discoverPacksInPath((Path)path, biConsumer));
    }

    protected void discoverPacksInPath(@Nullable Path path2, BiConsumer<String, Function<String, Pack>> biConsumer) {
        if (path2 != null && Files.isDirectory(path2, new LinkOption[0])) {
            try {
                FolderRepositorySource.discoverPacks(path2, this.validator, (path, resourcesSupplier) -> biConsumer.accept(BuiltInPackSource.pathToId(path), string -> this.createBuiltinPack((String)string, (Pack.ResourcesSupplier)resourcesSupplier, this.getPackTitle((String)string))));
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to discover packs in {}", (Object)path2, (Object)iOException);
            }
        }
    }

    private static String pathToId(Path path) {
        return StringUtils.removeEnd((String)path.getFileName().toString(), (String)".zip");
    }

    @Nullable
    protected abstract Pack createBuiltinPack(String var1, Pack.ResourcesSupplier var2, Component var3);

    protected static Pack.ResourcesSupplier fixedResources(final PackResources packResources) {
        return new Pack.ResourcesSupplier(){

            @Override
            public PackResources openPrimary(PackLocationInfo packLocationInfo) {
                return packResources;
            }

            @Override
            public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
                return packResources;
            }
        };
    }
}

