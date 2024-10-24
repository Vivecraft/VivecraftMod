package org.vivecraft.client.extensions;

import org.vivecraft.client.render.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    /**
     * @return map of standing VR player renderers
     */
    Map<String, VRPlayerRenderer> vivecraft$getSkinMapVR();

    /**
     * @return map of seated VR player renderers
     */
    Map<String, VRPlayerRenderer> vivecraft$getSkinMapVRSeated();
}
