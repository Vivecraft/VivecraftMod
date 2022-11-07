package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.gui.settings.GuiQuickCommandsInGame;
import org.vivecraft.settings.AutoCalibration;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.external.jkatvr;

@Mixin(PauseScreen.class)
public abstract class PauseScreenVRMixin extends Screen {

    private ClientDataHolder dataholder = ClientDataHolder.getInstance();

    protected PauseScreenVRMixin(Component component) {
        super(component);
    }


    @Inject(at =  @At("TAIL"), method = "createPauseMenu")
    public void addInit(CallbackInfo ci) {
        if (!Minecraft.getInstance().isMultiplayerServer()) {
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, Component.translatable("vivecraft.gui.chat"), (p) ->
            {
                this.minecraft.setScreen(new ChatScreen(""));
                if (ClientDataHolder.getInstance().vrSettings.autoOpenKeyboard)
                    KeyboardHandler.setOverlayShowing(true);
            }));
        } else {
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 46, 20, Component.translatable("vivecraft.gui.chat"), (p) ->
            {
                this.minecraft.setScreen(new ChatScreen(""));
            }));
            this.addRenderableWidget(new Button(this.width / 2 - 102 + 48, this.height / 4 + 72 + -16, 46, 20, Component.translatable("vivecraft.gui.social"), (p) ->
            {
                this.minecraft.setScreen(new SocialInteractionsScreen());
            }));
        }

        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, Component.translatable("vivecraft.gui.commands"), (p) ->
        {
            this.minecraft.setScreen(new GuiQuickCommandsInGame(this));
            this.init();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 49, 20, Component.translatable("vivecraft.gui.overlay"), (p) ->
        {
            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
            this.minecraft.setScreen((Screen) null);
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 52, this.height / 4 + 120 + -16, 49, 20, Component.translatable("vivecraft.gui.profiler"), (p) ->
        {
            if (!this.minecraft.options.renderDebug) this.minecraft.options.renderDebugCharts = false;
            this.minecraft.options.renderDebugCharts = !this.minecraft.options.renderDebugCharts;
            this.minecraft.options.renderDebug = this.minecraft.options.renderDebugCharts;
            this.minecraft.setScreen((Screen) null);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 120 + -16, 98, 20, Component.translatable("vivecraft.gui.screenshot"), (p) ->
        {
            this.minecraft.setScreen((Screen) null);
            ClientDataHolder.getInstance().grabScreenShot = true;
        }));

        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 144 + -16, 98, 20, Component.translatable("vivecraft.gui.calibrateheight"), (p) ->
            {
                AutoCalibration.calibrateManual();
                ClientDataHolder.getInstance().vrSettings.saveOptions();
                this.minecraft.setScreen((Screen) null);
            }));
        }

        if (ClientDataHolder.katvr) {
            this.addRenderableWidget(new Button(this.width / 2 + 106, this.height / 4 + 144 + -16, 98, 20, Component.translatable("vivecraft.gui.alignkatwalk"), (p) ->
            {
                jkatvr.resetYaw(ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.hmd.getYaw());
                this.minecraft.setScreen((Screen) null);
            }));
        }

        if (!ClientDataHolder.getInstance().vrSettings.seated || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 144 + -16, 98, 20, Component.translatable("vivecraft.gui.movethirdpersoncam"), (p) ->
            {
                if (!VRHotkeys.isMovingThirdPersonCam()) {
                    VRHotkeys.startMovingThirdPersonCam(1, VRHotkeys.Triggerer.MENUBUTTON);
                } else if (VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON) {
                    VRHotkeys.stopMovingThirdPersonCam();
                    ClientDataHolder.getInstance().vrSettings.saveOptions();
                }
            }));
        }

        // move every button up a bit
        if (!ClientDataHolder.getInstance().vrSettings.seated || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            for (Widget widget: this.renderables) {
                ((AbstractWidget)widget).y -= 24;
    }
        }
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 40))
    private int moveTitleUp(int constant) {
        return (!ClientDataHolder.getInstance().vrSettings.seated || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) ? 16 : 40;
    }

    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 3))
    private GuiEventListener remove(PauseScreen instance, GuiEventListener guiEventListener) {
        // Feedback button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((AbstractWidget)guiEventListener).visible = false;
        return this.addRenderableWidget((Button)guiEventListener);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/PauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 4))
    private GuiEventListener remove2(PauseScreen instance, GuiEventListener guiEventListener) {
        // report bugs button
        // don't remove, just hide, so mods that rely on it being there, still work
        ((AbstractWidget)guiEventListener).visible = false;
        return this.addRenderableWidget((Button)guiEventListener);
    }
    @Redirect(method = "createPauseMenu", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/Button;active:Z"))
    private void remove3(Button instance, boolean value) {}
    @ModifyConstant(method = "createPauseMenu", constant = @Constant(intValue = 120), remap = false)
    private int moveDown(int constant) {
        return (!ClientDataHolder.getInstance().vrSettings.seated || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) ? 168 : 144;
    }
}
