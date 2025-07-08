/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
    private final Map<ResourceLocation, CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(ItemStack itemStack) {
        return this.getCooldownPercent(itemStack, 0.0f) > 0.0f;
    }

    public float getCooldownPercent(ItemStack itemStack, float f) {
        ResourceLocation resourceLocation = this.getCooldownGroup(itemStack);
        CooldownInstance cooldownInstance = this.cooldowns.get(resourceLocation);
        if (cooldownInstance != null) {
            float f2 = cooldownInstance.endTime - cooldownInstance.startTime;
            float f3 = (float)cooldownInstance.endTime - ((float)this.tickCount + f);
            return Mth.clamp(f3 / f2, 0.0f, 1.0f);
        }
        return 0.0f;
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<ResourceLocation, CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, CooldownInstance> entry = iterator.next();
                if (entry.getValue().endTime > this.tickCount) continue;
                iterator.remove();
                this.onCooldownEnded(entry.getKey());
            }
        }
    }

    public ResourceLocation getCooldownGroup(ItemStack itemStack) {
        UseCooldown useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (useCooldown == null) {
            return resourceLocation;
        }
        return useCooldown.cooldownGroup().orElse(resourceLocation);
    }

    public void addCooldown(ItemStack itemStack, int n) {
        this.addCooldown(this.getCooldownGroup(itemStack), n);
    }

    public void addCooldown(ResourceLocation resourceLocation, int n) {
        this.cooldowns.put(resourceLocation, new CooldownInstance(this.tickCount, this.tickCount + n));
        this.onCooldownStarted(resourceLocation, n);
    }

    public void removeCooldown(ResourceLocation resourceLocation) {
        this.cooldowns.remove(resourceLocation);
        this.onCooldownEnded(resourceLocation);
    }

    protected void onCooldownStarted(ResourceLocation resourceLocation, int n) {
    }

    protected void onCooldownEnded(ResourceLocation resourceLocation) {
    }

    static final class CooldownInstance
    extends Record {
        final int startTime;
        final int endTime;

        CooldownInstance(int n, int n2) {
            this.startTime = n;
            this.endTime = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this, object);
        }

        public int startTime() {
            return this.startTime;
        }

        public int endTime() {
            return this.endTime;
        }
    }
}

