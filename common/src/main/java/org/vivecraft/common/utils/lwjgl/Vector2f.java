package org.vivecraft.common.utils.lwjgl;

import java.nio.FloatBuffer;

@Deprecated
public class Vector2f extends Vector {
    public float x;
    public float y;

    public Vector2f() {
    }

    public Vector2f(float x, float y) {
        this.set(x, y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public Vector2f translate(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vector2f negate(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }

        dest.x = -this.x;
        dest.y = -this.y;
        return dest;
    }

    public Vector2f normalise(Vector2f dest) {
        float f = this.length();

        if (dest == null) {
            dest = new Vector2f(this.x / f, this.y / f);
        } else {
            dest.set(this.x / f, this.y / f);
        }

        return dest;
    }

    public static float dot(Vector2f left, Vector2f right) {
        return left.x * right.x + left.y * right.y;
    }

    public static float angle(Vector2f a, Vector2f b) {
        float f = dot(a, b) / (a.length() * b.length());

        if (f < -1.0F) {
            f = -1.0F;
        } else if (f > 1.0F) {
            f = 1.0F;
        }

        return (float) Math.acos(f);
    }

    public static Vector2f add(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            return new Vector2f(left.x + right.x, left.y + right.y);
        } else {
            dest.set(left.x + right.x, left.y + right.y);
            return dest;
        }
    }

    public static Vector2f sub(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            return new Vector2f(left.x - right.x, left.y - right.y);
        } else {
            dest.set(left.x - right.x, left.y - right.y);
            return dest;
        }
    }

    public Vector store(FloatBuffer buf) {
        buf.put(this.x);
        buf.put(this.y);
        return this;
    }

    public Vector load(FloatBuffer buf) {
        this.x = buf.get();
        this.y = buf.get();
        return this;
    }

    public Vector scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public String toString() {
        String stringbuilder = "Vector2f[" +
            this.x +
            ", " +
            this.y +
            ']';
        return stringbuilder;
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public final void setX(float x) {
        this.x = x;
    }

    public final void setY(float y) {
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Vector2f vector2f = (Vector2f) obj;
            return this.x == vector2f.x && this.y == vector2f.y;
        }
    }
}
