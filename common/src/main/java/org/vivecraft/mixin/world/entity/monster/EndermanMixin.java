package org.vivecraft.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(EnderMan.class)
public abstract class EndermanMixin {

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$lookDirVR(
        Player instance, float partialTick, Operation<Vec3> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        if (instance instanceof ServerPlayer serverPlayer) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                hmdPos.set(serverVivePlayer.getHMDPos());
                return serverVivePlayer.getHMDDir();
            }
        }
        return original.call(instance, partialTick);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getX()D"))
    private double vivecraft$headPosX(
        Player instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().x : original.call(instance);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D"))
    private double vivecraft$headPosY(
        Player instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().y : original.call(instance);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getZ()D"))
    private double vivecraft$headPosZ(
        Player instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().z : original.call(instance);
    }

    @ModifyExpressionValue(method = "isLookingAtMe", at = @At(value = "CONSTANT", args = "doubleValue=0.025"))
    private double vivecraft$biggerViewCone(double original, @Local(argsOnly = true) Player player) {
        // increase the view cone check from 1.4° to 5.7°, makes it easier to stop enderman,
        // since it's hard to know where the center of the view is
        return player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer) ? 0.1 : original;
    }
}
