/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.audio.ListenerTransform;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.MultipliedFloats;
import org.slf4j.Logger;

public class SoundManager
extends SimplePreparableReloadListener<Preparations> {
    public static final ResourceLocation EMPTY_SOUND_LOCATION = ResourceLocation.withDefaultNamespace("empty");
    public static final Sound EMPTY_SOUND = new Sound(EMPTY_SOUND_LOCATION, ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), 1, Sound.Type.FILE, false, false, 16);
    public static final ResourceLocation INTENTIONALLY_EMPTY_SOUND_LOCATION = ResourceLocation.withDefaultNamespace("intentionally_empty");
    public static final WeighedSoundEvents INTENTIONALLY_EMPTY_SOUND_EVENT = new WeighedSoundEvents(INTENTIONALLY_EMPTY_SOUND_LOCATION, null);
    public static final Sound INTENTIONALLY_EMPTY_SOUND = new Sound(INTENTIONALLY_EMPTY_SOUND_LOCATION, ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), 1, Sound.Type.FILE, false, false, 16);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String SOUNDS_PATH = "sounds.json";
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(SoundEventRegistration.class, (Object)new SoundEventRegistrationSerializer()).create();
    private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>(){};
    private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
    private final SoundEngine soundEngine;
    private final Map<ResourceLocation, Resource> soundCache = new HashMap<ResourceLocation, Resource>();

    public SoundManager(Options options, MusicManager musicManager) {
        this.soundEngine = new SoundEngine(musicManager, this, options, ResourceProvider.fromMap(this.soundCache));
    }

    @Override
    protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Preparations preparations = new Preparations();
        try (Zone zone = profilerFiller.zone("list");){
            preparations.listResources(resourceManager);
        }
        for (String string : resourceManager.getNamespaces()) {
            try {
                Zone zone = profilerFiller.zone(string);
                try {
                    List<Resource> list = resourceManager.getResourceStack(ResourceLocation.fromNamespaceAndPath(string, SOUNDS_PATH));
                    for (Resource resource : list) {
                        profilerFiller.push(resource.sourcePackId());
                        try (BufferedReader bufferedReader = resource.openAsReader();){
                            profilerFiller.push("parse");
                            Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, (Reader)bufferedReader, SOUND_EVENT_REGISTRATION_TYPE);
                            profilerFiller.popPush("register");
                            for (Map.Entry<String, SoundEventRegistration> entry : map.entrySet()) {
                                preparations.handleRegistration(ResourceLocation.fromNamespaceAndPath(string, entry.getKey()), entry.getValue());
                            }
                            profilerFiller.pop();
                        }
                        catch (RuntimeException runtimeException) {
                            LOGGER.warn("Invalid {} in resourcepack: '{}'", new Object[]{SOUNDS_PATH, resource.sourcePackId(), runtimeException});
                        }
                        profilerFiller.pop();
                    }
                }
                finally {
                    if (zone == null) continue;
                    zone.close();
                }
            }
            catch (IOException iOException) {}
        }
        return preparations;
    }

    @Override
    protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        preparations.apply(this.registry, this.soundCache, this.soundEngine);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            for (ResourceLocation resourceLocation : this.registry.keySet()) {
                WeighedSoundEvents weighedSoundEvents = this.registry.get(resourceLocation);
                if (ComponentUtils.isTranslationResolvable(weighedSoundEvents.getSubtitle()) || !BuiltInRegistries.SOUND_EVENT.containsKey(resourceLocation)) continue;
                LOGGER.error("Missing subtitle {} for sound event: {}", (Object)weighedSoundEvents.getSubtitle(), (Object)resourceLocation);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            for (ResourceLocation resourceLocation : this.registry.keySet()) {
                if (BuiltInRegistries.SOUND_EVENT.containsKey(resourceLocation)) continue;
                LOGGER.debug("Not having sound event for: {}", (Object)resourceLocation);
            }
        }
        this.soundEngine.reload();
    }

    public List<String> getAvailableSoundDevices() {
        return this.soundEngine.getAvailableSoundDevices();
    }

    public ListenerTransform getListenerTransform() {
        return this.soundEngine.getListenerTransform();
    }

    static boolean validateSoundResource(Sound sound, ResourceLocation resourceLocation, ResourceProvider resourceProvider) {
        ResourceLocation resourceLocation2 = sound.getPath();
        if (resourceProvider.getResource(resourceLocation2).isEmpty()) {
            LOGGER.warn("File {} does not exist, cannot add it to event {}", (Object)resourceLocation2, (Object)resourceLocation);
            return false;
        }
        return true;
    }

    @Nullable
    public WeighedSoundEvents getSoundEvent(ResourceLocation resourceLocation) {
        return this.registry.get(resourceLocation);
    }

    public Collection<ResourceLocation> getAvailableSounds() {
        return this.registry.keySet();
    }

    public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {
        this.soundEngine.queueTickingSound(tickableSoundInstance);
    }

    public SoundEngine.PlayResult play(SoundInstance soundInstance) {
        return this.soundEngine.play(soundInstance);
    }

    public void playDelayed(SoundInstance soundInstance, int n) {
        this.soundEngine.playDelayed(soundInstance, n);
    }

    public void updateSource(Camera camera) {
        this.soundEngine.updateSource(camera);
    }

    public void pauseAllExcept(SoundSource ... soundSourceArray) {
        this.soundEngine.pauseAllExcept(soundSourceArray);
    }

    public void stop() {
        this.soundEngine.stopAll();
    }

    public void destroy() {
        this.soundEngine.destroy();
    }

    public void emergencyShutdown() {
        this.soundEngine.emergencyShutdown();
    }

    public void tick(boolean bl) {
        this.soundEngine.tick(bl);
    }

    public void resume() {
        this.soundEngine.resume();
    }

    public void updateSourceVolume(SoundSource soundSource, float f) {
        this.soundEngine.updateCategoryVolume(soundSource, f);
    }

    public void stop(SoundInstance soundInstance) {
        this.soundEngine.stop(soundInstance);
    }

    public void setVolume(SoundInstance soundInstance, float f) {
        this.soundEngine.setVolume(soundInstance, f);
    }

    public boolean isActive(SoundInstance soundInstance) {
        return this.soundEngine.isActive(soundInstance);
    }

    public void addListener(SoundEventListener soundEventListener) {
        this.soundEngine.addEventListener(soundEventListener);
    }

    public void removeListener(SoundEventListener soundEventListener) {
        this.soundEngine.removeEventListener(soundEventListener);
    }

    public void stop(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
        this.soundEngine.stop(resourceLocation, soundSource);
    }

    public String getDebugString() {
        return this.soundEngine.getDebugString();
    }

    public void reload() {
        this.soundEngine.reload();
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    protected static class Preparations {
        final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
        private Map<ResourceLocation, Resource> soundCache = Map.of();

        protected Preparations() {
        }

        void listResources(ResourceManager resourceManager) {
            this.soundCache = Sound.SOUND_LISTER.listMatchingResources(resourceManager);
        }

        void handleRegistration(ResourceLocation resourceLocation, SoundEventRegistration soundEventRegistration) {
            boolean bl;
            WeighedSoundEvents weighedSoundEvents = this.registry.get(resourceLocation);
            boolean bl2 = bl = weighedSoundEvents == null;
            if (bl || soundEventRegistration.isReplace()) {
                if (!bl) {
                    LOGGER.debug("Replaced sound event location {}", (Object)resourceLocation);
                }
                weighedSoundEvents = new WeighedSoundEvents(resourceLocation, soundEventRegistration.getSubtitle());
                this.registry.put(resourceLocation, weighedSoundEvents);
            }
            ResourceProvider resourceProvider = ResourceProvider.fromMap(this.soundCache);
            block4: for (final Sound sound : soundEventRegistration.getSounds()) {
                final ResourceLocation resourceLocation2 = sound.getLocation();
                weighedSoundEvents.addSound(switch (sound.getType()) {
                    case Sound.Type.FILE -> {
                        if (!SoundManager.validateSoundResource(sound, resourceLocation, resourceProvider)) continue block4;
                        yield sound;
                    }
                    case Sound.Type.SOUND_EVENT -> new Weighted<Sound>(){

                        @Override
                        public int getWeight() {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            return weighedSoundEvents == null ? 0 : weighedSoundEvents.getWeight();
                        }

                        @Override
                        public Sound getSound(RandomSource randomSource) {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            if (weighedSoundEvents == null) {
                                return EMPTY_SOUND;
                            }
                            Sound sound2 = weighedSoundEvents.getSound(randomSource);
                            return new Sound(sound2.getLocation(), new MultipliedFloats(sound2.getVolume(), sound.getVolume()), new MultipliedFloats(sound2.getPitch(), sound.getPitch()), sound.getWeight(), Sound.Type.FILE, sound2.shouldStream() || sound.shouldStream(), sound2.shouldPreload(), sound2.getAttenuationDistance());
                        }

                        @Override
                        public void preloadIfRequired(SoundEngine soundEngine) {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            if (weighedSoundEvents == null) {
                                return;
                            }
                            weighedSoundEvents.preloadIfRequired(soundEngine);
                        }

                        @Override
                        public /* synthetic */ Object getSound(RandomSource randomSource) {
                            return this.getSound(randomSource);
                        }
                    };
                    default -> throw new IllegalStateException("Unknown SoundEventRegistration type: " + String.valueOf((Object)sound.getType()));
                });
            }
        }

        public void apply(Map<ResourceLocation, WeighedSoundEvents> map, Map<ResourceLocation, Resource> map2, SoundEngine soundEngine) {
            map.clear();
            map2.clear();
            map2.putAll(this.soundCache);
            for (Map.Entry<ResourceLocation, WeighedSoundEvents> entry : this.registry.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
                entry.getValue().preloadIfRequired(soundEngine);
            }
        }
    }
}

