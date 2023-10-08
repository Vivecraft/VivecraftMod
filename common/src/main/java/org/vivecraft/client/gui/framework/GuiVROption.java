package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.layouts.LayoutElement;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import javax.annotation.Nonnull;

public interface GuiVROption extends LayoutElement {

    @Nonnull
    VrOptions getOption();
}
