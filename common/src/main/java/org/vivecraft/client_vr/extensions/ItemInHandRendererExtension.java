package org.vivecraft.client_vr.extensions;

import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

public interface ItemInHandRendererExtension {
    /**
     * sets the swing type for the players hand, when pressing use/attack
     * @param swingType swing type to set
     */
    void vivecraft$setSwingType(VRFirstPersonArmSwing swingType);
}
