package org.vivecraft.forge.mixin.client.resources.model;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
public abstract class ForgeModelBakeryMixin {
    @Shadow protected abstract void loadTopLevel(ModelResourceLocation modelResourceLocation);

    @Inject(method = "Lnet/minecraft/client/resources/model/ModelBakery;processLoading(Lnet/minecraft/util/profiling/ProfilerFiller;I)V", at = @At(value = "CONSTANT", args = "stringValue=textures"), remap = false)
    private void loadModels(ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        this.loadTopLevel(TelescopeTracker.scopeModel);
        this.loadTopLevel(ClientDataHolder.thirdPersonCameraModel);
        this.loadTopLevel(ClientDataHolder.thirdPersonCameraDisplayModel);
        this.loadTopLevel(CameraTracker.cameraModel);
        this.loadTopLevel(CameraTracker.cameraDisplayModel);
    }
}
