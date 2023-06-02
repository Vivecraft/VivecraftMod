package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;

import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    //TODO Add config file
//    private final Properties vrConfig = new Properties();
//    private final Path vrConfigPath = Xplat.getConfigPath("vivecraft-config.properties");
    private boolean showError = false;
    private AbstractWidget firstButton;
    private Button vrModeButton;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.AFTER, ordinal = 1), method = "createNormalMenuOptions")
    public void initFullGame(CallbackInfo ci) {
        addVRModeButton();
    }

    @Inject(at = @At("TAIL"), method = "createDemoMenuOptions")
    public void initDemo(CallbackInfo ci) {
        addVRModeButton();
    }

    private void addVRModeButton() {

        // get first button, to position warnings
        for (Renderable widget : renderables) {
            if (widget instanceof AbstractWidget) {
                firstButton = (AbstractWidget) widget;
                break;
            }
        }

        String vrMode = VRState.vrEnabled ? "VR ON" : "VR OFF";
        vrModeButton = new Button.Builder(Component.literal(getIcon() + vrMode), (button) -> {
            showError = false;
            VRState.vrEnabled = !VRState.vrEnabled;
            button.setMessage(Component.translatable(getIcon() + (VRState.vrEnabled ? "VR ON" : "VR OFF")));
        })
                .size(56, 20)
                .pos(this.width / 2 + 104, this.height / 4 + 72)
                .build();

        this.addRenderableWidget(vrModeButton);
    }

    private String getIcon() {
        return (showError ? "§c\u26A0§r " : "");
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = At.Shift.BEFORE, ordinal = 0), method = "render")
    public void renderText(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        int l = vrModeButton.getY() - 23;
        drawString(poseStack, this.font, "Vivecraft", this.width / 2 + 106, l, 16777215);
        drawString(poseStack, this.font, Component.translatable("vivecraft.messages.mode"), this.width / 2 + 106, l + 10, 16777215);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void renderWarning(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {

        if (vrModeButton.isMouseOver(i, j)) {
            renderTooltip(poseStack, font.split(Component.translatable("vivecraft.options.VR_MODE.tooltip"), Math.max(width / 2 - 43, 170)), i, j);
        }

        int warningHeight = firstButton.getY() - 10;
        Component warning = null;
        if (showError) {
            warning = Component.translatable("vivecraft.messages.configWriteError");
        }
        if (warning != null) {
            int length = 0;
            List<FormattedCharSequence> splitString = font.split(FormattedText.of(warning.getString()), 360);
            for (FormattedCharSequence string : splitString) {
                length = Math.max(length, this.font.width(string));
            }
            // move in front of button tooltips
            poseStack.pushPose();
            poseStack.translate(0, 0, 500);
            GuiComponent.fill(poseStack, this.width / 2 - length / 2 - 4, warningHeight - 4, this.width / 2 + length / 2 + 4, warningHeight + (font.lineHeight + 3) * splitString.size(), -536870912);
            for (int line = 0; line < splitString.size(); line++) {
                drawCenteredString(poseStack, this.font, splitString.get(line), this.width / 2, warningHeight + (font.lineHeight + 2) * line, 16777215);
            }
            poseStack.popPose();
        }
    }
}
