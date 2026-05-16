package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
public class ItemRendererVRMixin {

// hand item fade
// needs custom item renderer, since the regular one doesn't accept a non 1.0 alpha
/*
    @Unique
    float vivecraft$fade = 1.0F;
    @Unique
    float vivecraft$manualFade = 1.0F;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER))
    private void vivecraft$fade(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        LocalPlayer localplayer = Minecraft.getInstance().player;
        this.vivecraft$fade = localplayer != null && ClientDataHolderVR.isfphand
                              ? SwingTracker.getItemFade(localplayer, itemStack)
                              : this.vivecraft$manualFade;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"), ordinal = 0)
    private RenderType vivecraft$rendertypeFade(RenderType rendertype) {
        if (ClientDataHolderVR.isfphand && this.vivecraft$fade < 1.0F) {
            return Sheets.translucentCullBlockSheet();
        }
        return rendertype;
    }
*/
}
