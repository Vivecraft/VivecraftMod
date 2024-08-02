package org.vivecraft.client_vr.extensions;

import org.joml.Quaternionf;
import org.vivecraft.client_vr.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    /**
     * calculates the rotation so that something rotated with the resulting Quaternion is facing the camera
     * @param offset vertical offset to move the target point
     * @return Quaternion containing the rotation to look from the camera at the currently rendered entity
     */
    Quaternionf vivecraft$getCameraOrientationOffset(float offset);

    /**
     * @return map of VR arm renderers
     */
    Map<String, VRArmRenderer> vivecraft$getArmSkinMap();
}
