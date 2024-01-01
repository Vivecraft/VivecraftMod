package org.vivecraft.server.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.settings.GuiArrayValueEditScreen;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;
import org.vivecraft.client.gui.widgets.QuadWidget;
import org.vivecraft.client.gui.widgets.VectorWidget;
import org.vivecraft.common.ConfigBuilder;

import java.util.function.Supplier;

public class WidgetBuilder {
    public static Supplier<AbstractWidget> getBaseWidget(ConfigBuilder.ConfigValue<?> value, int width, int height) {
        return () -> Button
            .builder(Component.literal("" + value.get()), button -> {
            })
            .bounds(0, 0, width, height)
            .tooltip(Tooltip.create(Component.literal(value.getComment())))
            .build();
    }

    public static Supplier<AbstractWidget> getOnOffWidget(ConfigBuilder.BooleanValue booleanValue, int width, int height) {
        return () -> CycleButton
            .onOffBuilder(booleanValue.get())
            .displayOnlyValue()
            .withTooltip((bool) -> booleanValue.getComment() != null ? Tooltip.create(Component.literal(booleanValue.getComment())) : null)
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
            };
            box.setMaxLength(1000);
            box.setValue(stringValue.get());
            box.setTooltip(Tooltip.create(Component.literal(stringValue.getComment())));
            return box;
        };
    }

    public static <T> Supplier<AbstractWidget> getCycleWidget(ConfigBuilder.InListValue<T> inListValue, int width, int height) {
        return () -> CycleButton
            .builder((newValue) -> Component.literal("" + newValue))
            // toArray is needed here, because the button uses Objects, and the collection is of other types
            .withValues(inListValue.getValidValues().toArray())
            .withInitialValue(inListValue.get())
            .displayOnlyValue()
            .withTooltip((bool) -> inListValue.getComment() != null ? Tooltip.create(Component.literal(inListValue.getComment())) : null)
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
            };
            widget.setTooltip(Tooltip.create(Component.literal(numberValue.getComment())));
            return widget;
        };
    }

    public static <T> Supplier<AbstractWidget> getEditListWidget(ConfigBuilder.ListValue<T> listValue, int width, int height) {
        // TODO handle other types than String
        return () -> Button
            .builder(
                Component.translatable("vivecraft.options.editlist"),
                button -> Minecraft.getInstance()
                    .setScreen(
                        new GuiListValueEditScreen(Component.literal(listValue.getPath().substring(listValue.getPath().lastIndexOf("."))), Minecraft.getInstance().screen, (ConfigBuilder.ListValue<String>) listValue)
                    ))
            .size(width, height)
            .tooltip(Tooltip.create(Component.literal(listValue.getComment())))
            .build();
    }

    public static <T extends Enum<T>> Supplier<AbstractWidget> getEnumWidget(ConfigBuilder.EnumValue<T> enumValue, int width, int height) {
        return () -> CycleButton
            .<T>builder(newValue -> Component.literal(newValue.toString()))
            .withInitialValue(enumValue.get())
            .withValues(enumValue.get().getDeclaringClass().getEnumConstants())
            .displayOnlyValue()
            .withTooltip((bool) -> enumValue.getComment() != null ? Tooltip.create(Component.literal(enumValue.getComment())) : null)
            .create(0, 0, width, height, Component.empty(), (button, newValue) -> enumValue.set(newValue));
    }

    public static Supplier<AbstractWidget> getQuatWidget(ConfigBuilder.QuatValue quatValue, int width, int height) {
        return () -> new QuadWidget(0,0, width, height, Component.literal(quatValue.get().toString()), quatValue);
    }

    public static Supplier<AbstractWidget> getVectorWidget(ConfigBuilder.VectorValue vectorValue, int width, int height) {
        return () -> new VectorWidget(0,0, width, height, Component.literal(vectorValue.get().toString()), vectorValue);
    }

    public static <T> Supplier<AbstractWidget> getArrayWidget(ConfigBuilder.ArrayValue<T> arrayValue, int width, int height) {
        return () -> Button
            .builder(
                Component.translatable("vivecraft.options.editarray"),
                button -> Minecraft.getInstance()
                    .setScreen(
                        new GuiArrayValueEditScreen<>(Component.literal(arrayValue.getPath().substring(arrayValue.getPath().lastIndexOf("."))), Minecraft.getInstance().screen, arrayValue, arrayValue.fromString)
                    ))
            .size(width, height)
            .tooltip(Tooltip.create(Component.literal(arrayValue.getComment())))
            .build();
    }
}
