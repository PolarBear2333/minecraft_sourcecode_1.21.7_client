/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentScreen
extends AbstractContainerScreen<EnchantmentMenu> {
    private static final ResourceLocation[] ENABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3")};
    private static final ResourceLocation[] DISABLED_LEVEL_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/enchanting_table/level_1_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_2_disabled"), ResourceLocation.withDefaultNamespace("container/enchanting_table/level_3_disabled")};
    private static final ResourceLocation ENCHANTMENT_SLOT_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
    private static final ResourceLocation ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    private static final ResourceLocation ENCHANTMENT_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/enchanting_table/enchantment_slot");
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private final RandomSource random = RandomSource.create();
    private BookModel bookModel;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    public EnchantmentScreen(EnchantmentMenu enchantmentMenu, Inventory inventory, Component component) {
        super(enchantmentMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
        this.tickBook();
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        int n2 = (this.width - this.imageWidth) / 2;
        int n3 = (this.height - this.imageHeight) / 2;
        for (int i = 0; i < 3; ++i) {
            double d3 = d - (double)(n2 + 60);
            double d4 = d2 - (double)(n3 + 14 + 19 * i);
            if (!(d3 >= 0.0) || !(d4 >= 0.0) || !(d3 < 108.0) || !(d4 < 19.0) || !((EnchantmentMenu)this.menu).clickMenuButton(this.minecraft.player, i)) continue;
            this.minecraft.gameMode.handleInventoryButtonClick(((EnchantmentMenu)this.menu).containerId, i);
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3 = (this.width - this.imageWidth) / 2;
        int n4 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTING_TABLE_LOCATION, n3, n4, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        this.renderBook(guiGraphics, n3, n4);
        EnchantmentNames.getInstance().initSeed(((EnchantmentMenu)this.menu).getEnchantmentSeed());
        int n5 = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int i = 0; i < 3; ++i) {
            int n6 = n3 + 60;
            int n7 = n6 + 20;
            int n8 = ((EnchantmentMenu)this.menu).costs[i];
            if (n8 == 0) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_SPRITE, n6, n4 + 14 + 19 * i, 108, 19);
                continue;
            }
            String string = "" + n8;
            int n9 = 86 - this.font.width(string);
            FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(this.font, n9);
            int n10 = -9937334;
            if (!(n5 >= i + 1 && this.minecraft.player.experienceLevel >= n8 || this.minecraft.player.hasInfiniteMaterials())) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_SPRITE, n6, n4 + 14 + 19 * i, 108, 19);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISABLED_LEVEL_SPRITES[i], n6 + 1, n4 + 15 + 19 * i, 16, 16);
                guiGraphics.drawWordWrap(this.font, formattedText, n7, n4 + 16 + 19 * i, n9, ARGB.opaque((n10 & 0xFEFEFE) >> 1), false);
                n10 = -12550384;
            } else {
                int n11 = n - (n3 + 60);
                int n12 = n2 - (n4 + 14 + 19 * i);
                if (n11 >= 0 && n12 >= 0 && n11 < 108 && n12 < 19) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE, n6, n4 + 14 + 19 * i, 108, 19);
                    n10 = -128;
                } else {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_SPRITE, n6, n4 + 14 + 19 * i, 108, 19);
                }
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENABLED_LEVEL_SPRITES[i], n6 + 1, n4 + 15 + 19 * i, 16, 16);
                guiGraphics.drawWordWrap(this.font, formattedText, n7, n4 + 16 + 19 * i, n9, n10, false);
                n10 = -8323296;
            }
            guiGraphics.drawString(this.font, string, n7 + 86 - this.font.width(string), n4 + 16 + 19 * i + 7, n10);
        }
    }

    private void renderBook(GuiGraphics guiGraphics, int n, int n2) {
        float f = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float f2 = Mth.lerp(f, this.oOpen, this.open);
        float f3 = Mth.lerp(f, this.oFlip, this.flip);
        int n3 = n + 14;
        int n4 = n2 + 14;
        int n5 = n3 + 38;
        int n6 = n4 + 31;
        guiGraphics.submitBookModelRenderState(this.bookModel, ENCHANTING_BOOK_LOCATION, 40.0f, f2, f3, n3, n4, n5, n6);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        float f2 = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        super.render(guiGraphics, n, n2, f2);
        this.renderTooltip(guiGraphics, n, n2);
        boolean bl = this.minecraft.player.hasInfiniteMaterials();
        int n3 = ((EnchantmentMenu)this.menu).getGoldCount();
        for (int i = 0; i < 3; ++i) {
            int n4 = ((EnchantmentMenu)this.menu).costs[i];
            Optional optional = this.minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(((EnchantmentMenu)this.menu).enchantClue[i]);
            if (optional.isEmpty()) continue;
            int n5 = ((EnchantmentMenu)this.menu).levelClue[i];
            int n6 = i + 1;
            if (!this.isHovering(60, 14 + 19 * i, 108, 17, n, n2) || n4 <= 0 || n5 < 0 || optional == null) continue;
            ArrayList arrayList = Lists.newArrayList();
            arrayList.add(Component.translatable("container.enchant.clue", Enchantment.getFullname(optional.get(), n5)).withStyle(ChatFormatting.WHITE));
            if (!bl) {
                arrayList.add(CommonComponents.EMPTY);
                if (this.minecraft.player.experienceLevel < n4) {
                    arrayList.add(Component.translatable("container.enchant.level.requirement", ((EnchantmentMenu)this.menu).costs[i]).withStyle(ChatFormatting.RED));
                } else {
                    MutableComponent mutableComponent = n6 == 1 ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", n6);
                    arrayList.add(mutableComponent.withStyle(n3 >= n6 ? ChatFormatting.GRAY : ChatFormatting.RED));
                    MutableComponent mutableComponent2 = n6 == 1 ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", n6);
                    arrayList.add(mutableComponent2.withStyle(ChatFormatting.GRAY));
                }
            }
            guiGraphics.setComponentTooltipForNextFrame(this.font, arrayList, n, n2);
            break;
        }
    }

    public void tickBook() {
        ItemStack itemStack = ((EnchantmentMenu)this.menu).getSlot(0).getItem();
        if (!ItemStack.matches(itemStack, this.last)) {
            this.last = itemStack;
            do {
                this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.flip <= this.flipT + 1.0f && this.flip >= this.flipT - 1.0f);
        }
        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean bl = false;
        for (int i = 0; i < 3; ++i) {
            if (((EnchantmentMenu)this.menu).costs[i] == 0) continue;
            bl = true;
        }
        this.open = bl ? (this.open += 0.2f) : (this.open -= 0.2f);
        this.open = Mth.clamp(this.open, 0.0f, 1.0f);
        float f = (this.flipT - this.flip) * 0.4f;
        float f2 = 0.2f;
        f = Mth.clamp(f, -0.2f, 0.2f);
        this.flipA += (f - this.flipA) * 0.9f;
        this.flip += this.flipA;
    }
}

