package org.vivecraft.common.utils.math;

import org.vivecraft.common.utils.lwjgl.Matrix4f;

public class Angle {
    private float pitch;
    private float yaw;
    private float roll;
    private Order order;

    public Angle() {
        this.order = Order.YXZ;
    }

    public Angle(Order order) {
        this.order = order;
    }

    public Angle(float pitch, float yaw, float roll, Order order) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.order = order;
    }

    public Angle(float pitch, float yaw, float roll) {
        this(pitch, yaw, roll, Order.YXZ);
    }

    public Angle(float pitch, float yaw) {
        this(pitch, yaw, 0.0F, Order.YXZ);
    }

    public Angle(Angle other) {
        this.pitch = other.pitch;
        this.yaw = other.yaw;
        this.roll = other.roll;
        this.order = other.order;
    }

    public Angle copy() {
        return new Angle(this);
    }

    public void set(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public void set(Angle other) {
        this.pitch = other.pitch;
        this.yaw = other.yaw;
        this.roll = other.roll;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return this.roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Angle rotate(Axis axis, float degrees) {
        switch (axis) {
            case PITCH:
                return new Angle(this.pitch + degrees, this.yaw, this.roll);

            case YAW:
                return new Angle(this.pitch, this.yaw + degrees, this.roll);

            case ROLL:
                return new Angle(this.pitch, this.yaw, this.roll + degrees);

            default:
                return new Angle(this);
        }
    }

    public Angle add(Angle other) {
        return new Angle(this.pitch + other.pitch, this.yaw + other.yaw, this.roll + other.roll, this.order);
    }

    public Angle subtract(Angle other) {
        return new Angle(this.pitch - other.pitch, this.yaw - other.yaw, this.roll - other.roll, this.order);
    }

    public Matrix4f getMatrix() {
        return (new Quaternion(this)).getMatrix();
    }

    public Vector3 forward() {
        return (new Vector3(0.0F, 0.0F, -1.0F)).multiply(this.getMatrix());
    }

    public Vector3 up() {
        return (new Vector3(0.0F, 1.0F, 0.0F)).multiply(this.getMatrix());
    }

    public Vector3 right() {
        return (new Vector3(1.0F, 0.0F, 0.0F)).multiply(this.getMatrix());
    }

    public int hashCode() {
        int i = 5;
        i = 89 * i + Float.floatToIntBits(this.pitch);
        i = 89 * i + Float.floatToIntBits(this.yaw);
        return 89 * i + Float.floatToIntBits(this.roll);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Angle angle = (Angle) obj;

            if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(angle.pitch)) {
                return false;
            } else if (Float.floatToIntBits(this.yaw) != Float.floatToIntBits(angle.yaw)) {
                return false;
            } else {
                return Float.floatToIntBits(this.roll) == Float.floatToIntBits(angle.roll);
            }
        }
    }

    public String toString() {
        return "Angle{pitch=" + this.pitch + ", yaw=" + this.yaw + ", roll=" + this.roll + '}';
    }

    public enum Order {
        XYZ,
        ZYX,
        YXZ,
        ZXY,
        YZX,
        XZY
    }
}
