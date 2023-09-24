package org.vivecraft.mod_compat_vr.iris.mixin;

import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {"net.coderbot.iris.shadow.ShadowMatrices", "net.coderbot.iris.shadows.ShadowMatrices"})
public class IrisShadowMatricesMixin {

    private static float cachedShadowIntervalSize;
    private static Vec3 leftPass;
    private static Vec3 currentPass;


    // iris 1.4.2-
    @Group(name = "shadow interval", min = 1, max = 1)
    @Inject(target = @Desc(value = "snapModelViewToGrid", args = {Matrix4f.class, float.class, double.class, double.class, double.class}), at = @At("HEAD"), remap = false, expect = 0, require = 0)
    private static void cacheInterval(Matrix4f target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        cachedShadowIntervalSize = shadowIntervalSize;
    }

    // iris 1.4.3+
    @Group(name = "shadow interval", min = 1, max = 1)
    @Inject(target = @Desc(value = "snapModelViewToGrid", args = {PoseStack.class, float.class, double.class, double.class, double.class}), at = @At("HEAD"), remap = false, expect = 0, require = 0)
    private static void cacheInterval143(PoseStack target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        cachedShadowIntervalSize = shadowIntervalSize;
    }

    // offset camera pos, to be in the equal grid as left eye, but with correct offset
    @ModifyVariable( method = "snapModelViewToGrid", at = @At("STORE"), ordinal  = 1, remap = false)
    private static float modifyOffsetX(float original){
        if (!RenderPassType.isVanilla()) {
            currentPass = dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPosition();
            if (dh.currentPass == RenderPass.LEFT) {
                leftPass = currentPass;
            }
            return (float) (leftPass.x % cachedShadowIntervalSize - (leftPass.x - currentPass.x));
        } else {
            return original;
        }
    }
    @ModifyVariable( method = "snapModelViewToGrid", at = @At("STORE"), ordinal  = 2, remap = false)
    private static float modifyOffsetY(float original){
        if (!RenderPassType.isVanilla()) {
            return (float) (leftPass.y % cachedShadowIntervalSize - (leftPass.y - currentPass.y));
        } else {
            return original;
        }
    }
    @ModifyVariable( method = "snapModelViewToGrid", at = @At("STORE"), ordinal  = 3, remap = false)
    private static float modifyOffsetZ(float original){
        if (!RenderPassType.isVanilla()) {
            return (float) (leftPass.z % cachedShadowIntervalSize - (leftPass.z - currentPass.z));
        } else {
            return original;
        }
    }
}
