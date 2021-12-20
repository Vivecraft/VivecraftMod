package org.vivecraft.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

public class GuiMenuWorldSettings extends GuiVROptionsBase
{
    private VROptionEntry[] miscSettings = new VROptionEntry[] {
            new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_SELECTION),
            new VROptionEntry("vivecraft.gui.menuworld.refresh", (button, mousePos) -> {
                if (this.minecraft.menuWorldRenderer.getWorld() != null)
                {
                    try
                    {
                        this.minecraft.menuWorldRenderer.destroy();
                        this.minecraft.menuWorldRenderer.prepare();
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }

                return true;
            }),
            new VROptionEntry(VRSettings.VrOptions.DUMMY), new VROptionEntry("vivecraft.gui.menuworld.loadnew", (button, mousePos) -> {
                try {
                    if (this.minecraft.menuWorldRenderer.isReady())
                    {
                        this.minecraft.menuWorldRenderer.destroy();
                    }

                    this.minecraft.menuWorldRenderer.init();
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }

                return true;
            })
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
