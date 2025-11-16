package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;

@Mixin(LevelRenderer.class)
public class NoSodiumLevelRendererVRMixin {

    @Shadow
    @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Inject(method = "cullTerrain", at = @At("HEAD"))
    private void vivecraft$alwaysUpdateCull(CallbackInfo ci, @Local(argsOnly = true) Camera camera) {
        if (VRState.VR_RUNNING) {
            // if VR is on, always update the frustum, to fix flickering chunks between eyes
            this.sectionOcclusionGraph.invalidate();
            ((SectionOcclusionGraphAccessor) this.sectionOcclusionGraph).getNeedsFrustumUpdate().set(true);
        }
    }
}
