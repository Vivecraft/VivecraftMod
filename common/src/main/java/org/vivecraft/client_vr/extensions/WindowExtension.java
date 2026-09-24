package org.vivecraft.client_vr.extensions;

import org.joml.Vector2ic;

public interface WindowExtension {

    /**
     * @return the actual size of the desktop window, since we override the default method
     */
    Vector2ic vivecraft$getActualWindowSize();

    /**
     * @return the actual size of the desktop window framebuffer, since we override the default method
     */
    Vector2ic vivecraft$getActualFramebufferSize();

    /**
     * sets the resized flag, so that the game calls the resize routine
     */
    void vivecraft$resize();
}
