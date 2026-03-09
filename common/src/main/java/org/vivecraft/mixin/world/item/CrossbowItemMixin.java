package org.vivecraft.mixin.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @WrapOperation(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getUpVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$vrAimUp(LivingEntity instance, float partialTick, Operation<Vec3> original) {
        if (instance instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                return serverVivePlayer.getBodyPartVectorCustom(serverVivePlayer.getActiveItemBodyPart(), MathUtils.UP);
            }
        }
        return original.call(instance, partialTick);
    }

    @WrapOperation(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$vrAimForward(LivingEntity instance, float partialTick, Operation<Vec3> original) {
        if (instance instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                // can be shot with the offhand
                return serverVivePlayer.getAimDir(true);
            }
        }
        return original.call(instance, partialTick);
    }
}
