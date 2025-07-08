/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public class SoundBufferLibrary {
    private final ResourceProvider resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceProvider resourceProvider) {
        this.resourceManager = resourceProvider;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourceLocation2) {
        return this.cache.computeIfAbsent(resourceLocation2, resourceLocation -> CompletableFuture.supplyAsync(() -> {
            try (InputStream inputStream = this.resourceManager.open((ResourceLocation)resourceLocation);){
                SoundBuffer soundBuffer;
                try (JOrbisAudioStream jOrbisAudioStream = new JOrbisAudioStream(inputStream);){
                    ByteBuffer byteBuffer = jOrbisAudioStream.readAll();
                    soundBuffer = new SoundBuffer(byteBuffer, jOrbisAudioStream.getFormat());
                }
                return soundBuffer;
            }
            catch (IOException iOException) {
                throw new CompletionException(iOException);
            }
        }, Util.nonCriticalIoPool()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation resourceLocation, boolean bl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputStream = this.resourceManager.open(resourceLocation);
                return bl ? new LoopingAudioStream(JOrbisAudioStream::new, inputStream) : new JOrbisAudioStream(inputStream);
            }
            catch (IOException iOException) {
                throw new CompletionException(iOException);
            }
        }, Util.nonCriticalIoPool());
    }

    public void clear() {
        this.cache.values().forEach(completableFuture -> completableFuture.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> collection) {
        return CompletableFuture.allOf((CompletableFuture[])collection.stream().map(sound -> this.getCompleteBuffer(sound.getPath())).toArray(CompletableFuture[]::new));
    }
}

