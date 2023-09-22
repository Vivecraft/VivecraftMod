package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;

public class GuiQuickCommandEditor extends GuiVROptionsBase {
    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen par1Screen) {
        super(par1Screen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.quickcommands";
        this.guiList = new GuiQuickCommandsList(this, this.minecraft);
        super.init();
        super.addDefaultButtons();
        this.visibleList = this.guiList;
    }

    protected void loadDefaults() {
        super.loadDefaults();
        this.dataholder.vrSettings.vrQuickCommands = this.dataholder.vrSettings.getQuickCommandsDefaults();
    }

    protected boolean onDoneClicked() {
        for (int i = 0; i < 12; ++i) {
            String s = (this.guiList.children().get(i)).txt.getValue();
            this.dataholder.vrSettings.vrQuickCommands[i] = s;
        }

        return super.onDoneClicked();
    }
}
