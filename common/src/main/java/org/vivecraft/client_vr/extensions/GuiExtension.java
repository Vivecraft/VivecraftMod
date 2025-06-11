package org.vivecraft.client_vr.extensions;

public interface GuiExtension {

    /**
     * @return if the player list should always be shown
     */
    boolean vivecraft$getShowPlayerList();

    /**
     * set if the player list should be always shown
     *
     * @param showPlayerList if the player list should be shown
     */
    void vivecraft$setShowPlayerList(boolean showPlayerList);
}
