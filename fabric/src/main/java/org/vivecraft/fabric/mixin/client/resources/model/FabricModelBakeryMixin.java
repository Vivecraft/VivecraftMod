package org.vivecraft.fabric.mixin.client.resources.model;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.IOException;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class FabricModelBakeryMixin {
    @Shadow
    protected abstract void registerModel(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel);

    @Shadow
    protected abstract BlockModel loadBlockModel(ResourceLocation resourceLocation) throws IOException;

    @Shadow
    protected abstract void loadSpecialItemModelAndDependencies(ModelResourceLocation modelResourceLocation);

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=special"))
    private void loadModels(BlockColors blockColors, ProfilerFiller profilerFiller, Map map, Map map2, CallbackInfo ci) {
        // item models
        this.vivecraft$loadBlockModel(TelescopeTracker.scopeModel);
        this.vivecraft$loadBlockModel(ClimbTracker.clawsModel);

        // blockmodels
        this.vivecraft$loadBlockModel(ClientDataHolderVR.thirdPersonCameraModel);
        this.vivecraft$loadBlockModel(ClientDataHolderVR.thirdPersonCameraDisplayModel);
        this.vivecraft$loadBlockModel(CameraTracker.cameraModel);
        this.vivecraft$loadBlockModel(CameraTracker.cameraDisplayModel);
    }

    @Unique
    private void vivecraft$loadBlockModel(ModelResourceLocation modelResourceLocation) {
        try {
            this.registerModel(modelResourceLocation, this.loadBlockModel(modelResourceLocation.id()));
        } catch (IOException e) {
            VRSettings.logger.error("Failed to load vivecraft model '{}': {}", modelResourceLocation, e.getMessage());
        }
    }
}
