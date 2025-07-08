/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapDecoder
 *  com.mojang.serialization.MapEncoder
 *  com.mojang.serialization.MapLike
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public final class CustomData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomData EMPTY = new CustomData(new CompoundTag());
    private static final String TYPE_TAG = "id";
    public static final Codec<CustomData> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.FLATTENED_CODEC).xmap(CustomData::new, customData -> customData.tag);
    public static final Codec<CustomData> CODEC_WITH_ID = CODEC.validate(customData -> customData.getUnsafe().getString(TYPE_TAG).isPresent() ? DataResult.success((Object)customData) : DataResult.error(() -> "Missing id for entity in: " + String.valueOf(customData)));
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, customData -> customData.tag);
    private final CompoundTag tag;

    private CustomData(CompoundTag compoundTag) {
        this.tag = compoundTag;
    }

    public static CustomData of(CompoundTag compoundTag) {
        return new CustomData(compoundTag.copy());
    }

    public boolean matchedBy(CompoundTag compoundTag) {
        return NbtUtils.compareNbt(compoundTag, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, Consumer<CompoundTag> consumer) {
        CustomData customData = itemStack.getOrDefault(dataComponentType, EMPTY).update(consumer);
        if (customData.tag.isEmpty()) {
            itemStack.remove(dataComponentType);
        } else {
            itemStack.set(dataComponentType, customData);
        }
    }

    public static void set(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, CompoundTag compoundTag) {
        if (!compoundTag.isEmpty()) {
            itemStack.set(dataComponentType, CustomData.of(compoundTag));
        } else {
            itemStack.remove(dataComponentType);
        }
    }

    public CustomData update(Consumer<CompoundTag> consumer) {
        CompoundTag compoundTag = this.tag.copy();
        consumer.accept(compoundTag);
        return new CustomData(compoundTag);
    }

    @Nullable
    public ResourceLocation parseEntityId() {
        return this.tag.read(TYPE_TAG, ResourceLocation.CODEC).orElse(null);
    }

    @Nullable
    public <T> T parseEntityType(HolderLookup.Provider provider, ResourceKey<? extends Registry<T>> resourceKey) {
        ResourceLocation resourceLocation = this.parseEntityId();
        if (resourceLocation == null) {
            return null;
        }
        return provider.lookup(resourceKey).flatMap(registryLookup -> registryLookup.get(ResourceKey.create(resourceKey, resourceLocation))).map(Holder::value).orElse(null);
    }

    public void loadInto(Entity entity) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
            entity.saveWithoutId(tagValueOutput);
            CompoundTag compoundTag = tagValueOutput.buildResult();
            UUID uUID = entity.getUUID();
            compoundTag.merge(this.tag);
            entity.load(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)entity.registryAccess(), compoundTag));
            entity.setUUID(uUID);
        }
    }

    /*
     * Exception decompiling
     */
    public boolean loadInto(BlockEntity var1_1, HolderLookup.Provider var2_2) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [5[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public <T> DataResult<CustomData> update(DynamicOps<Tag> dynamicOps, MapEncoder<T> mapEncoder, T t) {
        return mapEncoder.encode(t, dynamicOps, dynamicOps.mapBuilder()).build((Object)this.tag).map(tag -> new CustomData((CompoundTag)tag));
    }

    public <T> DataResult<T> read(MapDecoder<T> mapDecoder) {
        return this.read(NbtOps.INSTANCE, mapDecoder);
    }

    public <T> DataResult<T> read(DynamicOps<Tag> dynamicOps, MapDecoder<T> mapDecoder) {
        MapLike mapLike = (MapLike)dynamicOps.getMap((Object)this.tag).getOrThrow();
        return mapDecoder.decode(dynamicOps, mapLike);
    }

    public int size() {
        return this.tag.size();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public CompoundTag copyTag() {
        return this.tag.copy();
    }

    public boolean contains(String string) {
        return this.tag.contains(string);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CustomData) {
            CustomData customData = (CustomData)object;
            return this.tag.equals(customData.tag);
        }
        return false;
    }

    public int hashCode() {
        return this.tag.hashCode();
    }

    public String toString() {
        return this.tag.toString();
    }

    @Deprecated
    public CompoundTag getUnsafe() {
        return this.tag;
    }

    private static /* synthetic */ String lambda$loadInto$5() {
        return "(rollback)";
    }
}

