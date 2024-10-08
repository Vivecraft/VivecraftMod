package org.vivecraft.mixin.client_vr.player;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
    protected ItemStack useItem;

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    public abstract boolean onClimbable();

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    protected void vivecraft$beforeReleaseUsingItem(CallbackInfo ci) {}
}
