package org.vivecraft.client.gui.framework;

import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.layouts.LayoutElement;

import javax.annotation.Nonnull;

public interface GuiVROption extends LayoutElement {

    @Nonnull
    VrOptions getOption();
}
