package org.vivecraft.mixin.client_vr.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleVRMixin {

    @Final
    @Shadow
    private Entity target;

    @Shadow
    protected double targetX;

    @Shadow
    protected double targetY;

    @Shadow
    protected double targetZ;

    @Shadow
    @Final
    protected EntityRenderState itemRenderState;

    @Inject(method = "updatePosition", at = @At("HEAD"), cancellable = true)
    private void vivecraft$updateX(CallbackInfo ci) {
        if (VRState.VR_RUNNING && this.target == Minecraft.getInstance().player) {
            Vec3 pos = RenderHelper.getControllerRenderPos(0);
            this.targetX = pos.x;
            this.targetY = pos.y - this.itemRenderState.boundingBoxHeight;
            this.targetZ = pos.z;
            ci.cancel();
        }
    }
}
