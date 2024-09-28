package org.vivecraft.mixin.client.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.network.SupporterReceiver;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.mixin.client_vr.world.entity.LivingEntityMixin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends LivingEntityMixin {
    public AbstractClientPlayerMixin(
        EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$addPatreonInfo(CallbackInfo ci) {
        SupporterReceiver.addPlayerInfo(((AbstractClientPlayer) (Object) this));
    }

    /**
     * inject into {@link LivingEntity#spawnItemParticles}
     */
    @Override
    protected void vivecraft$modifyEatParticles(
        Level instance, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed,
        double zSpeed, Operation<Void> original)
    {
        if ((Object) this == Minecraft.getInstance().player && VRState.vrRunning) {
            // local player
            Vec3 pos = RenderHelper.getControllerRenderPos(this.getUsedItemHand() == InteractionHand.MAIN_HAND ? 0 : 1);
            Vec3 dir = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().hmd.getDirection();

            vivecraft$particlesWithRandomOffset(instance, particleData, original, pos, dir);
        } else {
            // remote players
            VRPlayersClient.RotInfo rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(this.uuid);
            if (rotInfo != null) {
                Vec3 pos;
                if (this.getUsedItemHand() == InteractionHand.MAIN_HAND && !rotInfo.reverse) {
                    pos = rotInfo.rightArmPos.add(this.position());
                } else {
                    pos = rotInfo.leftArmPos.add(this.position());
                }

                Vec3 dir = rotInfo.headRot;

                vivecraft$particlesWithRandomOffset(instance, particleData, original, pos, dir);
            } else {
                original.call(instance, particleData, x, y, z, xSpeed, ySpeed, zSpeed);
            }
        }
    }

    @Unique
    private void vivecraft$particlesWithRandomOffset(
        Level instance, ParticleOptions particleData, Operation<Void> original, Vec3 pos, Vec3 dir)
    {
        float yOffset = this.random.nextFloat() * 0.2F - 0.1F;
        float xOffset = this.random.nextFloat() * 0.2F - 0.1F;
        float zOffset = this.random.nextFloat() * 0.2F - 0.1F;

        dir = dir.scale(0.1);

        original.call(instance, particleData,
            pos.x + yOffset, pos.y + xOffset, pos.z + zOffset,
            dir.x + yOffset * 0.5F, dir.y + xOffset * 0.5F + 0.1F, dir.z + zOffset * 0.5F);
    }
}
