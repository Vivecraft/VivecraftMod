package org.vivecraft.common.utils.math;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

@Deprecated
public class Matrix4f
{
    public float[][] M = new float[4][4];

    public Matrix4f(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24, float m31, float m32, float m33, float m34, float m41, float m42, float m43, float m44)
    {
        this.M[0][0] = m11;
        this.M[0][1] = m12;
        this.M[0][2] = m13;
        this.M[0][3] = m14;
        this.M[1][0] = m21;
        this.M[1][1] = m22;
        this.M[1][2] = m23;
        this.M[1][3] = m24;
        this.M[2][0] = m31;
        this.M[2][1] = m32;
        this.M[2][2] = m33;
        this.M[2][3] = m34;
        this.M[3][0] = m41;
        this.M[3][1] = m42;
        this.M[3][2] = m43;
        this.M[3][3] = m44;
    }

    public Matrix4f(float m11, float m12, float m13, float m21, float m22, float m23, float m31, float m32, float m33)
    {
        this.M[0][0] = m11;
        this.M[0][1] = m12;
        this.M[0][2] = m13;
        this.M[0][3] = 0.0F;
        this.M[1][0] = m21;
        this.M[1][1] = m22;
        this.M[1][2] = m23;
        this.M[1][3] = 0.0F;
        this.M[2][0] = m31;
        this.M[2][1] = m32;
        this.M[2][2] = m33;
        this.M[2][3] = 0.0F;
        this.M[3][0] = 0.0F;
        this.M[3][1] = 0.0F;
        this.M[3][2] = 0.0F;
        this.M[3][3] = 1.0F;
    }

    public Matrix4f(Quaternion q)
    {
        float f = q.w * q.w;
        float f1 = q.x * q.x;
        float f2 = q.y * q.y;
        float f3 = q.z * q.z;
        this.M[0][0] = f + f1 - f2 - f3;
        this.M[0][1] = 2.0F * (q.x * q.y - q.w * q.z);
        this.M[0][2] = 2.0F * (q.x * q.z + q.w * q.y);
        this.M[0][3] = 0.0F;
        this.M[1][0] = 2.0F * (q.x * q.y + q.w * q.z);
        this.M[1][1] = f - f1 + f2 - f3;
        this.M[1][2] = 2.0F * (q.y * q.z - q.w * q.x);
        this.M[1][3] = 0.0F;
        this.M[2][0] = 2.0F * (q.x * q.z - q.w * q.y);
        this.M[2][1] = 2.0F * (q.y * q.z + q.w * q.x);
        this.M[2][2] = f - f1 - f2 + f3;
        this.M[2][3] = 0.0F;
        this.M[3][0] = 0.0F;
        this.M[3][1] = 0.0F;
        this.M[3][2] = 0.0F;
        this.M[3][3] = 1.0F;
    }

    public Matrix4f()
    {
        this.SetIdentity();
    }

    public void SetIdentity()
    {
        this.M[0][0] = this.M[1][1] = this.M[2][2] = this.M[3][3] = 1.0F;
        this.M[0][1] = this.M[1][0] = this.M[2][3] = this.M[3][1] = 0.0F;
        this.M[0][2] = this.M[1][2] = this.M[2][0] = this.M[3][2] = 0.0F;
        this.M[0][3] = this.M[1][3] = this.M[2][1] = this.M[3][0] = 0.0F;
    }

    Matrix4f(Matrix4f c)
    {
        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 4; ++j)
            {
                this.M[i][j] = c.M[i][j];
            }
        }
    }

    public Matrix4f inverted()
    {
        float f = this.Determinant();
        return f == 0.0F ? null : this.Adjugated().Multiply(1.0F / f);
    }

    Matrix4f Multiply(float s)
    {
        Matrix4f matrix4f = new Matrix4f(this);

        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 4; ++j)
            {
                float[] afloat = matrix4f.M[i];
                afloat[j] *= s;
            }
        }

        return matrix4f;
    }

    public static Matrix4f multiply(Matrix4f a, Matrix4f b)
    {
        int i = 0;
        Matrix4f matrix4f = new Matrix4f();

        do
        {
            matrix4f.M[i][0] = a.M[i][0] * b.M[0][0] + a.M[i][1] * b.M[1][0] + a.M[i][2] * b.M[2][0] + a.M[i][3] * b.M[3][0];
            matrix4f.M[i][1] = a.M[i][0] * b.M[0][1] + a.M[i][1] * b.M[1][1] + a.M[i][2] * b.M[2][1] + a.M[i][3] * b.M[3][1];
            matrix4f.M[i][2] = a.M[i][0] * b.M[0][2] + a.M[i][1] * b.M[1][2] + a.M[i][2] * b.M[2][2] + a.M[i][3] * b.M[3][2];
            matrix4f.M[i][3] = a.M[i][0] * b.M[0][3] + a.M[i][1] * b.M[1][3] + a.M[i][2] * b.M[2][3] + a.M[i][3] * b.M[3][3];
            ++i;
        }
        while (i < 4);

        return matrix4f;
    }

    public Matrix4f transposed()
    {
        return new Matrix4f(this.M[0][0], this.M[1][0], this.M[2][0], this.M[3][0], this.M[0][1], this.M[1][1], this.M[2][1], this.M[3][1], this.M[0][2], this.M[1][2], this.M[2][2], this.M[3][2], this.M[0][3], this.M[1][3], this.M[2][3], this.M[3][3]);
    }

    float SubDet(int[] rows, int[] cols)
    {
        return this.M[rows[0]][cols[0]] * (this.M[rows[1]][cols[1]] * this.M[rows[2]][cols[2]] - this.M[rows[1]][cols[2]] * this.M[rows[2]][cols[1]]) - this.M[rows[0]][cols[1]] * (this.M[rows[1]][cols[0]] * this.M[rows[2]][cols[2]] - this.M[rows[1]][cols[2]] * this.M[rows[2]][cols[0]]) + this.M[rows[0]][cols[2]] * (this.M[rows[1]][cols[0]] * this.M[rows[2]][cols[1]] - this.M[rows[1]][cols[1]] * this.M[rows[2]][cols[0]]);
    }

    float Cofactor(int I, int J)
    {
        int[][] aint = new int[][] {{1, 2, 3}, {0, 2, 3}, {0, 1, 3}, {0, 1, 2}};
        return (I + J & 1) != 0 ? -this.SubDet(aint[I], aint[J]) : this.SubDet(aint[I], aint[J]);
    }

    float Determinant()
    {
        return this.M[0][0] * this.Cofactor(0, 0) + this.M[0][1] * this.Cofactor(0, 1) + this.M[0][2] * this.Cofactor(0, 2) + this.M[0][3] * this.Cofactor(0, 3);
    }

    Matrix4f Adjugated()
    {
        return new Matrix4f(this.Cofactor(0, 0), this.Cofactor(1, 0), this.Cofactor(2, 0), this.Cofactor(3, 0), this.Cofactor(0, 1), this.Cofactor(1, 1), this.Cofactor(2, 1), this.Cofactor(3, 1), this.Cofactor(0, 2), this.Cofactor(1, 2), this.Cofactor(2, 2), this.Cofactor(3, 2), this.Cofactor(0, 3), this.Cofactor(1, 3), this.Cofactor(2, 3), this.Cofactor(3, 3));
    }

    public FloatBuffer toFloatBuffer()
    {
        FloatBuffer floatbuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatbuffer.put(this.M[0][0]);
        floatbuffer.put(this.M[0][1]);
        floatbuffer.put(this.M[0][2]);
        floatbuffer.put(this.M[0][3]);
        floatbuffer.put(this.M[1][0]);
        floatbuffer.put(this.M[1][1]);
        floatbuffer.put(this.M[1][2]);
        floatbuffer.put(this.M[1][3]);
        floatbuffer.put(this.M[2][0]);
        floatbuffer.put(this.M[2][1]);
        floatbuffer.put(this.M[2][2]);
        floatbuffer.put(this.M[2][3]);
        floatbuffer.put(this.M[3][0]);
        floatbuffer.put(this.M[3][1]);
        floatbuffer.put(this.M[3][2]);
        floatbuffer.put(this.M[3][3]);
        ((Buffer)floatbuffer).flip();
        return floatbuffer;
    }

    public static Matrix4f rotationY(float angle)
    {
        double d0 = Math.sin((double)angle);
        double d1 = Math.cos((double)angle);
        return new Matrix4f((float)d1, 0.0F, (float)d0, 0.0F, 1.0F, 0.0F, -((float)d0), 0.0F, (float)d1);
    }

    public Vector3 transform(Vector3 v)
    {
        float f = 1.0F / (this.M[3][0] * v.x + this.M[3][1] * v.y + this.M[3][2] * v.z + this.M[3][3]);
        return new Vector3((this.M[0][0] * v.x + this.M[0][1] * v.y + this.M[0][2] * v.z + this.M[0][3]) * f, (this.M[1][0] * v.x + this.M[1][1] * v.y + this.M[1][2] * v.z + this.M[1][3]) * f, (this.M[2][0] * v.x + this.M[2][1] * v.y + this.M[2][2] * v.z + this.M[2][3]) * f);
    }

    public static Matrix4f translation(Vector3 v)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.M[0][3] = v.x;
        matrix4f.M[1][3] = v.y;
        matrix4f.M[2][3] = v.z;
        return matrix4f;
    }

    public static Matrix4f lookAtRH(Vector3 eye, Vector3 at, Vector3 up)
    {
        Vector3 vector3 = eye.subtract(at).normalized();
        Vector3 vector31 = up.cross(vector3).normalized();
        Vector3 vector32 = vector3.cross(vector31);
        return new Matrix4f(vector31.x, vector31.y, vector31.z, -vector31.dot(eye), vector32.x, vector32.y, vector32.z, -vector32.dot(eye), vector3.x, vector3.y, vector3.z, -vector3.dot(eye), 0.0F, 0.0F, 0.0F, 1.0F);
    }

    public com.mojang.math.Matrix4f toMCMatrix()
    {
        com.mojang.math.Matrix4f matrix4f = new com.mojang.math.Matrix4f();
        matrix4f.m00 = this.M[0][0];
        matrix4f.m01 = this.M[0][1];
        matrix4f.m02 = this.M[0][2];
        matrix4f.m03 = this.M[0][3];
        matrix4f.m10 = this.M[1][0];
        matrix4f.m11 = this.M[1][1];
        matrix4f.m12 = this.M[1][2];
        matrix4f.m13 = this.M[1][3];
        matrix4f.m20 = this.M[2][0];
        matrix4f.m21 = this.M[2][1];
        matrix4f.m22 = this.M[2][2];
        matrix4f.m23 = this.M[2][3];
        matrix4f.m30 = this.M[3][0];
        matrix4f.m31 = this.M[3][1];
        matrix4f.m32 = this.M[3][2];
        matrix4f.m33 = this.M[3][3];
        return matrix4f;
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(this.M[0][0]).append(' ').append(this.M[1][0]).append(' ').append(this.M[2][0]).append(' ').append(this.M[3][0]).append('\n');
        stringbuilder.append(this.M[0][1]).append(' ').append(this.M[1][1]).append(' ').append(this.M[2][1]).append(' ').append(this.M[3][1]).append('\n');
        stringbuilder.append(this.M[0][2]).append(' ').append(this.M[1][2]).append(' ').append(this.M[2][2]).append(' ').append(this.M[3][2]).append('\n');
        stringbuilder.append(this.M[0][3]).append(' ').append(this.M[1][3]).append(' ').append(this.M[2][3]).append(' ').append(this.M[3][3]).append('\n');
        return stringbuilder.toString();
    }
}
