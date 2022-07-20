package com.example.examplemod;

import com.mojang.math.Quaternion;

public interface EntityRenderDispatcherExtension {

    Quaternion getCameraOrientationOffset(float offset);
}
