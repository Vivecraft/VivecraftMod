package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.settings.VRSettings.BowMode;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static org.joml.Math.acos;
import static org.joml.Math.toDegrees;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class BowTracker extends Tracker {
    private float currentDraw;
    public boolean isDrawing;
    private boolean pressed;
    private boolean canDraw;
    public long startDrawTime;
    private float maxDraw;
    private long maxDrawMillis = 1100L;
    private final Vector3f aim = new Vector3f();
    float tsNotch = 0.0F;
    int hapcounter = 0;
    int lasthapStep = 0;

    public Vector3fc getAimVector() {
        return this.aim;
    }

    public float getDrawPercent() {
        return (float) (this.currentDraw / this.maxDraw);
    }

    public boolean isNotched() {
        return this.canDraw || this.isDrawing;
    }

    public static boolean isBow(ItemStack itemStack) {
        if (itemStack == ItemStack.EMPTY) {
            return false;
        } else if (dh.vrSettings.bowMode == BowMode.OFF) {
            return false;
        } else if (dh.vrSettings.bowMode == BowMode.VANILLA) {
            return itemStack.getItem() == Items.BOW;
        } else {
            return itemStack.getItem().getUseAnimation(itemStack) == UseAnim.BOW;
        }
    }

    public static boolean isHoldingBow(InteractionHand hand) {
        return !dh.vrSettings.seated && isBow(mc.player.getItemInHand(hand));
    }

    public static boolean isHoldingBowEither() {
        return isHoldingBow(InteractionHand.MAIN_HAND) || isHoldingBow(InteractionHand.OFF_HAND);
    }

    @Override
    public boolean isActive() {
        return this.isActive(null);
    }

    /**
     * Test a specific hand isActive.
     *
     * @param hand the hand to test or either hand (null)
     * @return bow tracker isActive for the specified hand or either hand
     */
    public boolean isActive(@Nullable InteractionHand hand) {
        if (mc.player == null) {
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else if (!mc.player.isAlive()) {
            return false;
        } else if (mc.player.isSleeping()) {
            return false;
        } else if (hand != null) {
            return isHoldingBow(hand);
        } else {
            return isHoldingBowEither();
        }
    }

    public boolean isCharged() {
        return Util.getMillis() - this.startDrawTime >= this.maxDrawMillis;
    }

    @Override
    public void reset() {
        this.isDrawing = false;
    }

    @Override
    public EntryPoint getEntryPoint() {
        return EntryPoint.SPECIAL_ITEMS;
    }

    @Override
    public void doProcess() {
        VRData vrdata = dh.vrPlayer.getVRDataWorld();

        if (dh.vrSettings.seated) {
            vrdata.getController(0).getCustomVector(this.aim.set(0.0F, 0.0F, 1.0F));
        } else {
            boolean lastpressed = this.pressed;
            boolean lastcanDraw = this.canDraw;
            this.maxDraw = mc.player.getBbHeight() * 0.22F;
            PlayerExtension playerExt = (PlayerExtension) mc.player;

            if (Xplat.isModLoaded("pehkui")) {
                // this is meant to be relative to the base Bb height, not the scaled one
                this.maxDraw /= PehkuiHelper.getPlayerBbScale(mc.player, mc.getFrameTime());
            }

            Vector3fc rightPos = vrdata.getController(0).getPosition(new Vector3f());
            Vector3fc leftPos = vrdata.getController(1).getPosition(new Vector3f());
            float controllersDist = leftPos.distance(rightPos);
            Vector3fc stringPos = vrdata.getHand(1).getCustomVector(new Vector3f(0.0F, vrdata.worldScale, 0.0F)).mul(this.maxDraw * 0.5F).add(leftPos);
            float notchDist = rightPos.distance(stringPos);
            rightPos.sub(leftPos, this.aim).normalize();
            Vector3fc rightAim = vrdata.getController(0).getCustomVector(new Vector3f(0.0F, 0.0F, -1.0F));
            Vector3fc leftAim = vrdata.getHand(1).getCustomVector(new Vector3f(0.0F, -1.0F, 0.0F));
            float controllersDot = (float) toDegrees(acos(leftAim.dot(rightAim)));
            this.pressed = mc.options.keyAttack.isDown();
            float notchDistThreshold = 0.15F * vrdata.worldScale;
            boolean main = isHoldingBow(InteractionHand.MAIN_HAND);
            InteractionHand interactionhand = main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            final ItemStack bow;
            final ItemStack ammo;
            if (main) {
                ammo = mc.player.getProjectile(bow = mc.player.getMainHandItem());
            } else {
                bow = mc.player.getOffhandItem();
                if (mc.player.getMainHandItem().is(ItemTags.ARROWS)){
                    ammo = mc.player.getMainHandItem();
                } else {
                    ammo = ItemStack.EMPTY;
                }
            };

            int stage0 = bow.getUseDuration();
            int stage1 = bow.getUseDuration() - 15;
            int stage2 = 0;

            if (ammo != ItemStack.EMPTY && notchDist <= notchDistThreshold && controllersDot <= 20.0F) {
                if (!this.canDraw) {
                    this.startDrawTime = Util.getMillis();
                }

                this.canDraw = true;
                this.tsNotch = (float) Util.getMillis();

                if (!this.isDrawing) {
                    playerExt.vivecraft$setItemInUseClient(bow, interactionhand);
                    playerExt.vivecraft$setItemInUseCountClient(stage0);
                    //Minecraft.getInstance().physicalGuiManager.preClickAction();
                }
            } else if ((float) Util.getMillis() - this.tsNotch > 500.0F) {
                this.canDraw = false;
                playerExt.vivecraft$setItemInUseClient(ItemStack.EMPTY, interactionhand);
            }

            if (!this.isDrawing && this.canDraw && this.pressed && !lastpressed) {
                this.isDrawing = true;
                //Minecraft.getInstance().physicalGuiManager.preClickAction();
                mc.gameMode.useItem(mc.player, interactionhand);
            }

            if (this.isDrawing && !this.pressed && lastpressed && this.getDrawPercent() > 0.0F) {
                dh.vr.triggerHapticPulse(0, 500);
                dh.vr.triggerHapticPulse(1, 3000);
                mc.getConnection().send(ClientNetworking.getVivecraftClientPacket(PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(this.getDrawPercent()).array()));
                mc.gameMode.releaseUsingItem(mc.player);
                mc.getConnection().send(ClientNetworking.getVivecraftClientPacket(PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat(0.0F).array()));
                this.isDrawing = false;
            }

            if (!this.pressed) {
                this.isDrawing = false;
            }

            if (!this.isDrawing && this.canDraw && !lastcanDraw) {
                dh.vr.triggerHapticPulse(1, 800);
                dh.vr.triggerHapticPulse(0, 800);
            }

            if (this.isDrawing) {
                this.currentDraw = (controllersDist - notchDistThreshold) / vrdata.worldScale;

                if (this.currentDraw > this.maxDraw) {
                    this.currentDraw = this.maxDraw;
                }

                float drawPerc = this.getDrawPercent();
                int hap;

                if (drawPerc > 0.0F) {
                    hap = (int) (drawPerc * 500.0F) + 700;
                } else {
                    hap = 0;
                }

                playerExt.vivecraft$setItemInUseClient(bow, interactionhand);

                if (drawPerc >= 1.0F) {
                    playerExt.vivecraft$setItemInUseCountClient(stage2);
                } else if (drawPerc > 0.4F) {
                    playerExt.vivecraft$setItemInUseCountClient(stage1);
                } else {
                    playerExt.vivecraft$setItemInUseCountClient(stage0);
                }

                int hapStep = (int) (drawPerc * 4.0F * 4.0F * 3.0F);

                if ((hapStep % 2) == 0 && this.lasthapStep != hapStep) {
                    dh.vr.triggerHapticPulse(0, hap);

                    if (drawPerc == 1.0F) {
                        dh.vr.triggerHapticPulse(1, hap);
                    }
                }

                if (this.isCharged() && this.hapcounter % 4 == 0) {
                    dh.vr.triggerHapticPulse(1, 200);
                }

                this.lasthapStep = hapStep;
                ++this.hapcounter;
            } else {
                this.hapcounter = 0;
                this.lasthapStep = 0;
            }
        }
    }
}
