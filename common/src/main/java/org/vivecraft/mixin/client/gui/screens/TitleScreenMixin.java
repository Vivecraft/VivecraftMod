package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client.gui.screens.UpdateScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    //TODO Add config file
//    private final Properties vrConfig = new Properties();
//    private final Path vrConfigPath = Xplat.getConfigPath("vivecraft-config.properties");
    private boolean showError = false;
    private Button vrModeButton;

    private Button updateButton;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void initFullGame(CallbackInfo ci) {
        addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void initDemo(CallbackInfo ci) {
        addVRModeButton();
    }

    private void addVRModeButton() {

        vrModeButton = new Button(
            this.width / 2 + 104, this.height / 4 + 72,
            56, 20,
            Component.translatable("vivecraft.gui.vr", getIcon() , VRState.vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
            (button) -> {
                showError = false;
                VRState.vrEnabled = !VRState.vrEnabled;
                ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.vrEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                button.setMessage(Component.translatable("vivecraft.gui.vr", getIcon(), VRState.vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
            });

        this.addRenderableWidget(vrModeButton);

        updateButton = new Button(
            this.width / 2 + 104, this.height / 4 + 96,
            56, 20,
            Component.translatable("vivecraft.gui.update"),
            (button) -> minecraft.setScreen(new UpdateScreen()));

        updateButton.visible = UpdateChecker.hasUpdate;

        this.addRenderableWidget(updateButton);
    }

    private String getIcon() {
        return (showError ? "§c\u26A0§r " : "");
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void renderToolTip(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        updateButton.visible = UpdateChecker.hasUpdate;

        if (vrModeButton.isMouseOver(i, j)) {
            renderTooltip(poseStack, font.split(Component.translatable("vivecraft.options.VR_MODE.tooltip"), Math.max(width / 2 - 43, 170)), i, j);
        }
        if (VRState.vrInitialized && !VRState.vrRunning) {
            Component hotswitchMessage = Component.translatable("vivecraft.messages.vrhotswitchinginfo");
            renderTooltip(poseStack, font.split(hotswitchMessage, 280), width / 2 - 140 - 12, 17);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render")
    public void maybeNoPanorama(PanoramaRenderer instance, float f, float g){
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()){
            return;
        }
        instance.render(f, g);
    }
}
