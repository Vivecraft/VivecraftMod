package org.vivecraft.client.extensions;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */

public interface RenderLayerExtension {
    Object clone() throws CloneNotSupportedException;
}
