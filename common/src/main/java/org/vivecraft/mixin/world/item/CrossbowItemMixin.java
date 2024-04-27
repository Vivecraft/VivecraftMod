package org.vivecraft.mixin.world.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.common.utils.math.Vector3;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"),
        method = "shootProjectile")
    private Vec3 vivecraft$shoot(LivingEntity livingEntity, float v) {
        Vec3 vec3 = livingEntity.getViewVector(v);
        if (livingEntity instanceof ServerPlayer player) {
            ServerVivePlayer serverviveplayer = ServerVRPlayers.getVivePlayer(player);
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
                serverviveplayer.getControllerVectorCustom(serverviveplayer.activeHand, new Vector3(0.0F, 1.0F, 0.0F));
            }
        }
        return vec3;
    }
}
