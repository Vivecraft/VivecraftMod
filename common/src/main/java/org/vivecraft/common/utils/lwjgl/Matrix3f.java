package org.vivecraft.common.utils.lwjgl;

import java.nio.FloatBuffer;

@Deprecated
public class Matrix3f extends Matrix {
    public float m00;
    public float m01;
    public float m02;
    public float m10;
    public float m11;
    public float m12;
    public float m20;
    public float m21;
    public float m22;

    public Matrix3f() {
        this.setIdentity();
    }

    public Matrix3f load(Matrix3f src) {
        return load(src, this);
    }

    public static Matrix3f load(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.m00 = src.m00;
        dest.m10 = src.m10;
        dest.m20 = src.m20;
        dest.m01 = src.m01;
        dest.m11 = src.m11;
        dest.m21 = src.m21;
        dest.m02 = src.m02;
        dest.m12 = src.m12;
        dest.m22 = src.m22;
        return dest;
    }

    public Matrix load(FloatBuffer buf) {
        this.m00 = buf.get();
        this.m01 = buf.get();
        this.m02 = buf.get();
        this.m10 = buf.get();
        this.m11 = buf.get();
        this.m12 = buf.get();
        this.m20 = buf.get();
        this.m21 = buf.get();
        this.m22 = buf.get();
        return this;
    }

    public Matrix loadTranspose(FloatBuffer buf) {
        this.m00 = buf.get();
        this.m10 = buf.get();
        this.m20 = buf.get();
        this.m01 = buf.get();
        this.m11 = buf.get();
        this.m21 = buf.get();
        this.m02 = buf.get();
        this.m12 = buf.get();
        this.m22 = buf.get();
        return this;
    }

    public Matrix store(FloatBuffer buf) {
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

    public Matrix storeTranspose(FloatBuffer buf) {
        buf.put(this.m00);
        buf.put(this.m10);
        buf.put(this.m20);
        buf.put(this.m01);
        buf.put(this.m11);
        buf.put(this.m21);
        buf.put(this.m02);
        buf.put(this.m12);
        buf.put(this.m22);
        return this;
    }

    public static Matrix3f add(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m02 = left.m02 + right.m02;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        dest.m12 = left.m12 + right.m12;
        dest.m20 = left.m20 + right.m20;
        dest.m21 = left.m21 + right.m21;
        dest.m22 = left.m22 + right.m22;
        return dest;
    }

    public static Matrix3f sub(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m02 = left.m02 - right.m02;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        dest.m12 = left.m12 - right.m12;
        dest.m20 = left.m20 - right.m20;
        dest.m21 = left.m21 - right.m21;
        dest.m22 = left.m22 - right.m22;
        return dest;
    }

    public static Matrix3f mul(Matrix3f left, Matrix3f right, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        float f = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02;
        float f1 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02;
        float f2 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02;
        float f3 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12;
        float f4 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12;
        float f5 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12;
        float f6 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22;
        float f7 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22;
        float f8 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22;
        dest.m00 = f;
        dest.m01 = f1;
        dest.m02 = f2;
        dest.m10 = f3;
        dest.m11 = f4;
        dest.m12 = f5;
        dest.m20 = f6;
        dest.m21 = f7;
        dest.m22 = f8;
        return dest;
    }

    public static Vector3f transform(Matrix3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }

        float f = left.m00 * right.x + left.m10 * right.y + left.m20 * right.z;
        float f1 = left.m01 * right.x + left.m11 * right.y + left.m21 * right.z;
        float f2 = left.m02 * right.x + left.m12 * right.y + left.m22 * right.z;
        dest.x = f;
        dest.y = f1;
        dest.z = f2;
        return dest;
    }

    public Matrix transpose() {
        return transpose(this, this);
    }

    public Matrix3f transpose(Matrix3f dest) {
        return transpose(this, dest);
    }

    public static Matrix3f transpose(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        float f = src.m00;
        float f1 = src.m10;
        float f2 = src.m20;
        float f3 = src.m01;
        float f4 = src.m11;
        float f5 = src.m21;
        float f6 = src.m02;
        float f7 = src.m12;
        float f8 = src.m22;
        dest.m00 = f;
        dest.m01 = f1;
        dest.m02 = f2;
        dest.m10 = f3;
        dest.m11 = f4;
        dest.m12 = f5;
        dest.m20 = f6;
        dest.m21 = f7;
        dest.m22 = f8;
        return dest;
    }

    public float determinant() {
        return this.m00 * (this.m11 * this.m22 - this.m12 * this.m21) + this.m01 * (this.m12 * this.m20 - this.m10 * this.m22) + this.m02 * (this.m10 * this.m21 - this.m11 * this.m20);
    }

    public String toString() {
        String stringbuilder = String.valueOf(this.m00) + ' ' + this.m10 + ' ' + this.m20 + ' ' + '\n' +
            this.m01 + ' ' + this.m11 + ' ' + this.m21 + ' ' + '\n' +
            this.m02 + ' ' + this.m12 + ' ' + this.m22 + ' ' + '\n';
        return stringbuilder;
    }

    public Matrix invert() {
        return invert(this, this);
    }

    public static Matrix3f invert(Matrix3f src, Matrix3f dest) {
        float f = src.determinant();

        if (f != 0.0F) {
            if (dest == null) {
                dest = new Matrix3f();
            }

            float f1 = 1.0F / f;
            float f2 = src.m11 * src.m22 - src.m12 * src.m21;
            float f3 = -src.m10 * src.m22 + src.m12 * src.m20;
            float f4 = src.m10 * src.m21 - src.m11 * src.m20;
            float f5 = -src.m01 * src.m22 + src.m02 * src.m21;
            float f6 = src.m00 * src.m22 - src.m02 * src.m20;
            float f7 = -src.m00 * src.m21 + src.m01 * src.m20;
            float f8 = src.m01 * src.m12 - src.m02 * src.m11;
            float f9 = -src.m00 * src.m12 + src.m02 * src.m10;
            float f10 = src.m00 * src.m11 - src.m01 * src.m10;
            dest.m00 = f2 * f1;
            dest.m11 = f6 * f1;
            dest.m22 = f10 * f1;
            dest.m01 = f5 * f1;
            dest.m10 = f3 * f1;
            dest.m20 = f4 * f1;
            dest.m02 = f8 * f1;
            dest.m12 = f9 * f1;
            dest.m21 = f7 * f1;
            return dest;
        } else {
            return null;
        }
    }

    public Matrix negate() {
        return this.negate(this);
    }

    public Matrix3f negate(Matrix3f dest) {
        return negate(this, dest);
    }

    public static Matrix3f negate(Matrix3f src, Matrix3f dest) {
        if (dest == null) {
            dest = new Matrix3f();
        }

        dest.m00 = -src.m00;
        dest.m01 = -src.m02;
        dest.m02 = -src.m01;
        dest.m10 = -src.m10;
        dest.m11 = -src.m12;
        dest.m12 = -src.m11;
        dest.m20 = -src.m20;
        dest.m21 = -src.m22;
        dest.m22 = -src.m21;
        return dest;
    }

    public Matrix setIdentity() {
        return setIdentity(this);
    }

    public static Matrix3f setIdentity(Matrix3f m) {
        m.m00 = 1.0F;
        m.m01 = 0.0F;
        m.m02 = 0.0F;
        m.m10 = 0.0F;
        m.m11 = 1.0F;
        m.m12 = 0.0F;
        m.m20 = 0.0F;
        m.m21 = 0.0F;
        m.m22 = 1.0F;
        return m;
    }

    public Matrix setZero() {
        return setZero(this);
    }

    public static Matrix3f setZero(Matrix3f m) {
        m.m00 = 0.0F;
        m.m01 = 0.0F;
        m.m02 = 0.0F;
        m.m10 = 0.0F;
        m.m11 = 0.0F;
        m.m12 = 0.0F;
        m.m20 = 0.0F;
        m.m21 = 0.0F;
        m.m22 = 0.0F;
        return m;
    }
}
