package org.vivecraft.mod_compat_vr.sereneseasons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.FoliageColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "sereneseasons.init.ModClient")
public class ModClientMixin {

    /**
     * menuworld fix
     */
    @Inject(method = "lambda$registerBlockColors$0", at = @At("HEAD"), cancellable = true)
    private static void vivecraft$grassColor(CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().player == null) {
            cir.setReturnValue(FoliageColor.FOLIAGE_BIRCH);
        }
    }
}
