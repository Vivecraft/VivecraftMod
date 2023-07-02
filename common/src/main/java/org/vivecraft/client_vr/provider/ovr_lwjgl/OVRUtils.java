//package org.vivecraft.provider.ovr_lwjgl;
//
//import org.lwjgl.ovr.OVRMatrix4f;
//import org.lwjgl.ovr.OVRPosef;
//import org.vivecraft.utils.Utils;
//import org.vivecraft.utils.math.Matrix4f;
//import org.vivecraft.utils.math.Quaternion;
//
//public class OVRUtils
//{
//    public static Matrix4f ovrPoseToMatrix(OVRPosef pose)
//    {
//        Quaternion quaternion = new Quaternion();
//        quaternion.x = pose.Orientation().x();
//        quaternion.y = pose.Orientation().y();
//        quaternion.z = pose.Orientation().z();
//        quaternion.w = pose.Orientation().w();
//        Matrix4f matrix4f = new Matrix4f(quaternion);
//        matrix4f.M[0][3] = pose.Position().x();
//        matrix4f.M[1][3] = pose.Position().y();
//        matrix4f.M[2][3] = pose.Position().z();
//        return matrix4f;
//    }
//
//    public static Matrix4f ovrMatrix4ToMatrix4f(OVRMatrix4f hmdMatrix)
//    {
//        Matrix4f matrix4f = new Matrix4f();
//        Utils.Matrix4fSet(matrix4f, hmdMatrix.M(0), hmdMatrix.M(1), hmdMatrix.M(2), hmdMatrix.M(3), hmdMatrix.M(4), hmdMatrix.M(5), hmdMatrix.M(6), hmdMatrix.M(7), hmdMatrix.M(8), hmdMatrix.M(9), hmdMatrix.M(10), hmdMatrix.M(11), hmdMatrix.M(12), hmdMatrix.M(13), hmdMatrix.M(14), hmdMatrix.M(15));
//        return matrix4f;
//    }
//}
