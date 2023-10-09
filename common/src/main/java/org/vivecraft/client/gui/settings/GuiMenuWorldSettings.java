package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiMenuWorldSettings extends GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.menuworld";

    public GuiMenuWorldSettings(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(VrOptions.MENU_WORLD_SELECTION);
        super.init(
            "vivecraft.gui.menuworld.refresh",
            (button, mousePos) ->
            {
                if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.getLevel() != null) {
                    try {
                        ClientDataHolderVR.getInstance().menuWorldRenderer.destroy();
                        ClientDataHolderVR.getInstance().menuWorldRenderer.prepare();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }

                return true;
            }
        );
        super.init(VrOptions.DUMMY);
        super.init(
            "vivecraft.gui.menuworld.loadnew",
            (button, mousePos) ->
            {
                if (ClientDataHolderVR.getInstance().menuWorldRenderer != null) {
                    try {
                        if (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()) {
                            ClientDataHolderVR.getInstance().menuWorldRenderer.destroy();
                        }

                        ClientDataHolderVR.getInstance().menuWorldRenderer.init();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }

                return true;
            }
        );
        super.addDefaultButtons();
    }
}
