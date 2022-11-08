package org.vivecraft.fabric.mixin.client.resources.model;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;

@Mixin(ModelBakery.class)
public abstract class FabricModelBakeryMixin {
    @Shadow protected abstract void loadTopLevel(ModelResourceLocation modelResourceLocation);

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=textures"))
    private void loadModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        this.loadTopLevel(TelescopeTracker.scopeModel);
        this.loadTopLevel(ClientDataHolder.thirdPersonCameraModel);
        this.loadTopLevel(ClientDataHolder.thirdPersonCameraDisplayModel);
        this.loadTopLevel(CameraTracker.cameraModel);
        this.loadTopLevel(CameraTracker.cameraDisplayModel);
    }
}
