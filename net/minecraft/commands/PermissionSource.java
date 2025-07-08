/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

import net.minecraft.server.commands.PermissionCheck;

public interface PermissionSource {
    public boolean hasPermission(int var1);

    default public boolean allowsSelectors() {
        return this.hasPermission(2);
    }

    public record Check<T extends PermissionSource>(int requiredLevel) implements PermissionCheck<T>
    {
        @Override
        public boolean test(T t) {
            return t.hasPermission(this.requiredLevel);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((T)((PermissionSource)object));
        }
    }
}

