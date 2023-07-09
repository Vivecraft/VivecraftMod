package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.components.AbstractWidget;
import org.vivecraft.client_vr.settings.VRSettings;

public interface GuiVROption {
    int getId();

    VRSettings.VrOptions getOption();


    // implement those with a cast to Abstract widget, since pre 1.19.4 those methods are there
    default int getY() {
        if (this instanceof AbstractWidget widget) {
            return widget.y;
        }
        return 0;
    }
    default int getX() {
        if (this instanceof AbstractWidget widget) {
            return widget.x;
        }
        return 0;
    }

    default int getWidth() {
        if (this instanceof AbstractWidget widget) {
            return widget.getWidth();
        }
        return 0;
    }

    default int getHeight() {
        if (this instanceof AbstractWidget widget) {
            return widget.getHeight();
        }
        return 0;
    }
}
