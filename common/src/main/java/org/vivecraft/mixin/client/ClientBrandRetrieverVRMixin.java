package org.vivecraft.mixin.client;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverVRMixin {
    @Inject(at = @At("RETURN"), method = "getClientModName", cancellable = true)
    private static void vivecraftClientBrand(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("vivecraft");
    }
}
