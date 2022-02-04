package org.vivecraft.gui.settings;

import org.vivecraft.gui.framework.GuiVROptionsBase;

import net.minecraft.client.gui.screens.Screen;

public class GuiQuickCommandEditor extends GuiVROptionsBase
{
    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen par1Screen)
    {
        super(par1Screen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.quickcommands";
        this.guiList = new GuiQuickCommandsList(this, this.minecraft);
        super.init();
        super.addDefaultButtons();
        this.visibleList = this.guiList;
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        this.dataHolder.vrSettings.vrQuickCommands = this.dataHolder.vrSettings.getQuickCommandsDefaults();
    }

    protected boolean onDoneClicked()
    {
        for (int i = 0; i < 12; ++i)
        {
            String s = (this.guiList.children().get(i)).txt.getValue();
            this.dataHolder.vrSettings.vrQuickCommands[i] = s;
        }

        return super.onDoneClicked();
    }
}
