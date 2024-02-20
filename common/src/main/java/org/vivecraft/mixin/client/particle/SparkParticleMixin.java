package org.vivecraft.mixin.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.SparkParticleExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.RenderPass;

import java.util.UUID;
import java.util.stream.Stream;

@Mixin(targets = "net.minecraft.client.particle.FireworkParticles$SparkParticle")
public class SparkParticleMixin implements SparkParticleExtension {
    @Unique
    private UUID vivecraft$playerUUID;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void vivecraft$hideSelfButtSparkles(VertexConsumer vertexConsumer, Camera camera, float f, CallbackInfo ci) {
        if (!ClientDataHolderVR.getInstance().vrSettings.selfButtSparklesInFirstPerson
            && camera.getEntity().getUUID().equals(this.vivecraft$playerUUID) && ((!VRState.vrRunning && !camera.isDetached())
            || (VRState.vrRunning && Stream.of(RenderPass.LEFT, RenderPass.RIGHT, RenderPass.CENTER).anyMatch(pass -> ClientDataHolderVR.getInstance().currentPass == pass)))) {
            ci.cancel();
        }
    }

    @Override
    public void vivecraft$setPlayerUUID(UUID playerUUID) {
        this.vivecraft$playerUUID = playerUUID;
    }
}
