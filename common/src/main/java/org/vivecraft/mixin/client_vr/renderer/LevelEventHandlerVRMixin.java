package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerVRMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void vivecraft$shakeOnSound(int type, BlockPos pos, int data, CallbackInfo ci) {
        boolean playerNearAndVR = VRState.VR_RUNNING && this.minecraft.player != null &&
            this.minecraft.player.isAlive() && this.minecraft.player.blockPosition().distSqr(pos) < 25.0D;
        if (playerNearAndVR) {
            switch (type) {
                /* pre 1.19.4, they are now separate
                case LevelEvent.LevelEvent.SOUND_CLOSE_IRON_DOOR,
                        LevelEvent.SOUND_CLOSE_WOODEN_DOOR,
                        LevelEvent.SOUND_CLOSE_WOODEN_TRAP_DOOR,
                        LevelEvent.SOUND_CLOSE_FENCE_GATE,
                        LevelEvent.SOUND_CLOSE_IRON_TRAP_DOOR
                        -> ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 250);
                 */
                case LevelEvent.SOUND_ZOMBIE_WOODEN_DOOR,
                     LevelEvent.SOUND_ZOMBIE_IRON_DOOR,
                     LevelEvent.SOUND_ZOMBIE_DOOR_CRASH -> {
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 750);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 750);
                }
                case LevelEvent.SOUND_ANVIL_USED -> ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 500);
                case LevelEvent.SOUND_ANVIL_LAND -> {
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 1250);
                    ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 1250);
                }
            }
        }
    }
}
