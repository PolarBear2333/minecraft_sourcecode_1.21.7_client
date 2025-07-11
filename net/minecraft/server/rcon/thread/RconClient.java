/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.PktUtils;
import net.minecraft.server.rcon.thread.GenericThread;
import org.slf4j.Logger;

public class RconClient
extends GenericThread {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE_VALUE = 0;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_AUTH_FAILURE = -1;
    private boolean authed;
    private final Socket client;
    private final byte[] buf = new byte[1460];
    private final String rconPassword;
    private final ServerInterface serverInterface;

    RconClient(ServerInterface serverInterface, String string, Socket socket) {
        super("RCON Client " + String.valueOf(socket.getInetAddress()));
        this.serverInterface = serverInterface;
        this.client = socket;
        try {
            this.client.setSoTimeout(0);
        }
        catch (Exception exception) {
            this.running = false;
        }
        this.rconPassword = string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            while (this.running) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(this.client.getInputStream());
                int n = bufferedInputStream.read(this.buf, 0, 1460);
                if (10 > n) {
                    return;
                }
                int n2 = 0;
                int n3 = PktUtils.intFromByteArray(this.buf, 0, n);
                if (n3 != n - 4) {
                    return;
                }
                int n4 = PktUtils.intFromByteArray(this.buf, n2 += 4, n);
                int n5 = PktUtils.intFromByteArray(this.buf, n2 += 4);
                n2 += 4;
                switch (n5) {
                    case 3: {
                        String string = PktUtils.stringFromByteArray(this.buf, n2, n);
                        n2 += string.length();
                        if (!string.isEmpty() && string.equals(this.rconPassword)) {
                            this.authed = true;
                            this.send(n4, 2, "");
                            break;
                        }
                        this.authed = false;
                        this.sendAuthFailure();
                        break;
                    }
                    case 2: {
                        if (this.authed) {
                            String string = PktUtils.stringFromByteArray(this.buf, n2, n);
                            try {
                                this.sendCmdResponse(n4, this.serverInterface.runCommand(string));
                            }
                            catch (Exception exception) {
                                this.sendCmdResponse(n4, "Error executing: " + string + " (" + exception.getMessage() + ")");
                            }
                            break;
                        }
                        this.sendAuthFailure();
                        break;
                    }
                    default: {
                        this.sendCmdResponse(n4, String.format(Locale.ROOT, "Unknown request %s", Integer.toHexString(n5)));
                    }
                }
            }
        }
        catch (IOException iOException) {
        }
        catch (Exception exception) {
            LOGGER.error("Exception whilst parsing RCON input", (Throwable)exception);
        }
        finally {
            this.closeSocket();
            LOGGER.info("Thread {} shutting down", (Object)this.name);
            this.running = false;
        }
    }

    private void send(int n, int n2, String string) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1248);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(Integer.reverseBytes(byArray.length + 10));
        dataOutputStream.writeInt(Integer.reverseBytes(n));
        dataOutputStream.writeInt(Integer.reverseBytes(n2));
        dataOutputStream.write(byArray);
        dataOutputStream.write(0);
        dataOutputStream.write(0);
        this.client.getOutputStream().write(byteArrayOutputStream.toByteArray());
    }

    private void sendAuthFailure() throws IOException {
        this.send(-1, 2, "");
    }

    private void sendCmdResponse(int n, String string) throws IOException {
        int n2;
        int n3 = string.length();
        do {
            n2 = 4096 <= n3 ? 4096 : n3;
            this.send(n, 0, string.substring(0, n2));
        } while (0 != (n3 = (string = string.substring(n2)).length()));
    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket();
        super.stop();
    }

    private void closeSocket() {
        try {
            this.client.close();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to close socket", (Throwable)iOException);
        }
    }
}

