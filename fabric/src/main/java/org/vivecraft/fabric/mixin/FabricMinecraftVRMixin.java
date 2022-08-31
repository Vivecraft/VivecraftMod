package org.vivecraft.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.CommonDataHolder;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;

@Mixin(Minecraft.class)
public class FabricMinecraftVRMixin {

    @Shadow
    public Screen screen;

    @Inject(at = @At("HEAD"), method = "method_24040", remap = false)
    public void menuInitvar(CallbackInfo ci) {
        if (ClientDataHolder.getInstance().vrRenderer.isInitialized()) {
            //DataHolder.getInstance().menuWorldRenderer.init();
        }
        ClientDataHolder.getInstance().vr.postinit();
    }

    @Inject(at = @At("HEAD"), method = "method_24228", remap = false)
    public void reloadVar(CallbackInfo ci) {
//		if (DataHolder.getInstance().menuWorldRenderer.isReady() && DataHolder.getInstance().resourcePacksChanged) {
//			try {
//				DataHolder.getInstance().menuWorldRenderer.destroy();
//				DataHolder.getInstance().menuWorldRenderer.prepare();
//			} catch (Exception exception) {
//				exception.printStackTrace();
//			}
//		}
        CommonDataHolder.getInstance().resourcePacksChanged = false;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = At.Shift.BEFORE, ordinal = 2), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void gui(Screen pGuiScreen, CallbackInfo info) {
        GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
    }
}
