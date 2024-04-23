package org.vivecraft.client_vr.extensions;

public interface GuiExtension {

    boolean vivecraft$getShowPlayerList();

    void vivecraft$setShowPlayerList(boolean showPlayerList);

    void vivecraft$drawMouseMenuQuad(int mouseX, int mouseY);
}
