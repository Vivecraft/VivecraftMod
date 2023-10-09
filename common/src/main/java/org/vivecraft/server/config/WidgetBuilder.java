package org.vivecraft.server.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;

import java.util.function.Supplier;

public class WidgetBuilder {
    public static Supplier<AbstractWidget> getBaseWidget(ConfigBuilder.ConfigValue<?> value, int width, int height) {
        return () -> new Button(
            0, 0, width, height,
            Component.literal("" + value.get()),
            button -> {
            },
            ((button, poseStack, x, y) ->
                Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(Component.literal(value.getComment()), 200), x, y)));
    }

    public static Supplier<AbstractWidget> getOnOffWidget(ConfigBuilder.BooleanValue booleanValue, int width, int height) {
        return () -> CycleButton
            .onOffBuilder(booleanValue.get())
            .displayOnlyValue()
            .withTooltip((bool) -> booleanValue.getComment() != null ? Minecraft.getInstance().font.split(Component.literal(booleanValue.getComment()), 200) : null)
            .create(0, 0, width, height, Component.empty(), (button, bool) -> booleanValue.set(bool));
    }

    public static Supplier<AbstractWidget> getEditBoxWidget(ConfigBuilder.StringValue stringValue, int width, int height) {
        return () -> {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, width - 1, height, Component.literal(stringValue.get())) {
                @Override
                public boolean charTyped(char c, int i) {
                    boolean ret = super.charTyped(c, i);
                    stringValue.set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(int i, int j, int k) {
                    boolean ret = super.keyPressed(i, j, k);
                    stringValue.set(this.getValue());
                    return ret;
                }

                @Override
                public void renderButton(PoseStack poseStack, int x, int y, float f) {
                    super.renderButton(poseStack, x, y, f);
                    if (this.isHovered) {
                        Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(Component.literal(stringValue.getComment()), 200), x, y);
                    }
                }
            };
            box.setMaxLength(1000);
            box.setValue(stringValue.get());
            return box;
        };
    }

    public static <T> Supplier<AbstractWidget> getCycleWidget(ConfigBuilder.InListValue<T> inListValue, int width, int height) {
        return () -> CycleButton
            .builder((newValue) -> Component.literal("" + newValue))
            .withInitialValue(inListValue.get())
            // toArray is needed here, because the button uses Objects, and the collection is of other types
            .withValues(inListValue.getValidValues().toArray())
            .displayOnlyValue()
            .withTooltip((bool) -> inListValue.getComment() != null ? Minecraft.getInstance().font.split(Component.literal(inListValue.getComment()), 200) : null)
            .create(0, 0, width, height, Component.empty(), (button, newValue) -> inListValue.set((T) newValue));
    }

    public static <E extends Number> Supplier<AbstractWidget> getSliderWidget(ConfigBuilder.NumberValue<E> numberValue, int width, int height) {
        return () -> {
            AbstractSliderButton widget = new AbstractSliderButton(0, 0, width, height, Component.literal("" + numberValue.get()), numberValue.normalize()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal("" + numberValue.get()));
                }

                @Override
                protected void applyValue() {
                    numberValue.fromNormalized(value);
                }

                @Override
                public void renderButton(PoseStack poseStack, int x, int y, float f) {
                    super.renderButton(poseStack, x, y, f);
                    if (this.isHovered) {
                        Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(Component.literal(numberValue.getComment()), 200), x, y);
                    }
                }
            };
            return widget;
        };
    }

    public static <T> Supplier<AbstractWidget> getEditListWidget(ConfigBuilder.ListValue<T> listValue, int width, int height) {
        // TODO handle other types than String
        return () -> new Button(
            0, 0, width, height,
            Component.translatable("vivecraft.options.editlist"),
            button -> Minecraft.getInstance()
                .setScreen(
                    new GuiListValueEditScreen(Component.literal(listValue.getPath().substring(listValue.getPath().lastIndexOf("."))), Minecraft.getInstance().screen, (ConfigBuilder.ListValue<String>) listValue)
                ),
            (button, poseStack, x, y) ->
                Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(Component.literal(listValue.getComment()), 200), x, y));
    }
}
