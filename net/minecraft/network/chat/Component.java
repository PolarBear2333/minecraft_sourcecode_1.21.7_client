/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.datafixers.util.Either
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public ComponentContents getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int n2 = n - stringBuilder.length();
            if (n2 <= 0) {
                return STOP_ITERATION;
            }
            stringBuilder.append(string.length() <= n2 ? string : string.substring(0, n2));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Component> getSiblings();

    @Nullable
    default public String tryCollapseToString() {
        ComponentContents componentContents = this.getContents();
        if (componentContents instanceof PlainTextContents) {
            PlainTextContents plainTextContents = (PlainTextContents)componentContents;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return plainTextContents.text();
            }
        }
        return null;
    }

    default public MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default public MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<Component>(this.getSiblings()), this.getStyle());
    }

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.getContents().visit(styledContentConsumer, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(styledContentConsumer, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        Optional<T> optional = this.getContents().visit(contentConsumer);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(contentConsumer);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default public List<Component> toFlatList(Style style2) {
        ArrayList arrayList = Lists.newArrayList();
        this.visit((style, string) -> {
            if (!string.isEmpty()) {
                arrayList.add(Component.literal(string).withStyle(style));
            }
            return Optional.empty();
        }, style2);
        return arrayList;
    }

    default public boolean contains(Component component) {
        List<Component> list;
        if (this.equals(component)) {
            return true;
        }
        List<Component> list2 = this.toFlatList();
        return Collections.indexOfSubList(list2, list = component.toFlatList(this.getStyle())) != -1;
    }

    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? Component.literal(string) : CommonComponents.EMPTY;
    }

    public static MutableComponent literal(String string) {
        return MutableComponent.create(PlainTextContents.create(string));
    }

    public static MutableComponent translatable(String string) {
        return MutableComponent.create(new TranslatableContents(string, null, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatable(String string, Object ... objectArray) {
        return MutableComponent.create(new TranslatableContents(string, null, objectArray));
    }

    public static MutableComponent translatableEscape(String string, Object ... objectArray) {
        for (int i = 0; i < objectArray.length; ++i) {
            Object object = objectArray[i];
            if (TranslatableContents.isAllowedPrimitiveArgument(object) || object instanceof Component) continue;
            objectArray[i] = String.valueOf(object);
        }
        return Component.translatable(string, objectArray);
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2) {
        return MutableComponent.create(new TranslatableContents(string, string2, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2, Object ... objectArray) {
        return MutableComponent.create(new TranslatableContents(string, string2, objectArray));
    }

    public static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
    }

    public static MutableComponent keybind(String string) {
        return MutableComponent.create(new KeybindContents(string));
    }

    public static MutableComponent nbt(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
        return MutableComponent.create(new NbtContents(string, bl, optional, dataSource));
    }

    public static MutableComponent score(SelectorPattern selectorPattern, String string) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.left((Object)selectorPattern), string));
    }

    public static MutableComponent score(String string, String string2) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.right((Object)string), string2));
    }

    public static MutableComponent selector(SelectorPattern selectorPattern, Optional<Component> optional) {
        return MutableComponent.create(new SelectorContents(selectorPattern, optional));
    }

    public static Component translationArg(Date date) {
        return Component.literal(date.toString());
    }

    public static Component translationArg(Message message) {
        Component component;
        if (message instanceof Component) {
            Component component2 = (Component)message;
            component = component2;
        } else {
            component = Component.literal(message.getString());
        }
        return component;
    }

    public static Component translationArg(UUID uUID) {
        return Component.literal(uUID.toString());
    }

    public static Component translationArg(ResourceLocation resourceLocation) {
        return Component.literal(resourceLocation.toString());
    }

    public static Component translationArg(ChunkPos chunkPos) {
        return Component.literal(chunkPos.toString());
    }

    public static Component translationArg(URI uRI) {
        return Component.literal(uRI.toString());
    }
}

