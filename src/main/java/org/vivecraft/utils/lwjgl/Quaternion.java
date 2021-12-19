package org.vivecraft.utils.lwjgl;

import java.nio.FloatBuffer;

public class Quaternion extends Vector implements ReadableVector4f
{
    private static final long serialVersionUID = 1L;
    public float x;
    public float y;
    public float z;
    public float w;

    public Quaternion()
    {
        this.setIdentity();
    }

    public Quaternion(ReadableVector4f src)
    {
        this.set(src);
    }

    public Quaternion(float x, float y, float z, float w)
    {
        this.set(x, y, z, w);
    }

    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion set(ReadableVector4f src)
    {
        this.x = src.getX();
        this.y = src.getY();
        this.z = src.getZ();
        this.w = src.getW();
        return this;
    }

    public Quaternion setIdentity()
    {
        return setIdentity(this);
    }

    public static Quaternion setIdentity(Quaternion q)
    {
        q.x = 0.0F;
        q.y = 0.0F;
        q.z = 0.0F;
        q.w = 1.0F;
        return q;
    }

    public float lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public static Quaternion normalise(Quaternion src, Quaternion dest)
    {
        float f = 1.0F / src.length();

        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.set(src.x * f, src.y * f, src.z * f, src.w * f);
        return dest;
    }

    public Quaternion normalise(Quaternion dest)
    {
        return normalise(this, dest);
    }

    public static float dot(Quaternion left, Quaternion right)
    {
        return left.x * right.x + left.y * right.y + left.z * right.z + left.w * right.w;
    }

    public Quaternion negate(Quaternion dest)
    {
        return negate(this, dest);
    }

    public static Quaternion negate(Quaternion src, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.x = -src.x;
        dest.y = -src.y;
        dest.z = -src.z;
        dest.w = src.w;
        return dest;
    }

    public Vector negate()
    {
        return negate(this, this);
    }

    public Vector load(FloatBuffer buf)
    {
        this.x = buf.get();
        this.y = buf.get();
        this.z = buf.get();
        this.w = buf.get();
        return this;
    }

    public Vector scale(float scale)
    {
        return scale(scale, this, this);
    }

    public static Quaternion scale(float scale, Quaternion src, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.x = src.x * scale;
        dest.y = src.y * scale;
        dest.z = src.z * scale;
        dest.w = src.w * scale;
        return dest;
    }

    public Vector store(FloatBuffer buf)
    {
        buf.put(this.x);
        buf.put(this.y);
        buf.put(this.z);
        buf.put(this.w);
        return this;
    }

    public final float getX()
    {
        return this.x;
    }

    public final float getY()
    {
        return this.y;
    }

    public final void setX(float x)
    {
        this.x = x;
    }

    public final void setY(float y)
    {
        this.y = y;
    }

    public void setZ(float z)
    {
        this.z = z;
    }

    public float getZ()
    {
        return this.z;
    }

    public void setW(float w)
    {
        this.w = w;
    }

    public float getW()
    {
        return this.w;
    }

    public String toString()
    {
        return "Quaternion: " + this.x + " " + this.y + " " + this.z + " " + this.w;
    }

    public static Quaternion mul(Quaternion left, Quaternion right, Quaternion dest)
    {
        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.set(left.x * right.w + left.w * right.x + left.y * right.z - left.z * right.y, left.y * right.w + left.w * right.y + left.z * right.x - left.x * right.z, left.z * right.w + left.w * right.z + left.x * right.y - left.y * right.x, left.w * right.w - left.x * right.x - left.y * right.y - left.z * right.z);
        return dest;
    }

    public static Quaternion mulInverse(Quaternion left, Quaternion right, Quaternion dest)
    {
        float f = right.lengthSquared();
        f = (double)f == 0.0D ? f : 1.0F / f;

        if (dest == null)
        {
            dest = new Quaternion();
        }

        dest.set((left.x * right.w - left.w * right.x - left.y * right.z + left.z * right.y) * f, (left.y * right.w - left.w * right.y - left.z * right.x + left.x * right.z) * f, (left.z * right.w - left.w * right.z - left.x * right.y + left.y * right.x) * f, (left.w * right.w + left.x * right.x + left.y * right.y + left.z * right.z) * f);
        return dest;
    }

    public final void setFromAxisAngle(Vector4f a1)
    {
        this.x = a1.x;
        this.y = a1.y;
        this.z = a1.z;
        float f = (float)Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z));
        float f1 = (float)(Math.sin(0.5D * (double)a1.w) / (double)f);
        this.x *= f1;
        this.y *= f1;
        this.z *= f1;
        this.w = (float)Math.cos(0.5D * (double)a1.w);
    }

    public final Quaternion setFromMatrix(Matrix4f m)
    {
        return setFromMatrix(m, this);
    }

    public static Quaternion setFromMatrix(Matrix4f m, Quaternion q)
    {
        return q.setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    public final Quaternion setFromMatrix(Matrix3f m)
    {
        return setFromMatrix(m, this);
    }

    public static Quaternion setFromMatrix(Matrix3f m, Quaternion q)
    {
        return q.setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    private Quaternion setFromMat(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
    {
        float f1 = m00 + m11 + m22;

        if ((double)f1 >= 0.0D)
        {
            float f = (float)Math.sqrt((double)f1 + 1.0D);
            this.w = f * 0.5F;
            f = 0.5F / f;
            this.x = (m21 - m12) * f;
            this.y = (m02 - m20) * f;
            this.z = (m10 - m01) * f;
        }
        else
        {
            float f2 = Math.max(Math.max(m00, m11), m22);

            if (f2 == m00)
            {
                float f3 = (float)Math.sqrt((double)(m00 - (m11 + m22)) + 1.0D);
                this.x = f3 * 0.5F;
                f3 = 0.5F / f3;
                this.y = (m01 + m10) * f3;
                this.z = (m20 + m02) * f3;
                this.w = (m21 - m12) * f3;
            }
            else if (f2 == m11)
            {
                float f4 = (float)Math.sqrt((double)(m11 - (m22 + m00)) + 1.0D);
                this.y = f4 * 0.5F;
                f4 = 0.5F / f4;
                this.z = (m12 + m21) * f4;
                this.x = (m01 + m10) * f4;
                this.w = (m02 - m20) * f4;
            }
            else
            {
                float f5 = (float)Math.sqrt((double)(m22 - (m00 + m11)) + 1.0D);
                this.z = f5 * 0.5F;
                f5 = 0.5F / f5;
                this.x = (m20 + m02) * f5;
                this.y = (m12 + m21) * f5;
                this.w = (m10 - m01) * f5;
            }
        }

        return this;
    }
}
