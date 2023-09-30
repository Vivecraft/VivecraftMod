package org.vivecraft.client.extensions;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */

public interface RenderLayerExtension {

    // no "vivecraft$" since that should mimic the method from the "Cloneable" interface
    Object clone() throws CloneNotSupportedException;
}
