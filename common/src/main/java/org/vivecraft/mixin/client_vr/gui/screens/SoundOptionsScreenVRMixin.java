package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(SoundOptionsScreen.class)
public abstract class SoundOptionsScreenVRMixin extends OptionsSubScreen {

    public SoundOptionsScreenVRMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void vivecraft$addVivecraftSettings(CallbackInfo ci) {
        this.list.addSmall(OptionInstance.createBoolean(
                "vivecraft.options.HRTF_SELECTION",
                boolean_ -> Tooltip.create(Component.translatable("vivecraft.options.HRTF_SELECTION.tooltip")),
                ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0,
                boolean_ -> {
                    ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                })
            , null);
    }
}
