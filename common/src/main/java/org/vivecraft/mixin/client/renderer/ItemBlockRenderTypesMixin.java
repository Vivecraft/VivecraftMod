package org.vivecraft.mixin.client.renderer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {
    // always use fancy leaves for menu world
    @Redirect(method = "getChunkRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;solid()Lnet/minecraft/client/renderer/RenderType;", ordinal = 0))
    private static RenderType vivecraft$leavesRenderTypeOverride() {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            return RenderType.cutoutMipped();
        }
        return RenderType.solid();
    }
}
