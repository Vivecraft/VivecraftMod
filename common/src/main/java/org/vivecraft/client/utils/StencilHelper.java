package org.vivecraft.client.utils;

import org.vivecraft.Xloader;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class StencilHelper {

    /**
     * @return if the stencil buffer is supported for rendering
     */
    public static boolean stencilBufferSupported() {
        return ClientDataHolderVR.getInstance().vrSettings.stencilBufferDisable &&
            // disable when satin is loaded, since it causes slowdowns when doing the depth copy
            !Xloader.isModLoaded("satin");
    }
}
