package org.vivecraft.mixin.client.renderer.entity;

import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//TODO I don't think this does anything? Optifine
@Mixin(net.minecraft.client.renderer.entity.ItemRenderer.class)
public class ItemRendererVRMixin {

    @Unique
    float fade = 1.0F;
    @Unique
    float manualFade = 1.0F;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = Shift.AFTER), method = "render")
    public void fade(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        if (mc.player != null && dh.isfphand)
        {
            this.fade = dh.swingTracker.getItemFade(itemStack);
        }
        else
        {
            this.fade = this.manualFade;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"), method = "render")
    public RenderType rendertypeFade(ItemStack itemStack, boolean bl) {
        if (dh.isfphand && this.fade < 1.0F) {
            return Sheets.translucentCullBlockSheet();
        }
        return ItemBlockRenderTypes.getRenderType(itemStack, bl);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFII)V", shift = Shift.BY, by = -3), method = "renderQuadList", locals = LocalCapture.CAPTURE_FAILHARD)
    public void specialItems(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j, CallbackInfo ci, boolean bl, Pose pose, Iterator var9, BakedQuad bakedQuad, int k, float f, float g, float h) {
        if (dh.jumpTracker.isBoots(itemStack)) {
            k = 0x0100FF00; // 0xAARRGGBB
        }
        else if (dh.climbTracker.isClaws(itemStack)) {
            k = 0x0182004B; // 0xAARRGGBB
        }
        else if (TelescopeTracker.isLegacyTelescope(itemStack)) {
            k = 0x01BE6E87; // 0xAARRGGBB
        }
    }

}
