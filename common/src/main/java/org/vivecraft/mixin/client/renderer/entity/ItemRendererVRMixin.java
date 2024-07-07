package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

@Mixin(ItemRenderer.class)
public class ItemRendererVRMixin {

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @ModifyVariable(at = @At(value = "STORE"), method = "getModel")
    public BakedModel vivecraft$modelOverride(BakedModel bakedModel, ItemStack itemStack) {
        if (VRState.vrRunning && itemStack.is(Items.SPYGLASS)) {
            return itemModelShaper.getModelManager().getModel(TelescopeTracker.scopeModel);
        }
        if (ClientDataHolderVR.getInstance().climbTracker.isClaws(itemStack)) {
            return itemModelShaper.getModelManager().getModel(ClimbTracker.clawsModel);
        }
        return bakedModel;
    }

// hand item fade
// needs custom item renderer, since the regular one doesn't accept a non 1.0 alpha
/*
    @Unique
    float vivecraft$fade = 1.0F;
    @Unique
    float vivecraft$manualFade = 1.0F;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER), method = "render")
    public void vivecraft$fade(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        LocalPlayer localplayer = Minecraft.getInstance().player;
        this.vivecraft$fade = localplayer != null && ClientDataHolderVR.isfphand
                              ? SwingTracker.getItemFade(localplayer, itemStack)
                              : this.vivecraft$manualFade;
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;"), ordinal = 0, method = "render")
    public RenderType vivecraft$rendertypeFade(RenderType rendertype) {
        if (ClientDataHolderVR.isfphand && this.vivecraft$fade < 1.0F) {
            return Sheets.translucentCullBlockSheet();
        }
        return rendertype;
    }
*/

// Color vivecraft items, this clashes with old sodium
/*
    @ModifyVariable(at = @At(value = "LOAD", ordinal = 0), ordinal = 2, method = "renderQuadList")
    public int vivecraft$specialItems(int color, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack) {
        if (ClientDataHolderVR.getInstance().jumpTracker.isBoots(itemStack)) {
            return this.vivecraft$makeColor(1, 0, 255, 0);
        } else if (ClientDataHolderVR.getInstance().climbTracker.isClaws(itemStack)) {
            return this.vivecraft$makeColor(1, 130, 0, 75);
        } else if (TelescopeTracker.isLegacyTelescope(itemStack)) {
            return this.vivecraft$makeColor(1, 190, 110, 135);
        }
        return color;
    }

    @Unique
    private int vivecraft$makeColor(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }
*/
}
