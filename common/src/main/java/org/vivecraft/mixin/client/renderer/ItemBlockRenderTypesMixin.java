package org.vivecraft.mixin.client.renderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(ModelBlockRenderer.class)
public class ItemBlockRenderTypesMixin {
    // always use fancy leaves for menuworld
    @ModifyVariable(method = "forceOpaque", at = @At("HEAD"), argsOnly = true)
    private static boolean vivecraft$fancyLeavesForMenuWorld(boolean cutoutLeaves) {
        return cutoutLeaves || (ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()
        );
    }
}
