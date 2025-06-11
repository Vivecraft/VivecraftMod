package org.vivecraft.client_vr.extensions;

import org.joml.Quaternionf;
import org.vivecraft.client_vr.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    /**
     * calculates the rotation so that something rotated with the resulting Quaternion is facing the camera
     *
     * @param scale  scale for the bounding box, 1.0 will be above the entity, 0.5 in the middle of the entity
     * @param offset vertical offset to move the target point
     * @return Quaternion containing the rotation to look from the camera at the currently rendered entity
     */
    Quaternionf vivecraft$getVRCameraOrientation(float scale, float offset);

    /**
     * @return map of VR arm renderers
     */
    Map<String, VRArmRenderer> vivecraft$getArmSkinMap();
}
