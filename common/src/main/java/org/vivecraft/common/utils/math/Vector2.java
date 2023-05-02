package org.vivecraft.common.utils.math;

import org.vivecraft.common.utils.lwjgl.Vector2f;

@Deprecated
public class Vector2
{
    protected float x;
    protected float y;

    public Vector2()
    {
    }

    public Vector2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2(Vector2f other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 copy()
    {
        return new Vector2(this);
    }

    public float getX()
    {
        return this.x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return this.y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2 other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 add(Vector2 other)
    {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other)
    {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(float number)
    {
        return new Vector2(this.x * number, this.y * number);
    }

    public Vector2 divide(float number)
    {
        return new Vector2(this.x / number, this.y / number);
    }

    public Vector2 negate()
    {
        return new Vector2(-this.x, -this.y);
    }

    public float angle(Vector2 other)
    {
        return (float)Math.toDegrees(Math.atan2((double)(other.y - this.y), (double)(other.x - this.x)));
    }

    public float length()
    {
        return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y));
    }

    public float lengthSquared()
    {
        return this.x * this.x + this.y * this.y;
    }

    public float distance(Vector2 other)
    {
        return other.subtract(this).length();
    }

    public float distanceSquared(Vector2 other)
    {
        return other.subtract(this).lengthSquared();
    }

    public void normalize()
    {
        this.set(this.divide(this.length()));
    }

    public Vector2 normalized()
    {
        return this.divide(this.length());
    }

    public float dot(Vector2 other)
    {
        return this.x * other.x + this.y * other.y;
    }

    public static Vector2 direction(float angle)
    {
        double d0 = Math.toRadians((double)angle);
        return new Vector2((float)Math.cos(d0), (float)Math.sin(d0));
    }

    public int hashCode()
    {
        int i = 7;
        i = 37 * i + Float.floatToIntBits(this.x);
        return 37 * i + Float.floatToIntBits(this.y);
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this.getClass() != obj.getClass())
        {
            return false;
        }
        else
        {
            Vector2 vector2 = (Vector2)obj;

            if (Float.floatToIntBits(this.x) != Float.floatToIntBits(vector2.x))
            {
                return false;
            }
            else
            {
                return Float.floatToIntBits(this.y) == Float.floatToIntBits(vector2.y);
            }
        }
    }

    public String toString()
    {
        return "Vector2{x=" + this.x + ", y=" + this.y + '}';
    }
}
