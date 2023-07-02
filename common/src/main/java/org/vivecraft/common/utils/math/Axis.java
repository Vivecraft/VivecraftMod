package org.vivecraft.common.utils.math;

public enum Axis
{
    PITCH(1.0F, 0.0F, 0.0F),
    YAW(0.0F, 1.0F, 0.0F),
    ROLL(0.0F, 0.0F, 1.0F),
    UNKNOWN(0.0F, 0.0F, 0.0F);

    private Vector3 vector;

    private Axis(float x, float y, float z)
    {
        this.vector = new Vector3(x, y, z);
    }

    public Vector3 getVector()
    {
        return this.vector.copy();
    }
}
