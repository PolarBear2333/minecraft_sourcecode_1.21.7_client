/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class AttributeMap {
    private final Map<Holder<Attribute>, AttributeInstance> attributes = new Object2ObjectOpenHashMap();
    private final Set<AttributeInstance> attributesToSync = new ObjectOpenHashSet();
    private final Set<AttributeInstance> attributesToUpdate = new ObjectOpenHashSet();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier attributeSupplier) {
        this.supplier = attributeSupplier;
    }

    private void onAttributeModified(AttributeInstance attributeInstance) {
        this.attributesToUpdate.add(attributeInstance);
        if (attributeInstance.getAttribute().value().isClientSyncable()) {
            this.attributesToSync.add(attributeInstance);
        }
    }

    public Set<AttributeInstance> getAttributesToSync() {
        return this.attributesToSync;
    }

    public Set<AttributeInstance> getAttributesToUpdate() {
        return this.attributesToUpdate;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return this.attributes.values().stream().filter(attributeInstance -> attributeInstance.getAttribute().value().isClientSyncable()).collect(Collectors.toList());
    }

    @Nullable
    public AttributeInstance getInstance(Holder<Attribute> holder2) {
        return this.attributes.computeIfAbsent(holder2, holder -> this.supplier.createInstance(this::onAttributeModified, (Holder<Attribute>)holder));
    }

    public boolean hasAttribute(Holder<Attribute> holder) {
        return this.attributes.get(holder) != null || this.supplier.hasAttribute(holder);
    }

    public boolean hasModifier(Holder<Attribute> holder, ResourceLocation resourceLocation) {
        AttributeInstance attributeInstance = this.attributes.get(holder);
        return attributeInstance != null ? attributeInstance.getModifier(resourceLocation) != null : this.supplier.hasModifier(holder, resourceLocation);
    }

    public double getValue(Holder<Attribute> holder) {
        AttributeInstance attributeInstance = this.attributes.get(holder);
        return attributeInstance != null ? attributeInstance.getValue() : this.supplier.getValue(holder);
    }

    public double getBaseValue(Holder<Attribute> holder) {
        AttributeInstance attributeInstance = this.attributes.get(holder);
        return attributeInstance != null ? attributeInstance.getBaseValue() : this.supplier.getBaseValue(holder);
    }

    public double getModifierValue(Holder<Attribute> holder, ResourceLocation resourceLocation) {
        AttributeInstance attributeInstance = this.attributes.get(holder);
        return attributeInstance != null ? attributeInstance.getModifier(resourceLocation).amount() : this.supplier.getModifierValue(holder, resourceLocation);
    }

    public void addTransientAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
        multimap.forEach((holder, attributeModifier) -> {
            AttributeInstance attributeInstance = this.getInstance((Holder<Attribute>)holder);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(attributeModifier.id());
                attributeInstance.addTransientModifier((AttributeModifier)attributeModifier);
            }
        });
    }

    public void removeAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
        multimap.asMap().forEach((holder, collection) -> {
            AttributeInstance attributeInstance = this.attributes.get(holder);
            if (attributeInstance != null) {
                collection.forEach(attributeModifier -> attributeInstance.removeModifier(attributeModifier.id()));
            }
        });
    }

    public void assignAllValues(AttributeMap attributeMap) {
        attributeMap.attributes.values().forEach(attributeInstance -> {
            AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
            if (attributeInstance2 != null) {
                attributeInstance2.replaceFrom((AttributeInstance)attributeInstance);
            }
        });
    }

    public void assignBaseValues(AttributeMap attributeMap) {
        attributeMap.attributes.values().forEach(attributeInstance -> {
            AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
            if (attributeInstance2 != null) {
                attributeInstance2.setBaseValue(attributeInstance.getBaseValue());
            }
        });
    }

    public void assignPermanentModifiers(AttributeMap attributeMap) {
        attributeMap.attributes.values().forEach(attributeInstance -> {
            AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
            if (attributeInstance2 != null) {
                attributeInstance2.addPermanentModifiers(attributeInstance.getPermanentModifiers());
            }
        });
    }

    public boolean resetBaseValue(Holder<Attribute> holder) {
        if (!this.supplier.hasAttribute(holder)) {
            return false;
        }
        AttributeInstance attributeInstance = this.attributes.get(holder);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(this.supplier.getBaseValue(holder));
        }
        return true;
    }

    public List<AttributeInstance.Packed> pack() {
        ArrayList<AttributeInstance.Packed> arrayList = new ArrayList<AttributeInstance.Packed>(this.attributes.values().size());
        for (AttributeInstance attributeInstance : this.attributes.values()) {
            arrayList.add(attributeInstance.pack());
        }
        return arrayList;
    }

    public void apply(List<AttributeInstance.Packed> list) {
        for (AttributeInstance.Packed packed : list) {
            AttributeInstance attributeInstance = this.getInstance(packed.attribute());
            if (attributeInstance == null) continue;
            attributeInstance.apply(packed);
        }
    }
}

