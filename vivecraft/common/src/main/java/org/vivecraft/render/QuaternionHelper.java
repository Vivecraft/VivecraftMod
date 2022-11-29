package org.vivecraft.render;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.utils.lwjgl.Matrix4f;
import org.vivecraft.utils.lwjgl.Quaternion;

public class QuaternionHelper
{
    public static final Quaternion IDENTITY_QUATERNION = (new Quaternion()).setIdentity();

    public static Quaternion clone(Quaternion q1)
    {
        return new Quaternion(q1.x, q1.y, q1.z, q1.w);
    }

    public static Quaternion pow(Quaternion q1, float power)
    {
        Quaternion quaternion = clone(q1);
        float f = magnitude(quaternion);
        Vec3 vec3 = (new Vec3((double)quaternion.x, (double)quaternion.y, (double)quaternion.z)).normalize();
        Quaternion quaternion1 = exp(scalarMultiply(new Quaternion((float)vec3.x, (float)vec3.y, (float)vec3.z, 0.0F), (float)((double)power * Math.acos((double)(quaternion.w / f)))));
        return scalarMultiply(quaternion1, (float)Math.pow((double)f, (double)power));
    }

    public static Quaternion mul(Quaternion left, Quaternion right)
    {
        Quaternion quaternion = IDENTITY_QUATERNION;
        Quaternion.mul(left, right, quaternion);
        return quaternion;
    }

    public static Quaternion exp(Quaternion input)
    {
        float f = input.w;
        Vec3 vec3 = new Vec3((double)input.x, (double)input.y, (double)input.z);
        float f1 = (float)(Math.exp((double)f) * Math.cos(vec3.length()));
        Vec3 vec31 = new Vec3(Math.exp((double)f) * vec3.normalize().x * (double)((float)Math.sin(vec3.length())), Math.exp((double)f) * vec3.normalize().y * (double)((float)Math.sin(vec3.length())), Math.exp((double)f) * vec3.normalize().z * (double)((float)Math.sin(vec3.length())));
        return new Quaternion((float)vec31.x, (float)vec31.y, (float)vec31.z, f1);
    }

    public static float magnitude(Quaternion input)
    {
        return (float)Math.sqrt((double)(input.x * input.x + input.y * input.y + input.z * input.z + input.w * input.w));
    }

    public static Quaternion scalarMultiply(Quaternion input, float scalar)
    {
        return new Quaternion(input.x * scalar, input.y * scalar, input.z * scalar, input.w * scalar);
    }

    public static Quaternion slerp(Quaternion q1, Quaternion q2, float t)
    {
        Quaternion quaternion = new Quaternion();

        if (isEqual(q1, q2))
        {
            return q1;
        }
        else
        {
            float f = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

            if (f < 0.0F)
            {
                q2 = conjugate(q2);
                f = -f;
            }

            float f1 = 1.0F - t;
            float f2 = 1.0F - t;
            float f3 = t;

            if (1.0F - f > 0.1F)
            {
                float f4 = (float)Math.acos((double)f);
                float f5 = (float)Math.sin((double)f4);
                f2 = (float)Math.sin((double)(f4 * f1)) / f5;
                f3 = (float)Math.sin((double)(f4 * t)) / f5;
            }

            quaternion.x = f2 * q1.x + f3 * q2.x;
            quaternion.y = f2 * q1.y + f3 * q2.y;
            quaternion.z = f2 * q1.z + f3 * q2.z;
            quaternion.w = f2 * q1.w + f3 * q2.w;
            return quaternion;
        }
    }

    public static Quaternion conjugate(Quaternion q1)
    {
        return new Quaternion(-q1.x, -q1.y, -q1.z, q1.w);
    }

    public static boolean isEqual(Quaternion q1, Quaternion q2)
    {
        return q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w;
    }

    public static Quaternion slerp2(Quaternion a, Quaternion b, float t)
    {
        Quaternion quaternion = new Quaternion();
        float f = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        float f1 = 1.0F - t;

        if (f >= 0.95F)
        {
            quaternion.x = a.x * f1 + b.x * t;
            quaternion.y = a.y * f1 + b.y * t;
            quaternion.z = a.z * f1 + b.z * t;
            quaternion.w = a.w * f1 + b.w * t;
            return quaternion;
        }
        else if (f <= -0.99F)
        {
            quaternion.x = 0.5F * (a.x + b.x);
            quaternion.y = 0.5F * (a.y + b.y);
            quaternion.z = 0.5F * (a.z + b.z);
            quaternion.w = 0.5F * (a.w + b.w);
            return quaternion;
        }
        else
        {
            float f2 = (float)Math.sqrt((double)(1.0F - f * f));
            float f3 = (float)Math.acos((double)f);
            float f4 = (float)Math.sin((double)(f1 * f3)) / f2;
            float f5 = (float)Math.sin((double)(t * f3)) / f2;
            quaternion.x = a.x * f4 + b.x * f5;
            quaternion.y = a.y * f4 + b.y * f5;
            quaternion.z = a.z * f4 + b.z * f5;
            quaternion.w = a.w * f4 + b.w * f5;
            return quaternion;
        }
    }

    public static Quaternion slerp1(Quaternion q1, Quaternion q2, float t1)
    {
        Quaternion quaternion = new Quaternion();

        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w)
        {
            quaternion.set(q1);
            return quaternion;
        }
        else
        {
            float f = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;

            if (f < 0.0F)
            {
                q2.x = -q2.x;
                q2.y = -q2.y;
                q2.z = -q2.z;
                q2.w = -q2.w;
                f = -f;
            }

            float f1 = 1.0F - t1;
            float f2 = t1;

            if (1.0F - f > 0.1F)
            {
                float f3 = (float)Math.acos((double)f);
                float f4 = 1.0F / (float)Math.sin((double)f3);
                f1 = (float)Math.sin((double)((1.0F - t1) * f3)) * f4;
                f2 = (float)Math.sin((double)(t1 * f3)) * f4;
            }

            quaternion.x = f1 * q1.x + f2 * q2.x;
            quaternion.y = f1 * q1.y + f2 * q2.y;
            quaternion.z = f1 * q1.z + f2 * q2.z;
            quaternion.w = f1 * q1.w + f2 * q2.w;
            return quaternion;
        }
    }

    public static Matrix4f quatToMatrix4f(Quaternion q)
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = 1.0F - 2.0F * (q.getY() * q.getY() + q.getZ() * q.getZ());
        matrix4f.m01 = 2.0F * (q.getX() * q.getY() + q.getZ() * q.getW());
        matrix4f.m02 = 2.0F * (q.getX() * q.getZ() - q.getY() * q.getW());
        matrix4f.m03 = 0.0F;
        matrix4f.m10 = 2.0F * (q.getX() * q.getY() - q.getZ() * q.getW());
        matrix4f.m11 = 1.0F - 2.0F * (q.getX() * q.getX() + q.getZ() * q.getZ());
        matrix4f.m12 = 2.0F * (q.getZ() * q.getY() + q.getX() * q.getW());
        matrix4f.m13 = 0.0F;
        matrix4f.m20 = 2.0F * (q.getX() * q.getZ() + q.getY() * q.getW());
        matrix4f.m21 = 2.0F * (q.getY() * q.getZ() - q.getX() * q.getW());
        matrix4f.m22 = 1.0F - 2.0F * (q.getX() * q.getX() + q.getY() * q.getY());
        matrix4f.m23 = 0.0F;
        matrix4f.m30 = 0.0F;
        matrix4f.m31 = 0.0F;
        matrix4f.m32 = 0.0F;
        matrix4f.m33 = 1.0F;
        return matrix4f;
    }

    public static FloatBuffer quatToMatrix4fFloatBuf(Quaternion q)
    {
        FloatBuffer floatbuffer = GLUtils.createFloatBuffer(16);
        Matrix4f matrix4f = quatToMatrix4f(q);
        matrix4f.store(floatbuffer);
        ((Buffer)floatbuffer).flip();
        return floatbuffer;
    }
}
