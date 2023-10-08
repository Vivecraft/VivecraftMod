package org.vivecraft.mixin.client_vr.world.entity.projectile;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.VRData.VRDevicePose;

import javax.annotation.Nullable;

import static org.vivecraft.client_vr.VRState.*;

@Mixin(net.minecraft.world.entity.projectile.FireworkRocketEntity.class)
public class FireworkRocketEntityVRMixin {

    @Shadow
    private @Nullable LivingEntity attachedToEntity;

    @Unique
    private final Vector3f vivecraft$handPos = new Vector3f();

    @Unique
    private boolean vivecraft$doHandPos = true;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 1, method = "tick")
    private double vivecraft$modifyX(double x) {
        if (this.attachedToEntity instanceof LocalPlayer localPlayer && this.attachedToEntity == mc.player && vrRunning) {
            VRDevicePose controller = dh.vrPlayer.getVRDataWorld().getHand(!localPlayer.getOffhandItem().is(Items.FIREWORK_ROCKET) && localPlayer.getMainHandItem().is(Items.FIREWORK_ROCKET) ? 0 : 1);
            controller.getPosition(this.vivecraft$handPos).add(controller.getDirection(new Vector3f()).mul(0.25F));
            this.vivecraft$doHandPos = true;
            return this.vivecraft$handPos.x;
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 2, method = "tick")
    private double vivecraft$modifyY(double y) {
        if (this.vivecraft$doHandPos) {
            return this.vivecraft$handPos.y;
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 3, method = "tick")
    private double vivecraft$modifyZ(double z) {
        if (this.vivecraft$doHandPos) {
            z = this.vivecraft$handPos.z;
            this.vivecraft$doHandPos = false;
        }
        return z;
    }

    /*
    // server offset, this is wrong somehow
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHandHoldingItemAngle(Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/phys/Vec3;"), method = "tick")
    private Vec3 vivecraft$redirectHandOffset(LivingEntity instance, Item item){
        if (instance instanceof ServerPlayer serverPlayer) {
            ServerVivePlayer vivePLayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            if(vivePLayer != null && vivePLayer.isVR()) {
                   return vivePLayer.getControllerPos(serverPlayer.getOffhandItem().is(item) && !serverPlayer.getMainHandItem().is(item) ? 1 : 0, serverPlayer, true).subtract(instance.position());
            }
        }
        return instance.getHandHoldingItemAngle(item);
    }
    */
}
