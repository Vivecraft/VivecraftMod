package org.vivecraft.client.utils;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.utils.lwjgl.*;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector2;
import org.vivecraft.common.utils.math.Vector3;

public class MathUtils {

    public static Vector3 convertToOVRVector(Vector3f vector) {
        return new Vector3(vector.x, vector.y, vector.z);
    }

    public static Vector3 convertToOVRVector(Vec3 vector) {
        return new Vector3((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Matrix4f convertOVRMatrix(org.vivecraft.common.utils.math.Matrix4f matrix) {
        Matrix4f mat = new Matrix4f();
        mat.m00 = matrix.M[0][0];
        mat.m01 = matrix.M[0][1];
        mat.m02 = matrix.M[0][2];
        mat.m03 = matrix.M[0][3];
        mat.m10 = matrix.M[1][0];
        mat.m11 = matrix.M[1][1];
        mat.m12 = matrix.M[1][2];
        mat.m13 = matrix.M[1][3];
        mat.m20 = matrix.M[2][0];
        mat.m21 = matrix.M[2][1];
        mat.m22 = matrix.M[2][2];
        mat.m23 = matrix.M[2][3];
        mat.m30 = matrix.M[3][0];
        mat.m31 = matrix.M[3][1];
        mat.m32 = matrix.M[3][2];
        mat.m33 = matrix.M[3][3];
        mat.transpose(mat);
        return mat;
    }

    public static org.vivecraft.common.utils.math.Matrix4f convertToOVRMatrix(Matrix4f matrixIn) {
        Matrix4f matrix = new Matrix4f();
        matrixIn.transpose(matrix);
        org.vivecraft.common.utils.math.Matrix4f mat = new org.vivecraft.common.utils.math.Matrix4f();
        mat.M[0][0] = matrix.m00;
        mat.M[0][1] = matrix.m01;
        mat.M[0][2] = matrix.m02;
        mat.M[0][3] = matrix.m03;
        mat.M[1][0] = matrix.m10;
        mat.M[1][1] = matrix.m11;
        mat.M[1][2] = matrix.m12;
        mat.M[1][3] = matrix.m13;
        mat.M[2][0] = matrix.m20;
        mat.M[2][1] = matrix.m21;
        mat.M[2][2] = matrix.m22;
        mat.M[2][3] = matrix.m23;
        mat.M[3][0] = matrix.m30;
        mat.M[3][1] = matrix.m31;
        mat.M[3][2] = matrix.m32;
        mat.M[3][3] = matrix.m33;
        return mat;
    }

    public static double lerp(double from, double to, double percent) {
        return from + (to - from) * percent;
    }

    public static double lerpMod(double from, double to, double percent, double mod) {
        return Math.abs(to - from) < mod / 2.0D ?
            from + (to - from) * percent :
            from + (to - from - Math.signum(to - from) * mod) * percent;
    }

    public static double absLerp(double value, double target, double stepSize) {
        double step = Math.abs(stepSize);

        if (target - value > step) {
            return value + step;
        } else if (target - value < -step) {
            return value - step;
        } else {
            return target;
        }
    }

    public static float angleDiff(float a, float b) {
        float d = Math.abs(a - b) % 360.0F;
        float r = d > 180.0F ? 360.0F - d : d;

        float diff = a - b;
        int sign = (diff >= 0.0F && diff <= 180.0F) || (diff <= -180.0F && diff >= -360.0F) ? 1 : -1;

        return r * sign;
    }

    public static float angleNormalize(float angle) {
        angle = angle % 360.0F;

        if (angle < 0.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static Vector3f directionFromMatrix(Matrix4f matrix, float x, float y, float z) {
        Vector4f vec = new Vector4f(x, y, z, 0.0F);
        Matrix4f.transform(matrix, vec, vec);
        vec.normalise(vec);
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    public static Vector2f convertVector(Vector2 vector) {
        return new Vector2f(vector.getX(), vector.getY());
    }

    public static Vector2 convertVector(Vector2f vector) {
        return new Vector2(vector.getX(), vector.getY());
    }

    public static Vector3f convertVector(Vector3 vector) {
        return new Vector3f(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vector3f vector) {
        return new Vector3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3 convertVector(Vec3 vector) {
        return new Vector3((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Vector3f convertToVector3f(Vec3 vector) {
        return new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
    }

    public static Vec3 convertToVector3d(Vector3 vector) {
        return new Vec3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vec3 convertToVector3d(Vector3f vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vector3f transformVector(Matrix4f matrix, Vector3f vector, boolean point) {
        Vector4f vec = Matrix4f.transform(matrix, new Vector4f(vector.x, vector.y, vector.z, point ? 1.0F : 0.0F),
            null);
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    public static Quaternion quatLerp(Quaternion start, Quaternion end, float fraction) {
        Quaternion quat = new Quaternion();
        quat.w = start.w + (end.w - start.w) * fraction;
        quat.x = start.x + (end.x - start.x) * fraction;
        quat.y = start.y + (end.y - start.y) * fraction;
        quat.z = start.z + (end.z - start.z) * fraction;
        return quat;
    }

    public static Matrix4f matrix3to4(Matrix3f matrix) {
        Matrix4f mat = new Matrix4f();
        mat.m00 = matrix.m00;
        mat.m01 = matrix.m01;
        mat.m02 = matrix.m02;
        mat.m10 = matrix.m10;
        mat.m11 = matrix.m11;
        mat.m12 = matrix.m12;
        mat.m20 = matrix.m20;
        mat.m21 = matrix.m21;
        mat.m22 = matrix.m22;
        return mat;
    }

    public static Quaternion slerp(Quaternion start, Quaternion end, float alpha) {
        float d = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
        float absDot = d < 0.0F ? -d : d;

        // Set the first and second scale for the interpolation
        float scale0 = 1.0F - alpha;
        float scale1 = alpha;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1.0F - absDot) > 0.1F) {
            // Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            float angle = (float) Math.acos(absDot);
            float invSinTheta = 1.0F / (float) Math.sin(angle);

            // Calculate the scale for q1 and q2, according to the angle and
            // its sine value
            scale0 = (float) Math.sin((1.0F - alpha) * angle) * invSinTheta;
            scale1 = (float) Math.sin(alpha * angle) * invSinTheta;
        }

        if (d < 0.0F) {
            scale1 = -scale1;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        float x = (scale0 * start.x) + (scale1 * end.x);
        float y = (scale0 * start.y) + (scale1 * end.y);
        float z = (scale0 * start.z) + (scale1 * end.z);
        float w = (scale0 * start.w) + (scale1 * end.w);

        // Return the interpolated quaternion
        return new Quaternion(w, x, y, z);
    }

    public static Vec3 vecLerp(Vec3 start, Vec3 end, double fraction) {
        double x = start.x + (end.x - start.x) * fraction;
        double y = start.y + (end.y - start.y) * fraction;
        double z = start.z + (end.z - start.z) * fraction;
        return new Vec3(x, y, z);
    }

    public static float applyDeadzone(float axis, float deadzone) {
        if (Math.abs(axis) > deadzone) {
            float scalar = 1.0F / (1.0F - deadzone);
            return (Math.abs(axis) - deadzone) * scalar * Math.signum(axis);
        } else {
            return 0F;
        }
    }

    public static Quaternion convertMatrix4ftoRotationQuat(
        float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
    {
        double lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m00 = (float) (m00 * lengthSquared);
            m10 = (float) (m10 * lengthSquared);
            m20 = (float) (m20 * lengthSquared);
        }

        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m01 = (float) (m01 * lengthSquared);
            m11 = (float) (m11 * lengthSquared);
            m21 = (float) (m21 * lengthSquared);
        }

        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;

        if (lengthSquared != 1.0D && lengthSquared != 0.0D) {
            lengthSquared = 1.0D / Math.sqrt(lengthSquared);
            m02 = (float) (m02 * lengthSquared);
            m12 = (float) (m12 * lengthSquared);
            m22 = (float) (m22 * lengthSquared);
        }

        // Use the Graphics Gems code, from
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        float t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        Quaternion quat = new Quaternion();

        if (t >= 0.0F) { // |w| >= .5
            double s = Math.sqrt(t + 1.0F); // |s|>=1 ...
            quat.w = (float) (0.5D * s);
            s = 0.5D / s; // so this division isn't bad
            quat.x = (float) ((m21 - m12) * s);
            quat.y = (float) ((m02 - m20) * s);
            quat.z = (float) ((m10 - m01) * s);
        } else if (m00 > m11 && m00 > m22) {
            double s = Math.sqrt(1.0D + m00 - m11 - m22); // |s|>=1
            quat.x = (float) (s * 0.5D); // |x| >= .5
            s = 0.5D / s;
            quat.y = (float) ((m10 + m01) * s);
            quat.z = (float) ((m02 + m20) * s);
            quat.w = (float) ((m21 - m12) * s);
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0D + m11 - m00 - m22); // |s|>=1
            quat.y = (float) (s * 0.5D); // |y| >= .5
            s = 0.5D / s;
            quat.x = (float) ((m10 + m01) * s);
            quat.z = (float) ((m21 + m12) * s);
            quat.w = (float) ((m02 - m20) * s);
        } else {
            double s = Math.sqrt(1.0D + m22 - m00 - m11); // |s|>=1
            quat.z = (float) (s * 0.5D); // |z| >= .5
            s = 0.5D / s;
            quat.x = (float) ((m02 + m20) * s);
            quat.y = (float) ((m21 + m12) * s);
            quat.w = (float) ((m10 - m01) * s);
        }

        return quat;
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationXMatrix(float angle) {
        float sina = (float) Math.sin(angle);
        float cosa = (float) Math.cos(angle);
        return new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F,
            0.0F, cosa, -sina,
            0.0F, sina, cosa);
    }

    public static org.vivecraft.common.utils.math.Matrix4f rotationZMatrix(float angle) {
        float sina = (float) Math.sin(angle);
        float cosa = (float) Math.cos(angle);
        return new org.vivecraft.common.utils.math.Matrix4f(cosa, -sina, 0.0F,
            sina, cosa, 0.0F,
            0.0F, 0.0F, 1.0F);
    }

    public static Vector3 convertMatrix4ftoTranslationVector(org.vivecraft.common.utils.math.Matrix4f mat) {
        return new Vector3(mat.M[0][3], mat.M[1][3], mat.M[2][3]);
    }

    public static void Matrix4fSet(
        org.vivecraft.common.utils.math.Matrix4f mat,
        float m11, float m12, float m13, float m14,
        float m21, float m22, float m23, float m24,
        float m31, float m32, float m33, float m34,
        float m41, float m42, float m43, float m44)
    {
        mat.M[0][0] = m11;
        mat.M[0][1] = m12;
        mat.M[0][2] = m13;
        mat.M[0][3] = m14;
        mat.M[1][0] = m21;
        mat.M[1][1] = m22;
        mat.M[1][2] = m23;
        mat.M[1][3] = m24;
        mat.M[2][0] = m31;
        mat.M[2][1] = m32;
        mat.M[2][2] = m33;
        mat.M[2][3] = m34;
        mat.M[3][0] = m41;
        mat.M[3][1] = m42;
        mat.M[3][2] = m43;
        mat.M[3][3] = m44;
    }

    public static void Matrix4fCopy(
        org.vivecraft.common.utils.math.Matrix4f source, org.vivecraft.common.utils.math.Matrix4f dest)
    {
        dest.M[0][0] = source.M[0][0];
        dest.M[0][1] = source.M[0][1];
        dest.M[0][2] = source.M[0][2];
        dest.M[0][3] = source.M[0][3];
        dest.M[1][0] = source.M[1][0];
        dest.M[1][1] = source.M[1][1];
        dest.M[1][2] = source.M[1][2];
        dest.M[1][3] = source.M[1][3];
        dest.M[2][0] = source.M[2][0];
        dest.M[2][1] = source.M[2][1];
        dest.M[2][2] = source.M[2][2];
        dest.M[2][3] = source.M[2][3];
        dest.M[3][0] = source.M[3][0];
        dest.M[3][1] = source.M[3][1];
        dest.M[3][2] = source.M[3][2];
        dest.M[3][3] = source.M[3][3];
    }
}
