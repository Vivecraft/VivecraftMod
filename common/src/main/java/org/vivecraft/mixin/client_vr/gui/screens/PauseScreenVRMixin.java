package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jkatvr;

@Mixin(value = PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends Screen {

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 4), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addInit(CallbackInfo ci, GridLayout gridWidget, GridLayout.RowHelper rowHelper) {
        if (!VRState.vrEnabled) {
            return;
        }
        // reset row to above
        try {
            rowHelper.addChild(null, -2);
        } catch (IllegalArgumentException e) {}

        if (!Minecraft.getInstance().isMultiplayerServer()) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        this.minecraft.setScreen(new ChatScreen(""));
                        if (ClientDataHolderVR.getInstance().vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(98).build());
        } else {
            GridLayout gridWidgetChat_Social = new GridLayout();
            gridWidgetChat_Social.defaultCellSetting().paddingRight(1);
            GridLayout.RowHelper rowHelperChat_Social = gridWidgetChat_Social.createRowHelper(2);
            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        this.minecraft.setScreen(new ChatScreen(""));
                        if (ClientDataHolderVR.getInstance().vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(48).build());

            rowHelperChat_Social.addChild(new Button.Builder(Component.translatable("vivecraft.gui.social"), (p) -> this.minecraft.setScreen(new SocialInteractionsScreen())).width(48).pos(50, 0).build());
            rowHelper.addChild(gridWidgetChat_Social);
        }

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.commands"), (p) -> this.minecraft.setScreen(new GuiQuickCommandsInGame(this))).width(98).build());
    }

    @Inject(at =  @At(value = "FIELD", opcode = Opcodes.PUTFIELD,target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = At.Shift.BY, by = -3), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addLowerButtons(CallbackInfo ci, GridLayout gridWidget, GridLayout.RowHelper rowHelper) {
        if (!VRState.vrEnabled) {
            return;
        }
        GridLayout gridWidgetOverlay_Profiler = new GridLayout();
        gridWidgetOverlay_Profiler.defaultCellSetting().paddingRight(1);
        GridLayout.RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.overlay"), (p) ->
        {
            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
            this.minecraft.setScreen((Screen) null);
        }).width(48).build());

        rowHelperOverlay_Profiler.addChild(new Button.Builder(Component.translatable("vivecraft.gui.profiler"), (p) ->
        {
            if (!this.minecraft.options.renderDebug) this.minecraft.options.renderDebugCharts = false;
            this.minecraft.options.renderDebugCharts = !this.minecraft.options.renderDebugCharts;
            this.minecraft.options.renderDebug = this.minecraft.options.renderDebugCharts;
            this.minecraft.setScreen((Screen) null);
        }).width(48).pos(50, 0).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.screenshot"), (p) ->
        {
            this.minecraft.setScreen((Screen) null);
            ClientDataHolderVR.getInstance().grabScreenShot = true;
        }).width(98).build());

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.calibrateheight"), (p) ->
            {
                AutoCalibration.calibrateManual();
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                this.minecraft.setScreen((Screen) null);
            }).width(98).build());
        }

        if (ClientDataHolderVR.katvr) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.alignkatwalk"), (p) ->
            {
                jkatvr.resetYaw(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                this.minecraft.setScreen((Screen) null);
            }).width(98).build());
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
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

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2))
    private LayoutElement remove(GridLayout.RowHelper instance, LayoutElement layoutElement) {
        // Feedback button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button)layoutElement).visible = !VRState.vrEnabled;
        return instance.addChild(layoutElement);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 3))
    private LayoutElement remove2(GridLayout.RowHelper instance, LayoutElement layoutElement) {
        // report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button)layoutElement).visible = !VRState.vrEnabled;
        return instance.addChild(layoutElement);
    }
    // TODO this seems unneeded?
    @Redirect(method = "createPauseMenu", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/Button;active:Z"))
    private void remove3(Button instance, boolean value) {}
}
