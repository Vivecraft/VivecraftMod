package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenVRMixin extends OptionsSubScreen {

    public SoundOptionsScreenVRMixin(
        Screen screen, Options options, Component component) {
        super(screen, options, component);
    }

    @Inject(method = "addOptions", at = @At(value = "TAIL"))
    private void vivecraft$addVivecraftSettings(CallbackInfo ci) {
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
            , null);
    }
}
