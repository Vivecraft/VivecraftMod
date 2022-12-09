package org.vivecraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.settings.VRSettings;

@Mixin(OptionInstance.class)
public class OptionInstanceVRMixin {

    // replace vanilla button with ours, so directional audio is on by default, when playing VR

    @Inject(method = "createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;", at = @At("RETURN"), cancellable = true)
    private void redirectHRTFButton2(Options options, int i, int j, int k, CallbackInfoReturnable<AbstractWidget> cir) {
        if ((Object)this == options.directionalAudio()) {
            cir.setReturnValue(CycleButton.builder((bool) -> (boolean)bool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
                    .withValues(false, true)
                    .withInitialValue(ClientDataHolder.getInstance().vrSettings.hrtfSelection >= 0)
                    .withTooltip(obj -> {
                        Tooltip tooltipON = Tooltip.create(Component.translatable("options.directionalAudio.on.tooltip"));
                        Tooltip tooltipOFF = Tooltip.create(Component.translatable("options.directionalAudio.off.tooltip"));
                        return ClientDataHolder.getInstance().vrSettings.hrtfSelection >= 0 ? tooltipON : tooltipOFF;
                    })
                    .create(i, j, k, 20, Component.translatable("options.directionalAudio"), (cycleButton, newValue) -> {
                        ClientDataHolder.getInstance().vrSettings.hrtfSelection = (boolean)newValue ? 0 : -1;
                        ClientDataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                        ClientDataHolder.getInstance().vrSettings.saveOptions();

                        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                        soundManager.reload();
                        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }));
        }

    }
}
