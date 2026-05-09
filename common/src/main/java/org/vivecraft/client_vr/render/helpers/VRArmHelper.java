package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Vec3i;
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

public class VRArmHelper {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static final Vec3i TP_UNLIMITED_COLOR = new Vec3i(173, 216, 230);
    public static final Vec3i TP_LIMITED_COLOR = new Vec3i(205, 169, 205);
    public static final Vec3i TP_INVALID_COLOR = new Vec3i(83, 83, 83);

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
     * @param output       SubmitNodeCollector to output to
     * @param vrState      VR renderstate
     * @param cameraState  camera render state for the position
     * @param renderMain   if the main hand should be rendered
     * @param renderOff    if the offhand should be rendered
     * @param menuHandMain if the right hand should render as the menu hand
     * @param menuHandOff  if the left hand should render as the menu hand
     * @param order        order to render at
     * @return order to render the next thing at
     */
    public static int renderVRHands(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean renderMain, boolean renderOff, boolean menuHandMain, boolean menuHandOff, int order)
    {
        if (!renderMain && !renderOff) return order;
        Profiler.get().push("hands");
        ClientDataHolderVR.isFpHand.set(true);

        if (renderMain) {
            if (menuHandMain) {
                order = renderMenuHand(output, vrState, cameraState, poseStack, 0, order);
            } else {
                // hand submits can't be ordered
                renderVRHand_Main(output, vrState, cameraState, poseStack);
            }
        }

        if (renderOff) {
            if (menuHandOff) {
                order = renderMenuHand(output, vrState, cameraState, poseStack, 1, order);
            } else {
                order = renderVRHand_Offhand(output, vrState, cameraState, poseStack, true, order);
            }
        }

        ClientDataHolderVR.isFpHand.set(false);
        Profiler.get().pop();
        return order;
    }

    /**
     * renders a menu hand for the specified controller, which is a gray box
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR renderstate
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param c           controller to render the hand for
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderMenuHand(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack, int c,
        int order)
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

        RenderType renderType = VRRenderTypes.quads(false);

        RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, renderType,
            (pose, consumer) -> RenderHelper.renderBox(consumer, start, end, -0.02F, 0.02F, -0.0125F, 0.0125F,
                color, alpha, pose));

        poseStack.popPose();
        return order;
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
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR renderstate
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     */
    public static void renderVRHand_Main(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack)
    {
        if (vrState.armsState.skipMainHandItemRendering) return;

        poseStack.pushPose();
        poseStack.translate(
            vrState.armsState.mainHandWorldPos.x - cameraState.pos.x,
            vrState.armsState.mainHandWorldPos.y - cameraState.pos.y,
            vrState.armsState.mainHandWorldPos.z - cameraState.pos.z);
        poseStack.mulPose(vrState.armsState.mainHandWorldRot);

        // TODO 26.1 optifine this doesn't work like that, do we still need that?
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
     * @param output         SubmitNodeCollector to output to
     * @param vrState        VR renderstate
     * @param cameraState    camera render state for the position
     * @param poseStack      PoseStack to use for positioning
     * @param renderTeleport if the teleport arc should be rendered
     * @param order          order to render at
     * @return order to render the next thing at
     */
    public static int renderVRHand_Offhand(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean renderTeleport, int order)
    {
        poseStack.pushPose();
        poseStack.translate(
            vrState.armsState.offHandWorldPos.x - cameraState.pos.x,
            vrState.armsState.offHandWorldPos.y - cameraState.pos.y,
            vrState.armsState.offHandWorldPos.z - cameraState.pos.z);
        poseStack.mulPose(vrState.armsState.offHandWorldRot);
        // don't render claws with model arms
        if (!vrState.armsState.skipOffHandItemRendering) {
            poseStack.pushPose();

            // TODO 26.1 optifine this doesn't work like that, do we still need that?
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

                Vec3 start = new Vec3(0.0D, 0.005D, 0.03D);
                float max = 0.03F;

                // TODO SHADERS use a shader with lightmaps

                if (vrState.teleportState.tpEnergySize > 0.0F) {
                    // tp energy quad, slightly above the max energy quad
                    order = RenderHelper.renderFlatQuad(start.add(0.0D, 0.05001D, 0.0D),
                        vrState.teleportState.tpEnergySize * max, vrState.teleportState.tpEnergySize * max, 0.0F,
                        TP_LIMITED_COLOR.getX(), TP_LIMITED_COLOR.getY(), TP_LIMITED_COLOR.getZ(), 128, poseStack,
                        false, output, order);
                }
                // max energy quad
                order = RenderHelper.renderFlatQuad(start.add(0.0D, 0.05D, 0.0D), max, max, 0.0F,
                    TP_LIMITED_COLOR.getX(), TP_LIMITED_COLOR.getY(), TP_LIMITED_COLOR.getZ(), 50, poseStack, false,
                    output, order);

                poseStack.popPose();
            }

            if (vrState.teleportState.aiming) {
                // renders from the head
                if (vrState.teleportState.arcAiming) {
                    order = renderTeleportArc(output, cameraState, vrState.teleportState, poseStack, order);
                } /* else {
                    renderTeleportLine(poseStack);
                }*/
            }
        }
        poseStack.popPose();
        return order;
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
     * @param output        SubmitNodeCollector to output to
     * @param cameraState   camera render state for the position
     * @param teleportState teleport render state
     * @param poseStack     PoseStack to use for positioning
     * @param order         order to render at
     * @return order to render the next thing at
     */
    public static int renderTeleportArc(
        SubmitNodeCollector output, CameraRenderState cameraState, TeleportRenderState teleportState,
        PoseStack poseStack, int order)
    {
        if (teleportState.showBeam) {
            Profiler.get().push("teleportArc");

            poseStack.pushPose();
            poseStack.setIdentity();
            // TODO SHADERS use a shader with lightmaps

            // to make shaders work
            RenderType renderType = VRRenderTypes.quads(false);

            // arc
            RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, renderType,
                (pose, consumer) -> {
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

                order = RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.6F, 0.6F, 0.0F,
                    (int) (teleportState.color.getX() * 1.03D),
                    (int) (teleportState.color.getY() * 1.03D),
                    (int) (teleportState.color.getZ() * 1.03D), 64, poseStack, false, output, order);

                y += offset;

                order = RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.4F, 0.4F, 0.0F,
                    (int) (teleportState.color.getX() * 1.04D),
                    (int) (teleportState.color.getY() * 1.04D),
                    (int) (teleportState.color.getZ() * 1.04D), 64, poseStack, false, output, order);

                y += offset;

                order = RenderHelper.renderFlatQuad(teleportState.dest.add(x, y, z), 0.2F, 0.2F, 0.0F,
                    (int) (teleportState.color.getX() * 1.05D),
                    (int) (teleportState.color.getY() * 1.05D),
                    (int) (teleportState.color.getZ() * 1.05D), 64, poseStack, false, output, order);
            }
            poseStack.popPose();

            Profiler.get().pop();
        }
        return order;
    }
}
