/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs.linkfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;

abstract class DummyFileAttributes
implements BasicFileAttributes {
    private static final FileTime EPOCH = FileTime.fromMillis(0L);

    DummyFileAttributes() {
    }

    @Override
    public FileTime lastModifiedTime() {
        return EPOCH;
    }

    @Override
    public FileTime lastAccessTime() {
        return EPOCH;
    }

    @Override
    public FileTime creationTime() {
        return EPOCH;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return 0L;
    }

    @Override
    @Nullable
    public Object fileKey() {
        return null;
    }
}

