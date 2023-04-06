package org.vivecraft.client.extensions;

import org.joml.Quaternionf;
import org.vivecraft.client.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    Quaternionf getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
