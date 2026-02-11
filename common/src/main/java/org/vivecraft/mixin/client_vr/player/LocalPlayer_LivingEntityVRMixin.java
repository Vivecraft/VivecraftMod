package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LocalPlayer_LivingEntityVRMixin extends LocalPlayer_EntityVRMixin {
    @Shadow
    public float zza;
    @Shadow
    protected int useItemRemaining;

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    public abstract boolean onClimbable();

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    protected void vivecraft$beforeReleaseUsingItem(CallbackInfo ci) {}

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z"))
    protected boolean vivecraft$disableVanillaClimbing(boolean original) {
        return original;
    }
}
