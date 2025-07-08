/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import org.slf4j.Logger;

public class GpuWarnlistManager
extends SimplePreparableReloadListener<Preparations> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation GPU_WARNLIST_LOCATION = ResourceLocation.withDefaultNamespace("gpu_warnlist.json");
    private ImmutableMap<String, String> warnings = ImmutableMap.of();
    private boolean showWarning;
    private boolean warningDismissed;
    private boolean skipFabulous;

    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }

    public boolean willShowWarning() {
        return this.hasWarnings() && !this.warningDismissed;
    }

    public void showWarning() {
        this.showWarning = true;
    }

    public void dismissWarning() {
        this.warningDismissed = true;
    }

    public void dismissWarningAndSkipFabulous() {
        this.warningDismissed = true;
        this.skipFabulous = true;
    }

    public boolean isShowingWarning() {
        return this.showWarning && !this.warningDismissed;
    }

    public boolean isSkippingFabulous() {
        return this.skipFabulous;
    }

    public void resetWarnings() {
        this.showWarning = false;
        this.warningDismissed = false;
        this.skipFabulous = false;
    }

    @Nullable
    public String getRendererWarnings() {
        return (String)this.warnings.get((Object)"renderer");
    }

    @Nullable
    public String getVersionWarnings() {
        return (String)this.warnings.get((Object)"version");
    }

    @Nullable
    public String getVendorWarnings() {
        return (String)this.warnings.get((Object)"vendor");
    }

    @Nullable
    public String getAllWarnings() {
        StringBuilder stringBuilder = new StringBuilder();
        this.warnings.forEach((string, string2) -> stringBuilder.append((String)string).append(": ").append((String)string2));
        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }

    @Override
    protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        ArrayList arrayList3 = Lists.newArrayList();
        JsonObject jsonObject = GpuWarnlistManager.parseJson(resourceManager, profilerFiller);
        if (jsonObject != null) {
            try (Zone zone = profilerFiller.zone("compile_regex");){
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("renderer"), arrayList);
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("version"), arrayList2);
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("vendor"), arrayList3);
            }
        }
        return new Preparations(arrayList, arrayList2, arrayList3);
    }

    @Override
    protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.warnings = preparations.apply();
    }

    private static void compilePatterns(JsonArray jsonArray, List<Pattern> list) {
        jsonArray.forEach(jsonElement -> list.add(Pattern.compile(jsonElement.getAsString(), 2)));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private static JsonObject parseJson(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try (Zone zone = profilerFiller.zone("parse_json");){
            JsonObject jsonObject;
            block14: {
                BufferedReader bufferedReader = resourceManager.openAsReader(GPU_WARNLIST_LOCATION);
                try {
                    jsonObject = StrictJsonParser.parse(bufferedReader).getAsJsonObject();
                    if (bufferedReader == null) break block14;
                }
                catch (Throwable throwable) {
                    if (bufferedReader != null) {
                        try {
                            ((Reader)bufferedReader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ((Reader)bufferedReader).close();
            }
            return jsonObject;
        }
        catch (JsonSyntaxException | IOException throwable) {
            LOGGER.warn("Failed to load GPU warnlist", throwable);
            return null;
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    protected static final class Preparations {
        private final List<Pattern> rendererPatterns;
        private final List<Pattern> versionPatterns;
        private final List<Pattern> vendorPatterns;

        Preparations(List<Pattern> list, List<Pattern> list2, List<Pattern> list3) {
            this.rendererPatterns = list;
            this.versionPatterns = list2;
            this.vendorPatterns = list3;
        }

        private static String matchAny(List<Pattern> list, String string) {
            ArrayList arrayList = Lists.newArrayList();
            for (Pattern pattern : list) {
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    arrayList.add(matcher.group());
                }
            }
            return String.join((CharSequence)", ", arrayList);
        }

        ImmutableMap<String, String> apply() {
            ImmutableMap.Builder builder = new ImmutableMap.Builder();
            GpuDevice gpuDevice = RenderSystem.getDevice();
            if (gpuDevice.getBackendName().equals("OpenGL")) {
                String string;
                String string2;
                String string3 = Preparations.matchAny(this.rendererPatterns, gpuDevice.getRenderer());
                if (!string3.isEmpty()) {
                    builder.put((Object)"renderer", (Object)string3);
                }
                if (!(string2 = Preparations.matchAny(this.versionPatterns, gpuDevice.getVersion())).isEmpty()) {
                    builder.put((Object)"version", (Object)string2);
                }
                if (!(string = Preparations.matchAny(this.vendorPatterns, gpuDevice.getVendor())).isEmpty()) {
                    builder.put((Object)"vendor", (Object)string);
                }
            }
            return builder.build();
        }
    }
}

