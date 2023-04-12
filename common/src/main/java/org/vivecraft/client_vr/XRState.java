package org.vivecraft.client_vr;

import org.lwjgl.glfw.GLFW;

public class XRState {

    public static boolean isXr;

    public void enableXR() {
        GLFW.glfwSwapInterval(0);
    }

    public void disableXR() {
//        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
