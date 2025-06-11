package org.vivecraft.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {
    // always use fancy leaves for menuworld
    @ModifyExpressionValue(method = "getChunkRenderType", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;renderCutout:Z"))
    private static boolean vivecraft$fancyLeavesForMenuWorld(boolean original) {
        return original || (ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()
        );
    }
}
