package org.vivecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.settings.GuiHUDSettings;
import org.vivecraft.client.gui.settings.GuiOtherHUDSettings;
import org.vivecraft.client.gui.settings.GuiQuickCommandEditor;
import org.vivecraft.client.gui.settings.GuiRadialConfiguration;
import org.vivecraft.client.gui.settings.GuiRenderOpticsSettings;
import org.vivecraft.client.gui.settings.GuiRoomscaleSettings;
import org.vivecraft.client.gui.settings.GuiSeatedOptions;
import org.vivecraft.client.gui.settings.GuiStandingSettings;
import org.vivecraft.client.gui.settings.GuiVRControls;
import org.vivecraft.config.ClientConfig;

public class VROptionScreen extends AbstractVROptionScreen {

    public VROptionScreen(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.main"), parent);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        //Always
        addRenderableWidget(booleanButton(ClientConfig.seated, getXLeft(), getY(0), button -> {
            reInit = true;
            if (ClientConfig.seated.get()) {
                return;
            }
            Minecraft.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                Minecraft.getInstance().setScreen(this);
                if (!confirmed) {
                    ClientConfig.seated.set(false);
                }
            }, Component.translatable("vivecraft.messages.seatedmode"), Component.literal("Do you want to change to seated mode?")));

        }));
        addRenderableWidget(booleanButton(ClientConfig.vrHotswitchingEnabled, getXRight(), getY(0)));

        addRenderableWidget(openScreenButton("vivecraft.options.screen.stereorendering.button", new StereoOptionScreen(this), getXLeft(), getY(1)));
        addRenderableWidget(openScreenButton("vivecraft.options.screen.quickcommands.button", new GuiQuickCommandEditor(this), getXRight(), getY(1)));

        addRenderableWidget(openScreenButton("vivecraft.options.screen.gui.button", new HUDOptionScreen(this), getXLeft(), getY(2)));
        addRenderableWidget(openScreenButton("vivecraft.options.screen.guiother.button", new GuiOtherHUDSettings(this), getXRight(), getY(2)));

        addRenderableWidget(slider(ClientConfig.worldScale, getXLeft(), getY(6)));
        addRenderableWidget(slider(ClientConfig.worldRotation, getXRight(), getY(6)));

        if (ClientConfig.seated.get()) {
            //Seated
            addRenderableWidget(openScreenButton("vivecraft.options.screen.seated.button", new GuiSeatedOptions(this), getXLeft(), getY(5)));
            //addRenderableWidget(openScreenButton("vivecraft.options.screen.radialmenu.button", new GuiRadialConfiguration(this), getXRight(), getY(5))); //RESET ORIGIN

        } else {
            //Standing
            addRenderableWidget(openScreenButton("vivecraft.options.screen.standing.button", new GuiStandingSettings(this), getXLeft(), getY(4)));
            addRenderableWidget(openScreenButton("vivecraft.options.screen.roomscale.button", new GuiRoomscaleSettings(this), getXRight(), getY(4)));

            addRenderableWidget(openScreenButton("vivecraft.options.screen.controls.button", new GuiVRControls(this), getXLeft(), getY(5)));
            addRenderableWidget(openScreenButton("vivecraft.options.screen.radialmenu.button", new GuiRadialConfiguration(this), getXRight(), getY(5)));
        }
    }

    @Override
    protected void resetOptions(Button b) {
        super.resetOptions(b);
        ClientConfig.seated.reset();
        ClientConfig.vrHotswitchingEnabled.reset();
        ClientConfig.worldScale.reset();
        ClientConfig.worldRotation.reset();
    }
}
