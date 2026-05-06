package org.vivecraft.client_vr.provider.openxr_lwjgl;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.openxr.XrFovf;
import org.lwjgl.openxr.XrPosef;

public class OpenXRUtil {

    /**
     * Converts an OpenXR pose (position + orientation quaternion) to a JOML Matrix4f.
     * OpenXR uses right-handed coordinate system with Y up.
     *
     * @param pose the OpenXR pose
     * @param dest the destination matrix
     * @return the destination matrix with the pose applied
     */
    public static Matrix4f poseToMatrix4f(XrPosef pose, Matrix4f dest) {
        Quaternionf q = new Quaternionf(
            pose.orientation().x(),
            pose.orientation().y(),
            pose.orientation().z(),
            pose.orientation().w()
        );
        return dest.rotation(q).setTranslation(
            pose.position$().x(),
            pose.position$().y(),
            pose.position$().z()
        );
    }

    /**
     * Converts OpenXR asymmetric field-of-view angles to a projection matrix.
     * OpenXR provides per-eye FoV as four angles in radians (left, right, up, down)
     * measured from the center/forward axis.
     *
     * @param fov      the OpenXR FoV structure
     * @param nearClip near clip plane distance
     * @param farClip  far clip plane distance
     * @return a projection matrix
     */
    public static Matrix4f fovToProjectionMatrix(XrFovf fov, float nearClip, float farClip) {
        float tanLeft = (float) Math.tan(fov.angleLeft());
        float tanRight = (float) Math.tan(fov.angleRight());
        float tanDown = (float) Math.tan(fov.angleDown());
        float tanUp = (float) Math.tan(fov.angleUp());

        float tanWidth = tanRight - tanLeft;
        float tanHeight = tanUp - tanDown;

        float a00 = 2.0F / tanWidth;
        float a11 = 2.0F / tanHeight;
        float a20 = (tanRight + tanLeft) / tanWidth;
        float a21 = (tanUp + tanDown) / tanHeight;
        float a22 = -(farClip + nearClip) / (farClip - nearClip);
        float a32 = -(2.0F * farClip * nearClip) / (farClip - nearClip);

        return new Matrix4f(
            a00, 0, 0, 0,
            0, a11, 0, 0,
            a20, a21, a22, -1,
            0, 0, a32, 0
        );
    }

    /**
     * Returns a human-readable string for an OpenXR result code.
     */
    public static String resultToString(int result) {
        // Values from the official OpenXR 1.1 spec:
        // https://registry.khronos.org/OpenXR/specs/1.1/man/html/XrResult.html
        return switch (result) {
            case 0 -> "XR_SUCCESS";
            case 1 -> "XR_TIMEOUT_EXPIRED";
            case 3 -> "XR_SESSION_LOSS_PENDING";
            case 4 -> "XR_EVENT_UNAVAILABLE";
            case 7 -> "XR_SPACE_BOUNDS_UNAVAILABLE";
            case 8 -> "XR_SESSION_NOT_FOCUSED";
            case 9 -> "XR_FRAME_DISCARDED";
            case -1 -> "XR_ERROR_VALIDATION_FAILURE";
            case -2 -> "XR_ERROR_RUNTIME_FAILURE";
            case -3 -> "XR_ERROR_OUT_OF_MEMORY";
            case -4 -> "XR_ERROR_API_VERSION_UNSUPPORTED";
            case -6 -> "XR_ERROR_INITIALIZATION_FAILED";
            case -7 -> "XR_ERROR_FUNCTION_UNSUPPORTED";
            case -8 -> "XR_ERROR_FEATURE_UNSUPPORTED";
            case -9 -> "XR_ERROR_EXTENSION_NOT_PRESENT";
            case -10 -> "XR_ERROR_LIMIT_REACHED";
            case -11 -> "XR_ERROR_SIZE_INSUFFICIENT";
            case -12 -> "XR_ERROR_HANDLE_INVALID";
            case -13 -> "XR_ERROR_INSTANCE_LOST";
            case -14 -> "XR_ERROR_SESSION_RUNNING";
            case -16 -> "XR_ERROR_SESSION_NOT_RUNNING";
            case -17 -> "XR_ERROR_SESSION_LOST";
            case -18 -> "XR_ERROR_SYSTEM_INVALID";
            case -19 -> "XR_ERROR_PATH_INVALID";
            case -20 -> "XR_ERROR_PATH_COUNT_EXCEEDED";
            case -21 -> "XR_ERROR_PATH_FORMAT_INVALID";
            case -22 -> "XR_ERROR_PATH_UNSUPPORTED";
            case -23 -> "XR_ERROR_LAYER_INVALID";
            case -24 -> "XR_ERROR_LAYER_LIMIT_EXCEEDED";
            case -25 -> "XR_ERROR_SWAPCHAIN_RECT_INVALID";
            case -26 -> "XR_ERROR_SWAPCHAIN_FORMAT_UNSUPPORTED";
            case -27 -> "XR_ERROR_ACTION_TYPE_MISMATCH";
            case -28 -> "XR_ERROR_SESSION_NOT_READY";
            case -29 -> "XR_ERROR_SESSION_NOT_STOPPING";
            case -30 -> "XR_ERROR_TIME_INVALID";
            case -31 -> "XR_ERROR_REFERENCE_SPACE_UNSUPPORTED";
            case -32 -> "XR_ERROR_FILE_ACCESS_ERROR";
            case -33 -> "XR_ERROR_FILE_CONTENTS_INVALID";
            case -34 -> "XR_ERROR_FORM_FACTOR_UNSUPPORTED";
            case -35 -> "XR_ERROR_FORM_FACTOR_UNAVAILABLE";
            case -36 -> "XR_ERROR_API_LAYER_NOT_PRESENT";
            case -37 -> "XR_ERROR_CALL_ORDER_INVALID";
            case -38 -> "XR_ERROR_GRAPHICS_DEVICE_INVALID";
            case -39 -> "XR_ERROR_POSE_INVALID";
            case -40 -> "XR_ERROR_INDEX_OUT_OF_RANGE";
            case -41 -> "XR_ERROR_VIEW_CONFIGURATION_TYPE_UNSUPPORTED";
            case -42 -> "XR_ERROR_ENVIRONMENT_BLEND_MODE_UNSUPPORTED";
            case -44 -> "XR_ERROR_NAME_DUPLICATED";
            case -45 -> "XR_ERROR_NAME_INVALID";
            case -46 -> "XR_ERROR_ACTIONSET_NOT_ATTACHED";
            case -47 -> "XR_ERROR_ACTIONSETS_ALREADY_ATTACHED";
            case -48 -> "XR_ERROR_LOCALIZED_NAME_DUPLICATED";
            case -49 -> "XR_ERROR_LOCALIZED_NAME_INVALID";
            case -50 -> "XR_ERROR_GRAPHICS_REQUIREMENTS_CALL_MISSING";
            case -51 -> "XR_ERROR_RUNTIME_UNAVAILABLE";
            default -> "XR_UNKNOWN_" + result;
        };
    }
}
