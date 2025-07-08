/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.language;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.resources.language.FormattedBidiReorder;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

public class ClientLanguage
extends Language {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, String> storage;
    private final boolean defaultRightToLeft;

    private ClientLanguage(Map<String, String> map, boolean bl) {
        this.storage = map;
        this.defaultRightToLeft = bl;
    }

    public static ClientLanguage loadFrom(ResourceManager resourceManager, List<String> list, boolean bl) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (String string : list) {
            String string2 = String.format(Locale.ROOT, "lang/%s.json", string);
            for (String string3 : resourceManager.getNamespaces()) {
                try {
                    ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(string3, string2);
                    ClientLanguage.appendFrom(string, resourceManager.getResourceStack(resourceLocation), hashMap);
                }
                catch (Exception exception) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", new Object[]{string3, string2, exception.toString()});
                }
            }
        }
        DeprecatedTranslationsInfo.loadFromDefaultResource().applyToMap(hashMap);
        return new ClientLanguage(Map.copyOf(hashMap), bl);
    }

    private static void appendFrom(String string, List<Resource> list, Map<String, String> map) {
        for (Resource resource : list) {
            try {
                InputStream inputStream = resource.open();
                try {
                    Language.loadFromJson(inputStream, map::put);
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to load translations for {} from pack {}", new Object[]{string, resource.sourcePackId(), iOException});
            }
        }
    }

    @Override
    public String getOrDefault(String string, String string2) {
        return this.storage.getOrDefault(string, string2);
    }

    @Override
    public boolean has(String string) {
        return this.storage.containsKey(string);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return this.defaultRightToLeft;
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
        return FormattedBidiReorder.reorder(formattedText, this.defaultRightToLeft);
    }
}

