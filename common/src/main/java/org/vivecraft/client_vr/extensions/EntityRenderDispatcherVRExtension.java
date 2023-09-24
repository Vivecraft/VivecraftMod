package org.vivecraft.client_vr.extensions;

import org.vivecraft.client_vr.render.VRArmRenderer;

import org.joml.Quaternionf;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    Quaternionf getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
