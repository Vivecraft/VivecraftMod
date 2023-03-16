package org.vivecraft.gui.framework;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.vivecraft.settings.VRSettings;

public interface GuiVROption extends LayoutElement {
    int getId();

    VRSettings.VrOptions getOption();
}
