package org.vivecraft.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.config.ClientConfig;

public class StereoOptionScreen extends AbstractVROptionScreen{
    public StereoOptionScreen(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.stereorendering"), parent);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(slider(ClientConfig.renderScaleFactor, getXLeft(), getY(0)));
        addRenderableWidget(enumButton(ClientConfig.displayMirrorMode, getXRight(), getY(0)));

        addRenderableWidget(booleanButton(ClientConfig.useFsaa, getXLeft(), getY(1)));
        addRenderableWidget(booleanButton(ClientConfig.stencilOn, getXRight(), getY(1)));

        addRenderableWidget(slider(ClientConfig.handCameraResScale, getXLeft(), getY(2)));
        addRenderableWidget(slider(ClientConfig.handCameraFov, getXRight(), getY(2)));

        addRenderableWidget(booleanButton(ClientConfig.displayMirrorLeftEye, getXRight(), getY(3)));

    }

    @Override
    protected void resetOptions(Button b) {
        super.resetOptions(b);
        ClientConfig.renderScaleFactor.reset();
        ClientConfig.displayMirrorMode.reset();
        ClientConfig.useFsaa.reset();
        ClientConfig.stencilOn.reset();
        ClientConfig.handCameraResScale.reset();
        ClientConfig.handCameraFov.reset();
        ClientConfig.displayMirrorLeftEye.reset();
    }
}
