package org.vivecraft.server.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiStringListEditorScreen;
import org.vivecraft.server.ServerNetworking;

import java.util.Collection;
import java.util.function.Supplier;

public class WidgetBuilder {
    /**
     * creates a simple ConfigValue Button that does nothing
     *
     * @param value  ConfigValue for this button
     * @param width  width of the button
     * @param height height of the button
     * @return Button with the value as text
     */
    public static Supplier<AbstractWidget> getBaseWidget(ConfigBuilder.ConfigValue<?> value, int width, int height) {
        return () -> Button
            .builder(Component.literal("" + value.get()), button -> {})
            .bounds(0, 0, width, height)
            .build();
    }

    /**
     * creates a Button that toggles the BooleanValue
     *
     * @param booleanValue BooleanValue for this button
     * @param width        width of the button
     * @param height       height of the button
     * @return Button with the value as text
     */
    public static Supplier<AbstractWidget> getOnOffWidget(
        ConfigBuilder.BooleanValue booleanValue, int width, int height)
    {
        return () -> CycleButton
            .onOffBuilder(booleanValue.get())
            .displayOnlyValue()
            .create(0, 0, width, height, Component.empty(), (button, bool) -> {
                booleanValue.set(bool);
                updateSettingsSinglePlayer(booleanValue);
            });
    }

    /**
     * creates an EditBox that holds the StringValue
     * any changes to the EditBox Aare saved in the StringValue
     *
     * @param stringValue StringValue for this editbox
     * @param width       width of the editbox
     * @param height      height of the editbox
     * @return EditBox with the value as text
     */
    public static Supplier<AbstractWidget> getEditBoxWidget(
        ConfigBuilder.StringValue stringValue, int width, int height)
    {
        return () -> {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, width - 1, height,
                Component.literal(stringValue.get()))
            {
                @Override
                public boolean charTyped(CharacterEvent characterEvent) {
                    boolean ret = super.charTyped(characterEvent);
                    stringValue.set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(KeyEvent keyEvent) {
                    boolean ret = super.keyPressed(keyEvent);
                    stringValue.set(this.getValue());
                    return ret;
                }
            };
            box.setMaxLength(1000);
            box.setValue(stringValue.get());
            return box;
        };
    }

    /**
     * creates a Button that cycles through the values of the InListValue
     *
     * @param configValue InListValue for this button
     * @param values      Collection of valid values
     * @param width       width of the button
     * @param height      height of the button
     * @return Button with the value as text
     */
    public static <T> Supplier<AbstractWidget> getCycleWidget(
        ConfigBuilder.ConfigValue<T> configValue, Collection<T> values, int width, int height)
    {
        return () -> CycleButton
            .builder((newValue) -> Component.translatable(
                "vivecraft.serverSettings." + configValue.getPath() + "." + newValue), configValue.get())
            .withValues(values)
            .displayOnlyValue()
            .create(0, 0, width, height, Component.empty(), (button, newValue) -> {
                configValue.set((T) newValue);
                updateSettingsSinglePlayer(configValue);
            });
    }

    /**
     * creates a Slider that holds the NumberValue
     *
     * @param numberValue NumberValue for this slider
     * @param width       width of the slider
     * @param height      height of the slider
     * @return Slider with the range of the numberValue
     */
    public static <E extends Number> Supplier<AbstractWidget> getSliderWidget(
        ConfigBuilder.NumberValue<E> numberValue, int width, int height)
    {
        return () -> new AbstractSliderButton(0, 0, width, height,
            Component.literal("" + numberValue.get()), numberValue.normalize())
        {
            @Override
            protected void updateMessage() {
                setMessage(Component.literal("" + numberValue.get()));
            }

            @Override
            protected void applyValue() {
                numberValue.fromNormalized(this.value);
                updateSettingsSinglePlayer(numberValue);
            }
        };
    }

    /**
     * creates a Button that opens an edit Screen for the given ListValue
     *
     * @param listValue ListValue for this button
     * @param width     width of the button
     * @param height    height of the button
     * @return Button that opens a screen to edit the list of {@code listValue}
     */
    public static <T> Supplier<AbstractWidget> getEditListWidget(
        ConfigBuilder.ListValue<T> listValue, int width, int height)
    {
        Object first = listValue.get().isEmpty() ? null : listValue.get().getFirst();
        if (first == null || first instanceof String) {
            ConfigBuilder.ListValue<String> stringValue = (ConfigBuilder.ListValue<String>) listValue;
            return () -> Button.builder(Component.translatable("vivecraft.options.editlist"),
                    button -> Minecraft.getInstance().setScreen(new GuiStringListEditorScreen(
                        Component.translatable("vivecraft.serverSettings." + listValue.getPath()),
                        Minecraft.getInstance().screen, false, stringValue::get, stringValue::reset, list -> {
                        stringValue.set(list);
                        updateSettingsSinglePlayer(stringValue);
                    })))
                .size(width, height)
                .build();
        } else {
            // TODO handle other types than String
            throw new RuntimeException("Unsupported listvalue type: " + first.getClass().getName());
        }
    }

    private static void updateSettingsSinglePlayer(ConfigBuilder.ConfigValue<?> configValue) {
        // send update to players if we are hosting a singleplayer server
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            configValue.onUpdate(Minecraft.getInstance().getSingleplayerServer());
            ServerNetworking.sendUpdatePacketToAll(Minecraft.getInstance().getSingleplayerServer(), configValue);
        }
    }
}
