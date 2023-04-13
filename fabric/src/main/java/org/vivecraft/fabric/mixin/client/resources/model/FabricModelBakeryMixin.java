package org.vivecraft.fabric.mixin.client.resources.model;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class FabricModelBakeryMixin {
    @Shadow protected abstract void loadTopLevel(ModelResourceLocation modelResourceLocation);

    @Inject(method = "<init>", at = @At(value = "CONSTANT", args = "stringValue=special"))
    private void loadModels(BlockColors blockColors, ProfilerFiller profilerFiller, Map map, Map map2, CallbackInfo ci) {
        this.loadTopLevel(TelescopeTracker.scopeModel);
        this.loadTopLevel(ClientDataHolderVR.thirdPersonCameraModel);
        this.loadTopLevel(ClientDataHolderVR.thirdPersonCameraDisplayModel);
        this.loadTopLevel(CameraTracker.cameraModel);
        this.loadTopLevel(CameraTracker.cameraDisplayModel);
    }
}
