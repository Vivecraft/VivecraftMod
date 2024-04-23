package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;

public class GuiQuickCommandEditor extends GuiVROptionsBase {
    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.quickcommands";
        this.guiList = new GuiQuickCommandsList(this, this.minecraft);
        super.init();
        super.addDefaultButtons();
        this.visibleList = this.guiList;
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        this.dataHolder.vrSettings.vrQuickCommands = this.dataHolder.vrSettings.getQuickCommandsDefaults();
    }

    @Override
    protected boolean onDoneClicked() {
        for (int i = 0; i < this.dataHolder.vrSettings.vrQuickCommands.length; i++) {
            String command = (this.guiList.children().get(i)).txt.getValue();
            this.dataHolder.vrSettings.vrQuickCommands[i] = command;
        }

        return super.onDoneClicked();
    }
}
