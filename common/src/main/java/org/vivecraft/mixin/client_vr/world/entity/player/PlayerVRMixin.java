package org.vivecraft.mixin.client_vr.world.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerVRMixin {
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"), method = "maybeBackOffFromEdge")
    private AABB moveSidewaysExtendDown(AABB instance, double x, double y, double z) {
        // this is to fix an issue with a maxStepUp size of 1 and trapdoors
        return new AABB(instance.minX + x, instance.minY + y, instance.minZ + z, instance.maxX + x, instance.maxY, instance.maxZ + z);
    }
}
