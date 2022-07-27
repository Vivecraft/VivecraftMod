package com.example.examplemod;

import com.mojang.math.Quaternion;
import org.vivecraft.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Quaternion getCameraOrientationOffset(float offset);

    Map<String, VRArmRenderer>  getArmSkinMap();
}
