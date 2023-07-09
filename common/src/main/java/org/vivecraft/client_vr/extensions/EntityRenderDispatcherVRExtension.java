package org.vivecraft.client_vr.extensions;

import com.mojang.math.Quaternion;
import org.vivecraft.client_vr.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    Quaternion getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
