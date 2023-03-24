package org.vivecraft.provider.openvr_jna;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Matrix4f;
import org.vivecraft.utils.math.Quaternion;

public class OpenVRUtil
{
    public static Matrix4f convertSteamVRMatrix3ToMatrix4f(HmdMatrix34 hmdMatrix, Matrix4f mat)
    {
        Utils.Matrix4fSet(mat, hmdMatrix.m(0), hmdMatrix.m(1), hmdMatrix.m(2), hmdMatrix.m(3), hmdMatrix.m(4), hmdMatrix.m(5), hmdMatrix.m(6), hmdMatrix.m(7), hmdMatrix.m(8), hmdMatrix.m(9), hmdMatrix.m(10), hmdMatrix.m(11), 0.0F, 0.0F, 0.0F, 1.0F);
        return mat;
    }

    public static Quaternion convertMatrix4ftoRotationQuat(Matrix4f mat)
    {
        return Utils.convertMatrix4ftoRotationQuat(mat.M[0][0], mat.M[0][1], mat.M[0][2], mat.M[1][0], mat.M[1][1], mat.M[1][2], mat.M[2][0], mat.M[2][1], mat.M[2][2]);
    }

}