/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.Model;

public record AdultAndBabyModelPair<T extends Model>(T adultModel, T babyModel) {
    public T getModel(boolean bl) {
        return bl ? this.babyModel : this.adultModel;
    }
}

