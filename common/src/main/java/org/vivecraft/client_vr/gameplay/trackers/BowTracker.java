package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.settings.VRSettings.BowMode;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import net.minecraft.Util;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

import static org.joml.Math.*;

public class BowTracker extends Tracker
{
    private double lastcontrollersDist;
    private double lastcontrollersDot;
    private double controllersDist;
    private double controllersDot;
    private double currentDraw;
    private double lastDraw;
    public boolean isDrawing;
    private boolean pressed;
    private boolean lastpressed;
    private boolean canDraw;
    private boolean lastcanDraw;
    public long startDrawTime;
    private final double notchDotThreshold = 20.0D;
    private double maxDraw;
    private long maxDrawMillis = 1100L;
    private Vec3 aim;
    float tsNotch = 0.0F;
    int hapcounter = 0;
    int lasthapStep = 0;

    public Vec3 getAimVector()
    {
        return this.aim;
    }

    public float getDrawPercent()
    {
        return (float)(this.currentDraw / this.maxDraw);
    }

    public boolean isNotched()
    {
        return this.canDraw || this.isDrawing;
    }

    public static boolean isBow(ItemStack itemStack)
    {
        if (itemStack == ItemStack.EMPTY)
        {
            return false;
        }
        else if (dh.vrSettings.bowMode == BowMode.OFF)
        {
            return false;
        }
        else if (dh.vrSettings.bowMode == BowMode.VANILLA)
        {
            return itemStack.getItem() == Items.BOW;
        }
        else
        {
            return itemStack.getItem().getUseAnimation(itemStack) == UseAnim.BOW;
        }
    }

    public static boolean isHoldingBow(InteractionHand hand)
    {
        return !dh.vrSettings.seated && isBow(mc.player.getItemInHand(hand));
    }

    public static boolean isHoldingBowEither()
    {
        return isHoldingBow(InteractionHand.MAIN_HAND) || isHoldingBow(InteractionHand.OFF_HAND);
    }

    @Override public boolean isActive()
    {
        return this.isActive(null);
    }

    /**
     * Test a specific hand isActive.
     * @param hand the hand to test or either hand (null)
     * @return bow tracker isActive for the specified hand or either hand
     */
    public boolean isActive(@Nullable InteractionHand hand)
    {
        if (mc.player == null)
        {
            return false;
        }
        else if (mc.gameMode == null)
        {
            return false;
        }
        else if (!mc.player.isAlive())
        {
            return false;
        }
        else if (mc.player.isSleeping())
        {
            return false;
        }
        else if (hand != null)
        {
            return isHoldingBow(hand);
        }
        else
        {
            return isHoldingBowEither();
        }
    }

    public boolean isCharged()
    {
        return Util.getMillis() - this.startDrawTime >= this.maxDrawMillis;
    }

    @Override public void reset()
    {
        this.isDrawing = false;
    }

    @Override public EntryPoint getEntryPoint()
    {
        return EntryPoint.SPECIAL_ITEMS;
    }

    @Override public void doProcess()
    {
        VRData vrdata = dh.vrPlayer.getVRDataWorld();

        if (dh.vrSettings.seated)
        {
            this.aim = vrdata.getController(0).getCustomVector(new Vec3(0.0D, 0.0D, 1.0D));
        }
        else
        {
            this.lastcontrollersDist = this.controllersDist;
            this.lastcontrollersDot = this.controllersDot;
            this.lastpressed = this.pressed;
            this.lastDraw = this.currentDraw;
            this.lastcanDraw = this.canDraw;
            this.maxDraw = (double)mc.player.getBbHeight() * 0.22D;

            if (Xplat.isModLoaded("pehkui")) {
                // this is meant to be relative to the base Bb height, not the scaled one
                this.maxDraw /= PehkuiHelper.getPlayerBbScale(mc.player, mc.getFrameTime());
            }

            Vec3 vec3 = vrdata.getController(0).getPosition();
            Vec3 vec31 = vrdata.getController(1).getPosition();
            this.controllersDist = vec31.distanceTo(vec3);
            Vec3 vec32 = new Vec3(0.0D, 1.0D * vrdata.worldScale, 0.0D);
            Vec3 vec33 = vrdata.getHand(1).getCustomVector(vec32).scale(this.maxDraw * 0.5D).add(vec31);
            double d0 = vec3.distanceTo(vec33);
            this.aim = vec3.subtract(vec31).normalize();
            Vec3 vec34 = vrdata.getController(0).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
            Vec3 vec35 = vrdata.getHand(1).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
            this.controllersDot = (180D / PI) * acos((double) convertToVector3f(vec35).dot(convertToVector3f(vec34)));
            this.pressed = mc.options.keyAttack.isDown();
            float f = 0.15F * vrdata.worldScale;
            boolean flag = isHoldingBow(InteractionHand.MAIN_HAND);
            InteractionHand interactionhand = flag ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack itemstack = ItemStack.EMPTY;
            ItemStack itemstack1 = ItemStack.EMPTY;

            if (flag)
            {
                itemstack1 = mc.player.getMainHandItem();
                itemstack = mc.player.getProjectile(itemstack1);
            }
            else
            {
                if (mc.player.getMainHandItem().is(ItemTags.ARROWS))
                {
                    itemstack = mc.player.getMainHandItem();
                }

                itemstack1 = mc.player.getOffhandItem();
            }

            int i = itemstack1.getUseDuration();
            int j = itemstack1.getUseDuration() - 15;
            int k = 0;

            if (itemstack != ItemStack.EMPTY && d0 <= (double)f && this.controllersDot <= 20.0D)
            {
                if (!this.canDraw)
                {
                    this.startDrawTime = Util.getMillis();
                }

                this.canDraw = true;
                this.tsNotch = (float)Util.getMillis();

                if (!this.isDrawing)
                {
                    ((PlayerExtension) mc.player).setItemInUseClient(itemstack1, interactionhand);
                    ((PlayerExtension) mc.player).setItemInUseCountClient(i);
                    //Minecraft.getInstance().physicalGuiManager.preClickAction();
                }
            }
            else if ((float)Util.getMillis() - this.tsNotch > 500.0F)
            {
                this.canDraw = false;
                ((PlayerExtension) mc.player).setItemInUseClient(ItemStack.EMPTY, interactionhand);
            }

            if (!this.isDrawing && this.canDraw && this.pressed && !this.lastpressed)
            {
                this.isDrawing = true;
                //Minecraft.getInstance().physicalGuiManager.preClickAction();
                mc.gameMode.useItem(mc.player, interactionhand);
            }

            if (this.isDrawing && !this.pressed && this.lastpressed && (double)this.getDrawPercent() > 0.0D)
            {
                dh.vr.triggerHapticPulse(0, 500);
                dh.vr.triggerHapticPulse(1, 3000);
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(this.getDrawPercent()).array());
                mc.getConnection().send(serverboundcustompayloadpacket);
                mc.gameMode.releaseUsingItem(mc.player);
                serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(0.0F).array());
                mc.getConnection().send(serverboundcustompayloadpacket);
                this.isDrawing = false;
            }

            if (!this.pressed)
            {
                this.isDrawing = false;
            }

            if (!this.isDrawing && this.canDraw && !this.lastcanDraw)
            {
                dh.vr.triggerHapticPulse(1, 800);
                dh.vr.triggerHapticPulse(0, 800);
            }

            if (this.isDrawing)
            {
                this.currentDraw = (this.controllersDist - (double)f) / vrdata.worldScale;

                if (this.currentDraw > this.maxDraw)
                {
                    this.currentDraw = this.maxDraw;
                }

                int j1 = 0;

                if (this.getDrawPercent() > 0.0F)
                {
                    j1 = (int)(this.getDrawPercent() * 500.0F) + 700;
                }

                int l = (int)((float)itemstack1.getUseDuration() - this.getDrawPercent() * (float)this.maxDrawMillis);
                ((PlayerExtension) mc.player).setItemInUseClient(itemstack1, interactionhand);
                double d1 = this.getDrawPercent();

                if (d1 >= 1.0D)
                {
                    ((PlayerExtension) mc.player).setItemInUseCountClient(k);
                }
                else if (d1 > 0.4D)
                {
                    ((PlayerExtension) mc.player).setItemInUseCountClient(j);
                }
                else
                {
                    ((PlayerExtension) mc.player).setItemInUseCountClient(i);
                }

                int i1 = (int)(d1 * 4.0D * 4.0D * 3.0D);

                if (i1 % 2 == 0 && this.lasthapStep != i1)
                {
                    dh.vr.triggerHapticPulse(0, j1);

                    if (d1 == 1.0D)
                    {
                        dh.vr.triggerHapticPulse(1, j1);
                    }
                }

                if (this.isCharged() && this.hapcounter % 4 == 0)
                {
                    dh.vr.triggerHapticPulse(1, 200);
                }

                this.lasthapStep = i1;
                ++this.hapcounter;
            }
            else
            {
                this.hapcounter = 0;
                this.lasthapStep = 0;
            }
        }
    }
}
