package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.settings.AutoCalibration;

import org.joml.Vector3d;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;

public class JumpTracker extends Tracker
{
    public Vec3[] latchStart = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartOrigin = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartPlayer = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    private boolean c0Latched = false;
    private boolean c1Latched = false;

    public boolean isClimbeyJump()
    {
        return !this.isActive() ? false : this.isClimbeyJumpEquipped();
    }

    public boolean isClimbeyJumpEquipped()
    {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) mc.player).isClimbeyJumpEquipped();
    }

    public boolean isActive()
    {
        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrPlayer.getFreeMove() && !dh.vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!dh.vrSettings.realisticJumpEnabled)
        {
            return false;
        }
        else if (mc.player != null && mc.player.isAlive())
        {
            if (mc.gameMode == null)
            {
                return false;
            }
            else if (!mc.player.isInWater() && !mc.player.isInLava() && mc.player.onGround())
            {
                return !mc.player.isShiftKeyDown() && !mc.player.isPassenger();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean isjumping()
    {
        return this.c1Latched || this.c0Latched;
    }

    public void idleTick()
    {
        dh.vr.getInputAction(VivecraftVRMod.keyClimbeyJump).setEnabled(this.isClimbeyJumpEquipped() && (this.isActive() || dh.climbTracker.isClimbeyClimbEquipped() && dh.climbTracker.isGrabbingLadder()));
    }

    public void reset()
    {
        this.c1Latched = false;
        this.c0Latched = false;
    }

    public void doProcess()
    {
        if (this.isClimbeyJumpEquipped())
        {
            boolean[] aboolean = new boolean[2];

            for (int i = 0; i < 2; ++i)
            {
                aboolean[i] = VivecraftVRMod.keyClimbeyJump.isDown();
            }

            boolean flag = false;

            if (!aboolean[0] && this.c0Latched)
            {
                dh.vr.triggerHapticPulse(0, 200);
                flag = true;
            }

            Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getController(0).getPosition();
            Vec3 vec31 = dh.vrPlayer.vrdata_room_pre.getController(1).getPosition();
            Vec3 vec32 = vec3.add(vec31).scale(0.5D);

            if (aboolean[0] && !this.c0Latched)
            {
                this.latchStart[0] = vec32;
                this.latchStartOrigin[0] = dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[0] = mc.player.position();
                dh.vr.triggerHapticPulse(0, 1000);
            }

            if (!aboolean[1] && this.c1Latched)
            {
                dh.vr.triggerHapticPulse(1, 200);
                flag = true;
            }

            if (aboolean[1] && !this.c1Latched)
            {
                this.latchStart[1] = vec32;
                this.latchStartOrigin[1] = dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[1] = mc.player.position();
                dh.vr.triggerHapticPulse(1, 1000);
            }

            this.c0Latched = aboolean[0];
            this.c1Latched = aboolean[1];
            int j = 0;
            Vec3 vec33 = vec32.subtract(this.latchStart[j]);
            vec33 = vec33.yRot(dh.vrPlayer.vrdata_world_pre.rotation_radians);

            if (!flag && this.isjumping())
            {
                dh.vr.triggerHapticPulse(0, 200);
                dh.vr.triggerHapticPulse(1, 200);
            }

            if (flag)
            {
                dh.climbTracker.forceActivate = true;
                Vec3 vec34 = convertToVec3(dh.vr.controllerHistory[0].netMovement(0.3D, new Vector3d()).add(dh.vr.controllerHistory[1].netMovement(0.3D, new Vector3d())));
                double d0 = (dh.vr.controllerHistory[0].averageSpeed(0.3D) + dh.vr.controllerHistory[1].averageSpeed(0.3D)) / 2.0D;
                vec34 = vec34.scale((double)0.33F * d0);
                float f = 0.66F;

                if (vec34.length() > (double)f)
                {
                    vec34 = vec34.scale((double)f / vec34.length());
                }

                if (mc.player.hasEffect(MobEffects.JUMP))
                {
                    vec34 = vec34.scale((double)mc.player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
                }

                vec34 = vec34.yRot(dh.vrPlayer.vrdata_world_pre.rotation_radians);
                Vec3 vec35 = mc.player.position().subtract(vec33);

                if (vec33.y < 0.0D && vec34.y < 0.0D)
                {
                    double d2 = -vec34.x;
                    double d1 = mc.player.getDeltaMovement().x + d2 * 1.25D;
                    d2 = -vec34.y;
                    double d3 = -vec34.z;
                    mc.player.setDeltaMovement(d1, d2, mc.player.getDeltaMovement().z + d3 * 1.25D);
                    mc.player.xOld = vec35.x;
                    mc.player.yOld = vec35.y;
                    mc.player.zOld = vec35.z;
                    vec35 = vec35.add(mc.player.getDeltaMovement());
                    mc.player.setPos(vec35.x, vec35.y, vec35.z);
                    dh.vrPlayer.snapRoomOriginToPlayerEntity(false, true);
                    mc.player.causeFoodExhaustion(0.3F);
                    mc.player.setOnGround(false);
                }
                else
                {
                    dh.vrPlayer.snapRoomOriginToPlayerEntity(false, true);
                }
            }
            else if (this.isjumping())
            {
                Vec3 vec36 = this.latchStartOrigin[0].subtract(this.latchStartPlayer[0]).add(mc.player.position()).subtract(vec33);
                dh.vrPlayer.setRoomOrigin(vec36.x, vec36.y, vec36.z, false);
            }
        }
        else if (dh.vr.hmdPivotHistory.netMovement(0.25D, new Vector3d()).y > 0.1D && dh.vr.hmdPivotHistory.latest(new Vector3d()).y - (double) AutoCalibration.getPlayerHeight() > (double)dh.vrSettings.jumpThreshold)
        {
            mc.player.jumpFromGround();
        }
    }

    public boolean isBoots(ItemStack i)
    {
        if (i.isEmpty())
        {
            return false;
        }
        else if (!i.hasCustomHoverName())
        {
            return false;
        }
        else if (i.getItem() != Items.LEATHER_BOOTS)
        {
            return false;
        }
        else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable"))
        {
            return false;
        }
        else
        {
            return i.getHoverName().getContents() instanceof TranslatableContents && ((TranslatableContents)i.getHoverName().getContents()).getKey().equals("vivecraft.item.jumpboots") || i.getHoverName().getString().equals("Jump Boots");
        }
    }
}
