package org.vivecraft.client_vr.provider.openxr;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.lwjgl.openxr.XrPosef;
import org.lwjgl.openxr.XrQuaternionf;
import org.vivecraft.common.utils.math.Matrix4f;

public class OpenXRUtil {

    public static void openXRPoseToMarix(XrPosef pose, Matrix4f mat) {
        Matrix3f matrix3f = new Matrix3f().set(new Quaternionf(pose.orientation().x(), pose.orientation().y(), pose.orientation().z(), pose.orientation().w()));
        mat.M[0][0] = matrix3f.m00;
        mat.M[0][1] = matrix3f.m10;
        mat.M[0][2] = matrix3f.m20;
        mat.M[0][3] = pose.position$().x();
        mat.M[1][0] = matrix3f.m01;
        mat.M[1][1] = matrix3f.m11;
        mat.M[1][2] = matrix3f.m21;
        mat.M[1][3] = pose.position$().y();
        mat.M[2][0] = matrix3f.m02;
        mat.M[2][1] = matrix3f.m12;
        mat.M[2][2] = matrix3f.m22;
        mat.M[2][3] = pose.position$().z();
        mat.M[3][0] = 0;
        mat.M[3][1] = 0;
        mat.M[3][2] = 0;
        mat.M[3][3] = 1;
    }

    public static void openXRPoseToMarix(XrQuaternionf quat, Matrix4f mat) {
        Matrix3f matrix3f = new Matrix3f().set(new Quaternionf(quat.x(), quat.y(), quat.z(), quat.w()));
        mat.M[0][0] = matrix3f.m00;
        mat.M[0][1] = matrix3f.m10;
        mat.M[0][2] = matrix3f.m20;
        mat.M[0][3] = 0;
        mat.M[1][0] = matrix3f.m01;
        mat.M[1][1] = matrix3f.m11;
        mat.M[1][2] = matrix3f.m21;
        mat.M[1][3] = 0;
        mat.M[2][0] = matrix3f.m02;
        mat.M[2][1] = matrix3f.m12;
        mat.M[2][2] = matrix3f.m22;
        mat.M[2][3] = 0;
        mat.M[3][0] = 0;
        mat.M[3][1] = 0;
        mat.M[3][2] = 0;
        mat.M[3][3] = 1;
    }
}
