/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public record ServerLinks(List<Entry> entries) {
    public static final ServerLinks EMPTY = new ServerLinks(List.of());
    public static final StreamCodec<ByteBuf, Either<KnownLinkType, Component>> TYPE_STREAM_CODEC = ByteBufCodecs.either(KnownLinkType.STREAM_CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC);
    public static final StreamCodec<ByteBuf, List<UntrustedEntry>> UNTRUSTED_LINKS_STREAM_CODEC = UntrustedEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<Entry> findKnownType(KnownLinkType knownLinkType) {
        return this.entries.stream().filter(entry -> (Boolean)entry.type.map(knownLinkType2 -> knownLinkType2 == knownLinkType, component -> false)).findFirst();
    }

    public List<UntrustedEntry> untrust() {
        return this.entries.stream().map(entry -> new UntrustedEntry(entry.type, entry.link.toString())).toList();
    }

    public static enum KnownLinkType {
        BUG_REPORT(0, "report_bug"),
        COMMUNITY_GUIDELINES(1, "community_guidelines"),
        SUPPORT(2, "support"),
        STATUS(3, "status"),
        FEEDBACK(4, "feedback"),
        COMMUNITY(5, "community"),
        WEBSITE(6, "website"),
        FORUMS(7, "forums"),
        NEWS(8, "news"),
        ANNOUNCEMENTS(9, "announcements");

        private static final IntFunction<KnownLinkType> BY_ID;
        public static final StreamCodec<ByteBuf, KnownLinkType> STREAM_CODEC;
        private final int id;
        private final String name;

        private KnownLinkType(int n2, String string2) {
            this.id = n2;
            this.name = string2;
        }

        private Component displayName() {
            return Component.translatable("known_server_link." + this.name);
        }

        public Entry create(URI uRI) {
            return Entry.knownType(this, uRI);
        }

        static {
            BY_ID = ByIdMap.continuous(knownLinkType -> knownLinkType.id, KnownLinkType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, knownLinkType -> knownLinkType.id);
        }
    }

    public record UntrustedEntry(Either<KnownLinkType, Component> type, String link) {
        public static final StreamCodec<ByteBuf, UntrustedEntry> STREAM_CODEC = StreamCodec.composite(TYPE_STREAM_CODEC, UntrustedEntry::type, ByteBufCodecs.STRING_UTF8, UntrustedEntry::link, UntrustedEntry::new);
    }

    public static final class Entry
    extends Record {
        final Either<KnownLinkType, Component> type;
        final URI link;

        public Entry(Either<KnownLinkType, Component> either, URI uRI) {
            this.type = either;
            this.link = uRI;
        }

        public static Entry knownType(KnownLinkType knownLinkType, URI uRI) {
            return new Entry((Either<KnownLinkType, Component>)Either.left((Object)((Object)knownLinkType)), uRI);
        }

        public static Entry custom(Component component, URI uRI) {
            return new Entry((Either<KnownLinkType, Component>)Either.right((Object)component), uRI);
        }

        public Component displayName() {
            return (Component)this.type.map(KnownLinkType::displayName, component -> component);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "type;link", "type", "link"}, this, object);
        }

        public Either<KnownLinkType, Component> type() {
            return this.type;
        }

        public URI link() {
            return this.link;
        }
    }
}

