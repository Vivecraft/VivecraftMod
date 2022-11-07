package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.settings.VRSettings;

import java.util.List;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenVRMixin extends Screen {
    protected SoundOptionsScreenVRMixin(Component component) {
        super(component);
    }

    // replace vanilla button with ours, so directional audio is on by default, when playing VR
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;", ordinal = 2))
    private AbstractWidget redirectHRTFButton(OptionInstance instance, Options options, int i, int j, int k){
        return CycleButton.builder((bool) -> (boolean)bool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
            .withValues(false, true)
            .withInitialValue(ClientDataHolder.getInstance().vrSettings.hrtfSelection >= 0)
            .withTooltip(obj -> {
                List<FormattedCharSequence> tooltipON = minecraft.font.split(Component.translatable("options.directionalAudio.on.tooltip"), 200);
                List<FormattedCharSequence> tooltipOFF = minecraft.font.split(Component.translatable("options.directionalAudio.off.tooltip"), 200);
                return ClientDataHolder.getInstance().vrSettings.hrtfSelection >= 0 ? tooltipON : tooltipOFF;
            })
            .create(i, j, k, 20, Component.translatable("options.directionalAudio"), (cycleButton, newValue) -> {
                ClientDataHolder.getInstance().vrSettings.hrtfSelection = (boolean)newValue ? 0 : -1;
                ClientDataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                ClientDataHolder.getInstance().vrSettings.saveOptions();

                SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                soundManager.reload();
                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            });
    }
}
