package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiMenuWorldSettings extends GuiVROptionsBase {
    private final VROptionEntry[] miscSettings = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_SELECTION),
        new VROptionEntry("vivecraft.gui.menuworld.refresh", (button, mousePos) -> {
            if (this.dataholder.menuWorldRenderer != null && this.dataholder.menuWorldRenderer.getLevel() != null) {
                try {
                    this.dataholder.menuWorldRenderer.destroy();
                    this.dataholder.menuWorldRenderer.prepare();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            return true;
        }),
        new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_FALLBACK),
        new VROptionEntry("vivecraft.gui.menuworld.loadnew", (button, mousePos) -> {
            if (this.dataholder.menuWorldRenderer != null) {
                try {
                    this.dataholder.menuWorldRenderer.destroy();
                    this.dataholder.menuWorldRenderer.init();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            return true;
        })
    };

    public GuiMenuWorldSettings(Screen guiScreen) {
        super(guiScreen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.menuworld";
        super.init(this.miscSettings, true);
        super.addDefaultButtons();
    }
}
