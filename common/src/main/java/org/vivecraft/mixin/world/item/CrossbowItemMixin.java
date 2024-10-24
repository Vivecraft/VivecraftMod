package org.vivecraft.mixin.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @WrapOperation(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$vrAim(LivingEntity instance, float partialTick, Operation<Vec3> original) {
        if (instance instanceof ServerPlayer player) {
            ServerVivePlayer serverviveplayer = ServerVRPlayers.getVivePlayer(player);
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                return serverviveplayer.getControllerDir(serverviveplayer.activeHand);
            }
        }
        return original.call(instance, partialTick);
    }
}
