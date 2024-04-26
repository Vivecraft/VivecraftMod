package org.vivecraft.mod_compat_vr.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.shadow.ShadowMatrices",
    "net.coderbot.iris.shadows.ShadowMatrices",
    "net.irisshaders.iris.shadows.ShadowMatrices"
})
public class IrisShadowMatricesMixin {

    @Unique
    private static float vivecraft$cachedShadowIntervalSize;
    @Unique
    private static Vec3 vivecraft$leftPass;
    @Unique
    private static Vec3 vivecraft$currentPass;


    // iris 1.4.2-
    @Group(name = "shadow interval", min = 1, max = 1)
    @Inject(target = @Desc(value = "snapModelViewToGrid", args = {Matrix4f.class, float.class, double.class, double.class, double.class}), at = @At("HEAD"), remap = false, expect = 0, require = 0)
    private static void vivecraft$cacheInterval(Matrix4f target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        vivecraft$cachedShadowIntervalSize = shadowIntervalSize;
    }

    // iris 1.4.3+
    @Group(name = "shadow interval", min = 1, max = 1)
    @Inject(target = @Desc(value = "snapModelViewToGrid", args = {PoseStack.class, float.class, double.class, double.class, double.class}), at = @At("HEAD"), remap = false, expect = 0, require = 0)
    private static void vivecraft$cacheInterval143(PoseStack target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        vivecraft$cachedShadowIntervalSize = shadowIntervalSize;
    }

    // offset camera pos, to be in the equal grid as left eye, but with correct offset
    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal = 1, remap = false)
    private static float vivecraft$modifyOffsetX(float original) {
        if (!RenderPassType.isVanilla()) {
            vivecraft$currentPass = RenderHelper.getSmoothCameraPosition(ClientDataHolderVR.getInstance().currentPass, ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld());
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
                vivecraft$leftPass = vivecraft$currentPass;
            }
            return (float) (vivecraft$leftPass.x % vivecraft$cachedShadowIntervalSize - (vivecraft$leftPass.x - vivecraft$currentPass.x));
        } else {
            return original;
        }
    }

    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal = 2, remap = false)
    private static float vivecraft$modifyOffsetY(float original) {
        if (!RenderPassType.isVanilla()) {
            return (float) (vivecraft$leftPass.y % vivecraft$cachedShadowIntervalSize - (vivecraft$leftPass.y - vivecraft$currentPass.y));
        } else {
            return original;
        }
    }

    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal = 3, remap = false)
    private static float vivecraft$modifyOffsetZ(float original) {
        if (!RenderPassType.isVanilla()) {
            return (float) (vivecraft$leftPass.z % vivecraft$cachedShadowIntervalSize - (vivecraft$leftPass.z - vivecraft$currentPass.z));
        } else {
            return original;
        }
    }
}
