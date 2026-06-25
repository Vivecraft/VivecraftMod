package org.vivecraft.server.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.vivecraft.client.gui.framework.screens.GuiStringListEditorScreen;
import org.vivecraft.client.utils.ClientUtils;
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
        return () -> new Button(0, 0, width, height,
            new TranslatableComponent("" + value.get()), button -> {});
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
            .create(0, 0, width, height, TextComponent.EMPTY, (button, bool) -> {
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
                new TextComponent(stringValue.get()))
            {
                @Override
                public boolean charTyped(char character, int modifiers) {
                    boolean ret = super.charTyped(character, modifiers);
                    stringValue.set(this.getValue());
                    updateSettingsSinglePlayer(stringValue);
                    return ret;
                }

                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    boolean ret = super.keyPressed(keyCode, scanCode, modifiers);
                    stringValue.set(this.getValue());
                    updateSettingsSinglePlayer(stringValue);
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
            .builder((newValue) -> new TranslatableComponent(
                "vivecraft.serverSettings." + configValue.getPath() + "." + newValue))
            .withInitialValue(configValue.get())
            // toArray is needed here, because the button uses Objects, and the collection is of other types
            .withValues(values.toArray())
            .withInitialValue(configValue.get())
            .displayOnlyValue()
            .create(0, 0, width, height, TextComponent.EMPTY, (button, newValue) -> {
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
            new TextComponent("" + numberValue.get()), numberValue.normalize())
        {
            @Override
            protected void updateMessage() {
                setMessage(new TextComponent("" + numberValue.get()));
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
        Object first = listValue.get().isEmpty() ? null : listValue.get().get(0);
        if (first == null || first instanceof String) {
            ConfigBuilder.ListValue<String> stringValue = (ConfigBuilder.ListValue<String>) listValue;
            return () -> new Button(0, 0, width, height,
                new TranslatableComponent("vivecraft.options.editlist"),
                button -> Minecraft.getInstance().setScreen(new GuiStringListEditorScreen(
                    new TranslatableComponent("vivecraft.serverSettings." + listValue.getPath()),
                    Minecraft.getInstance().screen, false, stringValue::get, stringValue::reset, list -> {
                    stringValue.set(list);
                    updateSettingsSinglePlayer(stringValue);
                })));
        } else {
            // TODO handle other types than String
            throw new RuntimeException("Unsupported listvalue type: " + first.getClass().getName());
        }
    }

    private static void updateSettingsSinglePlayer(ConfigBuilder.ConfigValue<?> configValue) {
        configValue.onUpdate(Minecraft.getInstance().getSingleplayerServer(), ClientUtils::addChatMessage);
        // send update to players if we are hosting a singleplayer server
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            ServerNetworking.sendUpdatePacketToAll(Minecraft.getInstance().getSingleplayerServer(), configValue,
                ClientUtils::addChatMessage);
        }
    }
}
