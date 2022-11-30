package org.vivecraft.api.extensions;

import com.mojang.math.Quaternion;
import org.vivecraft.api.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Quaternion getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
