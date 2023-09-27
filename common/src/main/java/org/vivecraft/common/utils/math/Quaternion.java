package org.vivecraft.common.utils.math;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.lwjgl.Matrix3f;

@Deprecated
public class Quaternion {
    public float w;
    public float x;
    public float y;
    public float z;

    public Quaternion() {
        this.w = 1.0F;
    }

    public Quaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(Quaternion other) {
        this.w = other.w;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Quaternion(Vector3 vector, float rotation) {
        rotation = (float) Math.toRadians(rotation);
        float f = (float) Math.sin(rotation / 2.0F);
        this.w = (float) Math.cos(rotation / 2.0F);
        this.x = vector.x * f;
        this.y = vector.y * f;
        this.z = vector.z * f;
    }

    public Quaternion(Axis axis, float rotation) {
        this(axis.getVector(), rotation);
    }

    public Quaternion(float pitch, float yaw, float roll, Angle.Order order) {
        Quaternion quaternion = new Quaternion(new Vector3(1.0F, 0.0F, 0.0F), pitch);
        Quaternion quaternion1 = new Quaternion(new Vector3(0.0F, 1.0F, 0.0F), yaw);
        Quaternion quaternion2 = new Quaternion(new Vector3(0.0F, 0.0F, 1.0F), roll);
        Quaternion quaternion3 = null;

        switch (order) {
            case XYZ:
                quaternion3 = quaternion.multiply(quaternion1).multiply(quaternion2);
                break;

            case ZYX:
                quaternion3 = quaternion2.multiply(quaternion1).multiply(quaternion);
                break;

            case YXZ:
                quaternion3 = quaternion1.multiply(quaternion).multiply(quaternion2);
                break;

            case ZXY:
                quaternion3 = quaternion2.multiply(quaternion).multiply(quaternion1);
                break;

            case YZX:
                quaternion3 = quaternion1.multiply(quaternion2).multiply(quaternion);
                break;

            case XZY:
                quaternion3 = quaternion.multiply(quaternion2).multiply(quaternion1);
        }

        this.w = quaternion3.w;
        this.x = quaternion3.x;
        this.y = quaternion3.y;
        this.z = quaternion3.z;
    }

    public Quaternion(float pitch, float yaw, float roll) {
        this(pitch, yaw, roll, Angle.Order.YXZ);
    }

    public Quaternion(Angle angle) {
        this(angle.getPitch(), angle.getYaw(), angle.getRoll(), angle.getOrder());
    }

    public Quaternion(Matrix3f matrix) {
        this(matrix.m00, matrix.m01, matrix.m02, matrix.m10, matrix.m11, matrix.m12, matrix.m20, matrix.m21, matrix.m22);
    }

    public Quaternion(org.vivecraft.common.utils.lwjgl.Matrix4f matrix) {
        this(matrix.m00, matrix.m01, matrix.m02, matrix.m10, matrix.m11, matrix.m12, matrix.m20, matrix.m21, matrix.m22);
    }

    public Quaternion(Matrix4f matrix) {
        this(matrix.M[0][0], matrix.M[0][1], matrix.M[0][2], matrix.M[1][0], matrix.M[1][1], matrix.M[1][2], matrix.M[2][0], matrix.M[2][1], matrix.M[2][2]);
    }

    private Quaternion(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float f1 = m00 + m11 + m22;

        if ((double) f1 >= 0.0D) {
            float f = (float) Math.sqrt((double) f1 + 1.0D);
            this.w = f * 0.5F;
            f = 0.5F / f;
            this.x = (m21 - m12) * f;
            this.y = (m02 - m20) * f;
            this.z = (m10 - m01) * f;
        } else {
            float f2 = Math.max(Math.max(m00, m11), m22);

            if (f2 == m00) {
                float f3 = (float) Math.sqrt((double) (m00 - (m11 + m22)) + 1.0D);
                this.x = f3 * 0.5F;
                f3 = 0.5F / f3;
                this.y = (m01 + m10) * f3;
                this.z = (m20 + m02) * f3;
                this.w = (m21 - m12) * f3;
            } else if (f2 == m11) {
                float f4 = (float) Math.sqrt((double) (m11 - (m22 + m00)) + 1.0D);
                this.y = f4 * 0.5F;
                f4 = 0.5F / f4;
                this.z = (m12 + m21) * f4;
                this.x = (m01 + m10) * f4;
                this.w = (m02 - m20) * f4;
            } else {
                float f5 = (float) Math.sqrt((double) (m22 - (m00 + m11)) + 1.0D);
                this.z = f5 * 0.5F;
                f5 = 0.5F / f5;
                this.x = (m20 + m02) * f5;
                this.y = (m12 + m21) * f5;
                this.w = (m10 - m01) * f5;
            }
        }
    }

    public Quaternion copy() {
        return new Quaternion(this);
    }

    public float getW() {
        return this.w;
    }

    public void setW(float w) {
        this.w = w;
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

    public void set(Quaternion other) {
        this.w = other.w;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public void normalize() {
        float f = (float) Math.sqrt(this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);

        if (f > 0.0F) {
            this.w /= f;
            this.x /= f;
            this.y /= f;
            this.z /= f;
        } else {
            this.w = 1.0F;
            this.x = 0.0F;
            this.y = 0.0F;
            this.z = 0.0F;
        }
    }

    public Quaternion normalized() {
        float f4 = (float) Math.sqrt(this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);
        float f;
        float f1;
        float f2;
        float f3;

        if (f4 > 0.0F) {
            f = this.w / f4;
            f1 = this.x / f4;
            f2 = this.y / f4;
            f3 = this.z / f4;
        } else {
            f = 1.0F;
            f1 = 0.0F;
            f2 = 0.0F;
            f3 = 0.0F;
        }

        return new Quaternion(f, f1, f2, f3);
    }

    public Angle toEuler() {
        Angle angle = new Angle();
        angle.setYaw((float) Math.toDegrees(Math.atan2(2.0F * (this.x * this.z + this.w * this.y), this.w * this.w - this.x * this.x - this.y * this.y + this.z * this.z)));
        angle.setPitch((float) Math.toDegrees(Math.asin(-2.0F * (this.y * this.z - this.w * this.x))));
        angle.setRoll((float) Math.toDegrees(Math.atan2(2.0F * (this.x * this.y + this.w * this.z), this.w * this.w - this.x * this.x + this.y * this.y - this.z * this.z)));
        return angle;
    }

    public Quaternion rotate(Axis axis, float degrees, boolean local) {
        if (local) {
            return this.multiply(new Quaternion(axis, degrees));
        } else {
            org.vivecraft.common.utils.lwjgl.Matrix4f matrix4f = this.getMatrix();
            matrix4f.rotate((float) Math.toRadians(degrees), Utils.convertVector(axis.getVector()));
            return new Quaternion(matrix4f);
        }
    }

    public Quaternion multiply(Quaternion other) {
        float f = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
        float f1 = this.w * other.x + other.w * this.x + this.y * other.z - this.z * other.y;
        float f2 = this.w * other.y + other.w * this.y - this.x * other.z + this.z * other.x;
        float f3 = this.w * other.z + other.w * this.z + this.x * other.y - this.y * other.x;
        return new Quaternion(f, f1, f2, f3);
    }

    public org.vivecraft.common.utils.lwjgl.Matrix4f getMatrix() {
        org.vivecraft.common.utils.lwjgl.Matrix4f matrix4f = new org.vivecraft.common.utils.lwjgl.Matrix4f();
        float f = this.w * this.w;
        float f1 = this.x * this.x;
        float f2 = this.y * this.y;
        float f3 = this.z * this.z;
        float f4 = 1.0F / (f1 + f2 + f3 + f);
        matrix4f.m00 = (f1 - f2 - f3 + f) * f4;
        matrix4f.m11 = (-f1 + f2 - f3 + f) * f4;
        matrix4f.m22 = (-f1 - f2 + f3 + f) * f4;
        float f5 = this.x * this.y;
        float f6 = this.z * this.w;
        matrix4f.m10 = 2.0F * (f5 + f6) * f4;
        matrix4f.m01 = 2.0F * (f5 - f6) * f4;
        f5 = this.x * this.z;
        f6 = this.y * this.w;
        matrix4f.m20 = 2.0F * (f5 - f6) * f4;
        matrix4f.m02 = 2.0F * (f5 + f6) * f4;
        f5 = this.y * this.z;
        f6 = this.x * this.w;
        matrix4f.m21 = 2.0F * (f5 + f6) * f4;
        matrix4f.m12 = 2.0F * (f5 - f6) * f4;
        return matrix4f;
    }

    public Quaternion inverse() {
        return new Quaternion(this.w, -this.x, -this.y, -this.z);
    }

    public static Quaternion createFromToVector(Vector3 from, Vector3 to) {
        Vector3 vector3 = from.cross(to);
        float f = (float) (Math.sqrt(Math.pow(from.length(), 2.0D) * Math.pow(to.length(), 2.0D)) + (double) from.dot(to));
        return (new Quaternion(f, vector3.x, vector3.y, vector3.z)).normalized();
    }

    public int hashCode() {
        int i = 3;
        i = 23 * i + Float.floatToIntBits(this.w);
        i = 23 * i + Float.floatToIntBits(this.x);
        i = 23 * i + Float.floatToIntBits(this.y);
        return 23 * i + Float.floatToIntBits(this.z);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Quaternion quaternion = (Quaternion) obj;

            if (Float.floatToIntBits(this.w) != Float.floatToIntBits(quaternion.w)) {
                return false;
            } else if (Float.floatToIntBits(this.x) != Float.floatToIntBits(quaternion.x)) {
                return false;
            } else if (Float.floatToIntBits(this.y) != Float.floatToIntBits(quaternion.y)) {
                return false;
            } else {
                return Float.floatToIntBits(this.z) == Float.floatToIntBits(quaternion.z);
            }
        }
    }

    public Vector3 multiply(Vector3 vec) {
        float f = this.x * 2.0F;
        float f1 = this.y * 2.0F;
        float f2 = this.z * 2.0F;
        float f3 = this.x * f;
        float f4 = this.y * f1;
        float f5 = this.z * f2;
        float f6 = this.x * f1;
        float f7 = this.x * f2;
        float f8 = this.y * f2;
        float f9 = this.w * f;
        float f10 = this.w * f1;
        float f11 = this.w * f2;
        Vector3 vector3 = new Vector3();
        vector3.x = (1.0F - (f4 + f5)) * vec.x + (f6 - f11) * vec.y + (f7 + f10) * vec.z;
        vector3.y = (f6 + f11) * vec.x + (1.0F - (f3 + f5)) * vec.y + (f8 - f9) * vec.z;
        vector3.z = (f7 - f10) * vec.x + (f8 + f9) * vec.y + (1.0F - (f3 + f4)) * vec.z;
        return vector3;
    }

    public Vec3 multiply(Vec3 vec) {
        return this.multiply(new Vector3(vec)).toVector3d();
    }

    public String toString() {
        return "Quaternion{w=" + this.w + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
    }
}
