package org.vivecraft.client_vr.extensions;

import org.joml.Quaternionf;
import org.vivecraft.client_vr.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    Quaternionf getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
