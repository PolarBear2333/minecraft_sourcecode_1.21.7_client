/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundLevelParticlesPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelParticlesPacket> STREAM_CODEC = Packet.codec(ClientboundLevelParticlesPacket::write, ClientboundLevelParticlesPacket::new);
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final boolean alwaysShow;
    private final ParticleOptions particle;

    public <T extends ParticleOptions> ClientboundLevelParticlesPacket(T t, boolean bl, boolean bl2, double d, double d2, double d3, float f, float f2, float f3, float f4, int n) {
        this.particle = t;
        this.overrideLimiter = bl;
        this.alwaysShow = bl2;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.xDist = f;
        this.yDist = f2;
        this.zDist = f3;
        this.maxSpeed = f4;
        this.count = n;
    }

    private ClientboundLevelParticlesPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.overrideLimiter = registryFriendlyByteBuf.readBoolean();
        this.alwaysShow = registryFriendlyByteBuf.readBoolean();
        this.x = registryFriendlyByteBuf.readDouble();
        this.y = registryFriendlyByteBuf.readDouble();
        this.z = registryFriendlyByteBuf.readDouble();
        this.xDist = registryFriendlyByteBuf.readFloat();
        this.yDist = registryFriendlyByteBuf.readFloat();
        this.zDist = registryFriendlyByteBuf.readFloat();
        this.maxSpeed = registryFriendlyByteBuf.readFloat();
        this.count = registryFriendlyByteBuf.readInt();
        this.particle = (ParticleOptions)ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeBoolean(this.overrideLimiter);
        registryFriendlyByteBuf.writeBoolean(this.alwaysShow);
        registryFriendlyByteBuf.writeDouble(this.x);
        registryFriendlyByteBuf.writeDouble(this.y);
        registryFriendlyByteBuf.writeDouble(this.z);
        registryFriendlyByteBuf.writeFloat(this.xDist);
        registryFriendlyByteBuf.writeFloat(this.yDist);
        registryFriendlyByteBuf.writeFloat(this.zDist);
        registryFriendlyByteBuf.writeFloat(this.maxSpeed);
        registryFriendlyByteBuf.writeInt(this.count);
        ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.particle);
    }

    @Override
    public PacketType<ClientboundLevelParticlesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleParticleEvent(this);
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    public boolean alwaysShow() {
        return this.alwaysShow;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getXDist() {
        return this.xDist;
    }

    public float getYDist() {
        return this.yDist;
    }

    public float getZDist() {
        return this.zDist;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public ParticleOptions getParticle() {
        return this.particle;
    }
}

