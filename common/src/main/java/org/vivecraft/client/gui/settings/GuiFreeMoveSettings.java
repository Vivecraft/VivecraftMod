package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiFreeMoveSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] standingSettings = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.FREEMOVE_MODE,
        VRSettings.VrOptions.FREEMOVE_FLY_MODE,
        VRSettings.VrOptions.FOV_REDUCTION,
        VRSettings.VrOptions.INERTIA_FACTOR,
        VRSettings.VrOptions.MOVEMENT_MULTIPLIER,
        VRSettings.VrOptions.AUTO_SPRINT,
        VRSettings.VrOptions.AUTO_SPRINT_THRESHOLD,
        VRSettings.VrOptions.ANALOG_MOVEMENT
    };
    private static final VRSettings.VrOptions[] seatedSettings = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.SEATED_HMD,
        VRSettings.VrOptions.FOV_REDUCTION,
        VRSettings.VrOptions.INERTIA_FACTOR
    };
    private static final VRSettings.VrOptions[] fovRed = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.FOV_REDUCTION_MIN,
        VRSettings.VrOptions.FOV_REDUCTION_OFFSET
    };

    public GuiFreeMoveSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.freemove";

        if (this.dataHolder.vrSettings.seated) {
            super.init(seatedSettings, true);
        } else {
            super.init(standingSettings, true);
        }

        if (this.dataHolder.vrSettings.useFOVReduction) {
            super.init(fovRed, false);
        }

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getId() == VRSettings.VrOptions.FOV_REDUCTION.ordinal()) {
                this.reinit = true;
            }
        }
    }
}
