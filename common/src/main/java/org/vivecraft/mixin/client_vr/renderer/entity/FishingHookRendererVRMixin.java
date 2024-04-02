package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererVRMixin extends EntityRenderer<FishingHook> {

    // dummy constructor
    protected FishingHookRendererVRMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(at = @At("HEAD"), method = "getPlayerHandPos", cancellable = true)
    private static void vivecraft$getVRHandPos(Player player, float f, Item item, EntityRenderDispatcher entityRenderDispatcher, CallbackInfoReturnable<Vec3> cir) {
        if (!RenderPassType.isVanilla() && (entityRenderDispatcher.options == null || entityRenderDispatcher.options.getCameraType().isFirstPerson()) && player == Minecraft.getInstance().player) {
            int c = 1;
            if (player.getMainHandItem().is(item)) {
                c = 0;
            }
            Vec3 handPos = RenderHelper.getControllerRenderPos(c);
            Vec3 rodOffset = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getHand(c).getDirection();
            cir.setReturnValue(handPos.add(rodOffset.scale(0.47 * ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.worldScale)));
        }
    }
}
