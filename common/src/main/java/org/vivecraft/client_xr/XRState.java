package org.vivecraft.client_xr;

import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VRMixinConfig;

public class XRState {

    public static boolean isXr = false;

    public void setupXR() {
        VRMixinConfig.initializeVR();
    }

    public void enableXR() {
        GLFW.glfwSwapInterval(0);
    }

    public void disableXR() {
//        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
