/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Node {
    public final int x;
    public final int y;
    public final int z;
    private final int hash;
    public int heapIdx = -1;
    public float g;
    public float h;
    public float f;
    @Nullable
    public Node cameFrom;
    public boolean closed;
    public float walkedDistance;
    public float costMalus;
    public PathType type = PathType.BLOCKED;

    public Node(int n, int n2, int n3) {
        this.x = n;
        this.y = n2;
        this.z = n3;
        this.hash = Node.createHash(n, n2, n3);
    }

    public Node cloneAndMove(int n, int n2, int n3) {
        Node node = new Node(n, n2, n3);
        node.heapIdx = this.heapIdx;
        node.g = this.g;
        node.h = this.h;
        node.f = this.f;
        node.cameFrom = this.cameFrom;
        node.closed = this.closed;
        node.walkedDistance = this.walkedDistance;
        node.costMalus = this.costMalus;
        node.type = this.type;
        return node;
    }

    public static int createHash(int n, int n2, int n3) {
        return n2 & 0xFF | (n & Short.MAX_VALUE) << 8 | (n3 & Short.MAX_VALUE) << 24 | (n < 0 ? Integer.MIN_VALUE : 0) | (n3 < 0 ? 32768 : 0);
    }

    public float distanceTo(Node node) {
        float f = node.x - this.x;
        float f2 = node.y - this.y;
        float f3 = node.z - this.z;
        return Mth.sqrt(f * f + f2 * f2 + f3 * f3);
    }

    public float distanceToXZ(Node node) {
        float f = node.x - this.x;
        float f2 = node.z - this.z;
        return Mth.sqrt(f * f + f2 * f2);
    }

    public float distanceTo(BlockPos blockPos) {
        float f = blockPos.getX() - this.x;
        float f2 = blockPos.getY() - this.y;
        float f3 = blockPos.getZ() - this.z;
        return Mth.sqrt(f * f + f2 * f2 + f3 * f3);
    }

    public float distanceToSqr(Node node) {
        float f = node.x - this.x;
        float f2 = node.y - this.y;
        float f3 = node.z - this.z;
        return f * f + f2 * f2 + f3 * f3;
    }

    public float distanceToSqr(BlockPos blockPos) {
        float f = blockPos.getX() - this.x;
        float f2 = blockPos.getY() - this.y;
        float f3 = blockPos.getZ() - this.z;
        return f * f + f2 * f2 + f3 * f3;
    }

    public float distanceManhattan(Node node) {
        float f = Math.abs(node.x - this.x);
        float f2 = Math.abs(node.y - this.y);
        float f3 = Math.abs(node.z - this.z);
        return f + f2 + f3;
    }

    public float distanceManhattan(BlockPos blockPos) {
        float f = Math.abs(blockPos.getX() - this.x);
        float f2 = Math.abs(blockPos.getY() - this.y);
        float f3 = Math.abs(blockPos.getZ() - this.z);
        return f + f2 + f3;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3 asVec3() {
        return new Vec3(this.x, this.y, this.z);
    }

    public boolean equals(Object object) {
        if (object instanceof Node) {
            Node node = (Node)object;
            return this.hash == node.hash && this.x == node.x && this.y == node.y && this.z == node.z;
        }
        return false;
    }

    public int hashCode() {
        return this.hash;
    }

    public boolean inOpenSet() {
        return this.heapIdx >= 0;
    }

    public String toString() {
        return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.y);
        friendlyByteBuf.writeInt(this.z);
        friendlyByteBuf.writeFloat(this.walkedDistance);
        friendlyByteBuf.writeFloat(this.costMalus);
        friendlyByteBuf.writeBoolean(this.closed);
        friendlyByteBuf.writeEnum(this.type);
        friendlyByteBuf.writeFloat(this.f);
    }

    public static Node createFromStream(FriendlyByteBuf friendlyByteBuf) {
        Node node = new Node(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        Node.readContents(friendlyByteBuf, node);
        return node;
    }

    protected static void readContents(FriendlyByteBuf friendlyByteBuf, Node node) {
        node.walkedDistance = friendlyByteBuf.readFloat();
        node.costMalus = friendlyByteBuf.readFloat();
        node.closed = friendlyByteBuf.readBoolean();
        node.type = friendlyByteBuf.readEnum(PathType.class);
        node.f = friendlyByteBuf.readFloat();
    }
}

