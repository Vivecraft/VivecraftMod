package org.vivecraft.mixin.client.renderer.entity;

import com.mojang.math.Axis;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.extensions.LevelRendererExtension;
import org.joml.Quaternionf;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRArmRenderer;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherVRMixin implements ResourceManagerReloadListener, EntityRenderDispatcherVRExtension {

    @Unique
    private VRArmRenderer armRenderer;
    @Unique
    public final Map<String, VRArmRenderer> armSkinMap = new HashMap<>();
    @Shadow
    public Camera camera;
    @Shadow
    private Quaternionf cameraOrientation;

    @Inject(at = @At("HEAD"), method = "cameraOrientation", cancellable = true)
    public void cameraOrientation(CallbackInfoReturnable<Quaternionf> cir) {
        if (ClientDataHolder.getInstance().currentPass == RenderPass.GUI) {
            cir.setReturnValue(cameraOrientation);
            return;
        }
        else {
            Entity entity = ((LevelRendererExtension)Minecraft.getInstance().levelRenderer).getRenderedEntity();
            if (entity == null) {
                cir.setReturnValue(this.camera.rotation());
                return;
            }
            else {
                Vec3 vec3 = ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
                if (ClientDataHolder.getInstance().currentPass == RenderPass.THIRD || ClientDataHolder.getInstance().currentPass == RenderPass.CAMERA) {
                    vec3 = ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolder.getInstance().currentPass).getPosition();
                }
                Vec3 vec31 = entity.position().add(0.0D, (double)(entity.getBbHeight() / 2.0F), 0.0D).subtract(vec3).normalize();
                Quaternionf q = new Quaternionf();
                q.mul(Axis.YP.rotationDegrees((float) (-Math.toDegrees(Math.atan2(-vec31.x, vec31.z)))));
                q.mul(Axis.XP.rotationDegrees((float) (-Math.toDegrees(Math.asin(vec31.y / vec31.length())))));
                cir.setReturnValue(q);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = At.Shift.AFTER),
            method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void reload(ResourceManager resourceManager, CallbackInfo ci, EntityRendererProvider.Context context) {
        this.armRenderer = new VRArmRenderer(context, false);
        this.armSkinMap.put("default", this.armRenderer);
        this.armSkinMap.put("slim", new VRArmRenderer(context, true));
    }

    @Override
    public Quaternionf getCameraOrientationOffset(float offset) {
        if (ClientDataHolder.getInstance().currentPass == RenderPass.GUI) {
            return this.camera.rotation();
        } else {
            Entity entity = ((LevelRendererExtension)Minecraft.getInstance().levelRenderer).getRenderedEntity();
            if (entity == null) {
                return this.camera.rotation();
            } else {
                Vec3 vec3 = ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition();
                if (ClientDataHolder.getInstance().currentPass == RenderPass.THIRD || ClientDataHolder.getInstance().currentPass == RenderPass.CAMERA) {
                    vec3 = ClientDataHolder.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolder.getInstance().currentPass).getPosition();
                }
                Vec3 vec31 = entity.position().add(0.0D, (double) (entity.getBbHeight() + offset), 0.0D).subtract(vec3).normalize();
                Quaternionf q = new Quaternionf();
                q.mul(Axis.YP.rotationDegrees((float) (-Math.toDegrees(Math.atan2(-vec31.x, vec31.z)))));
                q.mul(Axis.XP.rotationDegrees((float) (-Math.toDegrees(Math.asin(vec31.y / vec31.length())))));
                return q;
            }
        }
    }

    public Map<String, VRArmRenderer> getArmSkinMap() {
        return armSkinMap;
    }
}
