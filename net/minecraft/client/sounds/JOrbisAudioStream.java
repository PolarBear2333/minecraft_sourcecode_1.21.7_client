/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jcraft.jogg.Packet
 *  com.jcraft.jogg.Page
 *  com.jcraft.jogg.StreamState
 *  com.jcraft.jogg.SyncState
 *  com.jcraft.jorbis.Block
 *  com.jcraft.jorbis.Comment
 *  com.jcraft.jorbis.DspState
 *  com.jcraft.jorbis.Info
 *  it.unimi.dsi.fastutil.floats.FloatConsumer
 *  javax.annotation.Nullable
 */
package net.minecraft.client.sounds;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.FloatSampleSource;

public class JOrbisAudioStream
implements FloatSampleSource {
    private static final int BUFSIZE = 8192;
    private static final int PAGEOUT_RECAPTURE = -1;
    private static final int PAGEOUT_NEED_MORE_DATA = 0;
    private static final int PAGEOUT_OK = 1;
    private static final int PACKETOUT_ERROR = -1;
    private static final int PACKETOUT_NEED_MORE_DATA = 0;
    private static final int PACKETOUT_OK = 1;
    private final SyncState syncState = new SyncState();
    private final Page page = new Page();
    private final StreamState streamState = new StreamState();
    private final Packet packet = new Packet();
    private final Info info = new Info();
    private final DspState dspState = new DspState();
    private final Block block = new Block(this.dspState);
    private final AudioFormat audioFormat;
    private final InputStream input;
    private long samplesWritten;
    private long totalSamplesInStream = Long.MAX_VALUE;

    public JOrbisAudioStream(InputStream inputStream) throws IOException {
        this.input = inputStream;
        Comment comment = new Comment();
        Page page = this.readPage();
        if (page == null) {
            throw new IOException("Invalid Ogg file - can't find first page");
        }
        Packet packet = this.readIdentificationPacket(page);
        if (JOrbisAudioStream.isError(this.info.synthesis_headerin(comment, packet))) {
            throw new IOException("Invalid Ogg identification packet");
        }
        for (int i = 0; i < 2; ++i) {
            packet = this.readPacket();
            if (packet == null) {
                throw new IOException("Unexpected end of Ogg stream");
            }
            if (!JOrbisAudioStream.isError(this.info.synthesis_headerin(comment, packet))) continue;
            throw new IOException("Invalid Ogg header packet " + i);
        }
        this.dspState.synthesis_init(this.info);
        this.block.init(this.dspState);
        this.audioFormat = new AudioFormat(this.info.rate, 16, this.info.channels, true, false);
    }

    private static boolean isError(int n) {
        return n < 0;
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    private boolean readToBuffer() throws IOException {
        byte[] byArray = this.syncState.data;
        int n = this.syncState.buffer(8192);
        int n2 = this.input.read(byArray, n, 8192);
        if (n2 == -1) {
            return false;
        }
        this.syncState.wrote(n2);
        return true;
    }

    @Nullable
    private Page readPage() throws IOException {
        int n;
        block5: while (true) {
            n = this.syncState.pageout(this.page);
            switch (n) {
                case 1: {
                    if (this.page.eos() != 0) {
                        this.totalSamplesInStream = this.page.granulepos();
                    }
                    return this.page;
                }
                case 0: {
                    if (this.readToBuffer()) continue block5;
                    return null;
                }
                case -1: {
                    throw new IllegalStateException("Corrupt or missing data in bitstream");
                }
            }
            break;
        }
        throw new IllegalStateException("Unknown page decode result: " + n);
    }

    private Packet readIdentificationPacket(Page page) throws IOException {
        this.streamState.init(page.serialno());
        if (JOrbisAudioStream.isError(this.streamState.pagein(page))) {
            throw new IOException("Failed to parse page");
        }
        int n = this.streamState.packetout(this.packet);
        if (n != 1) {
            throw new IOException("Failed to read identification packet: " + n);
        }
        return this.packet;
    }

    @Nullable
    private Packet readPacket() throws IOException {
        block5: while (true) {
            int n = this.streamState.packetout(this.packet);
            switch (n) {
                case 1: {
                    return this.packet;
                }
                case 0: {
                    Page page = this.readPage();
                    if (page != null) continue block5;
                    return null;
                    if (!JOrbisAudioStream.isError(this.streamState.pagein(page))) continue block5;
                    throw new IOException("Failed to parse page");
                }
                case -1: {
                    throw new IOException("Failed to parse packet");
                }
                default: {
                    throw new IllegalStateException("Unknown packet decode result: " + n);
                }
            }
            break;
        }
    }

    private long getSamplesToWrite(int n) {
        long l;
        long l2 = this.samplesWritten + (long)n;
        if (l2 > this.totalSamplesInStream) {
            l = this.totalSamplesInStream - this.samplesWritten;
            this.samplesWritten = this.totalSamplesInStream;
        } else {
            this.samplesWritten = l2;
            l = n;
        }
        return l;
    }

    @Override
    public boolean readChunk(FloatConsumer floatConsumer) throws IOException {
        int n;
        float[][][] fArrayArray = new float[1][][];
        int[] nArray = new int[this.info.channels];
        Packet packet = this.readPacket();
        if (packet == null) {
            return false;
        }
        if (JOrbisAudioStream.isError(this.block.synthesis(packet))) {
            throw new IOException("Can't decode audio packet");
        }
        this.dspState.synthesis_blockin(this.block);
        while ((n = this.dspState.synthesis_pcmout((float[][][])fArrayArray, nArray)) > 0) {
            float[][] fArray = fArrayArray[0];
            long l = this.getSamplesToWrite(n);
            switch (this.info.channels) {
                case 1: {
                    JOrbisAudioStream.copyMono(fArray[0], nArray[0], l, floatConsumer);
                    break;
                }
                case 2: {
                    JOrbisAudioStream.copyStereo(fArray[0], nArray[0], fArray[1], nArray[1], l, floatConsumer);
                    break;
                }
                default: {
                    JOrbisAudioStream.copyAnyChannels(fArray, this.info.channels, nArray, l, floatConsumer);
                }
            }
            this.dspState.synthesis_read(n);
        }
        return true;
    }

    private static void copyAnyChannels(float[][] fArray, int n, int[] nArray, long l, FloatConsumer floatConsumer) {
        int n2 = 0;
        while ((long)n2 < l) {
            for (int i = 0; i < n; ++i) {
                int n3 = nArray[i];
                float f = fArray[i][n3 + n2];
                floatConsumer.accept(f);
            }
            ++n2;
        }
    }

    private static void copyMono(float[] fArray, int n, long l, FloatConsumer floatConsumer) {
        int n2 = n;
        while ((long)n2 < (long)n + l) {
            floatConsumer.accept(fArray[n2]);
            ++n2;
        }
    }

    private static void copyStereo(float[] fArray, int n, float[] fArray2, int n2, long l, FloatConsumer floatConsumer) {
        int n3 = 0;
        while ((long)n3 < l) {
            floatConsumer.accept(fArray[n + n3]);
            floatConsumer.accept(fArray2[n2 + n3]);
            ++n3;
        }
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }
}

