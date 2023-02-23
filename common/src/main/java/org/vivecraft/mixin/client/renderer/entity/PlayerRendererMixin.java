package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.extensions.RenderLayerExtension;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.RenderLayerTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.vivecraft.utils.RenderLayerTypes.LayerType.*;

/**
* A hacky way of copying regular PlayerRenderer layers to the VRPlayerRenderers
* an alternative would be to add the VRPlayerRenderers to the skin model list,
* so mods could add it manually, but some mods hardcode only the slim/default model,
* and that would mean the VRPlayerRenderers would be missing those layers completely
* */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {

    // dummy constructor
    public PlayerRendererMixin(EntityRendererProvider.Context context, M entityModel, float f) {
        super(context, entityModel, f);
    }

    @Override
    public boolean addLayer(RenderLayer<T, M> renderLayer) {
        // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
        // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
        // those default Layers already are added to the VRPlayerRenderer there
        if ((Object)this.getClass() == PlayerRenderer.class && !((EntityRenderDispatcherExtension) Minecraft.getInstance().getEntityRenderDispatcher()).getSkinMapVRSeated().isEmpty()) {

            // try to find a suitable constructor, so we can create a new Object without issues
            Constructor<?> constructor = null;
            RenderLayerTypes.LayerType type = OTHER;
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
                if (((PlayerModel<?>) model).slim) {
                    addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVRSeated().get("slim"));
                    addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVR().get("slim"));
                } else {
                    addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVRSeated().get("default"));
                    addLayerClone(renderLayer, (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVR().get("default"));
                }
            } else {
                // make a new instance with the vr model as parent
                if (((PlayerModel<?>) model).slim) {
                    addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVRSeated().get("slim"));
                    addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVR().get("slim"));
                } else {
                    addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVRSeated().get("default"));
                    addLayerConstructor(constructor, type, (LivingEntityRenderer<T, M>)((EntityRenderDispatcherExtension) entityRenderDispatcher).getSkinMapVR().get("default"));
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
            VRSettings.logger.warn("Copying layer: {} with Object.copy, this could cause issues", renderLayer.getClass());
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
    private void addLayerConstructor(Constructor<?> constructor, RenderLayerTypes.LayerType type, LivingEntityRenderer<T, M> target){
        try {
            switch (type) {
                case PARENT_ONLY -> target.addLayer((RenderLayer<T,M>) constructor.newInstance(target));
                case PARENT_MODELSET -> target.addLayer((RenderLayer<T,M>) constructor.newInstance(target, Minecraft.getInstance().getEntityModels()));
                case PARENT_MODEL_MODEL -> {
                    if (((PlayerModel<?>) model).slim) {
                        target.addLayer((RenderLayer<T, M>) constructor.newInstance(target,
                                new HumanoidModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                                new HumanoidModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR))));
                    } else {
                        target.addLayer((RenderLayer<T, M>) constructor.newInstance(target,
                                new HumanoidModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                                        new HumanoidModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
                    }
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
