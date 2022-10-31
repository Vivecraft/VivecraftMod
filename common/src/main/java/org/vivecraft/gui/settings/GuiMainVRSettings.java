package org.vivecraft.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionLayout;
import org.vivecraft.provider.MCVR;
import org.vivecraft.settings.VRSettings;

public class GuiMainVRSettings extends GuiVROptionsBase
{
    private VROptionLayout[] vrAlwaysOptions = new VROptionLayout[] {
            new VROptionLayout(GuiHUDSettings.class, VROptionLayout.Position.POS_LEFT, 1.0F, true, "vivecraft.options.screen.gui.button"),
            new VROptionLayout(GuiRenderOpticsSettings.class, VROptionLayout.Position.POS_LEFT, 0.0F, true, "vivecraft.options.screen.stereorendering.button"),
            new VROptionLayout(GuiQuickCommandEditor.class, VROptionLayout.Position.POS_RIGHT, 0.0F, true, "vivecraft.options.screen.quickcommands.button"),
            new VROptionLayout(GuiOtherHUDSettings.class, VROptionLayout.Position.POS_RIGHT, 1.0F, true, "vivecraft.options.screen.guiother.button"),
            new VROptionLayout(VRSettings.VrOptions.WORLD_SCALE, VROptionLayout.Position.POS_LEFT, 6.0F, true, (String)null),
            new VROptionLayout(VRSettings.VrOptions.WORLD_ROTATION, VROptionLayout.Position.POS_RIGHT, 6.0F, true, (String)null),
            new VROptionLayout(VRSettings.VrOptions.PLAY_MODE_SEATED, (button, mousePos) -> {
                this.reinit = true;

                if (!this.dataholder.vrSettings.seated)
                {
                    this.isConfirm = true;
                    return true;
                }
                else {
                    return false;
                }
            }, VROptionLayout.Position.POS_CENTER, 2.0F, true, (String)null),
            new VROptionLayout(VRSettings.VrOptions.LOW_HEALTH_INDICATOR, VROptionLayout.Position.POS_RIGHT, 7.0F, true, (String)null)
    };
    private VROptionLayout[] vrStandingOptions = new VROptionLayout[] {
            new VROptionLayout(GuiStandingSettings.class, VROptionLayout.Position.POS_LEFT, 4.0F, true, "vivecraft.options.screen.standing.button"),
            new VROptionLayout(GuiRoomscaleSettings.class, VROptionLayout.Position.POS_RIGHT, 4.0F, true, "vivecraft.options.screen.roomscale.button"),
            new VROptionLayout(GuiVRControls.class, VROptionLayout.Position.POS_LEFT, 5.0F, true, "vivecraft.options.screen.controls.button"),
            new VROptionLayout(GuiRadialConfiguration.class, VROptionLayout.Position.POS_RIGHT, 5.0F, true, "vivecraft.options.screen.radialmenu.button")
    };
    private VROptionLayout[] vrSeatedOptions = new VROptionLayout[] {
            new VROptionLayout(GuiSeatedOptions.class, VROptionLayout.Position.POS_LEFT, 4.0F, true, "vivecraft.options.screen.seated.button"),
            new VROptionLayout(VRSettings.VrOptions.RESET_ORIGIN, (button, mousePos) -> {
                this.resetOrigin();
                return true;
            }, VROptionLayout.Position.POS_RIGHT, 4.0F, true, (String)null)
    };
    private VROptionLayout[] vrConfirm = new VROptionLayout[] {
            new VROptionLayout((button, mousePos) -> {
                this.reinit = true;
                this.isConfirm = false;
                return false;
            }, VROptionLayout.Position.POS_RIGHT, 2.0F, true, "gui.cancel"),
            new VROptionLayout((button, mousePos) -> {
                this.dataholder.vrSettings.seated = true;
                this.settings.saveOptions();
                this.reinit = true;
                this.isConfirm = false;
                return false;
            }, VROptionLayout.Position.POS_LEFT, 2.0F, true, "vivecraft.gui.ok")
    };
    private boolean isConfirm = false;

    public GuiMainVRSettings(Screen lastScreen)
    {
        super(lastScreen);
    }

    protected void init()
    {
        if (!this.isConfirm)
        {
            this.vrTitle = "vivecraft.options.screen.main";

            if (this.dataholder.vrSettings.seated)
            {
                super.init(this.vrSeatedOptions, true);
            }
            else
            {
                super.init(this.vrStandingOptions, true);

                if (this.dataholder.vrSettings.allowStandingOriginOffset)
                {
                    super.init(new VROptionLayout[] {new VROptionLayout(VRSettings.VrOptions.RESET_ORIGIN, (button, mousePos) -> {
                            this.resetOrigin();
                            return true;
                        }, VROptionLayout.Position.POS_LEFT, 7.0F, true, (String)null)
                    }, false);
                }
            }

            super.init(this.vrAlwaysOptions, false);
            super.addDefaultButtons();
        }
        else
        {
            this.vrTitle = "vivecraft.messages.seatedmode";
            super.init(this.vrConfirm, true);
        }
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        MCVR.get().seatedRot = 0.0F;
        MCVR.get().clearOffset();
    }

    protected void resetOrigin()
    {
        MCVR.get().resetPosition();
        this.settings.saveOptions();
        this.minecraft.setScreen((Screen)null);
    }
}
