package org.vivecraft.client.utils;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */
public enum RenderLayerType {
    // cant put that inside the other class, because of Mixin
    PARENT_ONLY,
    PARENT_MODELSET,
    PARENT_MODEL_MODEL,
    OTHER
}
