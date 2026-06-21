package org.vivecraft.client_vr.extensions;

public interface SpectatorGuiExtension {
    /**
     * shows the spectator menu
     */
    void vivecraft$showMenu();

    /**
     * selects the specified slot and activates it
     *
     * @param slot slot to select
     */
    void vivecraft$selectAndActivateSlot(int slot);
}
