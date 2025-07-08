/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.slf4j.Logger;

public class MapItemSavedData
extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    private static final String FRAME_PREFIX = "frame-";
    public static final Codec<MapItemSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(mapItemSavedData -> mapItemSavedData.dimension), (App)Codec.INT.fieldOf("xCenter").forGetter(mapItemSavedData -> mapItemSavedData.centerX), (App)Codec.INT.fieldOf("zCenter").forGetter(mapItemSavedData -> mapItemSavedData.centerZ), (App)Codec.BYTE.optionalFieldOf("scale", (Object)0).forGetter(mapItemSavedData -> mapItemSavedData.scale), (App)Codec.BYTE_BUFFER.fieldOf("colors").forGetter(mapItemSavedData -> ByteBuffer.wrap(mapItemSavedData.colors)), (App)Codec.BOOL.optionalFieldOf("trackingPosition", (Object)true).forGetter(mapItemSavedData -> mapItemSavedData.trackingPosition), (App)Codec.BOOL.optionalFieldOf("unlimitedTracking", (Object)false).forGetter(mapItemSavedData -> mapItemSavedData.unlimitedTracking), (App)Codec.BOOL.optionalFieldOf("locked", (Object)false).forGetter(mapItemSavedData -> mapItemSavedData.locked), (App)MapBanner.CODEC.listOf().optionalFieldOf("banners", List.of()).forGetter(mapItemSavedData -> List.copyOf(mapItemSavedData.bannerMarkers.values())), (App)MapFrame.CODEC.listOf().optionalFieldOf("frames", List.of()).forGetter(mapItemSavedData -> List.copyOf(mapItemSavedData.frameMarkers.values()))).apply((Applicative)instance, MapItemSavedData::new));
    public final int centerX;
    public final int centerZ;
    public final ResourceKey<Level> dimension;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    public static SavedDataType<MapItemSavedData> type(MapId mapId) {
        return new SavedDataType<MapItemSavedData>(mapId.key(), () -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private MapItemSavedData(int n, int n2, byte by, boolean bl, boolean bl2, boolean bl3, ResourceKey<Level> resourceKey) {
        this.scale = by;
        this.centerX = n;
        this.centerZ = n2;
        this.dimension = resourceKey;
        this.trackingPosition = bl;
        this.unlimitedTracking = bl2;
        this.locked = bl3;
    }

    private MapItemSavedData(ResourceKey<Level> resourceKey, int n, int n2, byte by, ByteBuffer byteBuffer, boolean bl, boolean bl2, boolean bl3, List<MapBanner> list, List<MapFrame> list2) {
        this(n, n2, (byte)Mth.clamp(by, 0, 4), bl, bl2, bl3, resourceKey);
        if (byteBuffer.array().length == 16384) {
            this.colors = byteBuffer.array();
        }
        for (MapBanner record : list) {
            this.bannerMarkers.put(record.getId(), record);
            this.addDecoration(record.getDecoration(), null, record.getId(), record.pos().getX(), record.pos().getZ(), 180.0, record.name().orElse(null));
        }
        for (MapFrame mapFrame : list2) {
            this.frameMarkers.put(mapFrame.getId(), mapFrame);
            this.addDecoration(MapDecorationTypes.FRAME, null, MapItemSavedData.getFrameKey(mapFrame.entityId()), mapFrame.pos().getX(), mapFrame.pos().getZ(), mapFrame.rotation(), null);
        }
    }

    public static MapItemSavedData createFresh(double d, double d2, byte by, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int n = 128 * (1 << by);
        int n2 = Mth.floor((d + 64.0) / (double)n);
        int n3 = Mth.floor((d2 + 64.0) / (double)n);
        int n4 = n2 * n + n / 2 - 64;
        int n5 = n3 * n + n / 2 - 64;
        return new MapItemSavedData(n4, n5, by, bl, bl2, false, resourceKey);
    }

    public static MapItemSavedData createForClient(byte by, boolean bl, ResourceKey<Level> resourceKey) {
        return new MapItemSavedData(0, 0, by, false, false, bl, resourceKey);
    }

    public MapItemSavedData locked() {
        MapItemSavedData mapItemSavedData = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
        mapItemSavedData.bannerMarkers.putAll(this.bannerMarkers);
        mapItemSavedData.decorations.putAll(this.decorations);
        mapItemSavedData.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, mapItemSavedData.colors, 0, this.colors.length);
        return mapItemSavedData;
    }

    public MapItemSavedData scaled() {
        return MapItemSavedData.createFresh(this.centerX, this.centerZ, (byte)Mth.clamp(this.scale + 1, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> mapMatcher(ItemStack itemStack) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        return itemStack2 -> {
            if (itemStack2 == itemStack) {
                return true;
            }
            return itemStack2.is(itemStack.getItem()) && Objects.equals(mapId, itemStack2.get(DataComponents.MAP_ID));
        };
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        if (!this.carriedByPlayers.containsKey(player)) {
            object4 = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, (HoldingPlayer)object4);
            this.carriedBy.add((HoldingPlayer)object4);
        }
        object4 = MapItemSavedData.mapMatcher(itemStack);
        if (!player.getInventory().contains((Predicate<ItemStack>)object4)) {
            this.removeDecoration(player.getName().getString());
        }
        for (int i = 0; i < this.carriedBy.size(); ++i) {
            object3 = this.carriedBy.get(i);
            object2 = ((HoldingPlayer)object3).player;
            object = ((Player)object2).getName().getString();
            if (((Entity)object2).isRemoved() || !((Player)object2).getInventory().contains((Predicate<ItemStack>)object4) && !itemStack.isFramed()) {
                this.carriedByPlayers.remove(object2);
                this.carriedBy.remove(object3);
                this.removeDecoration((String)object);
            } else if (!itemStack.isFramed() && ((Entity)object2).level().dimension() == this.dimension && this.trackingPosition) {
                this.addDecoration(MapDecorationTypes.PLAYER, ((Entity)object2).level(), (String)object, ((Entity)object2).getX(), ((Entity)object2).getZ(), ((Entity)object2).getYRot(), null);
            }
            if (((Entity)object2).equals(player) || !MapItemSavedData.hasMapInvisibilityItemEquipped((Player)object2)) continue;
            this.removeDecoration((String)object);
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame itemFrame = itemStack.getFrame();
            object3 = itemFrame.getPos();
            object2 = this.frameMarkers.get(MapFrame.frameId((BlockPos)object3));
            if (object2 != null && itemFrame.getId() != ((MapFrame)object2).entityId() && this.frameMarkers.containsKey(((MapFrame)object2).getId())) {
                this.removeDecoration(MapItemSavedData.getFrameKey(((MapFrame)object2).entityId()));
            }
            object = new MapFrame((BlockPos)object3, itemFrame.getDirection().get2DDataValue() * 90, itemFrame.getId());
            this.addDecoration(MapDecorationTypes.FRAME, player.level(), MapItemSavedData.getFrameKey(itemFrame.getId()), ((Vec3i)object3).getX(), ((Vec3i)object3).getZ(), itemFrame.getDirection().get2DDataValue() * 90, null);
            MapFrame mapFrame = this.frameMarkers.put(((MapFrame)object).getId(), (MapFrame)object);
            if (!((MapFrame)object).equals(mapFrame)) {
                this.setDirty();
            }
        }
        MapDecorations mapDecorations = itemStack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        if (!this.decorations.keySet().containsAll(mapDecorations.decorations().keySet())) {
            mapDecorations.decorations().forEach((string, entry) -> {
                if (!this.decorations.containsKey(string)) {
                    this.addDecoration(entry.type(), player.level(), (String)string, entry.x(), entry.z(), entry.rotation(), null);
                }
            });
        }
    }

    private static boolean hasMapInvisibilityItemEquipped(Player player) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND || !player.getItemBySlot(equipmentSlot).is(ItemTags.MAP_INVISIBILITY_EQUIPMENT)) continue;
            return true;
        }
        return false;
    }

    private void removeDecoration(String string) {
        MapDecoration mapDecoration = this.decorations.remove(string);
        if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
            --this.trackedDecorationCount;
        }
        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String string, Holder<MapDecorationType> holder) {
        MapDecorations.Entry entry = new MapDecorations.Entry(holder, blockPos.getX(), blockPos.getZ(), 180.0f);
        itemStack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, mapDecorations -> mapDecorations.withDecoration(string, entry));
        if (holder.value().hasMapColor()) {
            itemStack.set(DataComponents.MAP_COLOR, new MapItemColor(holder.value().mapColor()));
        }
    }

    private void addDecoration(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, String string, double d, double d2, double d3, @Nullable Component component) {
        MapDecoration mapDecoration;
        int n = 1 << this.scale;
        float f = (float)(d - (double)this.centerX) / (float)n;
        float f2 = (float)(d2 - (double)this.centerZ) / (float)n;
        MapDecorationLocation mapDecorationLocation = this.calculateDecorationLocationAndType(holder, levelAccessor, d3, f, f2);
        if (mapDecorationLocation == null) {
            this.removeDecoration(string);
            return;
        }
        MapDecoration mapDecoration2 = new MapDecoration(mapDecorationLocation.type(), mapDecorationLocation.x(), mapDecorationLocation.y(), mapDecorationLocation.rot(), Optional.ofNullable(component));
        if (!mapDecoration2.equals(mapDecoration = this.decorations.put(string, mapDecoration2))) {
            if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
                --this.trackedDecorationCount;
            }
            if (mapDecorationLocation.type().value().trackCount()) {
                ++this.trackedDecorationCount;
            }
            this.setDecorationsDirty();
        }
    }

    @Nullable
    private MapDecorationLocation calculateDecorationLocationAndType(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float f2) {
        byte by = MapItemSavedData.clampMapCoordinate(f);
        byte by2 = MapItemSavedData.clampMapCoordinate(f2);
        if (holder.is(MapDecorationTypes.PLAYER)) {
            Pair<Holder<MapDecorationType>, Byte> pair = this.playerDecorationTypeAndRotation(holder, levelAccessor, d, f, f2);
            return pair == null ? null : new MapDecorationLocation((Holder)pair.getFirst(), by, by2, (Byte)pair.getSecond());
        }
        if (MapItemSavedData.isInsideMap(f, f2) || this.unlimitedTracking) {
            return new MapDecorationLocation(holder, by, by2, this.calculateRotation(levelAccessor, d));
        }
        return null;
    }

    @Nullable
    private Pair<Holder<MapDecorationType>, Byte> playerDecorationTypeAndRotation(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float f2) {
        if (MapItemSavedData.isInsideMap(f, f2)) {
            return Pair.of(holder, (Object)this.calculateRotation(levelAccessor, d));
        }
        Holder<MapDecorationType> holder2 = this.decorationTypeForPlayerOutsideMap(f, f2);
        if (holder2 == null) {
            return null;
        }
        return Pair.of(holder2, (Object)0);
    }

    private byte calculateRotation(@Nullable LevelAccessor levelAccessor, double d) {
        if (this.dimension == Level.NETHER && levelAccessor != null) {
            int n = (int)(levelAccessor.getLevelData().getDayTime() / 10L);
            return (byte)(n * n * 34187121 + n * 121 >> 15 & 0xF);
        }
        double d2 = d < 0.0 ? d - 8.0 : d + 8.0;
        return (byte)(d2 * 16.0 / 360.0);
    }

    private static boolean isInsideMap(float f, float f2) {
        int n = 63;
        return f >= -63.0f && f2 >= -63.0f && f <= 63.0f && f2 <= 63.0f;
    }

    @Nullable
    private Holder<MapDecorationType> decorationTypeForPlayerOutsideMap(float f, float f2) {
        boolean bl;
        int n = 320;
        boolean bl2 = bl = Math.abs(f) < 320.0f && Math.abs(f2) < 320.0f;
        if (bl) {
            return MapDecorationTypes.PLAYER_OFF_MAP;
        }
        return this.unlimitedTracking ? MapDecorationTypes.PLAYER_OFF_LIMITS : null;
    }

    private static byte clampMapCoordinate(float f) {
        int n = 63;
        if (f <= -63.0f) {
            return -128;
        }
        if (f >= 63.0f) {
            return 127;
        }
        return (byte)((double)(f * 2.0f) + 0.5);
    }

    @Nullable
    public Packet<?> getUpdatePacket(MapId mapId, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(mapId);
    }

    private void setColorsDirty(int n, int n2) {
        this.setDirty();
        for (HoldingPlayer holdingPlayer : this.carriedBy) {
            holdingPlayer.markColorsDirty(n, n2);
        }
    }

    private void setDecorationsDirty() {
        this.carriedBy.forEach(HoldingPlayer::markDecorationsDirty);
    }

    public HoldingPlayer getHoldingPlayer(Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        return holdingPlayer;
    }

    public boolean toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5;
        double d2 = (double)blockPos.getZ() + 0.5;
        int n = 1 << this.scale;
        double d3 = (d - (double)this.centerX) / (double)n;
        double d4 = (d2 - (double)this.centerZ) / (double)n;
        int n2 = 63;
        if (d3 >= -63.0 && d4 >= -63.0 && d3 <= 63.0 && d4 <= 63.0) {
            MapBanner mapBanner = MapBanner.fromWorld(levelAccessor, blockPos);
            if (mapBanner == null) {
                return false;
            }
            if (this.bannerMarkers.remove(mapBanner.getId(), mapBanner)) {
                this.removeDecoration(mapBanner.getId());
                this.setDirty();
                return true;
            }
            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(mapBanner.getId(), mapBanner);
                this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, d2, 180.0, mapBanner.name().orElse(null));
                this.setDirty();
                return true;
            }
        }
        return false;
    }

    public void checkBanners(BlockGetter blockGetter, int n, int n2) {
        Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();
        while (iterator.hasNext()) {
            MapBanner mapBanner;
            MapBanner mapBanner2 = iterator.next();
            if (mapBanner2.pos().getX() != n || mapBanner2.pos().getZ() != n2 || mapBanner2.equals(mapBanner = MapBanner.fromWorld(blockGetter, mapBanner2.pos()))) continue;
            iterator.remove();
            this.removeDecoration(mapBanner2.getId());
            this.setDirty();
        }
    }

    public Collection<MapBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPos blockPos, int n) {
        this.removeDecoration(MapItemSavedData.getFrameKey(n));
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
        this.setDirty();
    }

    public boolean updateColor(int n, int n2, byte by) {
        byte by2 = this.colors[n + n2 * 128];
        if (by2 != by) {
            this.setColor(n, n2, by);
            return true;
        }
        return false;
    }

    public void setColor(int n, int n2, byte by) {
        this.colors[n + n2 * 128] = by;
        this.setColorsDirty(n, n2);
    }

    public boolean isExplorationMap() {
        for (MapDecoration mapDecoration : this.decorations.values()) {
            if (!mapDecoration.type().value().explorationMapElement()) continue;
            return true;
        }
        return false;
    }

    public void addClientSideDecorations(List<MapDecoration> list) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;
        for (int i = 0; i < list.size(); ++i) {
            MapDecoration mapDecoration = list.get(i);
            this.decorations.put("icon-" + i, mapDecoration);
            if (!mapDecoration.type().value().trackCount()) continue;
            ++this.trackedDecorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int n) {
        return this.trackedDecorationCount >= n;
    }

    private static String getFrameKey(int n) {
        return FRAME_PREFIX + n;
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        HoldingPlayer(Player player) {
            this.player = player;
        }

        private MapPatch createPatch() {
            int n = this.minDirtyX;
            int n2 = this.minDirtyY;
            int n3 = this.maxDirtyX + 1 - this.minDirtyX;
            int n4 = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] byArray = new byte[n3 * n4];
            for (int i = 0; i < n3; ++i) {
                for (int j = 0; j < n4; ++j) {
                    byArray[i + j * n3] = MapItemSavedData.this.colors[n + i + (n2 + j) * 128];
                }
            }
            return new MapPatch(n, n2, n3, n4, byArray);
        }

        @Nullable
        Packet<?> nextUpdatePacket(MapId mapId) {
            Collection<MapDecoration> collection;
            MapPatch mapPatch;
            if (this.dirtyData) {
                this.dirtyData = false;
                mapPatch = this.createPatch();
            } else {
                mapPatch = null;
            }
            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                collection = MapItemSavedData.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || mapPatch != null) {
                return new ClientboundMapItemDataPacket(mapId, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapPatch);
            }
            return null;
        }

        void markColorsDirty(int n, int n2) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, n);
                this.minDirtyY = Math.min(this.minDirtyY, n2);
                this.maxDirtyX = Math.max(this.maxDirtyX, n);
                this.maxDirtyY = Math.max(this.maxDirtyY, n2);
            } else {
                this.dirtyData = true;
                this.minDirtyX = n;
                this.minDirtyY = n2;
                this.maxDirtyX = n;
                this.maxDirtyY = n2;
            }
        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    record MapDecorationLocation(Holder<MapDecorationType> type, byte x, byte y, byte rot) {
    }

    public record MapPatch(int startX, int startY, int width, int height, byte[] mapColors) {
        public static final StreamCodec<ByteBuf, Optional<MapPatch>> STREAM_CODEC = StreamCodec.of(MapPatch::write, MapPatch::read);

        private static void write(ByteBuf byteBuf, Optional<MapPatch> optional) {
            if (optional.isPresent()) {
                MapPatch mapPatch = optional.get();
                byteBuf.writeByte(mapPatch.width);
                byteBuf.writeByte(mapPatch.height);
                byteBuf.writeByte(mapPatch.startX);
                byteBuf.writeByte(mapPatch.startY);
                FriendlyByteBuf.writeByteArray(byteBuf, mapPatch.mapColors);
            } else {
                byteBuf.writeByte(0);
            }
        }

        private static Optional<MapPatch> read(ByteBuf byteBuf) {
            short s = byteBuf.readUnsignedByte();
            if (s > 0) {
                short s2 = byteBuf.readUnsignedByte();
                short s3 = byteBuf.readUnsignedByte();
                short s4 = byteBuf.readUnsignedByte();
                byte[] byArray = FriendlyByteBuf.readByteArray(byteBuf);
                return Optional.of(new MapPatch(s3, s4, s, s2, byArray));
            }
            return Optional.empty();
        }

        public void applyToMap(MapItemSavedData mapItemSavedData) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapItemSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
                }
            }
        }
    }
}

