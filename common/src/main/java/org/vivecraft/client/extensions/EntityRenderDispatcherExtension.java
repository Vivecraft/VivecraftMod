package org.vivecraft.client.extensions;

import org.vivecraft.client.render.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Map<String, VRPlayerRenderer> getSkinMapVR();

    Map<String, VRPlayerRenderer> getSkinMapVRSeated();
}
