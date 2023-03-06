package org.vivecraft.extensions;

import org.joml.Quaternionf;
import org.vivecraft.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Quaternionf getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
