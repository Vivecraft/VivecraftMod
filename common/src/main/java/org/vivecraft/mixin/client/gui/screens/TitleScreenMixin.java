package org.vivecraft.mixin.client.gui.screens;

import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.utils.UpdateChecker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.*;

import static org.joml.Math.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.TitleScreen.class)
public abstract class TitleScreenMixin extends net.minecraft.client.gui.screens.Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    //TODO Add config file
//    private final Properties vrConfig = new Properties();
//    private final Path vrConfigPath = Xplat.getConfigPath("vivecraft-config.properties");
    private boolean showError = false;
    private Button vrModeButton;

    private Button updateButton;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void initFullGame(CallbackInfo ci) {
        this.addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void initDemo(CallbackInfo ci) {
        this.addVRModeButton();
    }

    private void addVRModeButton() {

        this.vrModeButton = new Builder(Component.translatable("vivecraft.gui.vr", this.getIcon() , vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (button) -> {
            this.showError = false;
            vrEnabled = !vrEnabled;
            dh.vrSettings.vrEnabled = vrEnabled;
            dh.vrSettings.saveOptions();

            button.setMessage(Component.translatable("vivecraft.gui.vr", this.getIcon(), vrEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
        })
                .size(56, 20)
                .pos(this.width / 2 + 104, this.height / 4 + 72)
                .build();

        this.addRenderableWidget(this.vrModeButton);

        this.updateButton = new Builder(Component.translatable("vivecraft.gui.update"), (button) -> this.minecraft.setScreen(new UpdateScreen()))
                .size(56, 20)
                .pos(this.width / 2 + 104, this.height / 4 + 96)
                .build();

        this.updateButton.visible = UpdateChecker.hasUpdate;

        this.addRenderableWidget(this.updateButton);
    }

    private String getIcon() {
        return (this.showError ? "§c⚠§r " : "");
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void renderToolTip(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        this.updateButton.visible = UpdateChecker.hasUpdate;

        if (this.vrModeButton.isMouseOver(i, j)) {
            guiGraphics.renderTooltip(this.font, this.font.split(Component.translatable("vivecraft.options.VR_MODE.tooltip"), max(this.width / 2 - 43, 170)), i, j);
        }
        if (vrInitialized && !vrRunning) {
            Component hotswitchMessage = Component.translatable("vivecraft.messages.vrhotswitchinginfo");
            guiGraphics.renderTooltip(this.font, this.font.split(hotswitchMessage, 280), this.width / 2 - 140 - 12, 17);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render")
    public void maybeNoPanorama(PanoramaRenderer instance, float f, float g){
        if (vrRunning && dh.menuWorldRenderer.isReady()){
            return;
        }
        instance.render(f, g);
    }
}
