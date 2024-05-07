package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(ProjectileDeflection.class)
public interface ProjectileDeflectionMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"), method = "method_59862")
    private static Vec3 vivecraft$deflectLook(Entity instance) {
        if (instance instanceof ServerPlayer player) {
            ServerVivePlayer serverviveplayer = ServerVRPlayers.getVivePlayer(player);
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                return serverviveplayer.getHMDDir();
            }
        }
        return instance.getLookAngle();
    }
}
