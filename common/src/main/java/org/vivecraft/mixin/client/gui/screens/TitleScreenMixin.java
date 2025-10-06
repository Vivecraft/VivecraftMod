package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.GuiHelper;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button vivecraft$vrModeButton;
    @Unique
    private Button vivecraft$updateButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    /**
     * injects after the multiplayer button to be in the right spot for the tab navigation
     */
    @Inject(method = "createNormalMenuOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1))
    private void vivecraft$initFullGame(CallbackInfo ci) {
        vivecraft$addVRModeButton();
    }

    @Inject(method = "createDemoMenuOptions", at = @At("TAIL"))
    private void vivecraft$initDemo(CallbackInfo ci) {
        vivecraft$addVRModeButton();
    }

    @Unique
    private void vivecraft$addVRModeButton() {
        this.vivecraft$vrModeButton = new Button(
            this.width / 2 + 104, this.height / 4 + 72, 56, 20,
            new TranslatableComponent("vivecraft.gui.vr",
                VRState.VR_ENABLED ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (button) -> {
            VRState.VR_ENABLED = !VRState.VR_ENABLED;
            ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.VR_ENABLED;
            ClientDataHolderVR.getInstance().vrSettings.saveOptions();

            button.setMessage(new TranslatableComponent("vivecraft.gui.vr",
                VRState.VR_ENABLED ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
        }, (button, poseStack, x, y) -> GuiHelper.renderOnTooltip(button, poseStack, x, y,
            new TranslatableComponent("vivecraft.options.VR_ENABLED.tooltip")));
        this.vivecraft$vrModeButton.visible = ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled;

        this.addRenderableWidget(this.vivecraft$vrModeButton);

        this.vivecraft$updateButton = new Button(
            this.width / 2 + 104, this.height / 4 + 96, 56, 20,
            new TranslatableComponent("vivecraft.gui.update"),
            (button) -> this.minecraft.setScreen(new UpdateScreen()));

        this.vivecraft$updateButton.visible = UpdateChecker.HAS_UPDATE;

        this.addRenderableWidget(this.vivecraft$updateButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void vivecraft$renderToolTip(
        PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci)
    {
        // some mods cancel the title screen init
        if (this.vivecraft$updateButton != null) {
            this.vivecraft$updateButton.visible = UpdateChecker.HAS_UPDATE;
        }

        if (VRState.VR_INITIALIZED && !VRState.VR_RUNNING) {
            Component hotswitchMessage = new TranslatableComponent("vivecraft.messages.vrhotswitchinginfo");
            renderTooltip(poseStack, this.font.split(hotswitchMessage, 280), this.width / 2 - 140 - 12, 17);
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), index = 1)
    private float vivecraft$maybeNoPanorama(float alpha) {
        return VRState.VR_RUNNING && (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady() ||
            ClientDataHolderVR.getInstance().vrSettings.menuWorldFallbackPanorama
        ) ? 0.0F : alpha;
    }
}
