package org.vivecraft.gameplay.trackers;

import java.util.Random;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.VRPlayer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

public class EatingTracker extends Tracker
{
    float mouthtoEyeDistance = 0.0F;
    float threshold = 0.25F;
    public boolean[] eating = new boolean[2];
    int eattime = 2100;
    long eatStart;
    private Random r = new Random();

    public EatingTracker(Minecraft mc, ClientDataHolder dh)
    {
        super(mc, dh);
    }

    public boolean isEating()
    {
        return this.eating[0] || this.eating[1];
    }

    public boolean isActive(LocalPlayer p)
    {
        if (ClientDataHolder.getInstance().vrSettings.seated)
        {
            return false;
        }
        else if (p == null)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (!p.isAlive())
        {
            return false;
        }
        else if (p.isSleeping())
        {
            return false;
        }
        else
        {
            if (p.getMainHandItem() != null)
            {
                UseAnim useanim = p.getMainHandItem().getUseAnimation();

                if (useanim == UseAnim.EAT || useanim == UseAnim.DRINK || useanim == UseAnim.TOOT_HORN)
                {
                    return true;
                }
            }

            if (p.getOffhandItem() != null)
            {
                UseAnim useanim1 = p.getOffhandItem().getUseAnimation();

                if (useanim1 == UseAnim.EAT || useanim1 == UseAnim.DRINK || useanim1 == UseAnim.TOOT_HORN)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public void reset(LocalPlayer player)
    {
        this.eating[0] = false;
        this.eating[1] = false;
    }

    public void doProcess(LocalPlayer player)	{
        VRPlayer vrplayer = this.dh.vrPlayer;
        Vec3 hmdPos = vrplayer.vrdata_room_pre.hmd.getPosition();
        Vec3 mouthPos = vrplayer.vrdata_room_pre.getController(0).getCustomVector(new Vec3(0.0D, (double)(-this.mouthtoEyeDistance), 0.0D)).add(hmdPos);

        for (int c = 0; c < 2; ++c)	{

            Vec3 controllerPos = this.dh.vr.controllerHistory[c].averagePosition(0.333D).add(vrplayer.vrdata_room_pre.getController(c).getCustomVector(new Vec3(0.0D, 0.0D, -0.1D)));
            controllerPos = controllerPos.add(this.dh.vrPlayer.vrdata_room_pre.getController(c).getDirection().scale(0.1D));

            if (mouthPos.distanceTo(controllerPos) < (double)this.threshold)	{
                ItemStack itemstack = c == 0 ? player.getMainHandItem() : player.getOffhandItem();
                if (itemstack == ItemStack.EMPTY) continue;

                int crunchiness = 0;

                if (itemstack.getUseAnimation() == UseAnim.DRINK){//thats how liquid works.
                    if(vrplayer.vrdata_room_pre.getController(c).getCustomVector(new Vec3(0,1,0)).y > 0) continue;
                }
                else if (itemstack.getUseAnimation() == UseAnim.EAT) {
                    crunchiness = 2;
                }
                else if (itemstack.getUseAnimation() == UseAnim.TOOT_HORN) {
                    crunchiness = 1;
                }
                else {
                    continue;
                }

                if (!this.eating[c])
                        {
                            //Minecraft.getInstance().physicalGuiManager.preClickAction();

                            if (this.mc.gameMode.useItem(player, c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).consumesAction())
                            {
                        this.mc.gameRenderer.itemInHandRenderer.itemUsed(c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                        this.eating[c] = true;
                                this.eatStart = Util.getMillis();
                            }
                        }

                if (this.eating[c])
                        {
                            long k = (long)player.getUseItemRemainingTicks();

                    if (k > 0L && k % 5L <= (long)crunchiness)
                            {
                        this.dh.vr.triggerHapticPulse(c, 700);
                            }
                        }

                        if (Util.getMillis() - this.eatStart > (long)this.eattime)
                        {
                    this.eating[c] = false;
                        }
                    }
            else
            {
                this.eating[c] = false;
            }
        }
    }
}
