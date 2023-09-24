package org.vivecraft.mixin.client_vr.gui.screens;

import org.vivecraft.client.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRHotkeys.Triggerer;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_vr.utils.external.jkatvr;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.*;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = net.minecraft.client.gui.screens.PauseScreen.class, priority = 900)
public abstract class PauseScreenVRMixin extends net.minecraft.client.gui.screens.Screen {

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 4), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addInit(CallbackInfo ci, GridLayout gridWidget, RowHelper rowHelper) {
        if (!vrEnabled) {
            return;
        }
        // reset row to above
        try {
            rowHelper.addChild(null, -2);
        } catch (IllegalArgumentException ignored) {}

        if (!mc.isMultiplayerServer()) {
            rowHelper.addChild(new Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        mc.setScreen(new ChatScreen(""));
                        if (dh.vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(98).build());
        } else {
            GridLayout gridWidgetChat_Social = new GridLayout();
            gridWidgetChat_Social.defaultCellSetting().paddingRight(1);
            RowHelper rowHelperChat_Social = gridWidgetChat_Social.createRowHelper(2);
            rowHelperChat_Social.addChild(new Builder(Component.translatable("vivecraft.gui.chat"), (p) ->
                    {
                        mc.setScreen(new ChatScreen(""));
                        if (dh.vrSettings.autoOpenKeyboard)
                            KeyboardHandler.setOverlayShowing(true);
                    }).width(48).build());

            rowHelperChat_Social.addChild(new Builder(Component.translatable("vivecraft.gui.social"), (p) ->
                mc.setScreen(new SocialInteractionsScreen())).width(48).pos(50, 0).build()
            );
            rowHelper.addChild(gridWidgetChat_Social);
        }

        rowHelper.addChild(new Builder(Component.translatable("vivecraft.gui.commands"), (p) ->
            mc.setScreen(new GuiQuickCommandsInGame(this))).width(98).build()
        );
    }

    @Inject(at =  @At(value = "FIELD", opcode = PUTFIELD,target = "Lnet/minecraft/client/gui/screens/PauseScreen;disconnectButton:Lnet/minecraft/client/gui/components/Button;", shift = Shift.BY, by = -3), method = "createPauseMenu", locals = LocalCapture.CAPTURE_FAILHARD)
    public void addLowerButtons(CallbackInfo ci, GridLayout gridWidget, RowHelper rowHelper) {
        if (!vrEnabled) {
            return;
        }
        GridLayout gridWidgetOverlay_Profiler = new GridLayout();
        gridWidgetOverlay_Profiler.defaultCellSetting().paddingRight(1);
        RowHelper rowHelperOverlay_Profiler = gridWidgetOverlay_Profiler.createRowHelper(2);
        rowHelperOverlay_Profiler.addChild(new Builder(Component.translatable("vivecraft.gui.overlay"), (p) ->
        {
            mc.options.renderDebug = !mc.options.renderDebug;
            mc.setScreen(null);
        }).width(48).build());

        rowHelperOverlay_Profiler.addChild(new Builder(Component.translatable("vivecraft.gui.profiler"), (p) ->
        {
            if (!mc.options.renderDebug) mc.options.renderDebugCharts = false;
            mc.options.renderDebugCharts = !mc.options.renderDebugCharts;
            mc.options.renderDebug = mc.options.renderDebugCharts;
            mc.setScreen(null);
        }).width(48).pos(50, 0).build());

        rowHelper.addChild(gridWidgetOverlay_Profiler);

        rowHelper.addChild(new Builder(Component.translatable("vivecraft.gui.screenshot"), (p) ->
        {
            mc.setScreen(null);
            dh.grabScreenShot = true;
        }).width(98).build());

        if (!dh.vrSettings.seated) {
            rowHelper.addChild(new Builder(Component.translatable("vivecraft.gui.calibrateheight"), (p) ->
            {
                AutoCalibration.calibrateManual();
                dh.vrSettings.saveOptions();
                mc.setScreen(null);
            }).width(98).build());
        }

        if (dh.katvr) {
            rowHelper.addChild(new Builder(Component.translatable("vivecraft.gui.alignkatwalk"), (p) ->
            {
                jkatvr.resetYaw(dh.vrPlayer.vrdata_room_pre.hmd.getYaw());
                mc.setScreen(null);
            }).width(98).build());
        }

        if (!dh.vrSettings.seated || dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON || dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY) {
            rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.gui.movethirdpersoncam"), (p) ->
            {
                if (!VRHotkeys.isMovingThirdPersonCam()) {
                    VRHotkeys.startMovingThirdPersonCam(1, Triggerer.MENUBUTTON);
                } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == Triggerer.MENUBUTTON) {
                    VRHotkeys.stopMovingThirdPersonCam();
                    dh.vrSettings.saveOptions();
                }
            }).width(98).build());
        }
    }

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2))
    private LayoutElement remove(RowHelper instance, LayoutElement layoutElement) {
        // Feedback button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button)layoutElement).visible = !vrEnabled;
        return instance.addChild(layoutElement);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 3))
    private LayoutElement remove2(RowHelper instance, LayoutElement layoutElement) {
        // report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((Button)layoutElement).visible = !vrEnabled;
        return instance.addChild(layoutElement);
    }
    // TODO this seems unneeded?
    @Redirect(method = "createPauseMenu", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/Button;active:Z"))
    private void remove3(Button instance, boolean value) {}
}
