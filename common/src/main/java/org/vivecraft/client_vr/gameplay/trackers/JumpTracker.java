package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class JumpTracker extends Tracker
{
    public Vec3[] latchStart = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartOrigin = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartPlayer = new Vec3[] {new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    private boolean c0Latched = false;
    private boolean c1Latched = false;

    public JumpTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isClimbeyJump()
    {
        return !this.isActive(Minecraft.getInstance().player) ? false : this.isClimbeyJumpEquipped();
    }

    public boolean isClimbeyJumpEquipped()
    {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) Minecraft.getInstance().player).isClimbeyJumpEquipped();
    }

    public boolean isActive(LocalPlayer p)
    {
        if (ClientDataHolderVR.getInstance().vrSettings.seated)
        {
            return false;
        }
        else if (!ClientDataHolderVR.getInstance().vrPlayer.getFreeMove() && !ClientDataHolderVR.getInstance().vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!ClientDataHolderVR.getInstance().vrSettings.realisticJumpEnabled)
        {
            return false;
        }
        else if (p != null && p.isAlive())
        {
            if (this.mc.gameMode == null)
            {
                return false;
            }
            else if (!p.isInWater() && !p.isInLava() && p.onGround())
            {
                return !p.isShiftKeyDown() && !p.isPassenger();
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

    public void idleTick(LocalPlayer player)
    {
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyJump).setEnabled(this.isClimbeyJumpEquipped() && (this.isActive(player) || this.dh.climbTracker.isClimbeyClimbEquipped() && this.dh.climbTracker.isGrabbingLadder()));
    }

    public void reset(LocalPlayer player)
    {
        this.c1Latched = false;
        this.c0Latched = false;
    }

    public void doProcess(LocalPlayer player)
    {
        if (this.isClimbeyJumpEquipped())
        {
            VRPlayer vrplayer = this.dh.vrPlayer;
            boolean[] aboolean = new boolean[2];

            for (int i = 0; i < 2; ++i)
            {
                aboolean[i] = VivecraftVRMod.INSTANCE.keyClimbeyJump.isDown();
            }

            boolean flag = false;

            if (!aboolean[0] && this.c0Latched)
            {
                this.dh.vr.triggerHapticPulse(0, 200);
                flag = true;
            }

            Vec3 vec3 = this.dh.vrPlayer.vrdata_room_pre.getController(0).getPosition();
            Vec3 vec31 = this.dh.vrPlayer.vrdata_room_pre.getController(1).getPosition();
            Vec3 vec32 = vec3.add(vec31).scale(0.5D);

            if (aboolean[0] && !this.c0Latched)
            {
                this.latchStart[0] = vec32;
                this.latchStartOrigin[0] = this.dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[0] = this.mc.player.position();
                this.dh.vr.triggerHapticPulse(0, 1000);
            }

            if (!aboolean[1] && this.c1Latched)
            {
                this.dh.vr.triggerHapticPulse(1, 200);
                flag = true;
            }

            if (aboolean[1] && !this.c1Latched)
            {
                this.latchStart[1] = vec32;
                this.latchStartOrigin[1] = this.dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[1] = this.mc.player.position();
                this.dh.vr.triggerHapticPulse(1, 1000);
            }

            this.c0Latched = aboolean[0];
            this.c1Latched = aboolean[1];
            int j = 0;
            Vec3 vec33 = vec32.subtract(this.latchStart[j]);
            vec33 = vec33.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);

            if (!flag && this.isjumping())
            {
                this.dh.vr.triggerHapticPulse(0, 200);
                this.dh.vr.triggerHapticPulse(1, 200);
            }

            if (flag)
            {
                this.dh.climbTracker.forceActivate = true;
                Vec3 vec34 = this.dh.vr.controllerHistory[0].netMovement(0.3D).add(this.dh.vr.controllerHistory[1].netMovement(0.3D));
                double d0 = (this.dh.vr.controllerHistory[0].averageSpeed(0.3D) + this.dh.vr.controllerHistory[1].averageSpeed(0.3D)) / 2.0D;
                vec34 = vec34.scale((double)0.33F * d0);
                float f = 0.66F;

                if (vec34.length() > (double)f)
                {
                    vec34 = vec34.scale((double)f / vec34.length());
                }

                if (player.hasEffect(MobEffects.JUMP))
                {
                    vec34 = vec34.scale((double)player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
                }

                vec34 = vec34.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);
                Vec3 vec35 = this.mc.player.position().subtract(vec33);

                if (vec33.y < 0.0D && vec34.y < 0.0D)
                {
                    double d2 = -vec34.x;
                    double d1 = player.getDeltaMovement().x + d2 * 1.25D;
                    d2 = -vec34.y;
                    double d3 = -vec34.z;
                    player.setDeltaMovement(d1, d2, player.getDeltaMovement().z + d3 * 1.25D);
                    player.xOld = vec35.x;
                    player.yOld = vec35.y;
                    player.zOld = vec35.z;
                    vec35 = vec35.add(player.getDeltaMovement());
                    player.setPos(vec35.x, vec35.y, vec35.z);
                    this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                    this.mc.player.causeFoodExhaustion(0.3F);
                    this.mc.player.setOnGround(false);
                }
                else
                {
                    this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                }
            }
            else if (this.isjumping())
            {
                Vec3 vec36 = this.latchStartOrigin[0].subtract(this.latchStartPlayer[0]).add(this.mc.player.position()).subtract(vec33);
                this.dh.vrPlayer.setRoomOrigin(vec36.x, vec36.y, vec36.z, false);
            }
        }
        else if (this.dh.vr.hmdPivotHistory.netMovement(0.25D).y > 0.1D && this.dh.vr.hmdPivotHistory.latest().y - (double) AutoCalibration.getPlayerHeight() > (double)this.dh.vrSettings.jumpThreshold)
        {
            player.jumpFromGround();
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
