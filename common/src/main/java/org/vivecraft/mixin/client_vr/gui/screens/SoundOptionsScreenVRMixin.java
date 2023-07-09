package org.vivecraft.mixin.client_vr.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

    protected SoundOptionsScreenVRMixin(Component component) {
        super(component);
    }

    @Unique
    private AbstractWidget directionalAudioVRButton = null;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/SoundOptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 2, shift = At.Shift.BEFORE))
    private void addVivecraftSettings(CallbackInfo ci) {
        directionalAudioVRButton = CycleButton.builder((bool) -> (boolean)bool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
            .withValues(false, true)
            .withInitialValue(ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0)
            .withTooltip(obj ->
                minecraft.font.split(Component.translatable("vivecraft.options.HRTF_SELECTION.tooltip"), 200))
            .create(this.width / 2 - 155 + 160, this.height / 6 - 12 + 22 * 5, 150, 20, Component.translatable("vivecraft.options.HRTF_SELECTION"), (cycleButton, newValue) -> {
                ClientDataHolderVR.getInstance().vrSettings.hrtfSelection = (boolean)newValue ? 0 : -1;
                ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.HRTF_SELECTION);
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();

                SoundManager soundManager = Minecraft.getInstance().getSoundManager();
                soundManager.reload();
                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            });

        this.addRenderableWidget(directionalAudioVRButton);
    }
    @Inject(at = @At("TAIL"), method = "render")
    private void renderTooltip(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        if (directionalAudioVRButton != null && directionalAudioVRButton.isMouseOver(i, j)) {
            List<FormattedCharSequence> list = ((TooltipAccessor)this.directionalAudioVRButton).getTooltip();
            this.renderTooltip(poseStack, list, i, j);
        }
    }
}
