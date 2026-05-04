package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.render.renderstates.TeleportRenderState;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.data.ViveItems;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import javax.annotation.Nullable;

public class VRArmHelper {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    private static final Vec3i TP_UNLIMITED_COLOR = new Vec3i(173, 216, 230);
    private static final Vec3i TP_LIMITED_COLOR = new Vec3i(205, 169, 205);
    private static final Vec3i TP_INVALID_COLOR = new Vec3i(83, 83, 83);

    /**
     * @return if first person hands should be rendered in the current RenderPass
     */
    public static boolean shouldRenderHands() {
        if (DATA_HOLDER.viewOnly) {
            return false;
        } else if (DATA_HOLDER.currentPass == RenderPass.THIRD) {
            return DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;
        } else {
            return DATA_HOLDER.currentPass != RenderPass.CAMERA;
        }
    }

    /**
     * renders the VR hands
     *
     * @param vrState      VR renderstate
     * @param renderMain   if the main hand should be rendered
     * @param renderOff    if the offhand should be rendered
     * @param menuHandMain if the right hand should render as the menu hand
     * @param menuHandOff  if the left hand should render as the menu hand
     */
    public static void renderVRHands(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean renderMain, boolean renderOff, boolean menuHandMain, boolean menuHandOff)
    {
        if (!renderMain && !renderOff) return;
        Profiler.get().push("hands");
        // TODO 26.1 this will not work, is it still needed though?
        DATA_HOLDER.isFpHand = true;

        // TODO 26.1
        //VREffectsHelper.removeNausea(partialTick);

        if (renderMain) {
            if (menuHandMain) {
                renderMenuHand(output, vrState, cameraState, poseStack, 0, false);
            } else {
                renderVRHand_Main(output, vrState, cameraState, poseStack);
            }
        }

        if (renderOff) {
            if (menuHandOff) {
                renderMenuHand(output, vrState, cameraState, poseStack, 1, false);
            } else {
                renderVRHand_Offhand(output, vrState, cameraState, poseStack, true);
            }
        }

        // TODO 26.1
        //VREffectsHelper.reAddNausea();

        DATA_HOLDER.isFpHand = false;
        Profiler.get().pop();
    }

    /**
     * renders a menu hand for the specified controller, which is a gray box
     *
     * @param vrState     VR renderstate
     * @param c           controller to render the hand for
     * @param depthAlways if depth testing should be disabled for rendering
     */
    public static void renderMenuHand(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack, int c, boolean depthAlways)
    {

        float lightPercent = vrState.armsState.headLight / 15F;
        Vec3i color = new Vec3i(
            (int) (64 * lightPercent),
            (int) (64 * lightPercent),
            (int) (64 * lightPercent));
        byte alpha = (byte) 255;

        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(0D, 0D, 0.18D);

        poseStack.pushPose();
        poseStack.translate(
            (c == 0 ? vrState.armsState.mainHandWorldPos.x : vrState.armsState.offHandWorldPos.x) - cameraState.pos.x,
            (c == 0 ? vrState.armsState.mainHandWorldPos.y : vrState.armsState.offHandWorldPos.y) - cameraState.pos.y,
            (c == 0 ? vrState.armsState.mainHandWorldPos.z : vrState.armsState.offHandWorldPos.z) - cameraState.pos.z);
        poseStack.mulPose(c == 0 ? vrState.armsState.mainHandWorldRot : vrState.armsState.offHandWorldRot);

        RenderType renderType = VRRenderTypes.quads(depthAlways && c == 0);

        output.order(RenderHelper.getPipelineRenderOrder(renderType))
            .submitCustomGeometry(poseStack, renderType,
                (pose, consumer) -> RenderHelper.renderBox(consumer, start, end, -0.02F, 0.02F, -0.0125F, 0.0125F,
                    color, alpha, pose));

        poseStack.popPose();
    }

    public static ItemStack extractHandRenderItem(LocalPlayer player, InteractionHand hand) {
        if (player == null) return ItemStack.EMPTY;

        ItemStack item = player.getItemInHand(hand);
        ItemStack override = null; // physicalGuiManager.getHeldItemOverride();

        if (override != null) {
            item = override;
        }

        // climbey override
        if (DATA_HOLDER.climbTracker.isClimbeyClimb() && !ViveItems.isClimbingClaws(item) && override == null) {
            item = player.getItemInHand(InteractionHand.values()[1 - hand.ordinal()]);
        }

        // Roomscale bow override
        return getBowOverride(item, hand);
    }

    /**
     * renders the main minecraft hand
     *
     * @param partialTick current partial tick
     */
    public static void renderVRHand_Main(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack)
    {
        if (vrState.armsState.skipMainHandItemRendering) return;

        poseStack.pushPose();
        poseStack.translate(
            cameraState.pos.x - vrState.armsState.mainHandWorldPos.x,
            cameraState.pos.y - vrState.armsState.mainHandWorldPos.y,
            cameraState.pos.z - vrState.armsState.mainHandWorldPos.z);
        poseStack.mulPose(vrState.armsState.mainHandWorldRot);

        // TODO 26.1 this doesn't work like that, do we still need that?
        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            // if we don't do this shaders render the hands wrong
            OptifineHelper.beginEntities();
        }

        MC.gameRenderer.itemInHandRenderer.renderArmWithItem(MC.player, vrState.partialTick, 0.0F,
            InteractionHand.MAIN_HAND, MC.player.getAttackAnim(vrState.partialTick),
            vrState.armsState.mainHandRenderItem, 0.0F, poseStack, output, vrState.armsState.rawHeadLightCoords);

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            // undo the thing we did before
            OptifineHelper.endEntities();
        }
        poseStack.popPose();
    }

    /**
     * renders the offhand minecraft hand
     *
     * @param partialTick    current partial tick
     * @param renderTeleport if the teleport arc should be rendered
     */
    public static void renderVRHand_Offhand(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean renderTeleport)
    {
        // don't render claws with model arms
        if (!vrState.armsState.skipOffHandItemRendering){
            poseStack.pushPose();
            poseStack.translate(
                cameraState.pos.x - vrState.armsState.offHandWorldPos.x,
                cameraState.pos.y - vrState.armsState.offHandWorldPos.y,
                cameraState.pos.z - vrState.armsState.offHandWorldPos.z);
            poseStack.mulPose(vrState.armsState.offHandWorldRot);

            // TODO 26.1 this doesn't work like that, do we still need that?
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                // if we don't do this shaders render the hands wrong
                OptifineHelper.beginEntities();
            }

            MC.gameRenderer.itemInHandRenderer.renderArmWithItem(MC.player, vrState.partialTick, 0.0F,
                InteractionHand.OFF_HAND, MC.player.getAttackAnim(vrState.partialTick),
                vrState.armsState.offHandRenderItem, 0.0F, poseStack, output, vrState.armsState.rawHeadLightCoords);

            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                // undo the thing we did before
                OptifineHelper.endEntities();
            }

            // back to hmd rendering
            poseStack.popPose();
        }

        // teleport arc
        if (renderTeleport) {
            // TP energy
            if (vrState.teleportState.tpEnergy) {
                poseStack.pushPose();
                poseStack.translate(
                    cameraState.pos.x - vrState.armsState.offHandWorldPos.x,
                    cameraState.pos.y - vrState.armsState.offHandWorldPos.y,
                    cameraState.pos.z - vrState.armsState.offHandWorldPos.z);
                poseStack.mulPose(vrState.armsState.offHandWorldRot);

                Vec3 start = new Vec3(0.0D, 0.005D, 0.03D);
                float max = 0.03F;

                // TODO SHADERS use a shader with lightmaps

                if (vrState.teleportState.tpEnergySize > 0.0F) {
                    // tp energy quad, slightly above the max energy quad
                    RenderHelper.renderFlatQuad(start.add(0.0D, 0.05001D, 0.0D),
                        vrState.teleportState.tpEnergySize * max, vrState.teleportState.tpEnergySize * max, 0.0F,
                        TP_LIMITED_COLOR.getX(), TP_LIMITED_COLOR.getY(), TP_LIMITED_COLOR.getZ(), 128,
                        poseStack, false, output);
                }
                // max energy quad
                RenderHelper.renderFlatQuad(start.add(0.0D, 0.05D, 0.0D), max, max, 0.0F, TP_LIMITED_COLOR.getX(),
                    TP_LIMITED_COLOR.getY(), TP_LIMITED_COLOR.getZ(), 50, poseStack, false, output);

                poseStack.popPose();
            }

            if (vrState.teleportState.aiming) {
                // renders from the head
                if (vrState.teleportState.arcAiming) {
                    renderTeleportArc(output, cameraState, vrState.teleportState, poseStack);
                } /* else {
                    renderTeleportLine(poseStack);
                }*/
            }
        }
    }

    public static void extractTeleport(TeleportRenderState teleportState, @Nullable LocalPlayer player) {
        if (player == null) {
            teleportState.tpEnergy = false;
            teleportState.aiming = false;
            return;
        }
        teleportState.tpEnergy = ClientNetworking.isLimitedSurvivalTeleport() &&
            !DATA_HOLDER.vrPlayer.getFreeMove() &&
            MC.gameMode != null && MC.gameMode.hasMissTime() &&
            DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming &&
            !DATA_HOLDER.bowTracker.isActive(player);

        teleportState.aiming = DATA_HOLDER.teleportTracker.isAiming();

        if (teleportState.aiming) {
            teleportState.tpEnergySize = 2.0F * (DATA_HOLDER.teleportTracker.getTeleportEnergy() -
                4.0F * (float) DATA_HOLDER.teleportTracker.movementTeleportDistance
            ) / 100.0F;
        } else {
            teleportState.tpEnergySize = 2.0F * DATA_HOLDER.teleportTracker.getTeleportEnergy() / 100.0F;
        }

        teleportState.arcAiming = DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming;

        teleportState.showBeam = teleportState.aiming &&
            DATA_HOLDER.teleportTracker.vrMovementStyle.showBeam &&
            DATA_HOLDER.teleportTracker.isAiming() &&
            DATA_HOLDER.teleportTracker.movementTeleportArcSteps > 1;

        // don't need any of the arc stuff if we are not aiming
        if (!teleportState.showBeam) return;

        teleportState.dest = DATA_HOLDER.teleportTracker.getDestination();
        teleportState.validLocation =
            teleportState.dest.x != 0.0D || teleportState.dest.y != 0.0D || teleportState.dest.z != 0.0D;
        teleportState.showHitIndicator = DATA_HOLDER.teleportTracker.movementTeleportProgress >= 1.0D;

        double vOffset;
        if (!teleportState.validLocation) {
            // invalid location
            teleportState.color = TP_INVALID_COLOR;
            teleportState.alpha = (byte) 128;
            vOffset = DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset;
        } else {
            teleportState.alpha = (byte) 255;
            if (ClientNetworking.isLimitedSurvivalTeleport() && !player.getAbilities().mayfly) {
                teleportState.color = TP_LIMITED_COLOR;
            } else {
                teleportState.color = TP_UNLIMITED_COLOR;
            }

            vOffset = Util.getMillis() * 0.001D
                * (double) DATA_HOLDER.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
            DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset = vOffset;
        }

        teleportState.segmentHalfWidth = DATA_HOLDER.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
        int segments = DATA_HOLDER.teleportTracker.movementTeleportArcSteps - 1;

        if (DATA_HOLDER.teleportTracker.vrMovementStyle.beamGrow) {
            segments = (int) (segments * DATA_HOLDER.teleportTracker.movementTeleportProgress);
        }
        teleportState.segments.clear();
        if (segments > 0) {
            float segmentProgress = 1.0F / (float) segments;
            for (int i = 0; i < segments; i++) {
                float progress = Mth.frac((float) i / (float) segments + (float) (vOffset * segmentProgress));

                Vec3 start = DATA_HOLDER.teleportTracker.getInterpolatedArcPosition(progress - segmentProgress * 0.4F);

                Vec3 end = DATA_HOLDER.teleportTracker.getInterpolatedArcPosition(progress);

                teleportState.segments.add(new TeleportRenderState.Segment(start, end, progress * 2.0F));
            }
        }
    }

    /**
     * returns the hold item based on the roomscale bow state
     *
     * @param itemStack       the original item in the hand
     * @param interactionHand hand that should be checked
     * @return the overridden item, based on bow state
     */
    private static ItemStack getBowOverride(ItemStack itemStack, InteractionHand interactionHand) {
        if (DATA_HOLDER.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow()) {
            // reverse bow hands
            interactionHand =
                interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }

        if (interactionHand == InteractionHand.MAIN_HAND) {
            // main hand has the ammo
            if (BowTracker.isHoldingBow(MC.player, InteractionHand.MAIN_HAND)) {
                // do ammo override
                ItemStack ammo = MC.player.getProjectile(MC.player.getMainHandItem());

                if (ammo != ItemStack.EMPTY && !DATA_HOLDER.bowTracker.isNotched()) {
                    // render the arrow in right, left hand will check for and render bow.
                    itemStack = ammo;
                } else {
                    itemStack = ItemStack.EMPTY;
                }
            } else if (BowTracker.isHoldingBow(MC.player, InteractionHand.OFF_HAND) &&
                DATA_HOLDER.bowTracker.isNotched())
            {
                // don't render a hand item if the bow is notched
                itemStack = ItemStack.EMPTY;
            }
        } else {
            // offhand has the bow
            if (BowTracker.isHoldingBow(MC.player, InteractionHand.MAIN_HAND)) {
                itemStack = MC.player.getMainHandItem();
            }
        }

        return itemStack;
    }

    /**
     * renders the teleport arc
     *
     * @param matrix Matrix4f for positioning
     */
    public static void renderTeleportArc(
        SubmitNodeCollector output, CameraRenderState cameraState, TeleportRenderState teleportState,
        PoseStack poseStack)
    {
        if (teleportState.showBeam) {
            Profiler.get().push("teleportArc");

            poseStack.pushPose();
            // TODO SHADERS use a shader with lightmaps

            // to make shaders work
            RenderType renderType = VRRenderTypes.quads(false);

            // arc
            output.order(RenderHelper.getPipelineRenderOrder(renderType)).submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                for (TeleportRenderState.Segment segment : teleportState.segments) {
                    RenderHelper.renderBox(consumer,
                        segment.start().subtract(cameraState.pos.x, cameraState.pos.y, cameraState.pos.z),
                        segment.end().subtract(cameraState.pos.x, cameraState.pos.y, cameraState.pos.z),
                        -teleportState.segmentHalfWidth, teleportState.segmentHalfWidth,
                        (-1.0F + segment.vOffset()) * teleportState.segmentHalfWidth,
                        (1.0F + segment.vOffset()) * teleportState.segmentHalfWidth, teleportState.color,
                        teleportState.alpha, pose);
                }
            });

            // hit indicator
            if (teleportState.validLocation && teleportState.showHitIndicator) {
                // disable culling to show the hit from both sides
                float offset = 0.01F;
                double x = -cameraState.pos.x;
                double y = -cameraState.pos.y;
                double z = -cameraState.pos.z;

                y += offset;

                RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.6F, 0.6F, 0.0F,
                    (int) (teleportState.color.getX() * 1.03D),
                    (int) (teleportState.color.getY() * 1.03D),
                    (int) (teleportState.color.getZ() * 1.03D), 64, poseStack, false, output);

                y += offset;

                RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.4F, 0.4F, 0.0F,
                    (int) (teleportState.color.getX() * 1.04D),
                    (int) (teleportState.color.getY() * 1.04D),
                    (int) (teleportState.color.getZ() * 1.04D), 64, poseStack, false, output);

                y += offset;

                RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.2F, 0.2F, 0.0F,
                    (int) (teleportState.color.getX() * 1.05D),
                    (int) (teleportState.color.getY() * 1.05D),
                    (int) (teleportState.color.getZ() * 1.05D), 64, poseStack, false, output);
            }
            poseStack.popPose();

            Profiler.get().pop();
        }
    }
}
