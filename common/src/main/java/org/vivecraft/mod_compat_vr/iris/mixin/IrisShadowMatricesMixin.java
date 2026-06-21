package org.vivecraft.mod_compat_vr.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.shadow.ShadowMatrices",
    "net.coderbot.iris.shadows.ShadowMatrices",
    "net.irisshaders.iris.shadows.ShadowMatrices"
})
public class IrisShadowMatricesMixin {

    // offset camera pos, to be in the equal grid as the first pass, but with correct offset
    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private static float vivecraft$modifyOffsetX(
        float xOffset, @Local(argsOnly = true) float shadowIntervalSize, @Share("curPos") LocalRef<Vec3> curPos)
    {
        if (!RenderPassType.isVanilla() && !ShadersHelper.isSlowMode()) {
            curPos.set(Minecraft.getInstance().gameRenderer.getMainCamera().position());
            return (float) (ShadersHelper.SHADOW_CAMERA_POSITION.x % shadowIntervalSize -
                (ShadersHelper.SHADOW_CAMERA_POSITION.x - curPos.get().x)
            );
        } else {
            return xOffset;
        }
    }

    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    private static float vivecraft$modifyOffsetY(
        float yOffset, @Local(argsOnly = true) float shadowIntervalSize, @Share("curPos") LocalRef<Vec3> curPos)
    {
        if (!RenderPassType.isVanilla() && !ShadersHelper.isSlowMode()) {
            return (float) (ShadersHelper.SHADOW_CAMERA_POSITION.y % shadowIntervalSize -
                (ShadersHelper.SHADOW_CAMERA_POSITION.y - curPos.get().y)
            );
        } else {
            return yOffset;
        }
    }

    @ModifyVariable(method = "snapModelViewToGrid", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private static float vivecraft$modifyOffsetZ(
        float zOffset, @Local(argsOnly = true) float shadowIntervalSize, @Share("curPos") LocalRef<Vec3> curPos)
    {
        if (!RenderPassType.isVanilla() && !ShadersHelper.isSlowMode()) {
            return (float) (ShadersHelper.SHADOW_CAMERA_POSITION.z % shadowIntervalSize -
                (ShadersHelper.SHADOW_CAMERA_POSITION.z - curPos.get().z)
            );
        } else {
            return zOffset;
        }
    }
}
