package org.vivecraft.mixin.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.SparkParticleExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

import java.util.UUID;
import java.util.stream.Stream;

@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle")
public class SparkParticleMixin implements SparkParticleExtension {
    @Unique
    private UUID vivecraft$playerUUID;

    @Inject(method = "extract", at = @At("HEAD"), cancellable = true)
    private void vivecraft$hideSelfButtSparkles(
        QuadParticleRenderState quadParticleRenderState, Camera camera, float f, CallbackInfo ci)
    {
        if (!ClientDataHolderVR.getInstance().vrSettings.selfButtSparklesInFirstPerson &&
            camera.entity().getUUID().equals(this.vivecraft$playerUUID) &&
            ((!VRState.VR_RUNNING && !camera.isDetached()) || (VRState.VR_RUNNING &&
                Stream.of(RenderPass.LEFT, RenderPass.RIGHT, RenderPass.CENTER)
                    .anyMatch(pass -> ClientDataHolderVR.getInstance().currentPass == pass)
            )
            ))
        {
            ci.cancel();
        }
    }

    @Override
    @Unique
    public void vivecraft$setPlayerUUID(UUID playerUUID) {
        this.vivecraft$playerUUID = playerUUID;
    }
}
