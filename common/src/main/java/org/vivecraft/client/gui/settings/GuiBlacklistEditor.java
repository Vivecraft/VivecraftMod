package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiStringListEditorScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.Arrays;

public class GuiBlacklistEditor extends GuiStringListEditorScreen {

    public GuiBlacklistEditor(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.blocklist"), lastScreen,
            false,
            () -> Arrays.asList(ClientDataHolderVR.getInstance().vrSettings.vrServerBlacklist),
            () -> {
                ClientDataHolderVR.getInstance().vrSettings.vrServerBlacklist = ClientDataHolderVR.getInstance().vrSettings.getServerBlacklistDefault();
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            }, values -> {
                ClientDataHolderVR.getInstance().vrSettings.vrServerBlacklist = values.toArray(new String[0]);
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            });
    }
}
