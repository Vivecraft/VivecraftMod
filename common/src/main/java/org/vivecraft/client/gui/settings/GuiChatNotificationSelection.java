package org.vivecraft.client.gui.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.vivecraft.client.gui.framework.screens.GuiSelectionListScreen;
import org.vivecraft.client.gui.framework.widgets.SilentButton;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiChatNotificationSelection extends GuiSelectionListScreen<ResourceLocation> {

    // needs to be static to be able to access it before super call
    private static SoundInstance ACTIVE_SOUND;
    private static AbstractWidget ACTIVE_BUTTON;

    public GuiChatNotificationSelection(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.CHAT_NOTIFICATION_SOUND"), lastScreen,
            () -> BuiltInRegistries.SOUND_EVENT.keySet().stream().sorted().toList(),
            ClientUtils::getNameFromSoundEvent,
            resourceLocation -> "",
            resourceLocation -> {
                if (resourceLocation != null) {
                    ClientDataHolderVR.getInstance().vrSettings.chatNotificationSound = resourceLocation.getPath();
                } else {
                    ClientDataHolderVR.getInstance().vrSettings.loadDefault(
                        VRSettings.VrOptions.CHAT_NOTIFICATION_SOUND);
                }
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            },
            true,
            resourceLocation -> new SilentButton(Component.literal("♫"),
                b -> BuiltInRegistries.SOUND_EVENT.get(resourceLocation)
                    .ifPresent(soundEvent -> startSound(soundEvent.value(), b)),
                20, 20)
        );
    }

    @Override
    public void tick() {
        if (ACTIVE_SOUND != null) {
            if (!this.minecraft.getSoundManager().isActive(ACTIVE_SOUND)) {
                stopSound();
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().getSoundManager().stop(ACTIVE_SOUND);
        ACTIVE_SOUND = null;
    }

    private static void stopSound() {
        Minecraft.getInstance().getSoundManager().stop(ACTIVE_SOUND);
        ACTIVE_BUTTON.setMessage(Component.literal("♫"));
        ACTIVE_SOUND = null;
        ACTIVE_BUTTON = null;
    }

    private static void startSound(SoundEvent soundEvent, AbstractWidget abstractWidget) {
        if (ACTIVE_SOUND != null) {
            stopSound();
        }
        ACTIVE_SOUND = SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f);
        Minecraft.getInstance().getSoundManager().play(ACTIVE_SOUND);

        ACTIVE_BUTTON = abstractWidget;
        ACTIVE_BUTTON.setMessage(Component.literal("♫").withStyle(ChatFormatting.GREEN));
    }
}
