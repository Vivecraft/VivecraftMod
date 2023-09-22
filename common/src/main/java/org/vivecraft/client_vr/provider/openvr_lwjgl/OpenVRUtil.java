package org.vivecraft.client_vr.provider.openvr_lwjgl;

import org.lwjgl.openvr.HmdMatrix34;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;

public class OpenVRUtil {
    public static Matrix4f convertSteamVRMatrix3ToMatrix4f(HmdMatrix34 hmdMatrix, Matrix4f mat) {
        Utils.Matrix4fSet(mat, hmdMatrix.m(0), hmdMatrix.m(1), hmdMatrix.m(2), hmdMatrix.m(3), hmdMatrix.m(4), hmdMatrix.m(5), hmdMatrix.m(6), hmdMatrix.m(7), hmdMatrix.m(8), hmdMatrix.m(9), hmdMatrix.m(10), hmdMatrix.m(11), 0.0F, 0.0F, 0.0F, 1.0F);
        return mat;
    }

//    public static Matrix4f convertSteamVRMatrix4ToMatrix4f(HmdMatrix44_t hmdMatrix, Matrix4f mat)
//    {
//        Utils.Matrix4fSet(mat, hmdMatrix.m[0], hmdMatrix.m[1], hmdMatrix.m[2], hmdMatrix.m[3], hmdMatrix.m[4], hmdMatrix.m[5], hmdMatrix.m[6], hmdMatrix.m[7], hmdMatrix.m[8], hmdMatrix.m[9], hmdMatrix.m[10], hmdMatrix.m[11], hmdMatrix.m[12], hmdMatrix.m[13], hmdMatrix.m[14], hmdMatrix.m[15]);
//        return mat;
//    }

    public static Quaternion convertMatrix4ftoRotationQuat(Matrix4f mat) {
        return Utils.convertMatrix4ftoRotationQuat(mat.M[0][0], mat.M[0][1], mat.M[0][2], mat.M[1][0], mat.M[1][1], mat.M[1][2], mat.M[2][0], mat.M[2][1], mat.M[2][2]);
    }

//    public static HmdMatrix34_t convertToMatrix34(org.joml.Matrix4f matrix)
//    {
//        HmdMatrix34_t hmdmatrix34_t = new HmdMatrix34_t();
//        hmdmatrix34_t.m[0] = matrix.m00();
//        hmdmatrix34_t.m[1] = matrix.m10();
//        hmdmatrix34_t.m[2] = matrix.m20();
//        hmdmatrix34_t.m[3] = matrix.m30();
//        hmdmatrix34_t.m[4] = matrix.m01();
//        hmdmatrix34_t.m[5] = matrix.m11();
//        hmdmatrix34_t.m[6] = matrix.m21();
//        hmdmatrix34_t.m[7] = matrix.m31();
//        hmdmatrix34_t.m[8] = matrix.m02();
//        hmdmatrix34_t.m[9] = matrix.m12();
//        hmdmatrix34_t.m[10] = matrix.m22();
//        hmdmatrix34_t.m[11] = matrix.m32();
//        return hmdmatrix34_t;
//    }
}
