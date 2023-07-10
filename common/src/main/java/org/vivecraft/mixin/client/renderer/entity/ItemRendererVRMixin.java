package org.vivecraft.mixin.client.renderer.entity;

import org.vivecraft.client_vr.ClientDataHolderVR;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

import java.util.Iterator;
import java.util.List;

//TODO I don't think this does anything? Optifine
@Mixin(ItemRenderer.class)
public class ItemRendererVRMixin {

    @Unique
    float fade = 1.0F;
    @Unique
    float manualFade = 1.0F;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", shift = At.Shift.AFTER), method = "render")
    public void fade(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        LocalPlayer localplayer = Minecraft.getInstance().player;

        if (localplayer != null && ClientDataHolderVR.isfphand) {
            this.fade = SwingTracker.getItemFade(localplayer, itemStack);
        }
        else {
            this.fade = this.manualFade;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"), method = "render")
    public RenderType rendertypeFade(ItemStack itemStack, boolean bl) {
        if (ClientDataHolderVR.isfphand && this.fade < 1.0F) {
            return Sheets.translucentCullBlockSheet();
        }
        return ItemBlockRenderTypes.getRenderType(itemStack, bl);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFII)V", shift = At.Shift.BY, by = -3), method = "renderQuadList", locals = LocalCapture.CAPTURE_FAILHARD)
    public void specialItems(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j, CallbackInfo ci, boolean bl, PoseStack.Pose pose, Iterator var9, BakedQuad bakedQuad, int k, float f, float g, float h) {
        if (ClientDataHolderVR.getInstance().jumpTracker.isBoots(itemStack)) {
            k = this.makeColor(1, 0, 255, 0);
        }
        else if (ClientDataHolderVR.getInstance().climbTracker.isClaws(itemStack)) {
            k = this.makeColor(1, 130, 0, 75);
        }
        else if (TelescopeTracker.isLegacyTelescope(itemStack)) {
            k = this.makeColor(1, 190, 110, 135);
        }
    }

    private int makeColor(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }

}
