package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.gui.screens.Screen;

public class GuiMenuWorldSettings extends GuiVROptionsBase
{
    private VROptionEntry[] miscSettings = new VROptionEntry[] {
            new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_SELECTION),
//            new VROptionEntry("vivecraft.gui.menuworld.refresh", (button, mousePos) -> {
//                if (this.dataholder.menuWorldRenderer.getWorld() != null)
//                {
//                    try
//                    {
//                        this.dataholder.menuWorldRenderer.destroy();
//                        this.dataholder.menuWorldRenderer.prepare();
//                    }
//                    catch (Exception exception)
//                    {
//                        exception.printStackTrace();
//                    }
//                }
//
//                return true;
//            }),
//            new VROptionEntry(VRSettings.VrOptions.DUMMY), new VROptionEntry("vivecraft.gui.menuworld.loadnew", (button, mousePos) -> {
//                try {
//                    if (this.dataholder.menuWorldRenderer.isReady())
//                    {
//                        this.dataholder.menuWorldRenderer.destroy();
//                    }
//
//                    this.dataholder.menuWorldRenderer.init();
//                }
//                catch (Exception exception)
//                {
//                    exception.printStackTrace();
//                }
//
//                return true;
//            })
    };

    public GuiMenuWorldSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.menuworld";
        super.init(this.miscSettings, true);
        super.addDefaultButtons();
    }
}
