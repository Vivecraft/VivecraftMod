package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class LocalPlayer_PlayerVRMixin extends LocalPlayer_LivingEntityVRMixin {

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    protected abstract float getBlockSpeedFactor();

    @Shadow
    public abstract SoundSource getSoundSource();

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @Inject(method = "eat", at = @At("HEAD"))
    protected void vivecraft$beforeEat(CallbackInfoReturnable<ItemStack> cir, @Local(argsOnly = true) ItemStack food) {}
}
