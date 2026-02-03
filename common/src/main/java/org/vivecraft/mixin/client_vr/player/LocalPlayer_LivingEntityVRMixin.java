package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

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

    @Shadow
    @Nullable
    public abstract AttributeInstance getAttribute(Holder<Attribute> attribute);

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    protected void vivecraft$beforeReleaseUsingItem(CallbackInfo ci) {}

    @Inject(method = "onKineticHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/KineticWeapon;makeLocalHitSound(Lnet/minecraft/world/entity/Entity;)V"))
    protected void vivecraft$spearHaptic(CallbackInfo ci) {}

    @ModifyExpressionValue(method = "handleRelativeFrictionAndCalculateMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z"))
    protected boolean vivecraft$disableVanillaClimbing(boolean original) {
        return original;
    }
}
