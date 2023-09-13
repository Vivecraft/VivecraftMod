package org.vivecraft.client.extensions;

import org.vivecraft.client.render.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Map<String, VRPlayerRenderer> vivecraft$getSkinMapVR();

    Map<String, VRPlayerRenderer> vivecraft$getSkinMapVRSeated();
}
