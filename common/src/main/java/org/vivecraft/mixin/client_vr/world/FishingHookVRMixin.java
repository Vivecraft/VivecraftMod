package org.vivecraft.mixin.client_vr.world;

import org.vivecraft.client_vr.provider.ControllerType;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.projectile.FishingHook.class)
public abstract class FishingHookVRMixin extends net.minecraft.world.entity.Entity {

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

    @Inject(at = @At("HEAD"), method = "tick")
    private void fishhookFeedback(CallbackInfo ci){
        if (!vrRunning) {
            return;
        }
        Player player = this.getPlayerOwner();
        if (player != null && player.isLocalPlayer())
        {
            if (this.biting && !this.wasBiting) {
                // bite, big feedback
                dh.vr.triggerHapticPulse(
                    player.getMainHandItem().getItem() instanceof FishingRodItem ? ControllerType.RIGHT : ControllerType.LEFT,
                    0.005F,
                    160.0F,
                    0.5F
                );
            } else if (this.getDeltaMovement().y < -0.01 && !this.wasNibble) {
                // nibble, small feedback
                dh.vr.triggerHapticPulse(
                    player.getMainHandItem().getItem() instanceof FishingRodItem ? ControllerType.RIGHT : ControllerType.LEFT,
                    0.0005F,
                    160.0F,
                    0.05F
                );
                this.wasNibble = true;
            }
        }
        this.wasBiting = this.biting;
        this.wasNibble = this.wasNibble && this.getDeltaMovement().y < 0.0;
    }
}
