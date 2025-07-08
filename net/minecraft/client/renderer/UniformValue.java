/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.joml.Matrix4fc
 *  org.joml.Vector2f
 *  org.joml.Vector2fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector3i
 *  org.joml.Vector3ic
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public interface UniformValue {
    public static final Codec<UniformValue> CODEC = Type.CODEC.dispatch(UniformValue::type, type -> type.valueCodec);

    public void writeTo(Std140Builder var1);

    public void addSize(Std140SizeCalculator var1);

    public Type type();

    public static enum Type implements StringRepresentable
    {
        INT("int", IntUniform.CODEC),
        IVEC3("ivec3", IVec3Uniform.CODEC),
        FLOAT("float", FloatUniform.CODEC),
        VEC2("vec2", Vec2Uniform.CODEC),
        VEC3("vec3", Vec3Uniform.CODEC),
        VEC4("vec4", Vec4Uniform.CODEC),
        MATRIX4X4("matrix4x4", Matrix4x4Uniform.CODEC);

        public static final StringRepresentable.EnumCodec<Type> CODEC;
        private final String name;
        final MapCodec<? extends UniformValue> valueCodec;

        private Type(String string2, Codec<? extends UniformValue> codec) {
            this.name = string2;
            this.valueCodec = codec.fieldOf("value");
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
        }
    }

    public record Matrix4x4Uniform(Matrix4fc value) implements UniformValue
    {
        public static final Codec<Matrix4x4Uniform> CODEC = ExtraCodecs.MATRIX4F.xmap(Matrix4x4Uniform::new, Matrix4x4Uniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putMat4f(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putMat4f();
        }

        @Override
        public Type type() {
            return Type.MATRIX4X4;
        }
    }

    public record Vec4Uniform(Vector4f value) implements UniformValue
    {
        public static final Codec<Vec4Uniform> CODEC = ExtraCodecs.VECTOR4F.xmap(Vec4Uniform::new, Vec4Uniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putVec4((Vector4fc)this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putVec4();
        }

        @Override
        public Type type() {
            return Type.VEC4;
        }
    }

    public record Vec3Uniform(Vector3f value) implements UniformValue
    {
        public static final Codec<Vec3Uniform> CODEC = ExtraCodecs.VECTOR3F.xmap(Vec3Uniform::new, Vec3Uniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putVec3((Vector3fc)this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putVec3();
        }

        @Override
        public Type type() {
            return Type.VEC3;
        }
    }

    public record Vec2Uniform(Vector2f value) implements UniformValue
    {
        public static final Codec<Vec2Uniform> CODEC = ExtraCodecs.VECTOR2F.xmap(Vec2Uniform::new, Vec2Uniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putVec2((Vector2fc)this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putVec2();
        }

        @Override
        public Type type() {
            return Type.VEC2;
        }
    }

    public record FloatUniform(float value) implements UniformValue
    {
        public static final Codec<FloatUniform> CODEC = Codec.FLOAT.xmap(FloatUniform::new, FloatUniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putFloat(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putFloat();
        }

        @Override
        public Type type() {
            return Type.FLOAT;
        }
    }

    public record IVec3Uniform(Vector3i value) implements UniformValue
    {
        public static final Codec<IVec3Uniform> CODEC = ExtraCodecs.VECTOR3I.xmap(IVec3Uniform::new, IVec3Uniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putIVec3((Vector3ic)this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putIVec3();
        }

        @Override
        public Type type() {
            return Type.IVEC3;
        }
    }

    public record IntUniform(int value) implements UniformValue
    {
        public static final Codec<IntUniform> CODEC = Codec.INT.xmap(IntUniform::new, IntUniform::value);

        @Override
        public void writeTo(Std140Builder std140Builder) {
            std140Builder.putInt(this.value);
        }

        @Override
        public void addSize(Std140SizeCalculator std140SizeCalculator) {
            std140SizeCalculator.putInt();
        }

        @Override
        public Type type() {
            return Type.INT;
        }
    }
}

