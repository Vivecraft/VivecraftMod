package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiStandingSettings extends GuiVROptionsBase
{
    private VROptionEntry[] locomotionSettings = new VROptionEntry[] {
            new VROptionEntry(VRSettings.VrOptions.WALK_UP_BLOCKS),
            new VROptionEntry(VRSettings.VrOptions.VEHICLE_ROTATION),
            new VROptionEntry(VRSettings.VrOptions.WALK_MULTIPLIER),
            new VROptionEntry(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT),
            new VROptionEntry(VRSettings.VrOptions.BCB_ON),
            new VROptionEntry(VRSettings.VrOptions.ALLOW_STANDING_ORIGIN_OFFSET),
            new VROptionEntry(VRSettings.VrOptions.FORCE_STANDING_FREE_MOVE, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry("vivecraft.options.screen.teleport.button", (button, mousePos) -> {
                this.minecraft.setScreen(new GuiTeleportSettings(this));
                return true;
            }),
            new VROptionEntry("vivecraft.options.screen.freemove.button", (button, mousePos) -> {
                this.minecraft.setScreen(new GuiFreeMoveSettings(this));
                return true;
            })
    };

    public GuiStandingSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.standing";
        super.init(this.locomotionSettings, true);
        super.addDefaultButtons();
    }
}
