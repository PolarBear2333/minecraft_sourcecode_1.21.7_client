/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.realms;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.resources.ResourceLocation;

public abstract class RealmsScreen
extends Screen {
    protected static final int TITLE_HEIGHT = 17;
    protected static final int EXPIRATION_NOTIFICATION_DAYS = 7;
    protected static final long SIZE_LIMIT = 0x140000000L;
    protected static final int COLOR_DARK_GRAY = -11776948;
    protected static final int COLOR_MEDIUM_GRAY = -9671572;
    protected static final int COLOR_GREEN = -8388737;
    protected static final int COLOR_LINK = -13408581;
    protected static final int COLOR_LINK_HOVER = -9670204;
    protected static final int SKIN_FACE_SIZE = 32;
    protected static final int HARDCORE_HEART_SIZE = 8;
    protected static final ResourceLocation LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/realms.png");
    protected static final int LOGO_WIDTH = 128;
    protected static final int LOGO_HEIGHT = 34;
    protected static final int LOGO_TEXTURE_WIDTH = 128;
    protected static final int LOGO_TEXTURE_HEIGHT = 64;
    private final List<RealmsLabel> labels = Lists.newArrayList();

    public RealmsScreen(Component component) {
        super(component);
    }

    protected static int row(int n) {
        return 40 + n * 13;
    }

    protected RealmsLabel addLabel(RealmsLabel realmsLabel) {
        this.labels.add(realmsLabel);
        return this.addRenderableOnly(realmsLabel);
    }

    public Component createLabelNarration() {
        return CommonComponents.joinLines(this.labels.stream().map(RealmsLabel::getText).collect(Collectors.toList()));
    }

    protected static ImageWidget realmsLogo() {
        return ImageWidget.texture(128, 34, LOGO_LOCATION, 128, 64);
    }
}

