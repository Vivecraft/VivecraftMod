package org.vivecraft.client_vr.extensions;

public interface WindowExtension {
    /**
     * @return the actual height of the desktop window, since we override the default method
     */
    int vivecraft$getActualScreenHeight();

    /**
     * @return the actual width of the desktop window, since we override the default method
     */
    int vivecraft$getActualScreenWidth();

    /**
     * sets the resized flag, so that the game calls the resize routine
     */
    void vivecraft$resize();
}
