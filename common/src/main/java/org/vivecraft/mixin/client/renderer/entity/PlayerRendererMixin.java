package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.extensions.RenderLayerExtension;
import org.vivecraft.client.render.VRPlayerRenderer;
import org.vivecraft.client.utils.RenderLayerType;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRendererMixin<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    protected PlayerRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @ModifyArg(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;acos(D)D"))
    private double vivecraft$fixFlicker(double acos) {
        // because of imprecision issues this can cause nans
        // is fixed in 1.21.4
        return Math.min(1.0, acos);
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
        RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer,
        CallbackInfoReturnable<Boolean> cir)
    {
        // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
        // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
        // those default Layers already are added to the VRPlayerRenderer there
        EntityRenderDispatcherExtension renderExtension = (EntityRenderDispatcherExtension) this.entityRenderDispatcher;
        if ((Object) this.getClass() == PlayerRenderer.class &&
            !renderExtension.vivecraft$getSkinMapVRVanilla().isEmpty())
        {

            // try to find a suitable constructor, so we can create a new Object without issues
            Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> constructor = null;
            RenderLayerType type = RenderLayerType.OTHER;
            for (Constructor<?> c : renderLayer.getClass().getConstructors()) {
                if (c.getParameterCount() == 1 && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])) {
                    constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                    type = RenderLayerType.PARENT_ONLY;
                    break;
                } else if (c.getParameterCount() == 2 &&
                    RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0]) &&
                    EntityModelSet.class.isAssignableFrom(c.getParameterTypes()[1]))
                {
                    constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                    type = RenderLayerType.PARENT_MODELSET;
                } else if (c.getParameterCount() == 3 &&
                    RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0]) &&
                    HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[1]) &&
                    HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[2]) &&
                    renderLayer instanceof HumanoidArmorLayer)
                {
                    constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                    type = RenderLayerType.PARENT_MODEL_MODEL;
                }
            }

            String modelType = this.model.slim ? "slim" : "default";

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
        RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer, VRPlayerRenderer target)
    {
        // only add layers once
        if (target.hasLayerType(renderLayer)) return;
        try {
            VRSettings.LOGGER.warn("Vivecraft: Copying layer: {} with Object.copy, this could cause issues",
                renderLayer.getClass());
            RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> newLayer = (RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) ((RenderLayerExtension) renderLayer).clone();
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
        RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer,
        Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> constructor,
        RenderLayerType type, VRPlayerRenderer target)
    {
        // only add layers once
        if (target.hasLayerType(renderLayer)) return;

        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

        try {
            switch (type) {
                case PARENT_ONLY -> target.addLayer(constructor.newInstance(target));
                case PARENT_MODELSET -> target.addLayer(constructor.newInstance(target, modelSet));
                case PARENT_MODEL_MODEL -> {
                    if (this.model.slim) {
                        target.addLayer(constructor.newInstance(target,
                            new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                            new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR))));
                    } else {
                        target.addLayer(constructor.newInstance(target,
                            new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                            new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
                    }
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
