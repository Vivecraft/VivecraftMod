package org.vivecraft.gameplay.trackers;

import java.util.Random;

import org.vivecraft.gameplay.VRPlayer;

import com.example.examplemod.DataHolder;

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

    public EatingTracker(Minecraft mc)
    {
        super(mc);
    }

    public boolean isEating()
    {
        return this.eating[0] || this.eating[1];
    }

    public boolean isActive(LocalPlayer p)
    {
        if (DataHolder.getInstance().vrSettings.seated)
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

                if (useanim == UseAnim.EAT || useanim == UseAnim.DRINK)
                {
                    return true;
                }
            }

            if (p.getOffhandItem() != null)
            {
                UseAnim useanim1 = p.getOffhandItem().getUseAnimation();

                if (useanim1 == UseAnim.EAT || useanim1 == UseAnim.DRINK)
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

    public void doProcess(LocalPlayer player)
    {
        VRPlayer vrplayer = this.dh.vrPlayer;
        Vec3 vec3 = vrplayer.vrdata_room_pre.hmd.getPosition();
        Vec3 vec31 = vrplayer.vrdata_room_pre.getController(0).getCustomVector(new Vec3(0.0D, (double)(-this.mouthtoEyeDistance), 0.0D)).add(vec3);

        for (int i = 0; i < 2; ++i)
        {
            Vec3 vec32 = this.dh.vr.controllerHistory[i].averagePosition(0.333D).add(vrplayer.vrdata_room_pre.getController(i).getCustomVector(new Vec3(0.0D, 0.0D, -0.1D)));
            vec32 = vec32.add(this.dh.vrPlayer.vrdata_room_pre.getController(i).getDirection().scale(0.1D));

            if (vec31.distanceTo(vec32) < (double)this.threshold)
            {
                ItemStack itemstack = i == 0 ? player.getMainHandItem() : player.getOffhandItem();

                if (itemstack != ItemStack.EMPTY)
                {
                    int j = 0;

                    if ((itemstack.getUseAnimation() != UseAnim.DRINK || !(vrplayer.vrdata_room_pre.getController(i).getCustomVector(new Vec3(0.0D, 1.0D, 0.0D)).y > 0.0D)) && itemstack.getUseAnimation() == UseAnim.EAT)
                    {
                        j = 2;

                        if (!this.eating[i])
                        {
                            //Minecraft.getInstance().physicalGuiManager.preClickAction();

                            if (this.mc.gameMode.useItem(player, player.level, i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).consumesAction())
                            {
                                this.mc.gameRenderer.itemInHandRenderer.itemUsed(i == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                                this.eating[i] = true;
                                this.eatStart = Util.getMillis();
                            }
                        }

                        if (this.eating[i])
                        {
                            long k = (long)player.getUseItemRemainingTicks();

                            if (k > 0L && k % 5L <= (long)j)
                            {
                                this.dh.vr.triggerHapticPulse(i, 700);
                            }
                        }

                        if (Util.getMillis() - this.eatStart > (long)this.eattime)
                        {
                            this.eating[i] = false;
                        }
                    }
                }
            }
            else
            {
                this.eating[i] = false;
            }
        }
    }
}
