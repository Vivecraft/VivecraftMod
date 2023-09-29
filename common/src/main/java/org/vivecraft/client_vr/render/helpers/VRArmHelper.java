package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import static org.joml.Math.roundUsing;
import static org.joml.RoundingMode.FLOOR;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class VRArmHelper {

    private static final Vec3i tpUnlimitedColor = new Vec3i(173, 216, 230);
    private static final Vec3i tpLimitedColor = new Vec3i(205, 169, 205);
    private static final Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

    public static boolean shouldRenderHands() {
        if (dh.viewonly) {
            return false;
        } else if (dh.currentPass == RenderPass.THIRD) {
            return dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY;
        } else {
            return dh.currentPass != RenderPass.CAMERA;
        }
    }

    public static void renderVRHands(float partialTicks, boolean renderRight, boolean renderLeft, boolean menuHandRight,
        boolean menuHandLeft, PoseStack poseStack) {
        mc.getProfiler().push("hands");
        // backup projection matrix, not doing that breaks sodium water on 1.19.3
        RenderSystem.backupProjectionMatrix();

        if (renderRight) {
            mc.getItemRenderer();
            dh.ismainhand = true;

            if (menuHandRight) {
                renderMainMenuHand(0, partialTicks, false, poseStack);
            } else {
                ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
                PoseStack newPoseStack = new PoseStack();
                newPoseStack.last().pose().identity();
                RenderHelper.applyVRModelView(dh.currentPass, newPoseStack);
                renderVRHand_Main(newPoseStack, partialTicks);
            }

            dh.ismainhand = false;
        }

        if (renderLeft) {
            if (menuHandLeft) {
                renderMainMenuHand(1, partialTicks, false, poseStack);
            } else {
                ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
                PoseStack newPoseStack = new PoseStack();
                newPoseStack.last().pose().identity();
                RenderHelper.applyVRModelView(dh.currentPass, newPoseStack);
                renderVRHand_Offhand(partialTicks, true, newPoseStack);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        mc.getProfiler().pop();
    }

    public static void renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack) {
        ((GameRendererExtension) mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderHelper.applyVRModelView(dh.currentPass, poseStack);
        RenderHelper.setupRenderingAtController(c, poseStack);

        if (mc.getOverlay() == null) {
            mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        }

        Tesselator tesselator = Tesselator.getInstance();

        if (depthAlways && c == 0) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        Vec3i color = new Vec3i(64, 64, 64);
        byte alpha = (byte) 255;

        Vec3 dir = new Vec3(0.0D, 0.0D, -1.0D);

        Vec3 start = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 end = new Vec3(start.x - dir.x * 0.18D, start.y - dir.y * 0.18D, start.z - dir.z * 0.18D);

        if (mc.level != null) {
            float light = mc.level.getMaxLocalRawBrightness(
                BlockPos.containing(dh.vrPlayer.vrdata_world_render.hmd.getPosition())
            );

            int minLight = ShadersHelper.ShaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightPercent = light / (float) mc.level.getMaxLightLevel();
            color = new Vec3i(
                roundUsing(color.getX() * lightPercent, FLOOR),
                roundUsing(color.getY() * lightPercent, FLOOR),
                roundUsing(color.getZ() * lightPercent, FLOOR)
            );
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderHelper.renderBox(tesselator, start, end, -0.02F, 0.02F, -0.0125F, 0.0125F, color, alpha, poseStack);
        BufferUploader.drawWithShader(tesselator.getBuilder().end());
        poseStack.popPose();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    public static void renderVRHand_Main(PoseStack poseStack, float partialTicks) {
        poseStack.pushPose();
        RenderHelper.setupRenderingAtController(0, poseStack);
        ItemStack item = mc.player.getMainHandItem();
        ItemStack override = null; // this.minecraft.physicalGuiManager.getHeldItemOverride();

        if (override != null) {
            item = override;
        }

        if (dh.climbTracker.isClimbeyClimb() && item.getItem() != Items.SHEARS) {
            item = override == null ? mc.player.getOffhandItem() : override;
        }

        if (BowTracker.isHoldingBow(InteractionHand.MAIN_HAND)) {
            //do ammo override
            int c = 0;

            if (dh.vrSettings.reverseShootingEye) {
                c = 1;
            }

            ItemStack ammo = mc.player.getProjectile(mc.player.getMainHandItem());

            if (ammo != ItemStack.EMPTY && !dh.bowTracker.isNotched()) {
                //render the arrow in right, left hand will check for and render bow.
                item = ammo;
            } else {
                item = ItemStack.EMPTY;
            }
        } else if (BowTracker.isHoldingBow(InteractionHand.OFF_HAND) && dh.bowTracker.isNotched()) {
            int c = 0;

            if (dh.vrSettings.reverseShootingEye) {
                c = 1;
            }

            item = ItemStack.EMPTY;
        }

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
        }
        poseStack.pushPose();

        mc.gameRenderer.lightTexture().turnOnLightLayer();
        BufferSource bufferSource = mc.renderBuffers().bufferSource();
        mc.gameRenderer.itemInHandRenderer.renderArmWithItem(mc.player, partialTicks,
            0.0F, InteractionHand.MAIN_HAND, mc.player.getAttackAnim(partialTicks), item, 0.0F,
            poseStack, bufferSource,
            mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTicks)
        );
        bufferSource.endBatch();
        mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    public static void renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack poseStack) {
        poseStack.pushPose();
        RenderHelper.setupRenderingAtController(1, poseStack);
        ItemStack item = mc.player.getOffhandItem();
        ItemStack override = null;// this.minecraft.physicalGuiManager.getOffhandOverride();

        if (override != null) {
            item = override;
        }

        if (dh.climbTracker.isClimbeyClimb() && (item == null || item.getItem() != Items.SHEARS)) {
            item = mc.player.getMainHandItem();
        }

        if (BowTracker.isHoldingBow(InteractionHand.MAIN_HAND)) {
            int c = 1;

            if (dh.vrSettings.reverseShootingEye) {
                c = 0;
            }

            item = mc.player.getMainHandItem();
        }

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
        }
        poseStack.pushPose();

        mc.gameRenderer.lightTexture().turnOnLightLayer();
        BufferSource bufferSource = mc.renderBuffers().bufferSource();
        mc.gameRenderer.itemInHandRenderer.renderArmWithItem(mc.player, partialTicks,
            0.0F, InteractionHand.OFF_HAND, mc.player.getAttackAnim(partialTicks), item, 0.0F,
            poseStack, bufferSource,
            mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTicks)
        );
        bufferSource.endBatch();
        mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        poseStack.popPose();

        poseStack.popPose();

        if (renderTeleport) {
            poseStack.pushPose();
            poseStack.setIdentity();
            RenderHelper.applyVRModelView(dh.currentPass, poseStack);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA
            );

            //	TP energy
            if (ClientNetworking.isLimitedSurvivalTeleport() && !dh.vrPlayer.getFreeMove()
                && mc.gameMode.hasMissTime() && dh.teleportTracker.vrMovementStyle.arcAiming
                && !dh.bowTracker.isActive()
            ) {
                poseStack.pushPose();
                RenderHelper.setupRenderingAtController(1, poseStack);
                Vec3 start = new Vec3(0.0D, 0.005D, 0.03D);
                float max = 0.03F;
                float r;

                if (dh.teleportTracker.isAiming()) {
                    r = 2.0F * (float) ((double) dh.teleportTracker.getTeleportEnergy()
                        - 4.0D * dh.teleportTracker.movementTeleportDistance) / 100.0F * max;
                } else {
                    r = 2.0F * dh.teleportTracker.getTeleportEnergy() / 100.0F * max;
                }

                if (r < 0.0F) {
                    r = 0.0F;
                }
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                RenderHelper.renderFlatQuad(start.add(0.0D, 0.05001D, 0.0D), r, r, 0.0F, tpLimitedColor.getX(),
                    tpLimitedColor.getY(), tpLimitedColor.getZ(), 128, poseStack);
                RenderHelper.renderFlatQuad(start.add(0.0D, 0.05D, 0.0D), max, max, 0.0F, tpLimitedColor.getX(),
                    tpLimitedColor.getY(), tpLimitedColor.getZ(), 50, poseStack);
                poseStack.popPose();
            }

            if (dh.teleportTracker.isAiming()) {
                RenderSystem.enableDepthTest();

                if (dh.teleportTracker.vrMovementStyle.arcAiming) {
                    renderTeleportArc(poseStack);
                }
            }

            RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

            poseStack.popPose();
        }
    }

    public static void renderTeleportArc(PoseStack poseStack) {
        if (dh.teleportTracker.vrMovementStyle.showBeam && dh.teleportTracker.isAiming()
            && dh.teleportTracker.movementTeleportArcSteps > 1
        ) {
            mc.getProfiler().push("teleportArc");

            RenderSystem.enableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            // to make shaders work
            mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));

            Tesselator tesselator = Tesselator.getInstance();
            tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            double VOffset = dh.teleportTracker.lastTeleportArcDisplayOffset;
            Vec3 dest = dh.teleportTracker.getDestination();
            boolean validLocation = dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D;

            byte alpha = -1;
            Vec3i color;

            if (!validLocation) {
                color = tpInvalidColor;
                alpha = -128;
            } else {
                if (ClientNetworking.isLimitedSurvivalTeleport() && !mc.player.getAbilities().mayfly) {
                    color = tpLimitedColor;
                } else {
                    color = tpUnlimitedColor;
                }

                VOffset = dh.vrRenderer.getCurrentTimeSecs()
                    * (double) dh.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
                dh.teleportTracker.lastTeleportArcDisplayOffset = VOffset;
            }

            float segmentHalfWidth = dh.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
            int segments = dh.teleportTracker.movementTeleportArcSteps - 1;

            if (dh.teleportTracker.vrMovementStyle.beamGrow) {
                segments = (int) (segments * dh.teleportTracker.movementTeleportProgress);
            }

            double segmentProgress = 1.0D / (double) segments;

            // arc
            for (int i = 0; i < segments; ++i) {
                double progress = (double) i / (double) segments + VOffset * segmentProgress;
                int progressBase = roundUsing(progress, FLOOR);
                progress -= progressBase;

                Vec3 start = dh.teleportTracker
                    .getInterpolatedArcPosition((float) (progress - segmentProgress * (double) 0.4F))
                    .subtract(mc.getCameraEntity().position());

                Vec3 end = dh.teleportTracker.getInterpolatedArcPosition((float) progress)
                    .subtract(mc.getCameraEntity().position());
                float shift = (float) progress * 2.0F;
                RenderHelper.renderBox(tesselator, start, end, -segmentHalfWidth, segmentHalfWidth, (-1.0F + shift) * segmentHalfWidth, (1.0F + shift) * segmentHalfWidth, color, alpha, poseStack);
            }

            tesselator.end();

            // hit indicator
            if (validLocation && dh.teleportTracker.movementTeleportProgress >= 1.0D) {
                RenderSystem.disableCull();
                Vec3 vec34 = (new Vec3(dest.x, dest.y, dest.z)).subtract(mc.getCameraEntity().position());
                float offset = 0.01F;
                double x = 0.0D;
                double y = 0.0D;
                double z = 0.0D;

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.6F, 0.6F, 0.0F, (int) (color.getX() * 1.03D),
                    (int) (color.getY() * 1.03D), (int) (color.getZ() * 1.03D), 64, poseStack);

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.4F, 0.4F, 0.0F, (int) (color.getX() * 1.04D),
                    (int) (color.getY() * 1.04D), (int) (color.getZ() * 1.04D), 64, poseStack);

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.2F, 0.2F, 0.0F, (int) (color.getX() * 1.05D),
                    (int) (color.getY() * 1.05D), (int) (color.getZ() * 1.05D), 64, poseStack);
                RenderSystem.enableCull();
            }

            mc.getProfiler().pop();
        }
    }
}
