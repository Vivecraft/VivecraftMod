package org.vivecraft.mixin.client.renderer.entity;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.render.VRPlayerRenderer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherExtension {

	@Unique
	private final Map<String, VRPlayerRenderer> skinMapVR = new HashMap<>();

	public Map<String, VRPlayerRenderer> getSkinMapVR() {
		return skinMapVR;
	}

	@Unique
	private final Map<String, VRPlayerRenderer> skinMapVRSeated = new HashMap<>();

	public Map<String, VRPlayerRenderer> getSkinMapVRSeated() {
		return skinMapVRSeated;
	}
	@Unique
	private VRPlayerRenderer playerRendererVR;
	@Unique
	private VRPlayerRenderer playerRendererVRSeated;


	@Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
	public void renderer(Entity pEntity, CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> info) {
		if (pEntity instanceof AbstractClientPlayer) {
			String s = ((AbstractClientPlayer) pEntity).getModelName();
			VRPlayersClient.RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(pEntity.getUUID());
			if (playermodelcontroller$rotinfo != null) {
				VRPlayerRenderer vrplayerrenderer;
				if (playermodelcontroller$rotinfo.seated) {

					vrplayerrenderer = this.skinMapVRSeated.get(s);

					if (vrplayerrenderer == null) {
						vrplayerrenderer = this.playerRendererVRSeated;
					}
				} else {
					vrplayerrenderer = this.skinMapVR.get(s);
					if (vrplayerrenderer == null) {
						vrplayerrenderer = this.playerRendererVR;
					}
				}

				info.setReturnValue(vrplayerrenderer);
			}
		}
	}

	@Inject(at = @At(value = "HEAD"),method = "onResourceManagerReload")
	public void reloadClear(ResourceManager resourceManager, CallbackInfo ci) {
		skinMapVRSeated.clear();
		skinMapVR.clear();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = Shift.AFTER),
			method = "onResourceManagerReload", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void reload(ResourceManager p_174004_, CallbackInfo info, EntityRendererProvider.Context context) {
		this.playerRendererVRSeated = new VRPlayerRenderer(context, false, true);
		this.skinMapVRSeated.put("default", playerRendererVRSeated);
		this.skinMapVRSeated.put("slim", new VRPlayerRenderer(context, true, true));

		this.playerRendererVR = new VRPlayerRenderer(context, false, false);
		this.skinMapVR.put("default", playerRendererVR);
		this.skinMapVR.put("slim", new VRPlayerRenderer(context, true, false));
	}
}
