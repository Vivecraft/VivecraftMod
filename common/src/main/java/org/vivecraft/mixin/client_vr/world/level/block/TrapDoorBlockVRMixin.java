package org.vivecraft.mixin.client_vr.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.level.block.TrapDoorBlock.class)
public class TrapDoorBlockVRMixin {

    @Inject(at = @At("HEAD"), method = "playSound")
    public void hapticFeedbackOnClose(Player player, Level level, BlockPos blockPos, boolean opening, CallbackInfo ci) {
        if (vrRunning && !opening && mc.player != null && mc.player.isAlive() && mc.player.blockPosition().distSqr(blockPos) < 25.0D) {
            dh.vr.triggerHapticPulse(0, 250);
        }
    }
}
