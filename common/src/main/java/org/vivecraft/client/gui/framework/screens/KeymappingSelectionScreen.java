package org.vivecraft.client.gui.framework.screens;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Consumer;

public class KeymappingSelectionScreen extends GuiSelectionListScreen<KeyMapping> {
    /**
     * @param title      title to show on the screen
     * @param lastScreen previous screen to go back to when done
     * @param consumer   called with the selected KeyMapping, is called with {@code null} if it should reset
     */
    public KeymappingSelectionScreen(
        Component title, Screen lastScreen, Consumer<KeyMapping> consumer)
    {
        super(title, lastScreen,
            () -> Arrays.stream(Minecraft.getInstance().options.keyMappings).sorted().toList(),
            key -> Component.translatable(key.getName()),
            key -> key.getCategory().id().toLanguageKey("key.category"),
            consumer,
            true, false, null);
    }
}
