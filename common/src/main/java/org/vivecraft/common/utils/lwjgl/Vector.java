package org.vivecraft.common.utils.lwjgl;

import java.nio.FloatBuffer;

@Deprecated
public abstract class Vector {
    protected Vector() {
    }

    public final float length() {
        return (float) Math.sqrt(this.lengthSquared());
    }

    public abstract float lengthSquared();

    public abstract Vector load(FloatBuffer var1);

    public abstract Vector negate();

    public final Vector normalise() {
        float f = this.length();

        if (f != 0.0F) {
            float f1 = 1.0F / f;
            return this.scale(f1);
        } else {
            throw new IllegalStateException("Zero length vector");
        }
    }

    public abstract Vector store(FloatBuffer var1);

    public abstract Vector scale(float var1);
}
