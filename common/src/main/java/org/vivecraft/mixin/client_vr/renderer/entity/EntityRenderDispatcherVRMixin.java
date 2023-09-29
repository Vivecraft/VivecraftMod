package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.HashMap;
import java.util.Map;

import static org.joml.Math.asin;
import static org.joml.Math.atan2;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;


@Mixin(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherVRMixin implements
    net.minecraft.server.packs.resources.ResourceManagerReloadListener,
    org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension {

    @Unique
    private VRArmRenderer vivecraft$armRenderer;
    @Unique
    public final Map<String, VRArmRenderer> vivecraft$armSkinMap = new HashMap<>();
    @Shadow
    public net.minecraft.client.Camera camera;
    @Shadow
    private Quaternionf cameraOrientation;

    @Inject(at = @At("HEAD"), method = "cameraOrientation", cancellable = true)
    public void vivecraft$cameraOrientation(CallbackInfoReturnable<Quaternionf> cir) {
        if (RenderPassType.isVanilla() || RenderPassType.isGuiOnly()) {
            cir.setReturnValue(this.cameraOrientation);
        } else {
            Entity entity = ((LevelRendererExtension) mc.levelRenderer).vivecraft$getRenderedEntity();
            if (entity == null) {
                cir.setReturnValue(this.camera.rotation());
            } else {
                Vec3 vec3 = dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
                if (dh.currentPass == RenderPass.THIRD || dh.currentPass == RenderPass.CAMERA) {
                    vec3 = dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPosition();
                }
                Vec3 vec31 = entity.position().add(0.0D, entity.getBbHeight() / 2.0F, 0.0D).subtract(vec3).normalize();
                cir.setReturnValue(new Quaternionf()
                    .mul(new Quaternionf().rotationY((float) -atan2(-vec31.x, vec31.z)))
                    .mul(new Quaternionf().rotationX((float) -asin(vec31.y / vec31.length())))
                );
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = Shift.AFTER),
        method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void vivecraft$reload(ResourceManager resourceManager, CallbackInfo ci, Context context) {
        this.vivecraft$armRenderer = new VRArmRenderer(context, false);
        this.vivecraft$armSkinMap.put("default", this.vivecraft$armRenderer);
        this.vivecraft$armSkinMap.put("slim", new VRArmRenderer(context, true));
    }

    @Override
    @Unique
    public Quaternionf vivecraft$getCameraOrientationOffset(float offset) {
        if (RenderPassType.isVanilla() || RenderPassType.isGuiOnly()) {
            return this.cameraOrientation;
        } else {
            Entity entity = ((LevelRendererExtension) mc.levelRenderer).vivecraft$getRenderedEntity();
            if (entity == null) {
                return this.camera.rotation();
            } else {
                Vec3 vec3 = dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
                if (dh.currentPass == RenderPass.THIRD || dh.currentPass == RenderPass.CAMERA) {
                    vec3 = dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPosition();
                }
                Vec3 vec31 = entity.position().add(0.0D, entity.getBbHeight() + offset, 0.0D).subtract(vec3).normalize();
                return (new Quaternionf()
                    .mul(new Quaternionf().rotationY((float) -atan2(-vec31.x, vec31.z)))
                    .mul(new Quaternionf().rotationX((float) -asin(vec31.y / vec31.length())))
                );
            }
        }
    }

    @Override
    public Map<String, VRArmRenderer> vivecraft$getArmSkinMap() {
        return this.vivecraft$armSkinMap;
    }
}
