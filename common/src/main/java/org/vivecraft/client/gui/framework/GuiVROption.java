package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.vivecraft.client_vr.settings.VRSettings;

public interface GuiVROption extends LayoutElement {
    int getId();

    VRSettings.VrOptions getOption();
}
