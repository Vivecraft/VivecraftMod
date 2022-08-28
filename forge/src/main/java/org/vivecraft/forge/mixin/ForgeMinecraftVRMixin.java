package org.vivecraft.forge.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.DataHolder;

@Mixin(Minecraft.class)
public class ForgeMinecraftVRMixin {

    @Inject(at = @At("HEAD"), method = "lambda$new$2", remap = false)
    public void menuInitvarforge(CallbackInfo ci) {
        if (DataHolder.getInstance().vrRenderer.isInitialized()) {
            //DataHolder.getInstance().menuWorldRenderer.init();
        }
        DataHolder.getInstance().vr.postinit();
    }

    @Inject(at = @At("HEAD"), method = "lambda$reloadResourcePacks$18", remap = false)
    public void reloadVarforge(CallbackInfo ci) {
//		if (DataHolder.getInstance().menuWorldRenderer.isReady() && DataHolder.getInstance().resourcePacksChanged) {
//			try {
//				DataHolder.getInstance().menuWorldRenderer.destroy();
//				DataHolder.getInstance().menuWorldRenderer.prepare();
//			} catch (Exception exception) {
//				exception.printStackTrace();
//			}
//		}
        DataHolder.getInstance().resourcePacksChanged = false;
    }
}
