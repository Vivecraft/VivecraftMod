package org.vivecraft.mixin.client.renderer.entity;

import org.vivecraft.client.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.client.extensions.RenderLayerExtension;
import org.vivecraft.client.utils.RenderLayerTypes.LayerType;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.vivecraft.client.utils.RenderLayerTypes.LayerType.*;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
* A hacky way of copying regular PlayerRenderer layers to the VRPlayerRenderers
* an alternative would be to add the VRPlayerRenderers to the skin model list,
* so mods could add it manually, but some mods hardcode only the slim/default model,
* and that would mean the VRPlayerRenderers would be missing those layers completely
* */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {

    // dummy constructor
    public PlayerRendererMixin(Context context, M entityModel, float f) {
        super(context, entityModel, f);
    }

    @Override
    public boolean addLayer(RenderLayer<T, M> renderLayer) {
        // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
        // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
        // those default Layers already are added to the VRPlayerRenderer there
        if ((Object)this.getClass() == PlayerRenderer.class && !((EntityRenderDispatcherExtension) mc.getEntityRenderDispatcher()).getSkinMapVRSeated().isEmpty()) {

            // try to find a suitable constructor, so we can create a new Object without issues
            Constructor<?> constructor = null;
            LayerType type = OTHER;
            for (Constructor<?> c : renderLayer.getClass().getConstructors()) {
                if (c.getParameterCount() == 1
                        && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])) {
                    constructor = c;
                    type = PARENT_ONLY;
                    break;
                } else if (c.getParameterCount() == 2
                        && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])
                        && EntityModelSet.class.isAssignableFrom(c.getParameterTypes()[1])) {
                    constructor = c;
                    type = PARENT_MODELSET;
                } else if (c.getParameterCount() == 3
                        && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])
                        && HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[1])
                        && HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[2])
                        && renderLayer instanceof HumanoidArmorLayer) {
                    constructor = c;
                    type = PARENT_MODEL_MODEL;
                }
            }

            // if no suitable constructor was found, use do a basic Object.clone call, and replace the parent of the copy
            if (constructor == null) {
                // do a hacky clone, and replace parent
                if (((PlayerModel<?>)this.model).slim) {
                    this.addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVRSeated().get("slim"));
                    this.addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVR().get("slim"));
                } else {
                    this.addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVRSeated().get("default"));
                    this.addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVR().get("default"));
                }
            } else {
                // make a new instance with the vr model as parent
                if (((PlayerModel<?>)this.model).slim) {
                    this.addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVRSeated().get("slim"));
                    this.addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVR().get("slim"));
                } else {
                    this.addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVRSeated().get("default"));
                    this.addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getSkinMapVR().get("default"));
                }
            }
        }
        return super.addLayer(renderLayer);
    }

    /**
     * does a basic Object.clone() copy
     */
    @Unique
    private void addLayerClone(RenderLayer<T, M> renderLayer, LivingEntityRenderer<T, M> target){
        try {
            logger.warn("Copying layer: {} with Object.copy, this could cause issues", renderLayer.getClass());
            RenderLayer<T, M> newLayer = (RenderLayer<T, M>)((RenderLayerExtension) renderLayer).clone();
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
    private void addLayerConstructor(Constructor<?> constructor, LayerType type, LivingEntityRenderer<T, M> target){
        try {
            switch (type) {
                case PARENT_ONLY -> target.addLayer((RenderLayer<T,M>) constructor.newInstance(target));
                case PARENT_MODELSET -> target.addLayer((RenderLayer<T,M>) constructor.newInstance(target, mc.getEntityModels()));
                case PARENT_MODEL_MODEL -> {
                    if (((PlayerModel<?>)this.model).slim)
                    {
                        target.addLayer((RenderLayer<T, M>) constructor.newInstance(
                            target,
                            new HumanoidModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                            new HumanoidModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR))
                        ));
                    }
                    else
                    {
                        target.addLayer((RenderLayer<T, M>) constructor.newInstance(
                            target,
                            new HumanoidModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                            new HumanoidModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))
                        ));
                    }
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
