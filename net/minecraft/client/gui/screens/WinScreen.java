/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  org.apache.commons.lang3.StringUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WinScreen
extends Screen {
    private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/credits_vignette.png");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = String.valueOf(ChatFormatting.WHITE) + String.valueOf(ChatFormatting.OBFUSCATED) + String.valueOf(ChatFormatting.GREEN) + String.valueOf(ChatFormatting.AQUA);
    private static final float SPEEDUP_FACTOR = 5.0f;
    private static final float SPEEDUP_FACTOR_FAST = 15.0f;
    private static final ResourceLocation END_POEM_LOCATION = ResourceLocation.withDefaultNamespace("texts/end.txt");
    private static final ResourceLocation CREDITS_LOCATION = ResourceLocation.withDefaultNamespace("texts/credits.json");
    private static final ResourceLocation POSTCREDITS_LOCATION = ResourceLocation.withDefaultNamespace("texts/postcredits.txt");
    private final boolean poem;
    private final Runnable onFinished;
    private float scroll;
    private List<FormattedCharSequence> lines;
    private List<Component> narratorComponents;
    private IntSet centeredLines;
    private int totalScrollLength;
    private boolean speedupActive;
    private final IntSet speedupModifiers = new IntOpenHashSet();
    private float scrollSpeed;
    private final float unmodifiedScrollSpeed;
    private int direction;
    private final LogoRenderer logoRenderer = new LogoRenderer(false);

    public WinScreen(boolean bl, Runnable runnable) {
        super(GameNarrator.NO_TITLE);
        this.poem = bl;
        this.onFinished = runnable;
        this.unmodifiedScrollSpeed = !bl ? 0.75f : 0.5f;
        this.direction = 1;
        this.scrollSpeed = this.unmodifiedScrollSpeed;
    }

    private float calculateScrollSpeed() {
        if (this.speedupActive) {
            return this.unmodifiedScrollSpeed * (5.0f + (float)this.speedupModifiers.size() * 15.0f) * (float)this.direction;
        }
        return this.unmodifiedScrollSpeed * (float)this.direction;
    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float f = this.totalScrollLength + this.height + this.height + 24;
        if (this.scroll > f) {
            this.respawn();
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 265) {
            this.direction = -1;
        } else if (n == 341 || n == 345) {
            this.speedupModifiers.add(n);
        } else if (n == 32) {
            this.speedupActive = true;
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        if (n == 265) {
            this.direction = 1;
        }
        if (n == 32) {
            this.speedupActive = false;
        } else if (n == 341 || n == 345) {
            this.speedupModifiers.remove(n);
        }
        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyReleased(n, n2, n3);
    }

    @Override
    public void onClose() {
        this.respawn();
    }

    private void respawn() {
        this.onFinished.run();
    }

    @Override
    protected void init() {
        if (this.lines != null) {
            return;
        }
        this.lines = Lists.newArrayList();
        this.narratorComponents = Lists.newArrayList();
        this.centeredLines = new IntOpenHashSet();
        if (this.poem) {
            this.wrapCreditsIO(END_POEM_LOCATION, this::addPoemFile);
        }
        this.wrapCreditsIO(CREDITS_LOCATION, this::addCreditsFile);
        if (this.poem) {
            this.wrapCreditsIO(POSTCREDITS_LOCATION, this::addPoemFile);
        }
        this.totalScrollLength = this.lines.size() * 12;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration((Component[])this.narratorComponents.toArray(Component[]::new));
    }

    private void wrapCreditsIO(ResourceLocation resourceLocation, CreditsReader creditsReader) {
        try (BufferedReader bufferedReader = this.minecraft.getResourceManager().openAsReader(resourceLocation);){
            creditsReader.read(bufferedReader);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load credits from file {}", (Object)resourceLocation, (Object)exception);
        }
    }

    private void addPoemFile(Reader reader) throws IOException {
        int n;
        Object object;
        BufferedReader bufferedReader = new BufferedReader(reader);
        RandomSource randomSource = RandomSource.create(8124371L);
        while ((object = bufferedReader.readLine()) != null) {
            object = ((String)object).replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
            while ((n = ((String)object).indexOf(OBFUSCATE_TOKEN)) != -1) {
                String string = ((String)object).substring(0, n);
                String string2 = ((String)object).substring(n + OBFUSCATE_TOKEN.length());
                object = string + String.valueOf(ChatFormatting.WHITE) + String.valueOf(ChatFormatting.OBFUSCATED) + "XXXXXXXX".substring(0, randomSource.nextInt(4) + 3) + string2;
            }
            this.addPoemLines((String)object);
            this.addEmptyLine();
        }
        for (n = 0; n < 8; ++n) {
            this.addEmptyLine();
        }
    }

    private void addCreditsFile(Reader reader) {
        JsonArray jsonArray = GsonHelper.parseArray(reader);
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string = jsonObject.get("section").getAsString();
            this.addCreditsLine(SECTION_HEADING, true, false);
            this.addCreditsLine(Component.literal(string).withStyle(ChatFormatting.YELLOW), true, true);
            this.addCreditsLine(SECTION_HEADING, true, false);
            this.addEmptyLine();
            this.addEmptyLine();
            JsonArray jsonArray2 = jsonObject.getAsJsonArray("disciplines");
            for (JsonElement jsonElement2 : jsonArray2) {
                JsonObject jsonObject2 = jsonElement2.getAsJsonObject();
                String string2 = jsonObject2.get("discipline").getAsString();
                if (StringUtils.isNotEmpty((CharSequence)string2)) {
                    this.addCreditsLine(Component.literal(string2).withStyle(ChatFormatting.YELLOW), true, true);
                    this.addEmptyLine();
                    this.addEmptyLine();
                }
                JsonArray jsonArray3 = jsonObject2.getAsJsonArray("titles");
                for (JsonElement jsonElement3 : jsonArray3) {
                    JsonObject jsonObject3 = jsonElement3.getAsJsonObject();
                    String string3 = jsonObject3.get("title").getAsString();
                    JsonArray jsonArray4 = jsonObject3.getAsJsonArray("names");
                    this.addCreditsLine(Component.literal(string3).withStyle(ChatFormatting.GRAY), false, true);
                    for (JsonElement jsonElement4 : jsonArray4) {
                        String string4 = jsonElement4.getAsString();
                        this.addCreditsLine(Component.literal(NAME_PREFIX).append(string4).withStyle(ChatFormatting.WHITE), false, true);
                    }
                    this.addEmptyLine();
                    this.addEmptyLine();
                }
            }
        }
    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
        this.narratorComponents.add(CommonComponents.EMPTY);
    }

    private void addPoemLines(String string) {
        MutableComponent mutableComponent = Component.literal(string);
        this.lines.addAll(this.minecraft.font.split(mutableComponent, 256));
        this.narratorComponents.add(mutableComponent);
    }

    private void addCreditsLine(Component component, boolean bl, boolean bl2) {
        if (bl) {
            this.centeredLines.add(this.lines.size());
        }
        this.lines.add(component.getVisualOrderText());
        if (bl2) {
            this.narratorComponents.add(component);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderVignette(guiGraphics);
        this.scroll = Math.max(0.0f, this.scroll + f * this.scrollSpeed);
        int n3 = this.width / 2 - 128;
        int n4 = this.height + 50;
        float f2 = -this.scroll;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0.0f, f2);
        guiGraphics.nextStratum();
        this.logoRenderer.renderLogo(guiGraphics, this.width, 1.0f, n4);
        int n5 = n4 + 100;
        for (int i = 0; i < this.lines.size(); ++i) {
            float f3;
            if (i == this.lines.size() - 1 && (f3 = (float)n5 + f2 - (float)(this.height / 2 - 6)) < 0.0f) {
                guiGraphics.pose().translate(0.0f, -f3);
            }
            if ((float)n5 + f2 + 12.0f + 8.0f > 0.0f && (float)n5 + f2 < (float)this.height) {
                FormattedCharSequence formattedCharSequence = this.lines.get(i);
                if (this.centeredLines.contains(i)) {
                    guiGraphics.drawCenteredString(this.font, formattedCharSequence, n3 + 128, n5, -1);
                } else {
                    guiGraphics.drawString(this.font, formattedCharSequence, n3, n5, -1);
                }
            }
            n5 += 12;
        }
        guiGraphics.pose().popMatrix();
    }

    private void renderVignette(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.VIGNETTE, VIGNETTE_LOCATION, 0, 0, 0.0f, 0.0f, this.width, this.height, this.width, this.height);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.poem) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            TextureSetup textureSetup = TextureSetup.doubleTexture(textureManager.getTexture(TheEndPortalRenderer.END_SKY_LOCATION).getTextureView(), textureManager.getTexture(TheEndPortalRenderer.END_PORTAL_LOCATION).getTextureView());
            guiGraphics.fill(RenderPipelines.END_PORTAL, textureSetup, 0, 0, this.width, this.height);
        } else {
            super.renderBackground(guiGraphics, n, n2, f);
        }
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        float f = this.scroll * 0.5f;
        Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, 0, 0, 0.0f, f, n3, n4);
    }

    @Override
    public boolean isPauseScreen() {
        return !this.poem;
    }

    @Override
    public void removed() {
        this.minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
    }

    @Override
    public Music getBackgroundMusic() {
        return Musics.CREDITS;
    }

    @FunctionalInterface
    static interface CreditsReader {
        public void read(Reader var1) throws IOException;
    }
}

