package org.vivecraft.mixin.client_vr.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.List;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenVRMixin extends Screen {

    protected SoundOptionsScreenVRMixin(Component title) {
        super(title);
    }

    @Unique
    private CycleButton<?> vivecraft$directionalAudioVRButton = null;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/SoundOptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 2))
    private void vivecraft$addHRTFButton(CallbackInfo ci) {
        this.vivecraft$directionalAudioVRButton = CycleButton.builder(
                (bool) -> (boolean) bool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
            .withValues(false, true)
            .withInitialValue(ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0)
            .withTooltip(
                obj -> this.minecraft.font.split(Component.translatable("vivecraft.options.HRTF_SELECTION.tooltip"),
                    200))
            .create(this.width / 2 - 155 + 160, this.height / 6 - 12 + 22 * 5, 150, 20,
                Component.translatable("vivecraft.options.HRTF_SELECTION"),
                (cycleButton, newValue) -> {
                    ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                });

        this.addRenderableWidget(this.vivecraft$directionalAudioVRButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.vivecraft$directionalAudioVRButton != null &&
            this.vivecraft$directionalAudioVRButton.isMouseOver(mouseX, mouseY))
        {
            List<FormattedCharSequence> list = this.vivecraft$directionalAudioVRButton.getTooltip();
            this.renderTooltip(poseStack, list, mouseX, mouseY);
        }
    }
}
