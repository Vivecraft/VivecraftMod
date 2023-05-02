package org.vivecraft.common.utils.lwjgl;

import java.nio.FloatBuffer;

@Deprecated
public class Vector3f extends Vector
{
    public float x;
    public float y;
    public float z;

    public Vector3f()
    {
    }

    public Vector3f(float x, float y, float z)
    {
        this.set(x, y, z);
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

    public float lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3f translate(float x, float y, float z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public static Vector3f add(Vector3f left, Vector3f right, Vector3f dest)
    {
        if (dest == null)
        {
            return new Vector3f(left.x + right.x, left.y + right.y, left.z + right.z);
        }
        else
        {
            dest.set(left.x + right.x, left.y + right.y, left.z + right.z);
            return dest;
        }
    }

    public static Vector3f sub(Vector3f left, Vector3f right, Vector3f dest)
    {
        if (dest == null)
        {
            return new Vector3f(left.x - right.x, left.y - right.y, left.z - right.z);
        }
        else
        {
            dest.set(left.x - right.x, left.y - right.y, left.z - right.z);
            return dest;
        }
    }

    public static Vector3f cross(Vector3f left, Vector3f right, Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        dest.set(left.y * right.z - left.z * right.y, right.x * left.z - right.z * left.x, left.x * right.y - left.y * right.x);
        return dest;
    }

    public Vector negate()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vector3f negate(Vector3f dest)
    {
        if (dest == null)
        {
            dest = new Vector3f();
        }

        dest.x = -this.x;
        dest.y = -this.y;
        dest.z = -this.z;
        return dest;
    }

    public Vector3f normalise(Vector3f dest)
    {
        float f = this.length();

        if (dest == null)
        {
            dest = new Vector3f(this.x / f, this.y / f, this.z / f);
        }
        else
        {
            dest.set(this.x / f, this.y / f, this.z / f);
        }

        return dest;
    }

    public static float dot(Vector3f left, Vector3f right)
    {
        return left.x * right.x + left.y * right.y + left.z * right.z;
    }

    public static float angle(Vector3f a, Vector3f b)
    {
        float f = dot(a, b) / (a.length() * b.length());

        if (f < -1.0F)
        {
            f = -1.0F;
        }
        else if (f > 1.0F)
        {
            f = 1.0F;
        }

        return (float)Math.acos((double)f);
    }

    public Vector load(FloatBuffer buf)
    {
        this.x = buf.get();
        this.y = buf.get();
        this.z = buf.get();
        return this;
    }

    public Vector scale(float scale)
    {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    public Vector store(FloatBuffer buf)
    {
        buf.put(this.x);
        buf.put(this.y);
        buf.put(this.z);
        return this;
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder(64);
        stringbuilder.append("Vector3f[");
        stringbuilder.append(this.x);
        stringbuilder.append(", ");
        stringbuilder.append(this.y);
        stringbuilder.append(", ");
        stringbuilder.append(this.z);
        stringbuilder.append(']');
        return stringbuilder.toString();
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

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (this.getClass() != obj.getClass())
        {
            return false;
        }
        else
        {
            Vector3f vector3f = (Vector3f)obj;
            return this.x == vector3f.x && this.y == vector3f.y && this.z == vector3f.z;
        }
    }
}
