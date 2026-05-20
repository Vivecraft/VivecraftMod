package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = "net.optifine.shaders.ShadersRender")
public class ShadersRenderVRMixin {

    @Shadow
    public static void updateActiveRenderInfo(Camera activeRenderInfo, Minecraft mc, float partialTick) {}

    @Inject(method = {"renderHandTranslucent", "renderHand0"}, at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noTranslucentHandsInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderHandSolid", "renderHand1"}, at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noSolidHandsInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderShadowMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;"), cancellable = true)
    private static void vivecraft$shadowsOnlyOnce(
        CallbackInfo ci, @Local(argsOnly = true) Camera activeRenderInfo, @Local(argsOnly = true) float partialTick)
    {
        if (!RenderPassType.isVanilla() && !ClientDataHolderVR.getInstance().isFirstPass &&
            !ShadersHelper.isSlowMode())
        {
            RenderSystem.backupProjectionMatrix();
            updateActiveRenderInfo(activeRenderInfo, Minecraft.getInstance(), partialTick);
            OptifineHelper.setCameraShadow(new PoseStack(), activeRenderInfo, partialTick);
            RenderSystem.restoreProjectionMatrix();
            ci.cancel();
        }
    }
}
