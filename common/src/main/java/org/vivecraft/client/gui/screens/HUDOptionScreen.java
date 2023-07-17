package org.vivecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ClearInventoryCommands;
import org.vivecraft.config.ClientConfig;

public class HUDOptionScreen extends AbstractVROptionScreen{
    public HUDOptionScreen(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.gui"), parent);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(booleanButton(ClientConfig.hideGUI, getXLeft(), getY(0)));
        addRenderableWidget(enumButton(ClientConfig.vrHudLockMode, getXRight(), getY(0)));

        addRenderableWidget(slider(ClientConfig.headHudScale, getXLeft(), getY(1)));
        addRenderableWidget(slider(ClientConfig.hudDistance, getXRight(), getY(1)));

        addRenderableWidget(booleanButton(ClientConfig.hudOcclusion, getXLeft(), getY(2)));
        addRenderableWidget(slider(ClientConfig.hudOpacity, getXRight(), getY(2)));

        addRenderableWidget(booleanButton(ClientConfig.menuBackground, getXLeft(), getY(3)));
        addRenderableWidget(booleanButton(ClientConfig.vrTouchHotbar, getXRight(), getY(3)));

        addRenderableWidget(booleanButton(ClientConfig.autoOpenKeyboard, getXLeft(), getY(4)));
        addRenderableWidget(booleanButton(ClientConfig.menuAlwaysFollowFace, getXRight(), getY(4)));

        addRenderableWidget(booleanButton(ClientConfig.physicalKeyboard, getXLeft(), getY(5)));
        addRenderableWidget(booleanButton(ClientConfig.guiAppearOverBlock, getXRight(), getY(5)));

        addRenderableWidget(slider(ClientConfig.physicalKeyboardScale, getXLeft(), getY(6)));
        addRenderableWidget(enumButton(ClientConfig.menuWorldSelection, getXRight(), getY(6)));

        addRenderableWidget(enumButton(ClientConfig.physicalKeyboardTheme, getXLeft(), getY(7)));
        addRenderableWidget(enumButton(ClientConfig.shaderGUIRender, getXRight(), getY(7)));

    }

    @Override
    protected void resetOptions(Button b) {
        super.resetOptions(b);
        ClientConfig.hideGUI.reset();
        ClientConfig.vrHudLockMode.reset();
        ClientConfig.headHudScale.reset();
        ClientConfig.hudDistance.reset();
        ClientConfig.hudOcclusion.reset();
        ClientConfig.hudOpacity.reset();
        ClientConfig.menuBackground.reset();
        ClientConfig.vrTouchHotbar.reset();
        ClientConfig.autoOpenKeyboard.reset();
        ClientConfig.menuAlwaysFollowFace.reset();
        ClientConfig.physicalKeyboard.reset();
        ClientConfig.guiAppearOverBlock.reset();
        ClientConfig.physicalKeyboardScale.reset();
        ClientConfig.menuWorldSelection.reset();
        ClientConfig.physicalKeyboardTheme.reset();
        ClientConfig.shaderGUIRender.reset();
    }
}
