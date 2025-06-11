package org.vivecraft.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;

import javax.annotation.Nullable;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanFreezeWhenLookedAt")
public class EndermanFreezeWhenLookedAtMixin {

    @Shadow
    @Nullable
    private LivingEntity target;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/control/LookControl;setLookAt(DDD)V"))
    private void vivecraft$lookAtHead(LookControl instance, double x, double y, double z, Operation<Void> original) {
        if (this.target instanceof ServerPlayer player && ServerVRPlayers.isVRPlayer(player)) {
            Vec3 hmdPos = ServerVRPlayers.getVivePlayer(player).getHMDPos();
            original.call(instance, hmdPos.x, hmdPos.y, hmdPos.z);
        } else {
            original.call(instance, x, y, z);
        }
    }
}
