package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.vivecraft.config.ClientConfig;
import org.vivecraft.config.ConfigBuilder;

public class AbstractVROptionScreen extends Screen {
    protected final Screen parent;
    protected boolean reInit = false;
    public AbstractVROptionScreen(Component component, Screen parent) {
        super(component);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        //Reset or go back
        addRenderableWidget(openScreenButton("gui.back", parent, this.width / 2 + 5, this.height - 30));
        this.addRenderableWidget(Button.builder(Component.translatable("vivecraft.gui.loaddefaults"), p -> resetOptions(p))
                .pos(getXLeft(), this.height - 30)
                .size(150, 20)
                .build());
    }

    protected void resetOptions(Button b) {
        reInit = true;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (reInit) {
            reInit = false;
            this.init();
        }
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(poseStack, i, j, f);
    }

    protected int getY(int row) {
        return Mth.ceil((this.height / 6f) + 21.0f * row - 10.0f);
    }

    protected int getXLeft() {
        return this.width / 2 - 155;
    }

    protected int getXRight() {
        return this.width / 2 - 155 + 160;
    }

    public Button openScreenButton(String name, Screen screen, int x, int y) {
        return Button
                .builder(Component.translatable(name),button -> Minecraft.getInstance().setScreen(screen))
                .pos(x,y)
                .build();
    }

    public Button booleanButton(ConfigBuilder.BooleanValue config, int x, int y) {
        return booleanButton(config, x, y, button -> {});
    }

    public Button booleanButton(ConfigBuilder.BooleanValue config, int x, int y, Button.OnPress onPress) {
        return Button
                .builder(config.getName(), button -> {
                    config.set(!config.get());
                    onPress.onPress(button);
                })
                .pos(x, y)
                .build();
    }

    public Button enumButton(ConfigBuilder.EnumValue<?> config, int x, int y) {
        return Button
                .builder(config.getName(), button -> {
                   config.cycle();
                })
                .pos(x, y)
                .build();
    }

    public VRSlider slider(ConfigBuilder.DoubleValue config, int x, int y) {
        return new VRSlider(config, x, y, 150, 20);
    }

    public VRSliderInt slider(ConfigBuilder.IntValue config, int x, int y) {
        return new VRSliderInt(config, x, y, 150, 20);
    }

    public static class VRSlider extends AbstractSliderButton {

        private final ConfigBuilder.DoubleValue config;

        public VRSlider(ConfigBuilder.DoubleValue config, int x, int y, int width, int height) {
            super(x, y, width, height, config.getName(), config.normalize());
            this.config = config;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(config.getName().append(config.get() + ""));
        }

        @Override
        protected void applyValue() {
            config.fromNormalised(this.value);
        }
    }

    public static class VRSliderInt extends AbstractSliderButton {

        private final ConfigBuilder.IntValue config;

        public VRSliderInt(ConfigBuilder.IntValue config, int x, int y, int width, int height) {
            super(x, y, width, height, config.getName(), config.normalize());
            this.config = config;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(config.getName().append(config.get() + ""));
        }

        @Override
        protected void applyValue() {
            config.fromNormalised(this.value);
        }
    }
}
