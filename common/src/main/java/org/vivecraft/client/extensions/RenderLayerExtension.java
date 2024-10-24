package org.vivecraft.client.extensions;

/**
 * part of the hacky way, to copy RenderLayers from the regular PlayerRenderer, to the VRPlayerRenderer
 */

public interface RenderLayerExtension {

    /**
     * clones the object using the basic Object.clone() call
     * no "vivecraft$" since that should mimic the method from the "Cloneable" interface
     * @return cloned Object
     * @throws CloneNotSupportedException shouldn't be thrown, unless the mixin failed
     */
    Object clone() throws CloneNotSupportedException;
}
