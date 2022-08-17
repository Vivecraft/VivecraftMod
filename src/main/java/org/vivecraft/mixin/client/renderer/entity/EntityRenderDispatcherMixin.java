package org.vivecraft.mixin.client.renderer.entity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.render.PlayerModelController;
import org.vivecraft.render.VRPlayerRenderer;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener{

	@Unique
	private final Map<String, VRPlayerRenderer> skinMapVR = new HashMap<>();
	@Unique
	private final Map<String, VRPlayerRenderer> skinMapVRSeated = new HashMap<>();
	@Unique
	private VRPlayerRenderer playerRendererVR;
	@Unique
	private VRPlayerRenderer playerRendererVRSeated;
	@Unique
	private EntityRendererProvider.Context context;
	@Shadow
	private Map<String, EntityRenderer<? extends Player>> playerRenderers;
	@Shadow
	private Map<EntityType<?>, EntityRenderer<?>> renderers;


	@Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
	public void renderer(Entity pEntity, CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> info) {
		if (pEntity instanceof AbstractClientPlayer) {
			String s = ((AbstractClientPlayer) pEntity).getModelName();
			PlayerRenderer playerrenderer = (PlayerRenderer) this.playerRenderers.get(s);
			PlayerModelController.RotInfo playermodelcontroller$rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(pEntity.getUUID());
			if (playermodelcontroller$rotinfo != null) {
				VRPlayerRenderer vrplayerrenderer1;
				if (playermodelcontroller$rotinfo.seated) {
					if (this.playerRendererVRSeated == null) {
						this.playerRendererVRSeated = new VRPlayerRenderer(this.context, false, true);
						this.skinMapVRSeated.put("default", this.playerRendererVRSeated);
						this.skinMapVRSeated.put("slim", new VRPlayerRenderer(this.context, true, true));
						//TODO: where gone?
						//:shrug:
						//PlayerItemsLayer.register(this.skinMapVRSeated);
					}

					VRPlayerRenderer vrplayerrenderer = this.skinMapVRSeated.get(s);

					if (vrplayerrenderer != null) {
						vrplayerrenderer1 = vrplayerrenderer;
					} else {
						vrplayerrenderer1 = this.playerRendererVRSeated;
					}
				} else {
					if (this.playerRendererVR == null) {
						this.playerRendererVR = new VRPlayerRenderer(this.context, false, false);
						this.skinMapVR.put("default", this.playerRendererVR);
						this.skinMapVR.put("slim", new VRPlayerRenderer(this.context, true, false));
						// PlayerItemsLayer.register(this.skinMapVR);
					}
					VRPlayerRenderer vrplayerrenderer2 = this.skinMapVR.get(s);
					if (vrplayerrenderer2 != null) {
						vrplayerrenderer1 = vrplayerrenderer2;
					} else {
						vrplayerrenderer1 = this.playerRendererVR;
					}
				}
				info.setReturnValue(vrplayerrenderer1);
			} else {
				EntityRenderer<? extends Player> entityrenderer = this.playerRenderers.get(s);
				if (entityrenderer != null) {
					info.setReturnValue((EntityRenderer<AbstractClientPlayer>) entityrenderer);
				} else {
					info.setReturnValue((EntityRenderer<AbstractClientPlayer>) this.playerRenderers.get("default"));
				}
			}
		}
		else {
			info.setReturnValue((EntityRenderer)this.renderers.get(pEntity.getType()));
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = Shift.AFTER), 
			method = "onResourceManagerReload", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void reload(ResourceManager p_174004_, CallbackInfo info, EntityRendererProvider.Context context) {
		this.context = context;
		//this.playerRenderers = new HashMap<>(this.playerRenderers);
		this.skinMapVRSeated.put("default", new VRPlayerRenderer(context, false, true));
		this.skinMapVRSeated.put("slim", new VRPlayerRenderer(context, true, true));
		this.skinMapVR.put("default", new VRPlayerRenderer(context, false, false));
		this.skinMapVR.put("slim", new VRPlayerRenderer(context, true, false));
	}
}
