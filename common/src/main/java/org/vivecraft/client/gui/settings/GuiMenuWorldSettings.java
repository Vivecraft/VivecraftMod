package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.dh;

public class GuiMenuWorldSettings extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.menuworld";

    public GuiMenuWorldSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        super.init(VrOptions.MENU_WORLD_SELECTION);
        super.init(
            "vivecraft.gui.menuworld.refresh",
            (button, mousePos) ->
            {
                if (dh.menuWorldRenderer != null && dh.menuWorldRenderer.getLevel() != null)
                {
                    try
                    {
                        dh.menuWorldRenderer.destroy();
                        dh.menuWorldRenderer.prepare();
                    } catch (Exception exception)
                    {
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
                if (dh.menuWorldRenderer != null)
                {
                    try
                    {
                        if (dh.menuWorldRenderer.isReady())
                        {
                            dh.menuWorldRenderer.destroy();
                        }

                        dh.menuWorldRenderer.init();
                    } catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }

                return true;
            }
        );
        super.addDefaultButtons();
    }
}
