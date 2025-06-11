package org.vivecraft.client.utils;

public class StencilHelper {

    /**
     * @return if the stencil buffer is supported for rendering
     */
    public static boolean stencilBufferSupported() {
        return false;
        // TODO figure out stencil
            /*
            ClientDataHolderVR.getInstance().vrSettings.stencilBufferDisable &&
            // disable when satin is loaded, since it causes slowdowns when doing the depth copy
            !Xplat.isModLoaded("satin");
            */
    }
}
