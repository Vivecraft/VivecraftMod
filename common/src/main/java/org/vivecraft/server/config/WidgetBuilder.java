package org.vivecraft.server.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;

import java.util.Collection;
import java.util.function.Supplier;

public class WidgetBuilder {
    /**
     * creates a simple ConfigValue Button that does nothing
     * @param value ConfigValue for this button
     * @param width width of the button
     * @param height height of the button
     * @return Button with the value as text, and the comment as tooltip
     */
    public static Supplier<AbstractWidget> getBaseWidget(ConfigBuilder.ConfigValue<?> value, int width, int height) {
        return () -> Button
            .builder(Component.literal("" + value.get()), button -> {})
            .bounds(0, 0, width, height)
            .tooltip(Tooltip.create(Component.literal(value.getComment())))
            .build();
    }

    /**
     * creates a Button that toggles the BooleanValue
     * @param booleanValue BooleanValue for this button
     * @param width width of the button
     * @param height height of the button
     * @return Button with the value as text, and the comment as tooltip
     */
    public static Supplier<AbstractWidget> getOnOffWidget(ConfigBuilder.BooleanValue booleanValue, int width, int height) {
        return () -> CycleButton
            .onOffBuilder(booleanValue.get())
            .displayOnlyValue()
            .withTooltip((bool) -> booleanValue.getComment() != null ? Tooltip.create(Component.literal(booleanValue.getComment())) : null)
            .create(0, 0, width, height, Component.empty(), (button, bool) -> booleanValue.set(bool));
    }

    /**
     * creates an EditBox that holds the StringValue
     * any changes to the EditBox Aare saved in the StringValue
     * @param stringValue StringValue for this editbox
     * @param width width of the editbox
     * @param height height of the editbox
     * @return EditBox with the value as text, and the comment as tooltip
     */
    public static Supplier<AbstractWidget> getEditBoxWidget(ConfigBuilder.StringValue stringValue, int width, int height) {
        return () -> {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, width - 1, height, Component.literal(stringValue.get())) {
                @Override
                public boolean charTyped(char character, int modifiers) {
                    boolean ret = super.charTyped(character, modifiers);
                    stringValue.set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    boolean ret = super.keyPressed(keyCode, scanCode, modifiers);
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

    /**
     * creates a Button that cycles through the values of the InListValue
     * @param configValue InListValue for this button
     * @param values Collection of valid values
     * @param width width of the button
     * @param height height of the button
     * @return Button with the value as text, and the comment as tooltip
     */
    public static <T> Supplier<AbstractWidget> getCycleWidget(ConfigBuilder.ConfigValue<T> configValue, Collection<? extends T> values, int width, int height) {
        return () -> CycleButton
            .builder((newValue) -> Component.literal("" + newValue))
            // toArray is needed here, because the button uses Objects, and the collection is of other types
            .withValues(values.toArray())
            .withInitialValue(configValue.get())
            .displayOnlyValue()
            .withTooltip((bool) -> configValue.getComment() != null ? Tooltip.create(Component.literal(configValue.getComment())) : null)
            .create(0, 0, width, height, Component.empty(), (button, newValue) -> configValue.set((T) newValue));
    }

    /**
     * creates a Slider that holds the NumberValue
     * @param numberValue NumberValue for this slider
     * @param width width of the slider
     * @param height height of the slider
     * @return Slider with the range of the numberValue, and the comment as tooltip
     */
    public static <E extends Number> Supplier<AbstractWidget> getSliderWidget(ConfigBuilder.NumberValue<E> numberValue, int width, int height) {
        return () -> {
            AbstractSliderButton widget = new AbstractSliderButton(0, 0, width, height, Component.literal("" + numberValue.get()), numberValue.normalize()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal("" + numberValue.get()));
                }

                @Override
                protected void applyValue() {
                    numberValue.fromNormalized(this.value);
                }
            };
            widget.setTooltip(Tooltip.create(Component.literal(numberValue.getComment())));
            return widget;
        };
    }

    /**
     * creates a Button that opens an edit Screen for the given ListValue
     * @param listValue ListValue for this button
     * @param width width of the button
     * @param height height of the button
     * @return Button that opens a screen to edit the list of {@code listValue}, and the comment as tooltip
     */
    public static <T> Supplier<AbstractWidget> getEditListWidget(ConfigBuilder.ListValue<T> listValue, int width, int height) {
        // TODO handle other types than String
        return () -> Button
            .builder(
                Component.translatable("vivecraft.options.editlist"),
                button -> Minecraft.getInstance().setScreen(
                    new GuiListValueEditScreen(
                        Component.literal(listValue.getPath().substring(listValue.getPath().lastIndexOf("."))),
                        Minecraft.getInstance().screen, (ConfigBuilder.ListValue<String>) listValue)))
            .size(width, height)
            .tooltip(Tooltip.create(Component.literal(listValue.getComment())))
            .build();
    }
}
