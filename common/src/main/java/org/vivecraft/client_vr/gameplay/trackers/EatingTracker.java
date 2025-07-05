package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.ItemInUseTracker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.common.utils.MathUtils;

public class EatingTracker implements ItemInUseTracker, DebugRenderTracker {
    private static final float MOUTH_TO_EYE_DISTANCE = 0.0F;
    private static final float THRESHOLD = 0.25F;
    private static final long EAT_TIME = 2100L;

    private final Vector3f[] foodPos = new Vector3f[2];

    private final boolean[] eating = new boolean[2];
    private long eatStart;

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public EatingTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return false;
        } else if (player == null) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else if (this.dh.bowTracker.isActive(player)) {
            return false;
        } else {
            return isEatable(player.getMainHandItem()) || isEatable(player.getOffhandItem());
        }
    }

    private boolean isEatable(ItemStack stack) {
        if (stack != null) {
            ItemUseAnimation anim = stack.getUseAnimation();
            return anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK ||
                anim == ItemUseAnimation.TOOT_HORN;
        }
        return false;
    }

    @Override
    public boolean itemInUse(LocalPlayer player) {
        return this.eating[0] || this.eating[1];
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        this.eating[0] = false;
        this.eating[1] = false;
        this.foodPos[0] = null;
        this.foodPos[1] = null;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        VRData room_pre = this.dh.vrPlayer.vrdata_room_pre;
        Vector3f hmdPos = room_pre.hmd.getPositionF();
        Vector3f mouthPos = room_pre.hmd.getCustomVector(new Vector3f(0.0F, -MOUTH_TO_EYE_DISTANCE, 0.0F))
            .add(hmdPos);

        for (int c = 0; c < 2; c++) {
            ItemStack itemstack = c == 0 ? player.getMainHandItem() : player.getOffhandItem();
            if (!isEatable(itemstack)) {
                this.foodPos[c] = null;
                continue;
            }
            this.foodPos[c] = this.dh.vr.controllerHistory[c].averagePosition(0.333D)
                .add(room_pre.getController(c).getDirection().mul(0.2F));

            if (mouthPos.distance(this.foodPos[c]) < THRESHOLD) {

                int crunchiness = 0;

                if (itemstack.getUseAnimation() == ItemUseAnimation.DRINK) { // that's how liquid works.
                    if (room_pre.getController(c).getCustomVector(MathUtils.UP).y > 0) {
                        this.eating[c] = false;
                        continue;
                    }
                } else if (itemstack.getUseAnimation() == ItemUseAnimation.EAT) {
                    crunchiness = 2;
                } else if (itemstack.getUseAnimation() == ItemUseAnimation.TOOT_HORN) {
                    crunchiness = 1;
                } else {
                    continue;
                }

                if (!this.eating[c]) {
                    // Minecraft.getInstance().physicalGuiManager.preClickAction();

                    if (this.mc.gameMode.useItem(player, c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)
                        .consumesAction())
                    {
                        this.mc.gameRenderer.itemInHandRenderer.itemUsed(
                            c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                        this.eating[c] = true;
                        this.eatStart = Util.getMillis();
                    }
                }

                if (this.eating[c]) {
                    long k = player.getUseItemRemainingTicks();

                    if (k > 0L && k % 5L <= (long) crunchiness) {
                        this.dh.vr.triggerHapticPulse(c, 700);
                    }
                }

                if (Util.getMillis() - this.eatStart > EAT_TIME) {
                    this.eating[c] = false;
                }
            } else {
                this.eating[c] = false;
            }
        }
    }

    @Override
    public void renderDebug() {
        VRData world = this.dh.vrPlayer.getVRDataWorld();
        Vec3 cam = world.getEye(this.dh.currentPass).getPosition();
        for (int c = 0; c < 2; c++) {
            if (this.foodPos[c] != null) {
                Vector3fc food = MathUtils.subtractToVector3f(VRPlayer.roomToWorldPos(this.foodPos[c], world), cam);
                // food pos
                DebugRenderHelper.renderCube(food, 0.05F, this.eating[c] ? MathUtils.GREEN : MathUtils.RED);
                // food distance threshold
                DebugRenderHelper.renderSphere(food, THRESHOLD, this.eating[c] ? MathUtils.GREEN : MathUtils.RED);
            }
        }
    }
}
