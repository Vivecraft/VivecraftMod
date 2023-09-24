package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.dh;

public class GuiQuickCommandEditor extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.quickcommands";
    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen par1Screen)
    {
        super(par1Screen);
    }

    @Override
    public void init()
    {
        this.guiList = new GuiQuickCommandsList(this);
        super.init();
        super.addDefaultButtons();
        this.visibleList = this.guiList;
    }

    @Override
    protected void loadDefaults()
    {
        super.loadDefaults();
        dh.vrSettings.vrQuickCommands = dh.vrSettings.getQuickCommandsDefaults();
    }

    @Override
    protected boolean onDoneClicked()
    {
        for (int i = 0; i < 12; ++i)
        {
            String s = (this.guiList.children().get(i)).txt.getValue();
            dh.vrSettings.vrQuickCommands[i] = s;
        }

        return super.onDoneClicked();
    }
}
