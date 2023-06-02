package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenVRMixin {

    @Shadow private OptionsList list;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void addVivecraftSettings(CallbackInfo ci) {
        this.list.addSmall(OptionInstance.createBoolean(
            "vivecraft.options.HRTF_SELECTION",
            boolean_ -> Tooltip.create(Component.translatable("vivecraft.options.HRTF_SELECTION.tooltip")),
            ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0,
            boolean_ -> {
                ClientDataHolderVR.getInstance().vrSettings.hrtfSelection = boolean_ ? 0 : -1;
                ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();

                SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                soundManager.reload();
                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            })
            ,null);
    }
}
