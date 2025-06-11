package org.vivecraft.mixin.client_vr.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.data.ItemTags;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererVRMixin {

    @WrapOperation(method = "getPlayerHandPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 0))
    private float vivecraft$fishingLineStartRotation(
        float delta, float start, float end, Operation<Float> original, @Local(argsOnly = true) Player player)
    {
        ClientVRPlayers.RotInfo info;
        if (player != Minecraft.getInstance().player && ClientVRPlayers.getInstance().isVRPlayer(player) &&
            (info = ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID())).seated)
        {
            // other players in seated mode
            return Mth.RAD_TO_DEG * info.getBodyYawRad();
        } else {
            return original.call(delta, start, end);
        }
    }

    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void vivecraft$fishingLineStart(
        CallbackInfoReturnable<Vec3> cir, @Local(argsOnly = true) Player player,
        @Local(argsOnly = true, ordinal = 1) float partialTick)
    {
        boolean mainHandFishingRod = player.getMainHandItem().getItem() instanceof FishingRodItem ||
            player.getMainHandItem().is(ItemTags.VIVECRAFT_FISHING_RODS);
        ClientVRPlayers.RotInfo info;
        if (!RenderPassType.isVanilla() && player == Minecraft.getInstance().player) {
            // own player
            int c = mainHandFishingRod ? 0 : 1;
            Vec3 aimSource = RenderHelper.getControllerRenderPos(c);
            Vector3f aimDirection = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getHand(c)
                .getDirection();
            aimDirection.mul(0.47F * ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.worldScale);

            cir.setReturnValue(aimSource.add(aimDirection.x, aimDirection.y, aimDirection.z));
        } else if (ClientVRPlayers.getInstance().isVRPlayer(player) &&
            !(info = ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID())).seated)
        {
            // other players in standing mode
            Vector3fc aimSource = mainHandFishingRod ? info.mainHandPos : info.offHandPos;
            // just set it to the hand, everything else looks silly
            cir.setReturnValue(player.getPosition(partialTick).add(aimSource.x(), aimSource.y(), aimSource.z()));
        }
    }
}
