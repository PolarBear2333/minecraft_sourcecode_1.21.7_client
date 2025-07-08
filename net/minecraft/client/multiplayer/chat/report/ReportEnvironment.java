/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.request.AbuseReportRequest$ClientInfo
 *  com.mojang.authlib.yggdrasil.request.AbuseReportRequest$RealmInfo
 *  com.mojang.authlib.yggdrasil.request.AbuseReportRequest$ThirdPartyServerInfo
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.realmsclient.dto.RealmsServer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;

public record ReportEnvironment(String clientVersion, @Nullable Server server) {
    public static ReportEnvironment local() {
        return ReportEnvironment.create(null);
    }

    public static ReportEnvironment thirdParty(String string) {
        return ReportEnvironment.create(new Server.ThirdParty(string));
    }

    public static ReportEnvironment realm(RealmsServer realmsServer) {
        return ReportEnvironment.create(new Server.Realm(realmsServer));
    }

    public static ReportEnvironment create(@Nullable Server server) {
        return new ReportEnvironment(ReportEnvironment.getClientVersion(), server);
    }

    public AbuseReportRequest.ClientInfo clientInfo() {
        return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
    }

    @Nullable
    public AbuseReportRequest.ThirdPartyServerInfo thirdPartyServerInfo() {
        Server server = this.server;
        if (server instanceof Server.ThirdParty) {
            Server.ThirdParty thirdParty = (Server.ThirdParty)server;
            return new AbuseReportRequest.ThirdPartyServerInfo(thirdParty.ip);
        }
        return null;
    }

    @Nullable
    public AbuseReportRequest.RealmInfo realmInfo() {
        Server server = this.server;
        if (server instanceof Server.Realm) {
            Server.Realm realm = (Server.Realm)server;
            return new AbuseReportRequest.RealmInfo(String.valueOf(realm.realmId()), realm.slotId());
        }
        return null;
    }

    private static String getClientVersion() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("1.21.7");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append(" (modded)");
        }
        return stringBuilder.toString();
    }

    public static interface Server {

        public record Realm(long realmId, int slotId) implements Server
        {
            public Realm(RealmsServer realmsServer) {
                this(realmsServer.id, realmsServer.activeSlot);
            }
        }

        public static final class ThirdParty
        extends Record
        implements Server {
            final String ip;

            public ThirdParty(String string) {
                this.ip = string;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ThirdParty.class, "ip", "ip"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ThirdParty.class, "ip", "ip"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ThirdParty.class, "ip", "ip"}, this, object);
            }

            public String ip() {
                return this.ip;
            }
        }
    }
}

