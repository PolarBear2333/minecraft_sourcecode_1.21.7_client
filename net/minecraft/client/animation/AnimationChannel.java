/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.animation;

import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record AnimationChannel(Target target, Keyframe[] keyframes) {

    public static interface Target {
        public void apply(ModelPart var1, Vector3f var2);
    }

    public static class Interpolations {
        public static final Interpolation LINEAR = (vector3f, f, keyframeArray, n, n2, f2) -> {
            Vector3f vector3f2 = keyframeArray[n].target();
            Vector3f vector3f3 = keyframeArray[n2].target();
            return vector3f2.lerp((Vector3fc)vector3f3, f, vector3f).mul(f2);
        };
        public static final Interpolation CATMULLROM = (vector3f, f, keyframeArray, n, n2, f2) -> {
            Vector3f vector3f2 = keyframeArray[Math.max(0, n - 1)].target();
            Vector3f vector3f3 = keyframeArray[n].target();
            Vector3f vector3f4 = keyframeArray[n2].target();
            Vector3f vector3f5 = keyframeArray[Math.min(keyframeArray.length - 1, n2 + 1)].target();
            vector3f.set(Mth.catmullrom(f, vector3f2.x(), vector3f3.x(), vector3f4.x(), vector3f5.x()) * f2, Mth.catmullrom(f, vector3f2.y(), vector3f3.y(), vector3f4.y(), vector3f5.y()) * f2, Mth.catmullrom(f, vector3f2.z(), vector3f3.z(), vector3f4.z(), vector3f5.z()) * f2);
            return vector3f;
        };
    }

    public static class Targets {
        public static final Target POSITION = ModelPart::offsetPos;
        public static final Target ROTATION = ModelPart::offsetRotation;
        public static final Target SCALE = ModelPart::offsetScale;
    }

    public static interface Interpolation {
        public Vector3f apply(Vector3f var1, float var2, Keyframe[] var3, int var4, int var5, float var6);
    }
}

