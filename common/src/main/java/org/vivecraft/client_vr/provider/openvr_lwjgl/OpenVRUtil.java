package org.vivecraft.client_vr.provider.openvr_lwjgl;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.vivecraft.client.utils.MathUtils;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;

public class OpenVRUtil {
    public static Matrix4f convertSteamVRMatrix3ToMatrix4f(HmdMatrix34 hmdMatrix, Matrix4f mat) {
        MathUtils.Matrix4fSet(mat,
            hmdMatrix.m(0), hmdMatrix.m(1), hmdMatrix.m(2), hmdMatrix.m(3),
            hmdMatrix.m(4), hmdMatrix.m(5), hmdMatrix.m(6), hmdMatrix.m(7),
            hmdMatrix.m(8), hmdMatrix.m(9), hmdMatrix.m(10), hmdMatrix.m(11),
            0.0F, 0.0F, 0.0F, 1.0F);
        return mat;
    }

    public static org.joml.Matrix4f Matrix4fFromOpenVR(HmdMatrix44 in) {
        return new org.joml.Matrix4f(
            in.m(0), in.m(4), in.m(8), in.m(12),
            in.m(1), in.m(5), in.m(9), in.m(13),
            in.m(2), in.m(6), in.m(10), in.m(14),
            in.m(3), in.m(7), in.m(11), in.m(15));
    }

    public static Quaternion convertMatrix4ftoRotationQuat(Matrix4f mat) {
        return MathUtils.convertMatrix4ftoRotationQuat(
            mat.M[0][0], mat.M[0][1], mat.M[0][2],
            mat.M[1][0], mat.M[1][1], mat.M[1][2],
            mat.M[2][0], mat.M[2][1], mat.M[2][2]);
    }
}
