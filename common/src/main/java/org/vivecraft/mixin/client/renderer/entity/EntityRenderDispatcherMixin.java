package org.vivecraft.mixin.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.render.VRPlayerRenderer;
import org.vivecraft.common.utils.Utils;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherExtension {

    @Unique
    private final Map<String, VRPlayerRenderer> vivecraft$skinMapVR = new HashMap<>();

    @Override
    public Map<String, VRPlayerRenderer> vivecraft$getSkinMapVR() {
        return vivecraft$skinMapVR;
    }

    @Unique
    private final Map<String, VRPlayerRenderer> vivecraft$skinMapVRSeated = new HashMap<>();

    @Override
    public Map<String, VRPlayerRenderer> vivecraft$getSkinMapVRSeated() {
        return vivecraft$skinMapVRSeated;
    }

    @Unique
    private VRPlayerRenderer vivecraft$playerRendererVR;
    @Unique
    private VRPlayerRenderer vivecraft$playerRendererVRSeated;


    @Inject(at = @At("HEAD"), method = "renderHitbox")
    private static void vivecraft$headHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f, float g, float h, float i, CallbackInfo ci) {
        AABB headBox;
        if ((headBox = Utils.getEntityHeadHitbox(entity, 0.0)) != null) {
            // raw head box
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, headBox.move(-entity.getX(), -entity.getY(), -entity.getZ()), 1.0f, 1.0f, 0.0f, 1.0f);
            // inflated head box for arrows
            AABB headBoxArrow = Utils.getEntityHeadHitbox(entity, 0.3);
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, headBoxArrow.move(-entity.getX(), -entity.getY(), -entity.getZ()), 1.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    @Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
    public void vivecraft$renderer(Entity pEntity, CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> info) {
        if (pEntity instanceof AbstractClientPlayer) {
            String s = ((AbstractClientPlayer) pEntity).getSkin().model().id();
            VRPlayersClient.RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(pEntity.getUUID());
            if (playermodelcontroller$rotinfo != null) {
                VRPlayerRenderer vrplayerrenderer;
                if (playermodelcontroller$rotinfo.seated) {

                    vrplayerrenderer = this.vivecraft$skinMapVRSeated.get(s);

                    if (vrplayerrenderer == null) {
                        vrplayerrenderer = this.vivecraft$playerRendererVRSeated;
                    }
                } else {
                    vrplayerrenderer = this.vivecraft$skinMapVR.get(s);
                    if (vrplayerrenderer == null) {
                        vrplayerrenderer = this.vivecraft$playerRendererVR;
                    }
                }

                info.setReturnValue(vrplayerrenderer);
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "onResourceManagerReload")
    public void vivecraft$reloadClear(ResourceManager resourceManager, CallbackInfo ci) {
        vivecraft$skinMapVRSeated.clear();
        vivecraft$skinMapVR.clear();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"),
        method = "onResourceManagerReload", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void vivecraft$reload(ResourceManager p_174004_, CallbackInfo info, EntityRendererProvider.Context context) {
        this.vivecraft$playerRendererVRSeated = new VRPlayerRenderer(context, false, true);
        this.vivecraft$skinMapVRSeated.put("default", vivecraft$playerRendererVRSeated);
        this.vivecraft$skinMapVRSeated.put("slim", new VRPlayerRenderer(context, true, true));

        this.vivecraft$playerRendererVR = new VRPlayerRenderer(context, false, false);
        this.vivecraft$skinMapVR.put("default", vivecraft$playerRendererVR);
        this.vivecraft$skinMapVR.put("slim", new VRPlayerRenderer(context, true, false));
    }
}
