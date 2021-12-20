package org.vivecraft.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.gui.framework.GuiVROptionButton;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.settings.VRSettings;

public class GuiFreeMoveSettings extends GuiVROptionsBase
{
    private static VRSettings.VrOptions[] standingSettings = new VRSettings.VrOptions[] {
    		VRSettings.VrOptions.FREEMOVE_MODE,
			VRSettings.VrOptions.FOV_REDUCTION,
			VRSettings.VrOptions.INERTIA_FACTOR,
			VRSettings.VrOptions.MOVEMENT_MULTIPLIER,
			VRSettings.VrOptions.AUTO_SPRINT,
			VRSettings.VrOptions.AUTO_SPRINT_THRESHOLD,
			VRSettings.VrOptions.ANALOG_MOVEMENT
    };
    private static VRSettings.VrOptions[] seatedSettings = new VRSettings.VrOptions[] {
    		VRSettings.VrOptions.SEATED_HMD,
			VRSettings.VrOptions.FOV_REDUCTION,
			VRSettings.VrOptions.INERTIA_FACTOR
    };
    private static VRSettings.VrOptions[] fovRed = new VRSettings.VrOptions[] {
    		VRSettings.VrOptions.FOV_REDUCTION_MIN,
			VRSettings.VrOptions.FOV_REDUCTION_OFFSET
    };

    public GuiFreeMoveSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.freemove";

        if (this.minecraft.vrSettings.seated)
        {
            super.init(seatedSettings, true);
        }
        else
        {
            super.init(standingSettings, true);
        }

        if (this.minecraft.vrSettings.useFOVReduction)
        {
            super.init(fovRed, false);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROptionButton)
        {
            GuiVROptionButton guivroptionbutton = (GuiVROptionButton)widget;

            if (guivroptionbutton.id == VRSettings.VrOptions.FOV_REDUCTION.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
