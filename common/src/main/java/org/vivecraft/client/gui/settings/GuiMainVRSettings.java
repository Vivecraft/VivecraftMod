package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client.gui.framework.VROptionPosition.*;
import static org.vivecraft.client_vr.VRState.*;

public class GuiMainVRSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase
{
    private boolean isConfirm = false;
    public static String vrTitle = "vivecraft.options.screen.main";

    public GuiMainVRSettings(Screen lastScreen)
    {
        super(lastScreen);
    }

    @Override
    protected void init()
    {
        super.clearWidgets();
        if (!this.isConfirm)
        {
            vrTitle = "vivecraft.options.screen.main";
            super.init(
                VrOptions.PLAY_MODE_SEATED,
                (button, mousePos) -> {
                    if (dh.vrSettings.seated)
                    {
                        return false;
                    }
                    else
                    {
                        return this.isConfirm = true;
                    }
                }
            );
            super.init(VrOptions.VR_HOTSWITCH);
            super.init(
                GuiRenderOpticsSettings.class,
                GuiQuickCommandEditor.class,
                GuiHUDSettings.class,
                GuiOtherHUDSettings.class
            );

            super.init(VrOptions.DUMMY, POS_CENTER);

            if (dh.vrSettings.seated)
            {
                super.init(GuiSeatedOptions.class);
                super.init(VrOptions.RESET_ORIGIN);
                super.init(VrOptions.DUMMY, POS_CENTER);
            }
            else
            {
                super.init(
                    GuiStandingSettings.class,
                    GuiRoomscaleSettings.class,
                    GuiVRControls.class,
                    GuiRadialConfiguration.class
                );
            }

            super.init(
                VrOptions.WORLD_SCALE,
                VrOptions.WORLD_ROTATION,
                !dh.vrSettings.seated && dh.vrSettings.allowStandingOriginOffset ? VrOptions.RESET_ORIGIN : VrOptions.DUMMY,
                VrOptions.LOW_HEALTH_INDICATOR
            );
            super.addDefaultButtons();
        }
        else
        {
            vrTitle = "vivecraft.messages.seatedmode";
            super.init(
                (button, mousePos) -> {
                    dh.vrSettings.seated = true;
                    dh.vrSettings.saveOptions();
                    this.reinit = true;
                    this.isConfirm = false;
                    return false;
                },
                POS_LEFT,
                2.0F,
                "gui.ok"
            );
            super.init(
                (button, mousePos) -> {
                    this.reinit = true;
                    this.isConfirm = false;
                    return false;
                },
                POS_RIGHT,
                2.0F,
                "gui.cancel"
            );
        }
    }

    @Override
    protected void loadDefaults()
    {
        super.loadDefaults();
        if (vrInitialized) {
            dh.vr.seatedRot = 0.0F;
            dh.vr.clearOffset();
        }
    }

    protected void resetOrigin()
    {
        if (dh.vr != null) {
            dh.vr.resetPosition();
        }
        dh.vrSettings.saveOptions();
        mc.setScreen(null);
    }

    @Override
    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            switch(guivroption.getOption()){
                case PLAY_MODE_SEATED -> { this.reinit = true; }
                case RESET_ORIGIN -> { this.resetOrigin(); }
            }
        }
    }
}
