package org.vivecraft.mixin.client_vr.world;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.ControllerType;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(net.minecraft.world.entity.projectile.FishingHook.class)
public abstract class FishingHookVRMixin extends net.minecraft.world.entity.Entity {

    @Shadow
    private boolean biting;

    @Shadow
    public abstract Player getPlayerOwner();

    public FishingHookVRMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private boolean vivecraft$wasBiting = false;
    @Unique
    private boolean vivecraft$wasNibble = false;

    @Inject(at = @At("HEAD"), method = "tick")
    private void vivecraft$fishhookFeedback(CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }
        Player player = this.getPlayerOwner();
        if (player != null && player.isLocalPlayer()) {
            if (this.biting && !this.vivecraft$wasBiting) {
                // bite, big feedback
                dh.vr.triggerHapticPulse(
                    player.getMainHandItem().getItem() instanceof FishingRodItem ?
                    ControllerType.RIGHT :
                    ControllerType.LEFT,
                    0.005F,
                    160.0F,
                    0.5F
                );
            } else if (this.getDeltaMovement().y < -0.01 && !this.vivecraft$wasNibble) {
                // nibble, small feedback
                dh.vr.triggerHapticPulse(
                    player.getMainHandItem().getItem() instanceof FishingRodItem ?
                    ControllerType.RIGHT :
                    ControllerType.LEFT,
                    0.0005F,
                    160.0F,
                    0.05F
                );
                this.vivecraft$wasNibble = true;
            }
        }
        this.vivecraft$wasBiting = this.biting;
        this.vivecraft$wasNibble = this.vivecraft$wasNibble && this.getDeltaMovement().y < 0.0;
    }
}
