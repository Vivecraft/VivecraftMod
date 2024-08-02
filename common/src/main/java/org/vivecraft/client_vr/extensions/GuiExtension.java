package org.vivecraft.client_vr.extensions;

public interface GuiExtension {

    /**
     * @return if the player list should always be shown
     */
    boolean vivecraft$getShowPlayerList();

    /**
     * set if the player list should be always shown
     * @param showPlayerList if the player list should be shown
     */
    void vivecraft$setShowPlayerList(boolean showPlayerList);

    /**
     * draws the crosshair at the specified location on the screen
     * @param mouseX x coordinate in screen pixel coordinates
     * @param mouseY y coordinate in screen pixel coordinates
     */
    void vivecraft$drawMouseMenuQuad(int mouseX, int mouseY);
}
