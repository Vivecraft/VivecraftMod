package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.extensions.RenderLayerExtension;
import org.vivecraft.client.render.VRPlayerRenderData;
import org.vivecraft.client.render.VRPlayerRenderer;
import org.vivecraft.client.utils.RenderLayerType;
import org.vivecraft.client.utils.ScaleHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin extends LivingEntityRendererMixin<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    protected AvatarRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("HEAD"))
    private void vivecraft$addRotInfo(
        Avatar entity, AvatarRenderState reusedState, float partialTick, CallbackInfo ci)
    {
        // don't do any animations for dummy players
        if (entity.getClass() == LocalPlayer.class || entity.getClass() == RemotePlayer.class) {
            ((EntityRenderStateExtension) reusedState).vivecraft$setRotInfo(
                ClientVRPlayers.getInstance().getRotationsForPlayer(entity.getUUID()));
        } else {
            ((EntityRenderStateExtension) reusedState).vivecraft$setRotInfo(null);
        }

        ((EntityRenderStateExtension) reusedState).vivecraft$setFirstPersonPlayer(
            VREffectsHelper.isFirstPersonPlayer(entity));

        ((EntityRenderStateExtension) reusedState).vivecraft$setTotalScale(
            ScaleHelper.getEntityEyeHeightScale(entity, partialTick));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void vivecraft$vrPlayerState(
        Avatar entity, AvatarRenderState reusedState, float partialTick, CallbackInfo ci)
    {
        // need to set this last, because it depends on the vanilla state
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) reusedState).vivecraft$getRotInfo();
        // don't do any animations for dummy players
        if (rotInfo != null) {
            ((EntityRenderStateExtension) reusedState).vivecraft$setVRRenderData(
                vivecraft$extractVRModelData(VREffectsHelper.isFirstPersonPlayer(entity), rotInfo, reusedState));
        } else {
            ((EntityRenderStateExtension) reusedState).vivecraft$setVRRenderData(null);
        }
    }

    @Unique
    private VRPlayerRenderData vivecraft$extractVRModelData(
        boolean isMainPlayer, ClientVRPlayers.RotInfo rotInfo, AvatarRenderState renderState)
    {
        boolean laying = renderState.swimAmount > 0.0F || renderState.isFallFlying;
        float layAmount = renderState.isFallFlying ? 1F : renderState.swimAmount;
        boolean swimming = (laying && renderState.isInWater) || renderState.isFallFlying;
        boolean noLowerBodyAnimation = swimming || rotInfo.fbtMode == FBTMode.ARMS_ONLY;

        float bodyYaw = isMainPlayer ? ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyYawRad() :
            rotInfo.getBodyYawRad();
        float xRot =
            swimming ? layAmount * (-Mth.HALF_PI - Mth.DEG_TO_RAD * renderState.xRot) : layAmount * -Mth.HALF_PI;

        float bodyScale = 1F;
        float armScale = 1F;
        float legScale = 1F;

        // this check is similar to VREffectsHelper#isFirstPersonEntityPass,
        // but does different stuff for shaders shadow pass
        if (isMainPlayer && ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal()) &&
            (!ShadersHelper.isRenderingShadows() &&
                RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass)
            ) ||
            (ShadersHelper.isRenderingShadows() && ClientDataHolderVR.getInstance().vrSettings.shaderFullSizeShadowLimbs
            ))
        {
            bodyScale = ClientDataHolderVR.getInstance().vrSettings.playerModelBodyScale;
            armScale = ClientDataHolderVR.getInstance().vrSettings.playerModelArmsScale;
            legScale = ClientDataHolderVR.getInstance().vrSettings.playerModelLegScale;
        }

        HumanoidArm attackArm = null;
        if (renderState.attackTime > 0F) {
            // we ignore the vanilla main arm setting
            attackArm = renderState.attackArm;
            if (rotInfo.leftHanded) {
                attackArm = attackArm.getOpposite();
            }
        }

        return new VRPlayerRenderData(isMainPlayer,
            bodyYaw, xRot,
            laying, layAmount, swimming, noLowerBodyAnimation,
            attackArm, rotInfo.leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT,
            bodyScale, armScale, legScale);
    }


    /**
     * A hacky way of copying regular PlayerRenderer layers to the VRPlayerRenderers
     * an alternative would be to add the VRPlayerRenderers to the skin model list,
     * so mods could add it manually, but some mods hardcode only the slim/default model,
     * and that would mean the VRPlayerRenderers would be missing those layers completely
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void vivecraft$onAddLayer(
        RenderLayer<AvatarRenderState, PlayerModel> renderLayer,
        CallbackInfoReturnable<Boolean> cir)
    {
        // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
        // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
        // those default Layers already are added to the VRPlayerRenderer there
        EntityRenderDispatcherExtension renderExtension = (EntityRenderDispatcherExtension) this.entityRenderDispatcher;
        if ((Object) this.getClass() == AvatarRenderer.class &&
            !renderExtension.vivecraft$getSkinMapVRVanilla().isEmpty())
        {

            // try to find a suitable constructor, so we can create a new Object without issues
            Constructor<RenderLayer<AvatarRenderState, PlayerModel>> constructor = null;
            RenderLayerType type = RenderLayerType.OTHER;
            for (Constructor<?> c : renderLayer.getClass().getConstructors()) {
                if (c.getParameterCount() == 1 && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])) {
                    constructor = (Constructor<RenderLayer<AvatarRenderState, PlayerModel>>) c;
                    type = RenderLayerType.PARENT_ONLY;
                    break;
                } else if (c.getParameterCount() == 2 &&
                    RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0]) &&
                    EntityModelSet.class.isAssignableFrom(c.getParameterTypes()[1]))
                {
                    constructor = (Constructor<RenderLayer<AvatarRenderState, PlayerModel>>) c;
                    type = RenderLayerType.PARENT_MODELSET;
                }
            }

            PlayerModelType modelType = this.model.slim ? PlayerModelType.SLIM : PlayerModelType.WIDE;

            // if no suitable constructor was found, use do a basic Object.clone call, and replace the parent of the copy
            if (constructor == null) {
                // do a hacky clone, and replace parent
                vivecraft$addLayerClone(renderLayer, renderExtension.vivecraft$getSkinMapVRVanilla().get(modelType));
                vivecraft$addLayerClone(renderLayer, renderExtension.vivecraft$getSkinMapVRArms().get(modelType));
                vivecraft$addLayerClone(renderLayer, renderExtension.vivecraft$getSkinMapVRLegs().get(modelType));
            } else {
                if (!constructor.canAccess(null)) {
                    // make sure the target class is accessible or this will error
                    VRSettings.LOGGER.warn("Vivecraft: layer constructor of '{}' was private, making it accessible",
                        renderLayer.getClass());
                    constructor.setAccessible(true);
                }
                // make a new instance with the vr model as parent
                vivecraft$addLayerConstructor(renderLayer, constructor, type,
                    renderExtension.vivecraft$getSkinMapVRVanilla().get(modelType));
                vivecraft$addLayerConstructor(renderLayer, constructor, type,
                    renderExtension.vivecraft$getSkinMapVRArms().get(modelType));
                vivecraft$addLayerConstructor(renderLayer, constructor, type,
                    renderExtension.vivecraft$getSkinMapVRLegs().get(modelType));
            }
        }
    }

    /**
     * does a basic Object.clone() copy
     */
    @SuppressWarnings("unchecked")
    @Unique
    private void vivecraft$addLayerClone(
        RenderLayer<AvatarRenderState, PlayerModel> renderLayer, VRPlayerRenderer target)
    {
        // only add layers once
        if (target.hasLayerType(renderLayer)) return;
        try {
            VRSettings.LOGGER.warn("Vivecraft: Copying layer: {} with Object.copy, this could cause issues",
                renderLayer.getClass());
            RenderLayer<AvatarRenderState, PlayerModel> newLayer = (RenderLayer<AvatarRenderState, PlayerModel>) ((RenderLayerExtension) renderLayer).clone();
            newLayer.renderer = target;
            target.addLayer(newLayer);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * uses the provided constructor, to create a new RenderLayer Instance
     */
    @Unique
    private void vivecraft$addLayerConstructor(
        RenderLayer<AvatarRenderState, PlayerModel> renderLayer,
        Constructor<RenderLayer<AvatarRenderState, PlayerModel>> constructor,
        RenderLayerType type, VRPlayerRenderer target)
    {
        // only add layers once
        if (target.hasLayerType(renderLayer)) return;

        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        try {
            switch (type) {
                case PARENT_ONLY -> target.addLayer(constructor.newInstance(target));
                case PARENT_MODELSET -> target.addLayer(constructor.newInstance(target, modelSet));
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
