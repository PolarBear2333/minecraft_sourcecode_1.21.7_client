/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft;

import java.util.Date;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion {
    public DataVersion dataVersion();

    public String id();

    public String name();

    public int protocolVersion();

    public int packVersion(PackType var1);

    public Date buildTime();

    public boolean stable();

    public record Simple(String id, String name, DataVersion dataVersion, int protocolVersion, int resourcePackVersion, int datapackVersion, Date buildTime, boolean stable) implements WorldVersion
    {
        @Override
        public int packVersion(PackType packType) {
            return switch (packType) {
                default -> throw new MatchException(null, null);
                case PackType.CLIENT_RESOURCES -> this.resourcePackVersion;
                case PackType.SERVER_DATA -> this.datapackVersion;
            };
        }
    }
}

