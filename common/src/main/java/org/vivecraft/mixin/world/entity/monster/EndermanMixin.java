package org.vivecraft.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(EnderMan.class)
public abstract class EndermanMixin extends Monster {

    protected EndermanMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

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
    private double vivecraft$biggerViewCone(double original, @Share("hmdPos") LocalRef<Vec3> hmdPos) {
        // increase the view cone check from 1.4° to 5.7°, makes it easier to stop enderman,
        // since it's hard to know where the center of the view is
        return hmdPos.get() != null ? 0.1 : original;
    }
}
