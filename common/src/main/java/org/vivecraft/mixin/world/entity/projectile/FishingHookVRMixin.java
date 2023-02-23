package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.MCVR;

@Mixin(FishingHook.class)
public abstract class FishingHookVRMixin extends Entity {

    @Shadow private boolean biting;

    @Shadow
    public abstract Player getPlayerOwner();

    public FishingHookVRMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private boolean wasBiting = false;
    @Unique
    private boolean wasNibble = false;

    @Inject(at = @At(value = "HEAD"), method = "tick")
    private void fishhookFeedback(CallbackInfo ci){
        Player player = this.getPlayerOwner();
        if (player != null && player.isLocalPlayer())
        {
            if (biting && !wasBiting) {
                // bite, big feedback
                MCVR.get().triggerHapticPulse(
                        player.getMainHandItem().getItem() instanceof FishingRodItem ? ControllerType.RIGHT : ControllerType.LEFT,
                        0.005F, 160.0F, 0.5F);
            } else if (getDeltaMovement().y < -0.01 && !wasNibble) {
                // nibble, small feedback
                MCVR.get().triggerHapticPulse(
                        player.getMainHandItem().getItem() instanceof FishingRodItem ? ControllerType.RIGHT : ControllerType.LEFT,
                        0.0005F, 160.0F, 0.05F);
                wasNibble = true;
            }
        }
        wasBiting = biting;
        wasNibble = wasNibble && getDeltaMovement().y < 0.0;
    }
}