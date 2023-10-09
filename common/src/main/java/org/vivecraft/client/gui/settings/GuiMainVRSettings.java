package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.VROptionPosition;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiMainVRSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    private boolean isConfirm = false;
    public static String vrTitle = "vivecraft.options.screen.main";

    public GuiMainVRSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    protected void init() {
        super.clearWidgets();
        if (!this.isConfirm) {
            vrTitle = "vivecraft.options.screen.main";
            super.init(
                VrOptions.PLAY_MODE_SEATED,
                (button, mousePos) -> {
                    if (ClientDataHolderVR.getInstance().vrSettings.seated) {
                        return false;
                    } else {
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

            super.init(VrOptions.DUMMY, VROptionPosition.POS_CENTER);

            if (ClientDataHolderVR.getInstance().vrSettings.seated) {
                super.init(GuiSeatedOptions.class);
                super.init(VrOptions.RESET_ORIGIN);
                super.init(VrOptions.DUMMY, VROptionPosition.POS_CENTER);
            } else {
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
                !ClientDataHolderVR.getInstance().vrSettings.seated && ClientDataHolderVR.getInstance().vrSettings.allowStandingOriginOffset ? VrOptions.RESET_ORIGIN : VrOptions.DUMMY,
                VrOptions.LOW_HEALTH_INDICATOR
            );
            super.addDefaultButtons();
        } else {
            vrTitle = "vivecraft.messages.seatedmode";
            super.init(
                (button, mousePos) -> {
                    ClientDataHolderVR.getInstance().vrSettings.seated = true;
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                    this.reinit = true;
                    this.isConfirm = false;
                    return false;
                },
                VROptionPosition.POS_LEFT,
                2.0F,
                "gui.ok"
            );
            super.init(
                (button, mousePos) -> {
                    this.reinit = true;
                    this.isConfirm = false;
                    return false;
                },
                VROptionPosition.POS_RIGHT,
                2.0F,
                "gui.cancel"
            );
        }
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vr.seatedRot = 0.0F;
            ClientDataHolderVR.getInstance().vr.clearOffset();
        }
    }

    protected void resetOrigin() {
        if (ClientDataHolderVR.getInstance().vr != null) {
            ClientDataHolderVR.getInstance().vr.resetPosition();
        }
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            switch (guivroption.getOption()) {
                case PLAY_MODE_SEATED -> {
                    this.reinit = true;
                }
                case RESET_ORIGIN -> {
                    this.resetOrigin();
                }
            }
        }
    }
}
