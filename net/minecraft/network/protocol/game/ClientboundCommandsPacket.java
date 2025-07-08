/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCommandsPacket> STREAM_CODEC = Packet.codec(ClientboundCommandsPacket::write, ClientboundCommandsPacket::new);
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte FLAG_RESTRICTED = 32;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<Entry> entries;

    public <S> ClientboundCommandsPacket(RootCommandNode<S> rootCommandNode, NodeInspector<S> nodeInspector) {
        Object2IntMap<CommandNode<S>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(rootCommandNode);
        this.entries = ClientboundCommandsPacket.createEntries(object2IntMap, nodeInspector);
        this.rootIndex = object2IntMap.getInt(rootCommandNode);
    }

    private ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entries = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = friendlyByteBuf.readVarInt();
        ClientboundCommandsPacket.validateEntries(this.entries);
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
        friendlyByteBuf2.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<Entry> list, BiPredicate<Entry, IntSet> biPredicate) {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet((IntCollection)IntSets.fromTo((int)0, (int)list.size()));
        while (!intOpenHashSet.isEmpty()) {
            boolean bl = intOpenHashSet.removeIf(arg_0 -> ClientboundCommandsPacket.lambda$validateEntries$1(biPredicate, list, (IntSet)intOpenHashSet, arg_0));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validateEntries(List<Entry> list) {
        ClientboundCommandsPacket.validateEntries(list, Entry::canBuild);
        ClientboundCommandsPacket.validateEntries(list, Entry::canResolve);
    }

    private static <S> Object2IntMap<CommandNode<S>> enumerateNodes(RootCommandNode<S> rootCommandNode) {
        CommandNode commandNode;
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        ArrayDeque<Object> arrayDeque = new ArrayDeque<Object>();
        arrayDeque.add(rootCommandNode);
        while ((commandNode = (CommandNode)arrayDeque.poll()) != null) {
            if (object2IntOpenHashMap.containsKey((Object)commandNode)) continue;
            int n = object2IntOpenHashMap.size();
            object2IntOpenHashMap.put((Object)commandNode, n);
            arrayDeque.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            arrayDeque.add(commandNode.getRedirect());
        }
        return object2IntOpenHashMap;
    }

    private static <S> List<Entry> createEntries(Object2IntMap<CommandNode<S>> object2IntMap, NodeInspector<S> nodeInspector) {
        ObjectArrayList objectArrayList = new ObjectArrayList(object2IntMap.size());
        objectArrayList.size(object2IntMap.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            objectArrayList.set(entry.getIntValue(), (Object)ClientboundCommandsPacket.createEntry((CommandNode)entry.getKey(), nodeInspector, object2IntMap));
        }
        return objectArrayList;
    }

    private static Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        int[] nArray = friendlyByteBuf.readVarIntArray();
        int n = (by & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        NodeStub nodeStub = ClientboundCommandsPacket.read(friendlyByteBuf, by);
        return new Entry(nodeStub, by, n, nArray);
    }

    @Nullable
    private static NodeStub read(FriendlyByteBuf friendlyByteBuf, byte by) {
        int n = by & 3;
        if (n == 2) {
            String string = friendlyByteBuf.readUtf();
            int n2 = friendlyByteBuf.readVarInt();
            ArgumentTypeInfo argumentTypeInfo = (ArgumentTypeInfo)BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(n2);
            if (argumentTypeInfo == null) {
                return null;
            }
            Object t = argumentTypeInfo.deserializeFromNetwork(friendlyByteBuf);
            ResourceLocation resourceLocation = (by & 0x10) != 0 ? friendlyByteBuf.readResourceLocation() : null;
            return new ArgumentNodeStub(string, (ArgumentTypeInfo.Template<?>)t, resourceLocation);
        }
        if (n == 1) {
            String string = friendlyByteBuf.readUtf();
            return new LiteralNodeStub(string);
        }
        return null;
    }

    private static <S> Entry createEntry(CommandNode<S> commandNode, NodeInspector<S> nodeInspector, Object2IntMap<CommandNode<S>> object2IntMap) {
        Record record;
        int n;
        int n2 = 0;
        if (commandNode.getRedirect() != null) {
            n2 |= 8;
            n = object2IntMap.getInt((Object)commandNode.getRedirect());
        } else {
            n = 0;
        }
        if (nodeInspector.isExecutable(commandNode)) {
            n2 |= 4;
        }
        if (nodeInspector.isRestricted(commandNode)) {
            n2 |= 0x20;
        }
        CommandNode<S> commandNode2 = commandNode;
        Objects.requireNonNull(commandNode2);
        Object object = commandNode2;
        int n3 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, ArgumentCommandNode.class, LiteralCommandNode.class}, object, n3)) {
            case 0: {
                RootCommandNode rootCommandNode = (RootCommandNode)object;
                n2 |= 0;
                record = null;
                break;
            }
            case 1: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)object;
                ResourceLocation resourceLocation = nodeInspector.suggestionId(argumentCommandNode);
                record = new ArgumentNodeStub(argumentCommandNode.getName(), ArgumentTypeInfos.unpack(argumentCommandNode.getType()), resourceLocation);
                n2 |= 2;
                if (resourceLocation != null) {
                    n2 |= 0x10;
                }
                break;
            }
            case 2: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)object;
                record = new LiteralNodeStub(literalCommandNode.getLiteral());
                n2 |= 1;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown node type " + String.valueOf(commandNode));
            }
        }
        object = commandNode.getChildren().stream().mapToInt(arg_0 -> object2IntMap.getInt(arg_0)).toArray();
        return new Entry((NodeStub)((Object)record), n2, n, (int[])object);
    }

    @Override
    public PacketType<ClientboundCommandsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_COMMANDS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommands(this);
    }

    public <S> RootCommandNode<S> getRoot(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
        return (RootCommandNode)new NodeResolver<S>(commandBuildContext, nodeBuilder, this.entries).resolve(this.rootIndex);
    }

    private static /* synthetic */ boolean lambda$validateEntries$1(BiPredicate biPredicate, List list, IntSet intSet, int n) {
        return biPredicate.test((Entry)list.get(n), intSet);
    }

    public static interface NodeInspector<S> {
        @Nullable
        public ResourceLocation suggestionId(ArgumentCommandNode<S, ?> var1);

        public boolean isExecutable(CommandNode<S> var1);

        public boolean isRestricted(CommandNode<S> var1);
    }

    static final class Entry
    extends Record {
        @Nullable
        final NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable NodeStub nodeStub, int n, int n2, int[] nArray) {
            this.stub = nodeStub;
            this.flags = n;
            this.redirect = n2;
            this.children = nArray;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.flags);
            friendlyByteBuf.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                friendlyByteBuf.writeVarInt(this.redirect);
            }
            if (this.stub != null) {
                this.stub.write(friendlyByteBuf);
            }
        }

        public boolean canBuild(IntSet intSet) {
            if ((this.flags & 8) != 0) {
                return !intSet.contains(this.redirect);
            }
            return true;
        }

        public boolean canResolve(IntSet intSet) {
            for (int n : this.children) {
                if (!intSet.contains(n)) continue;
                return false;
            }
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this, object);
        }

        @Nullable
        public NodeStub stub() {
            return this.stub;
        }

        public int flags() {
            return this.flags;
        }

        public int redirect() {
            return this.redirect;
        }

        public int[] children() {
            return this.children;
        }
    }

    static interface NodeStub {
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext var1, NodeBuilder<S> var2);

        public void write(FriendlyByteBuf var1);
    }

    record ArgumentNodeStub(String id, ArgumentTypeInfo.Template<?> argumentType, @Nullable ResourceLocation suggestionId) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
            Object obj = this.argumentType.instantiate(commandBuildContext);
            return nodeBuilder.createArgument(this.id, (ArgumentType<?>)obj, this.suggestionId);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
            ArgumentNodeStub.serializeCap(friendlyByteBuf, this.argumentType);
            if (this.suggestionId != null) {
                friendlyByteBuf.writeResourceLocation(this.suggestionId);
            }
        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo.Template<A> template) {
            ArgumentNodeStub.serializeCap(friendlyByteBuf, template.type(), template);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
            friendlyByteBuf.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(argumentTypeInfo));
            argumentTypeInfo.serializeToNetwork(template, friendlyByteBuf);
        }
    }

    record LiteralNodeStub(String id) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
            return nodeBuilder.createLiteral(this.id);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
        }
    }

    static class NodeResolver<S> {
        private final CommandBuildContext context;
        private final NodeBuilder<S> builder;
        private final List<Entry> entries;
        private final List<CommandNode<S>> nodes;

        NodeResolver(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder, List<Entry> list) {
            this.context = commandBuildContext;
            this.builder = nodeBuilder;
            this.entries = list;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            objectArrayList.size(list.size());
            this.nodes = objectArrayList;
        }

        public CommandNode<S> resolve(int n) {
            RootCommandNode rootCommandNode;
            CommandNode<S> commandNode = this.nodes.get(n);
            if (commandNode != null) {
                return commandNode;
            }
            Entry entry = this.entries.get(n);
            if (entry.stub == null) {
                rootCommandNode = new RootCommandNode();
            } else {
                ArgumentBuilder<S, ?> argumentBuilder = entry.stub.build(this.context, this.builder);
                if ((entry.flags & 8) != 0) {
                    argumentBuilder.redirect(this.resolve(entry.redirect));
                }
                int n2 = (entry.flags & 4) != 0 ? 1 : 0;
                int n3 = (entry.flags & 0x20) != 0 ? 1 : 0;
                rootCommandNode = this.builder.configure(argumentBuilder, n2 != 0, n3 != 0).build();
            }
            this.nodes.set(n, (CommandNode<S>)rootCommandNode);
            for (int n4 : entry.children) {
                CommandNode<S> commandNode2 = this.resolve(n4);
                if (commandNode2 instanceof RootCommandNode) continue;
                rootCommandNode.addChild(commandNode2);
            }
            return rootCommandNode;
        }
    }

    public static interface NodeBuilder<S> {
        public ArgumentBuilder<S, ?> createLiteral(String var1);

        public ArgumentBuilder<S, ?> createArgument(String var1, ArgumentType<?> var2, @Nullable ResourceLocation var3);

        public ArgumentBuilder<S, ?> configure(ArgumentBuilder<S, ?> var1, boolean var2, boolean var3);
    }
}

