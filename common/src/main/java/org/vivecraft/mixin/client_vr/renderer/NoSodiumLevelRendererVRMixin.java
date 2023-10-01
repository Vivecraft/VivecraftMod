package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;

import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public class NoSodiumLevelRendererVRMixin {

    @Shadow
    @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Inject(at = @At("HEAD"), method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V")
    public void vivecraft$alwaysUpdateCull(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo info) {
        if (vrRunning) {
            // if VR is on, always update the frustum, to fix flickering chunks between eyes
            this.sectionOcclusionGraph.invalidate();
        }
    }

    @ModifyConstant(method = "renderSectionLayer", constant = @Constant(intValue = 12))
    public int vivecraft$moreTextures(int constant) {
        return RenderSystemAccessor.getShaderTextures().length;
    }
}
