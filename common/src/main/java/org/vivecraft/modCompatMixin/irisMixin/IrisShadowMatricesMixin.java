package org.vivecraft.modCompatMixin.irisMixin;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.shadow.ShadowMatrices;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.render.RenderPass;

@Pseudo
@Mixin(ShadowMatrices.class)
public class IrisShadowMatricesMixin {

    private static float cachedShadowIntervalSize;
    private static Vec3 leftPass;
    private static Vec3 currentPass;

    @Inject(method = "snapModelViewToGrid", at = @At("HEAD"))
    private static void cacheInterval(Matrix4f target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        cachedShadowIntervalSize = shadowIntervalSize;
    }

    // offset camera pos, to be in the equal grid as left eye, but with correct offset
    @ModifyVariable( method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal  = 1)
    private static float modifyOffsetX(float original){
        currentPass = ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolder.getInstance().currentPass).getPosition();
        if (ClientDataHolder.getInstance().currentPass == RenderPass.LEFT) {
            leftPass = currentPass;
        }
        return (float) (leftPass.x % cachedShadowIntervalSize - (leftPass.x - currentPass.x));
    }
    @ModifyVariable( method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal  = 2)
    private static float modifyOffsetY(float original){
        return (float) (leftPass.y % cachedShadowIntervalSize - (leftPass.y - currentPass.y));
    }
    @ModifyVariable( method = "snapModelViewToGrid", at = @At(value = "STORE"), ordinal  = 3)
    private static float modifyOffsetZ(float original){
        return (float) (leftPass.z % cachedShadowIntervalSize - (leftPass.z - currentPass.z));
    }
}
