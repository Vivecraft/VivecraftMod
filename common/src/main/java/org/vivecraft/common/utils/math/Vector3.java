package org.vivecraft.common.utils.math;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.utils.lwjgl.Matrix3f;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector3f;

@Deprecated
public class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3() {
    }

    public Vector3(Vec3 vec3d) {
        this.x = (float) vec3d.x;
        this.y = (float) vec3d.y;
        this.z = (float) vec3d.z;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vector3(Vector3f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vec3 toVector3d() {
        return new Vec3(this.x, this.y, this.z);
    }

    public Vector3 copy() {
        return new Vector3(this);
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 add(float number) {
        return new Vector3(this.x + number, this.y + number, this.z + number);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3 subtract(float number) {
        return new Vector3(this.x - number, this.y - number, this.z - number);
    }

    public Vector3 multiply(float number) {
        return new Vector3(this.x * number, this.y * number, this.z * number);
    }

    public Vector3 multiply(Matrix3f matrix) {
        float f = matrix.m00 * this.x + matrix.m01 * this.y + matrix.m02 * this.z;
        float f1 = matrix.m10 * this.x + matrix.m11 * this.y + matrix.m12 * this.z;
        float f2 = matrix.m20 * this.x + matrix.m21 * this.y + matrix.m22 * this.z;
        return new Vector3(f, f1, f2);
    }

    public Vector3 multiply(Matrix4f matrix) {
        float f = matrix.m00 * this.x + matrix.m01 * this.y + matrix.m02 * this.z + matrix.m03;
        float f1 = matrix.m10 * this.x + matrix.m11 * this.y + matrix.m12 * this.z + matrix.m13;
        float f2 = matrix.m20 * this.x + matrix.m21 * this.y + matrix.m22 * this.z + matrix.m23;
        return new Vector3(f, f1, f2);
    }

    public Vector3 divide(float number) {
        return new Vector3(this.x / number, this.y / number, this.z / number);
    }

    public Vector3 negate() {
        return new Vector3(-this.x, -this.y, -this.z);
    }

    public Angle angle(Vector3 other) {
        float f = other.x - this.x;
        float f1 = other.z - this.z;
        float f2 = (float) Math.toDegrees(Math.atan2(other.y - this.y, Math.sqrt(f * f + f1 * f1)));
        float f3 = (float) Math.toDegrees(Math.atan2(-f, -f1));
        return new Angle(f2, f3);
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public float distance(Vector3 other) {
        float f = other.x - this.x;
        float f1 = other.y - this.y;
        float f2 = other.z - this.z;
        return (float) Math.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public float distanceSquared(Vector3 other) {
        float f = other.x - this.x;
        float f1 = other.y - this.y;
        float f2 = other.z - this.z;
        return f * f + f1 * f1 + f2 * f2;
    }

    public float distance2D(Vector2 other) {
        return other.subtract(new Vector2(this.x, this.z)).length();
    }

    public float distanceSquared2D(Vector2 other) {
        return other.subtract(new Vector2(this.x, this.z)).lengthSquared();
    }

    public float distance2D(Vector3 other) {
        return this.distance2D(new Vector2(other.x, other.z));
    }

    public float distanceSquared2D(Vector3 other) {
        return this.distanceSquared2D(new Vector2(other.x, other.z));
    }

    public void normalize() {
        this.set(this.divide(this.length()));
    }

    public Vector3 normalized() {
        return this.divide(this.length());
    }

    public float dot(Vector3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    public Vector3 project(Vector3 other) {
        return other.multiply(other.dot(this) / other.dot(other));
    }

    public static Vector3 forward() {
        return new Vector3(0.0F, 0.0F, -1.0F);
    }

    public static Vector3 up() {
        return new Vector3(0.0F, -1.0F, 0.0F);
    }

    public static Vector3 right() {
        return new Vector3(-1.0F, 0.0F, 0.0F);
    }

    public static Vector3 lerp(Vector3 start, Vector3 end, float fraction) {
        return start.add(end.subtract(start).multiply(fraction));
    }

    public static Vector3 slerp(Vector3 start, Vector3 end, float fraction) {
        float f = start.dot(end);
        float f1 = (float) Math.acos(f) * fraction;
        Vector3 vector3 = end.subtract(start.multiply(f));
        vector3.normalize();
        return start.multiply((float) Math.cos(f1)).add(vector3.multiply((float) Math.sin(f1)));
    }

    public static Matrix3f lookMatrix(Vector3 forward, Vector3 up) {
        Vector3 vector3 = forward.normalized();
        Vector3 vector31 = up.cross(vector3).normalized();
        Vector3 vector32 = vector3.cross(vector31);
        Matrix3f matrix3f = new Matrix3f();
        matrix3f.m00 = vector31.x;
        matrix3f.m01 = vector31.y;
        matrix3f.m02 = vector31.z;
        matrix3f.m10 = vector32.x;
        matrix3f.m11 = vector32.y;
        matrix3f.m12 = vector32.z;
        matrix3f.m20 = vector3.x;
        matrix3f.m21 = vector3.y;
        matrix3f.m22 = vector3.z;
        return matrix3f;
    }

    public int hashCode() {
        int i = 7;
        i = 53 * i + Float.floatToIntBits(this.x);
        i = 53 * i + Float.floatToIntBits(this.y);
        return 53 * i + Float.floatToIntBits(this.z);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Vector3 vector3 = (Vector3) obj;

            if (Float.floatToIntBits(this.x) != Float.floatToIntBits(vector3.x)) {
                return false;
            } else if (Float.floatToIntBits(this.y) != Float.floatToIntBits(vector3.y)) {
                return false;
            } else {
                return Float.floatToIntBits(this.z) == Float.floatToIntBits(vector3.z);
            }
        }
    }

    public String toString() {
        return "(" + String.format("%.2f", this.x) + ", " + String.format("%.2f", this.y) + ", " + String.format("%.2f", this.z) + ")";
    }
}
