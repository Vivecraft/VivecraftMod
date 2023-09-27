package org.vivecraft.common.utils.lwjgl;

import java.nio.FloatBuffer;

@Deprecated
public class Matrix2f extends Matrix {
    public float m00;
    public float m01;
    public float m10;
    public float m11;

    public Matrix2f() {
        this.setIdentity();
    }

    public Matrix2f(Matrix2f src) {
        this.load(src);
    }

    public Matrix2f load(Matrix2f src) {
        return load(src, this);
    }

    public static Matrix2f load(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        return dest;
    }

    public Matrix load(FloatBuffer buf) {
        this.m00 = buf.get();
        this.m01 = buf.get();
        this.m10 = buf.get();
        this.m11 = buf.get();
        return this;
    }

    public Matrix loadTranspose(FloatBuffer buf) {
        this.m00 = buf.get();
        this.m10 = buf.get();
        this.m01 = buf.get();
        this.m11 = buf.get();
        return this;
    }

    public Matrix store(FloatBuffer buf) {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m10);
        buf.put(this.m11);
        return this;
    }

    public Matrix storeTranspose(FloatBuffer buf) {
        buf.put(this.m00);
        buf.put(this.m10);
        buf.put(this.m01);
        buf.put(this.m11);
        return this;
    }

    public static Matrix2f add(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        return dest;
    }

    public static Matrix2f sub(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        return dest;
    }

    public static Matrix2f mul(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        float f = left.m00 * right.m00 + left.m10 * right.m01;
        float f1 = left.m01 * right.m00 + left.m11 * right.m01;
        float f2 = left.m00 * right.m10 + left.m10 * right.m11;
        float f3 = left.m01 * right.m10 + left.m11 * right.m11;
        dest.m00 = f;
        dest.m01 = f1;
        dest.m10 = f2;
        dest.m11 = f3;
        return dest;
    }

    public static Vector2f transform(Matrix2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }

        float f = left.m00 * right.x + left.m10 * right.y;
        float f1 = left.m01 * right.x + left.m11 * right.y;
        dest.x = f;
        dest.y = f1;
        return dest;
    }

    public Matrix transpose() {
        return this.transpose(this);
    }

    public Matrix2f transpose(Matrix2f dest) {
        return transpose(this, dest);
    }

    public static Matrix2f transpose(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        float f = src.m10;
        float f1 = src.m01;
        dest.m01 = f;
        dest.m10 = f1;
        return dest;
    }

    public Matrix invert() {
        return invert(this, this);
    }

    public static Matrix2f invert(Matrix2f src, Matrix2f dest) {
        float f = src.determinant();

        if (f != 0.0F) {
            if (dest == null) {
                dest = new Matrix2f();
            }

            float f1 = 1.0F / f;
            float f2 = src.m11 * f1;
            float f3 = -src.m01 * f1;
            float f4 = src.m00 * f1;
            float f5 = -src.m10 * f1;
            dest.m00 = f2;
            dest.m01 = f3;
            dest.m10 = f5;
            dest.m11 = f4;
            return dest;
        } else {
            return null;
        }
    }

    public String toString() {
        String stringbuilder = String.valueOf(this.m00) + ' ' + this.m10 + ' ' + '\n' +
            this.m01 + ' ' + this.m11 + ' ' + '\n';
        return stringbuilder;
    }

    public Matrix negate() {
        return this.negate(this);
    }

    public Matrix2f negate(Matrix2f dest) {
        return negate(this, dest);
    }

    public static Matrix2f negate(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }

        dest.m00 = -src.m00;
        dest.m01 = -src.m01;
        dest.m10 = -src.m10;
        dest.m11 = -src.m11;
        return dest;
    }

    public Matrix setIdentity() {
        return setIdentity(this);
    }

    public static Matrix2f setIdentity(Matrix2f src) {
        src.m00 = 1.0F;
        src.m01 = 0.0F;
        src.m10 = 0.0F;
        src.m11 = 1.0F;
        return src;
    }

    public Matrix setZero() {
        return setZero(this);
    }

    public static Matrix2f setZero(Matrix2f src) {
        src.m00 = 0.0F;
        src.m01 = 0.0F;
        src.m10 = 0.0F;
        src.m11 = 0.0F;
        return src;
    }

    public float determinant() {
        return this.m00 * this.m11 - this.m01 * this.m10;
    }
}
