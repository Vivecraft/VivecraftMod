package org.vivecraft.utils;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */
public class RenderLayerTypes {

    // cant put that inside the other class, because of Mixin
    public enum LayerType {
        PARENT_ONLY,
        PARENT_MODELSET,
        PARENT_MODEL_MODEL,
        OTHER
    }
}
