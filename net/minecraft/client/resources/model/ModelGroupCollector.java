/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ModelGroupCollector {
    static final int SINGLETON_MODEL_GROUP = -1;
    private static final int INVISIBLE_MODEL_GROUP = 0;

    public static Object2IntMap<BlockState> build(BlockColors blockColors, BlockStateModelLoader.LoadedModels loadedModels) {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        loadedModels.models().forEach((blockState, unbakedRoot) -> {
            List list = hashMap.computeIfAbsent(blockState.getBlock(), block -> List.copyOf(blockColors.getColoringProperties((Block)block)));
            GroupKey groupKey2 = GroupKey.create(blockState, unbakedRoot, list);
            hashMap2.computeIfAbsent(groupKey2, groupKey -> Sets.newIdentityHashSet()).add(blockState);
        });
        int n = 1;
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        object2IntOpenHashMap.defaultReturnValue(-1);
        for (Set set : hashMap2.values()) {
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                BlockState blockState2 = (BlockState)iterator.next();
                if (blockState2.getRenderShape() == RenderShape.MODEL) continue;
                iterator.remove();
                object2IntOpenHashMap.put((Object)blockState2, 0);
            }
            if (set.size() <= 1) continue;
            int n2 = n++;
            set.forEach(arg_0 -> ModelGroupCollector.lambda$build$3((Object2IntMap)object2IntOpenHashMap, n2, arg_0));
        }
        return object2IntOpenHashMap;
    }

    private static /* synthetic */ void lambda$build$3(Object2IntMap object2IntMap, int n, BlockState blockState) {
        object2IntMap.put((Object)blockState, n);
    }

    record GroupKey(Object equalityGroup, List<Object> coloringValues) {
        public static GroupKey create(BlockState blockState, BlockStateModel.UnbakedRoot unbakedRoot, List<Property<?>> list) {
            List<Object> list2 = GroupKey.getColoringValues(blockState, list);
            Object object = unbakedRoot.visualEqualityGroup(blockState);
            return new GroupKey(object, list2);
        }

        private static List<Object> getColoringValues(BlockState blockState, List<Property<?>> list) {
            Object[] objectArray = new Object[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                objectArray[i] = blockState.getValue(list.get(i));
            }
            return List.of(objectArray);
        }
    }
}

