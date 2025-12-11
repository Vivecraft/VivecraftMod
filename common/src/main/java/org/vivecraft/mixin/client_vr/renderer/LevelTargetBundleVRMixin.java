package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;

import javax.annotation.Nullable;

@Mixin(LevelTargetBundle.class)
public class LevelTargetBundleVRMixin implements LevelTargetBundleExtension {

    @Unique
    @Nullable
    private ResourceHandle<RenderTarget> vivecraft$occluded;
    @Unique
    @Nullable
    private ResourceHandle<RenderTarget> vivecraft$unoccluded;
    @Unique
    @Nullable
    private ResourceHandle<RenderTarget> vivecraft$hands;

    @Inject(method = "replace", at = @At("HEAD"), cancellable = true)
    private void vivecraft$replace(
        Identifier identifier, ResourceHandle<RenderTarget> resourceHandle, CallbackInfo ci)
    {
        if (identifier.equals(OCCLUDED_TARGET_ID)) {
            this.vivecraft$occluded = resourceHandle;
            ci.cancel();
        } else if (identifier.equals(UNOCCLUDED_TARGET_ID)) {
            this.vivecraft$unoccluded = resourceHandle;
            ci.cancel();
        } else if (identifier.equals(HANDS_TARGET_ID)) {
            this.vivecraft$hands = resourceHandle;
            ci.cancel();
        }
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void vivecraft$get(
        Identifier identifier, CallbackInfoReturnable<ResourceHandle<RenderTarget>> cir)
    {
        if (identifier.equals(OCCLUDED_TARGET_ID)) {
            cir.setReturnValue(this.vivecraft$occluded);
        } else if (identifier.equals(UNOCCLUDED_TARGET_ID)) {
            cir.setReturnValue(this.vivecraft$unoccluded);
        } else if (identifier.equals(HANDS_TARGET_ID)) {
            cir.setReturnValue(this.vivecraft$hands);
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void vivecraft$clear(CallbackInfo ci) {
        this.vivecraft$occluded = null;
        this.vivecraft$unoccluded = null;
        this.vivecraft$hands = null;
    }


    @Override
    public ResourceHandle<RenderTarget> vivecraft$getOccluded() {
        return this.vivecraft$occluded;
    }

    @Override
    public ResourceHandle<RenderTarget> vivecraft$getUnoccluded() {
        return this.vivecraft$unoccluded;
    }

    @Override
    public ResourceHandle<RenderTarget> vivecraft$getHands() {
        return this.vivecraft$hands;
    }
}
