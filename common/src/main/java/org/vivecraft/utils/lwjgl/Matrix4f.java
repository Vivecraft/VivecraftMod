package org.vivecraft.utils.lwjgl;

import java.io.Serializable;
import java.nio.FloatBuffer;

public class Matrix4f extends Matrix implements Serializable
{
    private static final long serialVersionUID = 1L;
    public float m00;
    public float m01;
    public float m02;
    public float m03;
    public float m10;
    public float m11;
    public float m12;
    public float m13;
    public float m20;
    public float m21;
    public float m22;
    public float m23;
    public float m30;
    public float m31;
    public float m32;
    public float m33;

    public Matrix4f()
    {
        this.setIdentity();
    }

    public Matrix4f(Matrix4f src)
    {
        this.load(src);
    }

    public Matrix4f(org.joml.Matrix4f src)
    {
        this.m00 = src.m00();
        this.m01 = src.m01();
        this.m02 = src.m02();
        this.m03 = src.m03();
        this.m10 = src.m10();
        this.m11 = src.m11();
        this.m12 = src.m12();
        this.m13 = src.m13();
        this.m20 = src.m20();
        this.m21 = src.m21();
        this.m22 = src.m22();
        this.m23 = src.m23();
        this.m30 = src.m30();
        this.m31 = src.m31();
        this.m32 = src.m32();
        this.m33 = src.m33();
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(this.m00).append(' ').append(this.m10).append(' ').append(this.m20).append(' ').append(this.m30).append('\n');
        stringbuilder.append(this.m01).append(' ').append(this.m11).append(' ').append(this.m21).append(' ').append(this.m31).append('\n');
        stringbuilder.append(this.m02).append(' ').append(this.m12).append(' ').append(this.m22).append(' ').append(this.m32).append('\n');
        stringbuilder.append(this.m03).append(' ').append(this.m13).append(' ').append(this.m23).append(' ').append(this.m33).append('\n');
        return stringbuilder.toString();
    }

    public Matrix setIdentity()
    {
        return setIdentity(this);
    }

    public static Matrix4f setIdentity(Matrix4f m)
    {
        m.m00 = 1.0F;
        m.m01 = 0.0F;
        m.m02 = 0.0F;
        m.m03 = 0.0F;
        m.m10 = 0.0F;
        m.m11 = 1.0F;
        m.m12 = 0.0F;
        m.m13 = 0.0F;
        m.m20 = 0.0F;
        m.m21 = 0.0F;
        m.m22 = 1.0F;
        m.m23 = 0.0F;
        m.m30 = 0.0F;
        m.m31 = 0.0F;
        m.m32 = 0.0F;
        m.m33 = 1.0F;
        return m;
    }

    public Matrix setZero()
    {
        return setZero(this);
    }

    public static Matrix4f setZero(Matrix4f m)
    {
        m.m00 = 0.0F;
        m.m01 = 0.0F;
        m.m02 = 0.0F;
        m.m03 = 0.0F;
        m.m10 = 0.0F;
        m.m11 = 0.0F;
        m.m12 = 0.0F;
        m.m13 = 0.0F;
        m.m20 = 0.0F;
        m.m21 = 0.0F;
        m.m22 = 0.0F;
        m.m23 = 0.0F;
        m.m30 = 0.0F;
        m.m31 = 0.0F;
        m.m32 = 0.0F;
        m.m33 = 0.0F;
        return m;
    }

    public Matrix4f load(Matrix4f src)
    {
        return load(src, this);
    }

    public static Matrix4f load(Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m02 = src.m02;
        dest.m03 = src.m03;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        dest.m12 = src.m12;
        dest.m13 = src.m13;
        dest.m20 = src.m20;
        dest.m21 = src.m21;
        dest.m22 = src.m22;
        dest.m23 = src.m23;
        dest.m30 = src.m30;
        dest.m31 = src.m31;
        dest.m32 = src.m32;
        dest.m33 = src.m33;
        return dest;
    }

    public Matrix load(FloatBuffer buf)
    {
        this.m00 = buf.get();
        this.m01 = buf.get();
        this.m02 = buf.get();
        this.m03 = buf.get();
        this.m10 = buf.get();
        this.m11 = buf.get();
        this.m12 = buf.get();
        this.m13 = buf.get();
        this.m20 = buf.get();
        this.m21 = buf.get();
        this.m22 = buf.get();
        this.m23 = buf.get();
        this.m30 = buf.get();
        this.m31 = buf.get();
        this.m32 = buf.get();
        this.m33 = buf.get();
        return this;
    }

    public Matrix loadTranspose(FloatBuffer buf)
    {
        this.m00 = buf.get();
        this.m10 = buf.get();
        this.m20 = buf.get();
        this.m30 = buf.get();
        this.m01 = buf.get();
        this.m11 = buf.get();
        this.m21 = buf.get();
        this.m31 = buf.get();
        this.m02 = buf.get();
        this.m12 = buf.get();
        this.m22 = buf.get();
        this.m32 = buf.get();
        this.m03 = buf.get();
        this.m13 = buf.get();
        this.m23 = buf.get();
        this.m33 = buf.get();
        return this;
    }

    public Matrix store(FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m02);
        buf.put(this.m03);
        buf.put(this.m10);
        buf.put(this.m11);
        buf.put(this.m12);
        buf.put(this.m13);
        buf.put(this.m20);
        buf.put(this.m21);
        buf.put(this.m22);
        buf.put(this.m23);
        buf.put(this.m30);
        buf.put(this.m31);
        buf.put(this.m32);
        buf.put(this.m33);
        return this;
    }

    public Matrix storeTranspose(FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m10);
        buf.put(this.m20);
        buf.put(this.m30);
        buf.put(this.m01);
        buf.put(this.m11);
        buf.put(this.m21);
        buf.put(this.m31);
        buf.put(this.m02);
        buf.put(this.m12);
        buf.put(this.m22);
        buf.put(this.m32);
        buf.put(this.m03);
        buf.put(this.m13);
        buf.put(this.m23);
        buf.put(this.m33);
        return this;
    }

    public Matrix store3f(FloatBuffer buf)
    {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m02);
        buf.put(this.m10);
        buf.put(this.m11);
        buf.put(this.m12);
        buf.put(this.m20);
        buf.put(this.m21);
        buf.put(this.m22);
        return this;
    }

    public static Matrix4f add(Matrix4f left, Matrix4f right, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m02 = left.m02 + right.m02;
        dest.m03 = left.m03 + right.m03;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        dest.m12 = left.m12 + right.m12;
        dest.m13 = left.m13 + right.m13;
        dest.m20 = left.m20 + right.m20;
        dest.m21 = left.m21 + right.m21;
        dest.m22 = left.m22 + right.m22;
        dest.m23 = left.m23 + right.m23;
        dest.m30 = left.m30 + right.m30;
        dest.m31 = left.m31 + right.m31;
        dest.m32 = left.m32 + right.m32;
        dest.m33 = left.m33 + right.m33;
        return dest;
    }

    public static Matrix4f sub(Matrix4f left, Matrix4f right, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m02 = left.m02 - right.m02;
        dest.m03 = left.m03 - right.m03;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        dest.m12 = left.m12 - right.m12;
        dest.m13 = left.m13 - right.m13;
        dest.m20 = left.m20 - right.m20;
        dest.m21 = left.m21 - right.m21;
        dest.m22 = left.m22 - right.m22;
        dest.m23 = left.m23 - right.m23;
        dest.m30 = left.m30 - right.m30;
        dest.m31 = left.m31 - right.m31;
        dest.m32 = left.m32 - right.m32;
        dest.m33 = left.m33 - right.m33;
        return dest;
    }

    public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        float f = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
        float f1 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
        float f2 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
        float f3 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
        float f4 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13;
        float f5 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13;
        float f6 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13;
        float f7 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13;
        float f8 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23;
        float f9 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23;
        float f10 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23;
        float f11 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23;
        float f12 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33;
        float f13 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33;
        float f14 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33;
        float f15 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33;
        dest.m00 = f;
        dest.m01 = f1;
        dest.m02 = f2;
        dest.m03 = f3;
        dest.m10 = f4;
        dest.m11 = f5;
        dest.m12 = f6;
        dest.m13 = f7;
        dest.m20 = f8;
        dest.m21 = f9;
        dest.m22 = f10;
        dest.m23 = f11;
        dest.m30 = f12;
        dest.m31 = f13;
        dest.m32 = f14;
        dest.m33 = f15;
        return dest;
    }

    public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f dest)
    {
        if (dest == null)
        {
            dest = new Vector4f();
        }

        float f = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z + left.m30 * right.w;
        float f1 = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z + left.m31 * right.w;
        float f2 = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z + left.m32 * right.w;
        float f3 = left.m03 * right.x + left.m13 * right.y + left.m23 * right.z + left.m33 * right.w;
        dest.x = f;
        dest.y = f1;
        dest.z = f2;
        dest.w = f3;
        return dest;
    }

    public Matrix transpose()
    {
        return this.transpose(this);
    }

    public Matrix4f translate(Vector2f vec)
    {
        return this.translate(vec, this);
    }

    public Matrix4f translate(Vector3f vec)
    {
        return this.translate(vec, this);
    }

    public Matrix4f scale(Vector3f vec)
    {
        return scale(vec, this, this);
    }

    public static Matrix4f scale(Vector3f vec, Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = src.m00 * vec.x;
        dest.m01 = src.m01 * vec.x;
        dest.m02 = src.m02 * vec.x;
        dest.m03 = src.m03 * vec.x;
        dest.m10 = src.m10 * vec.y;
        dest.m11 = src.m11 * vec.y;
        dest.m12 = src.m12 * vec.y;
        dest.m13 = src.m13 * vec.y;
        dest.m20 = src.m20 * vec.z;
        dest.m21 = src.m21 * vec.z;
        dest.m22 = src.m22 * vec.z;
        dest.m23 = src.m23 * vec.z;
        return dest;
    }

    public Matrix4f rotate(float angle, Vector3f axis)
    {
        return this.rotate(angle, axis, this);
    }

    public Matrix4f rotate(float angle, Vector3f axis, Matrix4f dest)
    {
        return rotate(angle, axis, this, dest);
    }

    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        float f = (float)Math.cos((double)angle);
        float f1 = (float)Math.sin((double)angle);
        float f2 = 1.0F - f;
        float f3 = axis.x * axis.y;
        float f4 = axis.y * axis.z;
        float f5 = axis.x * axis.z;
        float f6 = axis.x * f1;
        float f7 = axis.y * f1;
        float f8 = axis.z * f1;
        float f9 = axis.x * axis.x * f2 + f;
        float f10 = f3 * f2 + f8;
        float f11 = f5 * f2 - f7;
        float f12 = f3 * f2 - f8;
        float f13 = axis.y * axis.y * f2 + f;
        float f14 = f4 * f2 + f6;
        float f15 = f5 * f2 + f7;
        float f16 = f4 * f2 - f6;
        float f17 = axis.z * axis.z * f2 + f;
        float f18 = src.m00 * f9 + src.m10 * f10 + src.m20 * f11;
        float f19 = src.m01 * f9 + src.m11 * f10 + src.m21 * f11;
        float f20 = src.m02 * f9 + src.m12 * f10 + src.m22 * f11;
        float f21 = src.m03 * f9 + src.m13 * f10 + src.m23 * f11;
        float f22 = src.m00 * f12 + src.m10 * f13 + src.m20 * f14;
        float f23 = src.m01 * f12 + src.m11 * f13 + src.m21 * f14;
        float f24 = src.m02 * f12 + src.m12 * f13 + src.m22 * f14;
        float f25 = src.m03 * f12 + src.m13 * f13 + src.m23 * f14;
        dest.m20 = src.m00 * f15 + src.m10 * f16 + src.m20 * f17;
        dest.m21 = src.m01 * f15 + src.m11 * f16 + src.m21 * f17;
        dest.m22 = src.m02 * f15 + src.m12 * f16 + src.m22 * f17;
        dest.m23 = src.m03 * f15 + src.m13 * f16 + src.m23 * f17;
        dest.m00 = f18;
        dest.m01 = f19;
        dest.m02 = f20;
        dest.m03 = f21;
        dest.m10 = f22;
        dest.m11 = f23;
        dest.m12 = f24;
        dest.m13 = f25;
        return dest;
    }

    public Matrix4f translate(Vector3f vec, Matrix4f dest)
    {
        return translate(vec, this, dest);
    }

    public static Matrix4f translate(Vector3f vec, Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m30 += src.m00 * vec.x + src.m10 * vec.y + src.m20 * vec.z;
        dest.m31 += src.m01 * vec.x + src.m11 * vec.y + src.m21 * vec.z;
        dest.m32 += src.m02 * vec.x + src.m12 * vec.y + src.m22 * vec.z;
        dest.m33 += src.m03 * vec.x + src.m13 * vec.y + src.m23 * vec.z;
        return dest;
    }

    public Matrix4f translate(Vector2f vec, Matrix4f dest)
    {
        return translate(vec, this, dest);
    }

    public static Matrix4f translate(Vector2f vec, Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m30 += src.m00 * vec.x + src.m10 * vec.y;
        dest.m31 += src.m01 * vec.x + src.m11 * vec.y;
        dest.m32 += src.m02 * vec.x + src.m12 * vec.y;
        dest.m33 += src.m03 * vec.x + src.m13 * vec.y;
        return dest;
    }

    public Matrix4f transpose(Matrix4f dest)
    {
        return transpose(this, dest);
    }

    public static Matrix4f transpose(Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        float f = src.m00;
        float f1 = src.m10;
        float f2 = src.m20;
        float f3 = src.m30;
        float f4 = src.m01;
        float f5 = src.m11;
        float f6 = src.m21;
        float f7 = src.m31;
        float f8 = src.m02;
        float f9 = src.m12;
        float f10 = src.m22;
        float f11 = src.m32;
        float f12 = src.m03;
        float f13 = src.m13;
        float f14 = src.m23;
        float f15 = src.m33;
        dest.m00 = f;
        dest.m01 = f1;
        dest.m02 = f2;
        dest.m03 = f3;
        dest.m10 = f4;
        dest.m11 = f5;
        dest.m12 = f6;
        dest.m13 = f7;
        dest.m20 = f8;
        dest.m21 = f9;
        dest.m22 = f10;
        dest.m23 = f11;
        dest.m30 = f12;
        dest.m31 = f13;
        dest.m32 = f14;
        dest.m33 = f15;
        return dest;
    }

    public float determinant()
    {
        float f = this.m00 * (this.m11 * this.m22 * this.m33 + this.m12 * this.m23 * this.m31 + this.m13 * this.m21 * this.m32 - this.m13 * this.m22 * this.m31 - this.m11 * this.m23 * this.m32 - this.m12 * this.m21 * this.m33);
        f = f - this.m01 * (this.m10 * this.m22 * this.m33 + this.m12 * this.m23 * this.m30 + this.m13 * this.m20 * this.m32 - this.m13 * this.m22 * this.m30 - this.m10 * this.m23 * this.m32 - this.m12 * this.m20 * this.m33);
        f = f + this.m02 * (this.m10 * this.m21 * this.m33 + this.m11 * this.m23 * this.m30 + this.m13 * this.m20 * this.m31 - this.m13 * this.m21 * this.m30 - this.m10 * this.m23 * this.m31 - this.m11 * this.m20 * this.m33);
        return f - this.m03 * (this.m10 * this.m21 * this.m32 + this.m11 * this.m22 * this.m30 + this.m12 * this.m20 * this.m31 - this.m12 * this.m21 * this.m30 - this.m10 * this.m22 * this.m31 - this.m11 * this.m20 * this.m32);
    }

    private static float determinant3x3(float t00, float t01, float t02, float t10, float t11, float t12, float t20, float t21, float t22)
    {
        return t00 * (t11 * t22 - t12 * t21) + t01 * (t12 * t20 - t10 * t22) + t02 * (t10 * t21 - t11 * t20);
    }

    public Matrix invert()
    {
        return invert(this, this);
    }

    public static Matrix4f invert(Matrix4f src, Matrix4f dest)
    {
        float f = src.determinant();

        if (f != 0.0F)
        {
            if (dest == null)
            {
                dest = new Matrix4f();
            }

            float f1 = 1.0F / f;
            float f2 = determinant3x3(src.m11, src.m12, src.m13, src.m21, src.m22, src.m23, src.m31, src.m32, src.m33);
            float f3 = -determinant3x3(src.m10, src.m12, src.m13, src.m20, src.m22, src.m23, src.m30, src.m32, src.m33);
            float f4 = determinant3x3(src.m10, src.m11, src.m13, src.m20, src.m21, src.m23, src.m30, src.m31, src.m33);
            float f5 = -determinant3x3(src.m10, src.m11, src.m12, src.m20, src.m21, src.m22, src.m30, src.m31, src.m32);
            float f6 = -determinant3x3(src.m01, src.m02, src.m03, src.m21, src.m22, src.m23, src.m31, src.m32, src.m33);
            float f7 = determinant3x3(src.m00, src.m02, src.m03, src.m20, src.m22, src.m23, src.m30, src.m32, src.m33);
            float f8 = -determinant3x3(src.m00, src.m01, src.m03, src.m20, src.m21, src.m23, src.m30, src.m31, src.m33);
            float f9 = determinant3x3(src.m00, src.m01, src.m02, src.m20, src.m21, src.m22, src.m30, src.m31, src.m32);
            float f10 = determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m31, src.m32, src.m33);
            float f11 = -determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m30, src.m32, src.m33);
            float f12 = determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m30, src.m31, src.m33);
            float f13 = -determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m30, src.m31, src.m32);
            float f14 = -determinant3x3(src.m01, src.m02, src.m03, src.m11, src.m12, src.m13, src.m21, src.m22, src.m23);
            float f15 = determinant3x3(src.m00, src.m02, src.m03, src.m10, src.m12, src.m13, src.m20, src.m22, src.m23);
            float f16 = -determinant3x3(src.m00, src.m01, src.m03, src.m10, src.m11, src.m13, src.m20, src.m21, src.m23);
            float f17 = determinant3x3(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12, src.m20, src.m21, src.m22);
            dest.m00 = f2 * f1;
            dest.m11 = f7 * f1;
            dest.m22 = f12 * f1;
            dest.m33 = f17 * f1;
            dest.m01 = f6 * f1;
            dest.m10 = f3 * f1;
            dest.m20 = f4 * f1;
            dest.m02 = f10 * f1;
            dest.m12 = f11 * f1;
            dest.m21 = f8 * f1;
            dest.m03 = f14 * f1;
            dest.m30 = f5 * f1;
            dest.m13 = f15 * f1;
            dest.m31 = f9 * f1;
            dest.m32 = f13 * f1;
            dest.m23 = f16 * f1;
            return dest;
        }
        else
        {
            return null;
        }
    }

    public Matrix negate()
    {
        return this.negate(this);
    }

    public Matrix4f negate(Matrix4f dest)
    {
        return negate(this, dest);
    }

    public static Matrix4f negate(Matrix4f src, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }

        dest.m00 = -src.m00;
        dest.m01 = -src.m01;
        dest.m02 = -src.m02;
        dest.m03 = -src.m03;
        dest.m10 = -src.m10;
        dest.m11 = -src.m11;
        dest.m12 = -src.m12;
        dest.m13 = -src.m13;
        dest.m20 = -src.m20;
        dest.m21 = -src.m21;
        dest.m22 = -src.m22;
        dest.m23 = -src.m23;
        dest.m30 = -src.m30;
        dest.m31 = -src.m31;
        dest.m32 = -src.m32;
        dest.m33 = -src.m33;
        return dest;
    }
}
