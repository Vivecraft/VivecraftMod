package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.ItemInUseTracker;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client.utils.ScaleHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.data.ViveItemTags;

public class BowTracker implements ItemInUseTracker, DebugRenderTracker {
    private static final long MAX_DRAW_MILLIS = 1100L;
    private static final double NOTCH_DOT_THRESHOLD = 20F;

    // when the arrow was started drawing, to handle charged shots
    public long startDrawTime;
    private boolean wasDrawing;

    private boolean canDraw;
    private float currentDraw;
    private float maxDraw;
    private Vector3f aim;

    // when the arrow was nocked,
    private float tsNotch = 0.0F;
    private int hapCounter = 0;
    private int lastHapStep = 0;

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public BowTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    public Vector3fc getAimVector() {
        return this.aim;
    }

    public float getDrawPercent() {
        return this.currentDraw / this.maxDraw;
    }

    public boolean isNotched() {
        return this.canDraw || this.isDrawing();
    }

    public boolean isCharged() {
        return Util.getMillis() - this.startDrawTime >= MAX_DRAW_MILLIS;
    }

    public boolean isDrawing() {
        return this.dh.bowModule.isPressed();
    }

    public static boolean isBow(ItemStack itemStack) {
        if (itemStack == ItemStack.EMPTY) {
            return false;
        } else if (ClientDataHolderVR.getInstance().vrSettings.bowMode == VRSettings.BowMode.OFF) {
            return false;
        } else if (ClientDataHolderVR.getInstance().vrSettings.bowMode == VRSettings.BowMode.VANILLA) {
            return itemStack.getItem() == Items.BOW;
        } else {
            return itemStack.getItem().getUseAnimation(itemStack) == ItemUseAnimation.BOW && !itemStack.is(
                ViveItemTags.VIVECRAFT_BOW_EXCLUSION);
        }
    }

    public static boolean isHoldingBow(LivingEntity entity, InteractionHand hand) {
        return !ClientDataHolderVR.getInstance().vrSettings.seated && isBow(entity.getItemInHand(hand));
    }

    public static boolean isHoldingBowEither(LivingEntity entity) {
        return isHoldingBow(entity, InteractionHand.MAIN_HAND) || isHoldingBow(entity, InteractionHand.OFF_HAND);
    }

    @Override
    public boolean itemInUse(LocalPlayer player) {
        return this.isDrawing();
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (player == null) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (this.mc.screen != null) {
            return false;
        } else if (KeyboardHandler.SHOWING) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else {
            return isHoldingBowEither(player);
        }
    }

    @Override
    public void inactiveProcess(LocalPlayer player) {
        this.wasDrawing = false;
        this.canDraw = false;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_FRAME;
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        VRData vrData = this.dh.vrPlayer.getVRDataWorld();
        boolean lastCanDraw = this.canDraw;

        this.maxDraw = this.mc.player.getBbHeight() * 0.22F;

        // this is meant to be relative to the base Bb height, not the scaled one
        this.maxDraw /= ScaleHelper.getEntityBbScale(player, ClientUtils.getCurrentPartialTick());

        int bowHand = 1;
        int arrowHand = 0;

        // reverse bow hands
        if (this.dh.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow()) {
            bowHand = 0;
            arrowHand = 1;
        }

        // these are wrong since this is called every frame but should be fine so long as they're only compared to each other.
        Vec3 arrowPos = vrData.getController(arrowHand).getPosition();
        Vec3 bowPos = vrData.getController(bowHand).getPosition();
        //

        float controllersDist = (float) bowPos.distanceTo(arrowPos);
        Vector3f up = new Vector3f(0.0F, vrData.worldScale, 0.0F);

        Vec3 stringPos = new Vec3(vrData.getHand(bowHand).getCustomVector(up).mul(this.maxDraw * 0.5F)).add(bowPos);

        double notchDist = arrowPos.distanceTo(stringPos);

        this.aim = MathUtils.subtractToVector3f(arrowPos, bowPos).normalize();

        Vector3f arrowAim = vrData.getController(arrowHand).getCustomVector(MathUtils.BACK);
        Vector3f bowAim = vrData.getHand(bowHand).getCustomVector(MathUtils.DOWN);

        double controllersDot = Math.toDegrees(Math.acos(bowAim.dot(arrowAim)));

        float notchDistThreshold = 0.15F * vrData.worldScale;
        boolean main = isHoldingBow(player, InteractionHand.MAIN_HAND);

        ItemStack ammo = ItemStack.EMPTY;
        ItemStack bow = ItemStack.EMPTY;

        if (main) {
            // autofind ammo, this only works for items that extend ProjectileWeaponItem
            bow = player.getMainHandItem();
            ammo = player.getProjectile(bow);
        } else {
            // brig your own ammo
            if (player.getMainHandItem().is(ItemTags.ARROWS)) {
                ammo = player.getMainHandItem();
            }
            bow = player.getOffhandItem();
        }

        int stage0 = bow.getUseDuration(player);
        int stage1 = bow.getUseDuration(player) - 15;
        int stage2 = 0;

        if (ammo != ItemStack.EMPTY &&
            notchDist <= notchDistThreshold &&
            controllersDot <= NOTCH_DOT_THRESHOLD)
        {
            this.canDraw = true;
            this.tsNotch = (float) Util.getMillis();

            if (!this.isDrawing()) {
                // set client side so that it renders correctly
                ((PlayerExtension) player).vivecraft$setItemInUseRemainingClient(stage0);
                // Minecraft.getInstance().physicalGuiManager.preClickAction();
            }
        } else if (!this.isDrawing() && (float) Util.getMillis() - this.tsNotch > 500.0F) {
            this.canDraw = false;
        }

        // start counting when we started drawing
        if (this.isDrawing() && !this.wasDrawing) {
            this.startDrawTime = Util.getMillis();
        }

        if (!this.isDrawing() && this.canDraw && !lastCanDraw) {
            // notch
            this.dh.vr.triggerHapticPulse(arrowHand, 800);
            this.dh.vr.triggerHapticPulse(bowHand, 800);
        }

        if (this.isDrawing()) {
            this.currentDraw = (controllersDist - notchDistThreshold) / vrData.worldScale;

            if (this.currentDraw > this.maxDraw) {
                this.currentDraw = this.maxDraw;
            }

            int hapStrength = 0;

            if (this.getDrawPercent() > 0.0F) {
                hapStrength = (int) (this.getDrawPercent() * 500.0F) + 700;
            }

            // set client side so that it renders correctly
            double drawPercent = this.getDrawPercent();

            if (drawPercent >= 1.0D) {
                ((PlayerExtension) player).vivecraft$setItemInUseRemainingClient(stage2);
            } else if (drawPercent > 0.4D) {
                ((PlayerExtension) player).vivecraft$setItemInUseRemainingClient(stage1);
            } else {
                ((PlayerExtension) player).vivecraft$setItemInUseRemainingClient(stage0);
            }

            int hapStep = (int) (drawPercent * 4.0D * 4.0D * 3.0D);

            if (hapStep % 2 == 0 && this.lastHapStep != hapStep) {
                this.dh.vr.triggerHapticPulse(arrowHand, hapStrength);

                if (drawPercent == 1.0D) {
                    this.dh.vr.triggerHapticPulse(bowHand, hapStrength);
                }
            }

            if (this.isCharged() && this.hapCounter % 4 == 0) {
                // TODO: this should probably be on tick, and framerate independent
                this.dh.vr.triggerHapticPulse(bowHand, 200);
            }

            this.lastHapStep = hapStep;
            this.hapCounter++;
        } else {
            this.hapCounter = 0;
            this.lastHapStep = 0;
        }

        this.wasDrawing = this.isDrawing();
    }

    @Override
    public void renderDebug() {
        VRData world = this.dh.vrPlayer.getVRDataWorld();
        Vec3 cam = world.getEye(this.dh.currentPass).getPosition();
        int bowHand = this.dh.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow() ? 0 : 1;
        Vector3f bowPos = MathUtils.subtractToVector3f(world.getController(bowHand).getPosition(), cam);
        if (this.isDrawing() || this.dh.vrSettings.seated) {
            // aim dir
            DebugRenderHelper.renderLine(MathUtils.RED, bowPos, this.aim.mul(-1F, new Vector3f()).add(bowPos));
        } else {
            float dist = 0.15F * world.worldScale;
            VRData.VRDevicePose bowHandPose = world.getHand(bowHand);
            VRData.VRDevicePose arrowPose = world.getController(1 - bowHand);

            // bow distance threshold and angle cone
            Vector3f stringPos = bowHandPose.getCustomVector(MathUtils.UP)
                .mul(world.worldScale * this.maxDraw * 0.5F).add(bowPos);
            DebugRenderHelper.renderSphere(stringPos, dist,
                isNotched() ? MathUtils.GREEN : MathUtils.RED);
            Vector3f bowDir = bowHandPose.getCustomVector(MathUtils.DOWN);
            DebugRenderHelper.renderCone(bowDir.mul(-dist, new Vector3f()).add(stringPos), bowDir, 20.F,
                0.25F * world.worldScale, isNotched() ? MathUtils.GREEN : MathUtils.RED);

            // arrow point dir
            DebugRenderHelper.renderLine(isNotched() ? MathUtils.GREEN : MathUtils.RED,
                MathUtils.subtractToVector3f(arrowPose.getPosition(), cam),
                MathUtils.subtractToVector3f(arrowPose.getPosition(), cam)
                    .add(arrowPose.getDirection().mul(world.worldScale)));
        }
    }
}
