/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class OptimizeWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ToIntFunction<ResourceKey<Level>> DIMENSION_COLORS = (ToIntFunction)Util.make(new Reference2IntOpenHashMap(), reference2IntOpenHashMap -> {
        reference2IntOpenHashMap.put(Level.OVERWORLD, -13408734);
        reference2IntOpenHashMap.put(Level.NETHER, -10075085);
        reference2IntOpenHashMap.put(Level.END, -8943531);
        reference2IntOpenHashMap.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    @Nullable
    public static OptimizeWorldScreen create(Minecraft minecraft, BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
        WorldOpenFlows worldOpenFlows = minecraft.createWorldOpenFlows();
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        WorldStem worldStem = worldOpenFlows.loadWorldStem(levelStorageAccess.getDataTag(), false, packRepository);
        try {
            WorldData worldData = worldStem.worldData();
            RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
            levelStorageAccess.saveDataTag(frozen, worldData);
            OptimizeWorldScreen optimizeWorldScreen = new OptimizeWorldScreen(booleanConsumer, dataFixer, levelStorageAccess, worldData, bl, frozen);
            if (worldStem != null) {
                worldStem.close();
            }
            return optimizeWorldScreen;
        }
        catch (Throwable throwable) {
            try {
                if (worldStem != null) {
                    try {
                        worldStem.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)exception);
                return null;
            }
        }
    }

    private OptimizeWorldScreen(BooleanConsumer booleanConsumer, DataFixer dataFixer, LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, boolean bl, RegistryAccess registryAccess) {
        super(Component.translatable("optimizeWorld.title", worldData.getLevelSettings().levelName()));
        this.callback = booleanConsumer;
        this.upgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldData, registryAccess, bl, false);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.upgrader.cancel();
            this.callback.accept(false);
        }).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
    }

    @Override
    public void tick() {
        if (this.upgrader.isFinished()) {
            this.callback.accept(true);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.upgrader.cancel();
        this.upgrader.close();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, -1);
        int n3 = this.width / 2 - 150;
        int n4 = this.width / 2 + 150;
        int n5 = this.height / 4 + 100;
        int n6 = n5 + 10;
        guiGraphics.drawCenteredString(this.font, this.upgrader.getStatus(), this.width / 2, n5 - this.font.lineHeight - 2, -6250336);
        if (this.upgrader.getTotalChunks() > 0) {
            guiGraphics.fill(n3 - 1, n5 - 1, n4 + 1, n6 + 1, -16777216);
            guiGraphics.drawString(this.font, Component.translatable("optimizeWorld.info.converted", this.upgrader.getConverted()), n3, 40, -6250336);
            guiGraphics.drawString(this.font, Component.translatable("optimizeWorld.info.skipped", this.upgrader.getSkipped()), n3, 40 + this.font.lineHeight + 3, -6250336);
            guiGraphics.drawString(this.font, Component.translatable("optimizeWorld.info.total", this.upgrader.getTotalChunks()), n3, 40 + (this.font.lineHeight + 3) * 2, -6250336);
            int n7 = 0;
            for (ResourceKey<Level> object2 : this.upgrader.levels()) {
                int mutableComponent = Mth.floor(this.upgrader.dimensionProgress(object2) * (float)(n4 - n3));
                guiGraphics.fill(n3 + n7, n5, n3 + n7 + mutableComponent, n6, DIMENSION_COLORS.applyAsInt(object2));
                n7 += mutableComponent;
            }
            int n9 = this.upgrader.getConverted() + this.upgrader.getSkipped();
            MutableComponent mutableComponent = Component.translatable("optimizeWorld.progress.counter", n9, this.upgrader.getTotalChunks());
            MutableComponent mutableComponent2 = Component.translatable("optimizeWorld.progress.percentage", Mth.floor(this.upgrader.getProgress() * 100.0f));
            guiGraphics.drawCenteredString(this.font, mutableComponent, this.width / 2, n5 + 2 * this.font.lineHeight + 2, -6250336);
            guiGraphics.drawCenteredString(this.font, mutableComponent2, this.width / 2, n5 + (n6 - n5) / 2 - this.font.lineHeight / 2, -6250336);
        }
    }
}

