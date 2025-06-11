package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiStringListEditorScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.Arrays;

public class GuiQuickCommandEditor extends GuiStringListEditorScreen {
    public GuiQuickCommandEditor(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.quickcommands"), lastScreen,
            true,
            () -> Arrays.asList(ClientDataHolderVR.getInstance().vrSettings.vrQuickCommands),
            () -> {
                ClientDataHolderVR.getInstance().vrSettings.vrQuickCommands = ClientDataHolderVR.getInstance().vrSettings.getQuickCommandsDefaults();
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            }, values -> {
                ClientDataHolderVR.getInstance().vrSettings.vrQuickCommands = values.toArray(new String[0]);
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            });
    }
}
