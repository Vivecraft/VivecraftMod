package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiOrderedListEditorScreen;
import org.vivecraft.client.gui.framework.screens.GuiSelectionListScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GuiActiveKeyboardLayoutSelector extends GuiOrderedListEditorScreen<VRSettings.KeyboardLayout> {
    public GuiActiveKeyboardLayoutSelector(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.activekeyboardlayouts"),
            lastScreen, false,
            () -> Arrays.stream(ClientDataHolderVR.getInstance().vrSettings.keyboardLayoutOrder)
                .map(l -> ClientDataHolderVR.getInstance().vrSettings.keyboardLayouts.get(l)).toList(),
            () -> {
                VRSettings vrSettings = ClientDataHolderVR.getInstance().vrSettings;
                vrSettings.currentKeyboardLayout = 0;
                vrSettings.keyboardLayoutOrder = new String[]{"custom", "en_us"};
                vrSettings.saveOptions();
            }, keyboardLayouts -> {
                VRSettings vrSettings = ClientDataHolderVR.getInstance().vrSettings;
                vrSettings.currentKeyboardLayout = 0;
                vrSettings.keyboardLayoutOrder = keyboardLayouts.stream().map(VRSettings.KeyboardLayout::id)
                    .toArray(String[]::new);
                vrSettings.saveOptions();
            });
    }

    @Override
    public void onClose() {
        super.onClose();
        KeyboardHandler.reinitKeyboard();
    }

    @Override
    protected void addNewValue() {
        this.minecraft.setScreen(getSelectionScreen(this,
            layout -> this.elements.stream().noneMatch(e -> e.id().equals(layout.id())),
            layout -> {
                super.addNewValue();
                this.elements.add(layout);
            }, false));
    }

    @Override
    protected ValueEntry<VRSettings.KeyboardLayout> toEntry(VRSettings.KeyboardLayout value, int index) {
        return new KeyboardEntry(value, index);
    }

    public static GuiSelectionListScreen<VRSettings.KeyboardLayout> getSelectionScreen(
        Screen parent, Predicate<VRSettings.KeyboardLayout> filter, Consumer<VRSettings.KeyboardLayout> consumer,
        boolean hasReset)
    {
        return new GuiSelectionListScreen<>(
            Component.translatable("vivecraft.options.screen.keyboardlayoutselection"), parent,
            () -> ClientDataHolderVR.getInstance().vrSettings.keyboardLayouts.values().stream().filter(filter)
                .sorted(Comparator.comparing(key -> getLangComponent(key).getString())).toList(),
            GuiActiveKeyboardLayoutSelector::getLangComponent,
            null, consumer, hasReset, true, null);
    }

    private class KeyboardEntry extends OrderedEntry<VRSettings.KeyboardLayout> {
        public KeyboardEntry(VRSettings.KeyboardLayout layout, int index) {
            super(getLangComponent(layout), layout, index);
        }
    }

    private static Component getLangComponent(VRSettings.KeyboardLayout layout) {
        LanguageInfo info = Minecraft.getInstance().getLanguageManager().getLanguages().get(layout.id());
        return info != null ? info.toComponent() : layout.fallbackName();
    }
}
