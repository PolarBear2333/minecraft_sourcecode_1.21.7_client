/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.resource;

import com.mojang.blaze3d.resource.ResourceDescriptor;

public interface GraphicsResourceAllocator {
    public static final GraphicsResourceAllocator UNPOOLED = new GraphicsResourceAllocator(){

        @Override
        public <T> T acquire(ResourceDescriptor<T> resourceDescriptor) {
            T t = resourceDescriptor.allocate();
            resourceDescriptor.prepare(t);
            return t;
        }

        @Override
        public <T> void release(ResourceDescriptor<T> resourceDescriptor, T t) {
            resourceDescriptor.free(t);
        }
    };

    public <T> T acquire(ResourceDescriptor<T> var1);

    public <T> void release(ResourceDescriptor<T> var1, T var2);
}

