package org.vivecraft.mixin.client_vr.player;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class LocalPlayer_PlayerVRMixin extends LocalPlayer_LivingEntityVRMixin {

    @Shadow
    public abstract Abilities getAbilities();

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    protected abstract float getBlockSpeedFactor();

    @Shadow
    public abstract SoundSource getSoundSource();
}
