package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.render.VRPlayerRenderer;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherExtension {
    @Unique
    private final Map<PlayerModelType, VRPlayerRenderer> vivecraft$skinMapVRVanilla = new HashMap<>();

    @Unique
    private final Map<PlayerModelType, VRPlayerRenderer> vivecraft$skinMapVRArms = new HashMap<>();

    @Unique
    private final Map<PlayerModelType, VRPlayerRenderer> vivecraft$skinMapVRLegs = new HashMap<>();

    @Unique
    private VRPlayerRenderer vivecraft$playerRendererVRVanilla;

    @Unique
    private VRPlayerRenderer vivecraft$playerRendererVRArms;

    @Unique
    private VRPlayerRenderer vivecraft$playerRendererVRLegs;

    @Override
    public Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRVanilla() {
        return this.vivecraft$skinMapVRVanilla;
    }

    @Override
    public Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRArms() {
        return this.vivecraft$skinMapVRArms;
    }

    @Override
    public Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRLegs() {
        return this.vivecraft$skinMapVRLegs;
    }

    @Inject(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
    private void vivecraft$getVRPlayerRenderer(
        Entity entity, CallbackInfoReturnable<EntityRenderer> cir)
    {
        // don't do any animations for dummy players
        if (entity instanceof AbstractClientPlayer player &&
            (player.getClass() == LocalPlayer.class || player.getClass() == RemotePlayer.class))
        {
            if (ClientVRPlayers.getInstance().isVRPlayer(player)) {
                cir.setReturnValue(
                    vivecraft$getVRRenderer(player.getSkin().model(),
                        ClientVRPlayers.getInstance().isVRAndSeated(player.getUUID())));
            }
        }
    }

    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0), cancellable = true)
    private void vivecraft$getVRPlayerRenderer(
        CallbackInfoReturnable<EntityRenderer> cir, @Local AvatarRenderState playerRenderState)
    {
        // don't do any animations for dummy players
        if (((EntityRenderStateExtension) playerRenderState).vivecraft$getRotInfo() != null) {
            cir.setReturnValue(vivecraft$getVRRenderer(playerRenderState.skin.model(),
                ((EntityRenderStateExtension) playerRenderState).vivecraft$getRotInfo().seated));
        }
    }

    @Unique
    private VRPlayerRenderer vivecraft$getVRRenderer(PlayerModelType skinType, boolean seated) {
        VRPlayerRenderer vrPlayerRenderer;
        if (seated ||
            ClientDataHolderVR.getInstance().vrSettings.playerModelType == VRSettings.PlayerModelType.VANILLA)
        {
            vrPlayerRenderer = this.vivecraft$skinMapVRVanilla.getOrDefault(skinType,
                this.vivecraft$playerRendererVRVanilla);
        } else if (ClientDataHolderVR.getInstance().vrSettings.playerModelType ==
            VRSettings.PlayerModelType.SPLIT_ARMS)
        {
            vrPlayerRenderer = this.vivecraft$skinMapVRArms.getOrDefault(skinType,
                this.vivecraft$playerRendererVRArms);
        } else {
            vrPlayerRenderer = this.vivecraft$skinMapVRLegs.getOrDefault(skinType,
                this.vivecraft$playerRendererVRLegs);
        }
        return vrPlayerRenderer;
    }

    @Inject(method = "onResourceManagerReload", at = @At(value = "HEAD"))
    private void vivecraft$clearVRPlayerRenderer(CallbackInfo ci) {
        this.vivecraft$skinMapVRVanilla.clear();
        this.vivecraft$skinMapVRArms.clear();
        this.vivecraft$skinMapVRLegs.clear();
    }

    @Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createAvatarRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", ordinal = 0))
    private void vivecraft$reloadVRPlayerRenderer(CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        this.vivecraft$playerRendererVRVanilla = new VRPlayerRenderer(context, false,
            VRPlayerRenderer.ModelType.VANILLA);
        this.vivecraft$skinMapVRVanilla.put(PlayerModelType.WIDE, this.vivecraft$playerRendererVRVanilla);
        this.vivecraft$skinMapVRVanilla.put(PlayerModelType.SLIM,
            new VRPlayerRenderer(context, true, VRPlayerRenderer.ModelType.VANILLA));

        this.vivecraft$playerRendererVRArms = new VRPlayerRenderer(context, false,
            VRPlayerRenderer.ModelType.SPLIT_ARMS);
        this.vivecraft$skinMapVRArms.put(PlayerModelType.WIDE, this.vivecraft$playerRendererVRArms);
        this.vivecraft$skinMapVRArms.put(PlayerModelType.SLIM, new VRPlayerRenderer(context, true,
            VRPlayerRenderer.ModelType.SPLIT_ARMS));

        this.vivecraft$playerRendererVRLegs = new VRPlayerRenderer(context, false,
            VRPlayerRenderer.ModelType.SPLIT_ARMS_LEGS);
        this.vivecraft$skinMapVRLegs.put(PlayerModelType.WIDE, this.vivecraft$playerRendererVRLegs);
        this.vivecraft$skinMapVRLegs.put(PlayerModelType.SLIM,
            new VRPlayerRenderer(context, true, VRPlayerRenderer.ModelType.SPLIT_ARMS_LEGS));
    }
}
