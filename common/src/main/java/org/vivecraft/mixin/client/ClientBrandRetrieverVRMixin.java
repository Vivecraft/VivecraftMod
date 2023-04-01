package org.vivecraft.mixin.client;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.VRMixinConfig;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverVRMixin {
    @Inject(at = @At("RETURN"), method = "getClientModName", cancellable = true, remap = VRMixinConfig.remapIsForgeAvailable)
    private static void vivecraftClientBrand(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("vivecraft");
    }
}
