package org.vivecraft.extensions;

import org.vivecraft.render.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Map<String, VRPlayerRenderer> getSkinMapVR();

    Map<String, VRPlayerRenderer> getSkinMapVRSeated();
}
