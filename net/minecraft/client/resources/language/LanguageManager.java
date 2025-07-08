/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("US", "English", false);
    private Map<String, LanguageInfo> languages = ImmutableMap.of((Object)"en_us", (Object)DEFAULT_LANGUAGE);
    private String currentCode;
    private final Consumer<ClientLanguage> reloadCallback;

    public LanguageManager(String string, Consumer<ClientLanguage> consumer) {
        this.currentCode = string;
        this.reloadCallback = consumer;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> stream) {
        HashMap hashMap = Maps.newHashMap();
        stream.forEach(packResources -> {
            try {
                LanguageMetadataSection languageMetadataSection = packResources.getMetadataSection(LanguageMetadataSection.TYPE);
                if (languageMetadataSection != null) {
                    languageMetadataSection.languages().forEach(hashMap::putIfAbsent);
                }
            }
            catch (IOException | RuntimeException exception) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)packResources.packId(), (Object)exception);
            }
        });
        return ImmutableMap.copyOf((Map)hashMap);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Object object;
        this.languages = LanguageManager.extractLanguages(resourceManager.listPacks());
        ArrayList<String> arrayList = new ArrayList<String>(2);
        boolean bl = DEFAULT_LANGUAGE.bidirectional();
        arrayList.add("en_us");
        if (!this.currentCode.equals("en_us") && (object = this.languages.get(this.currentCode)) != null) {
            arrayList.add(this.currentCode);
            bl = ((LanguageInfo)object).bidirectional();
        }
        object = ClientLanguage.loadFrom(resourceManager, arrayList, bl);
        I18n.setLanguage((Language)object);
        Language.inject((Language)object);
        this.reloadCallback.accept((ClientLanguage)object);
    }

    public void setSelected(String string) {
        this.currentCode = string;
    }

    public String getSelected() {
        return this.currentCode;
    }

    public SortedMap<String, LanguageInfo> getLanguages() {
        return new TreeMap<String, LanguageInfo>(this.languages);
    }

    @Nullable
    public LanguageInfo getLanguage(String string) {
        return this.languages.get(string);
    }
}

