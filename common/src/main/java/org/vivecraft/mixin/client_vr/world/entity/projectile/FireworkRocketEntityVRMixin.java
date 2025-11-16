package org.vivecraft.mixin.client_vr.world.entity.projectile;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;

import javax.annotation.Nullable;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityVRMixin {

    @Shadow
    private @Nullable LivingEntity attachedToEntity;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 1)
    private double vivecraft$modifyX(double x, @Share("handPos") LocalRef<Vec3> handPos) {
        if (this.attachedToEntity instanceof Player player && ClientVRPlayers.getInstance().isVRPlayer(player)) {
            boolean fireworkInMainHand = player.getMainHandItem().is(Items.FIREWORK_ROCKET) ||
                !player.getOffhandItem().is(Items.FIREWORK_ROCKET);
            if (VRState.VR_RUNNING && this.attachedToEntity == Minecraft.getInstance().player) {
                VRData.VRDevicePose controller = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld()
                    .getHand(fireworkInMainHand ? 0 : 1);
                Vector3f offset = controller.getDirection().mul(0.25F);
                handPos.set(controller.getPosition().add(offset.x, offset.y, offset.z));
            } else {
                ClientVRPlayers.RotInfo rotInfo = ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID());
                Vector3fc pos = fireworkInMainHand ? rotInfo.mainHandPos : rotInfo.offHandPos;
                handPos.set(player.position().add(pos.x(), pos.y(), pos.z()));
            }
            return handPos.get().x;
        }
        return x;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 2)
    private double vivecraft$modifyY(double y, @Share("handPos") LocalRef<Vec3> handPos) {
        return handPos.get() != null ? handPos.get().y : y;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 3)
    private double vivecraft$modifyZ(double z, @Share("handPos") LocalRef<Vec3> handPos) {
        return handPos.get() != null ? handPos.get().z : z;
    }
}
