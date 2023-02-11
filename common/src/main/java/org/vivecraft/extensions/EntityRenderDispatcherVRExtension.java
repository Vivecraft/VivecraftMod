package org.vivecraft.extensions;

import org.joml.Quaternionf;
import org.vivecraft.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    Quaternionf getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
