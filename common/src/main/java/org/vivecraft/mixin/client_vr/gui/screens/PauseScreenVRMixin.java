package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.screens.FBTCalibrationScreen;
import org.vivecraft.client.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jkatvr;

@Mixin(value = PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends Screen {

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SpriteIconButton;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;Z)Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;", ordinal = 0))
    private void vivecraft$addTopButtons(CallbackInfo ci, @Local LinearLayout rowHelper) {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return;
        }

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"),
            (p) -> this.minecraft.gui.setScreen(new ChatScreen("", false))).width(48).build());
    }

    @ModifyExpressionValue(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LayoutSettings;alignHorizontallyCenter()Lnet/minecraft/client/gui/layouts/LayoutSettings;"))
    private LayoutSettings vivecraft$addTopButtons2(
        LayoutSettings layoutSettings, @Local(ordinal = 0) LinearLayout rowHelper)
    {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return layoutSettings;
        }

        Button commands;
        if (ClientDataHolderVR.getInstance().vrSettings.commandsButtonIcon) {
            commands = SpriteIconButton.builder(Component.translatable("vivecraft.gui.commands"),
                    (p) -> this.minecraft.gui.setScreen(new GuiQuickCommandsInGame(this)), true)
                .width(20)
                .sprite(Identifier.fromNamespaceAndPath("vivecraft", "icon/commands"), 15, 15)
                .withTootip()
                .build();
        } else {
            commands = new Button.Builder(Component.translatable("vivecraft.gui.commands"),
                (p) -> this.minecraft.gui.setScreen(new GuiQuickCommandsInGame(this))).width(56).build();
        }
        rowHelper.addChild(commands);

        // calculate width of the row
        rowHelper.arrangeElements();
        // if it is wider than the button gred offset it to the left, so that it is centered
        if (rowHelper.getWidth() > 204) {
            return layoutSettings.paddingHorizontal((204 - rowHelper.getWidth()) / 2 - 8);
        } else {
            return layoutSettings;
        }
    }

    // use the disconnect button as an anchor, and shift by -3 to shift before the addChild call
    @Inject(method = "createPauseMenu", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = At.Shift.BY, by = -3))
    private void vivecraft$addLowerButtons(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (!VRState.VR_INITIALIZED || !ClientDataHolderVR.getInstance().vrSettings.modifyPauseMenu) {
            return;
        }
        GridLayout gridWidgetOverlay_Profiler = new GridLayout();
        GridLayout.RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.overlay"),
            (p) -> {
                this.minecraft.debugEntries.toggleDebugOverlay();
                this.minecraft.gui.setScreen(null);
            }).width(48).build(), LayoutSettings.defaults().paddingRight(2));

        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.profiler"),
            (p) -> {
                this.minecraft.getDebugOverlay().toggleProfilerChart();
                this.minecraft.gui.setScreen(null);
            }).width(48).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.screenshot"),
            (p) -> {
                this.minecraft.gui.setScreen(null);
                ClientDataHolderVR.getInstance().grabScreenShot = true;
            }).width(98).build());

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            if (ClientDataHolderVR.getInstance().vr.hasFBT() ||
                ClientDataHolderVR.getInstance().vr.getTrackers().size() >= 3)
            {
                rowHelper.addChild(new Button.Builder(
                    Component.translatable("vivecraft.options.screen.fbtcalibration.button"),
                    (p) -> this.minecraft.gui.setScreen(new FBTCalibrationScreen(this)))
                    .width(98).build());
            } else {
                rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.calibrateheight"),
                    (p) -> {
                        AutoCalibration.calibrateManual();
                        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                        this.minecraft.gui.setScreen(null);
                    }).width(98).build());
            }
        }

        if (ClientDataHolderVR.getInstance().katVr) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.alignkatwalk"),
                (p) -> {
                    jkatvr.resetYaw(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                    this.minecraft.gui.setScreen(null);
                }).width(98).build());
        }

        if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON ||
            ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY)
        {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.movethirdpersoncam"), (p) ->
            {
                if (!VRHotkeys.isMovingThirdPersonCam()) {
                    VRHotkeys.startMovingThirdPersonCam(1, VRHotkeys.Triggerer.MENUBUTTON);
                } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON) {
                    VRHotkeys.stopMovingThirdPersonCam();
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                }
            }).width(98).build());
        }
    }
}
