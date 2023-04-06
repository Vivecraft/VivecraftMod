package org.vivecraft.client_vr.gui.framework;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.vivecraft.client.settings.VRSettings;

public interface GuiVROption extends LayoutElement {
    int getId();

    VRSettings.VrOptions getOption();
}
