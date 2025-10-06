package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.gui.framework.widgets.MultilineComponent;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.helpers.GuiHelper;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.common.utils.MathUtils;

public class FBTCalibrationScreen extends Screen {

    private static final Vec3i COLOR_INACTIVE = new Vec3i(128, 64, 64);
    private static final Vec3i COLOR_ACTIVE = new Vec3i(64, 128, 64);
    private static final byte ALPHA = (byte) 200;

    private final Screen parent;

    private final boolean wasFbtCalibrated;
    private final boolean wasFbtExtendedCalibrated;

    private final Vector3fc[] oldFbtOffsets;
    private final Quaternionfc[] oldFbtRotations;

    private boolean calibrated = false;

    private boolean usingUnlabeledTrackers = false;

    private boolean rightHandAtPosition = false;
    private boolean leftHandAtPosition = false;

    private Vector3f rightHand = new Vector3f();
    private Vector3f leftHand = new Vector3f();

    private float yaw;

    private MultilineComponent calibrationText;
    private MultilineComponent unlabeledTrackersWarningText;
    private MultilineComponent unlabeledTrackersConfirmationText;
    private Button resetButton;
    private Button cancelButton;

    public FBTCalibrationScreen(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.fbtcalibration"));
        this.parent = parent;
        // copy old settings to be able to reset them on cancel
        this.wasFbtCalibrated = ClientDataHolderVR.getInstance().vrSettings.fbtCalibrated;
        this.wasFbtExtendedCalibrated = ClientDataHolderVR.getInstance().vrSettings.fbtExtendedCalibrated;
        this.oldFbtOffsets = new Vector3fc[ClientDataHolderVR.getInstance().vrSettings.fbtOffsets.length];
        this.oldFbtRotations = new Quaternionfc[ClientDataHolderVR.getInstance().vrSettings.fbtRotations.length];
        for (int i = 0; i < this.oldFbtOffsets.length; i++) {
            this.oldFbtOffsets[i] = new Vector3f(ClientDataHolderVR.getInstance().vrSettings.fbtOffsets[i]);
            this.oldFbtRotations[i] = new Quaternionf(ClientDataHolderVR.getInstance().vrSettings.fbtRotations[i]);
        }

        // mark as shown, since the user is currently calibrating
        ClientDataHolderVR.getInstance().showedFbtCalibrationNotification = true;
        ClientDataHolderVR.getInstance().vrSettings.fbtCalibrated = false;
        ClientDataHolderVR.getInstance().vrSettings.fbtExtendedCalibrated = false;

        if (VRState.VR_INITIALIZED) {
            boolean fbt = ClientDataHolderVR.getInstance().vr.hasFBT();
            boolean extended = ClientDataHolderVR.getInstance().vr.hasExtendedFBT();

            int trackers = ClientDataHolderVR.getInstance().vr.getTrackers().size();
            this.usingUnlabeledTrackers = ClientDataHolderVR.getInstance().vrSettings.unlabeledTrackersUsed ||
                (!extended && trackers >= 7) || (!fbt && trackers >= 3);
        }
    }

    private void reset() {
        this.calibrated = false;
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vr.resetFBT();
        }
        ClientDataHolderVR.getInstance().vrSettings.fbtCalibrated = false;
        ClientDataHolderVR.getInstance().vrSettings.fbtExtendedCalibrated = false;
        ClientDataHolderVR.getInstance().vrSettings.unlabeledTrackersUsed = this.usingUnlabeledTrackers;
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
        this.cancelButton.setMessage(Component.translatable("gui.cancel"));
        this.resetButton.visible = false;
    }

    public boolean isCalibrated() {
        return this.calibrated;
    }

    @Override
    protected void init() {
        this.calibrationText = new MultilineComponent(this.width / 2, 30, 400,
            Component.translatable("vivecraft.messages.fbtcalibration"), true, this.font);

        this.unlabeledTrackersWarningText = new MultilineComponent(this.width / 2,
            this.calibrationText.y + this.calibrationText.getHeight(), 400,
            Component.translatable("vivecraft.messages.fbtcalibration.unlabeledTrackers"), true, this.font);
        this.unlabeledTrackersWarningText.visible = this.usingUnlabeledTrackers;

        this.unlabeledTrackersConfirmationText = new MultilineComponent(this.width / 2, 30, 400,
            Component.translatable("vivecraft.messages.fbtcalibration.unlabeledTrackersConfirm"), true, this.font);
        this.unlabeledTrackersConfirmationText.visible = false;

        this.resetButton = new Button(this.width / 2 - 75, this.height - 54, 150, 20,
            Component.translatable("controls.reset"), p -> reset());
        this.resetButton.visible = this.calibrated;

        this.cancelButton = new Button(this.width / 2 - 75, this.height - 32, 150, 20,
            Component.translatable(this.calibrated ? "vivecraft.gui.ok" : "gui.cancel"),
            p -> this.minecraft.setScreen(this.parent));

        this.addRenderableWidget(this.calibrationText);
        this.addRenderableWidget(this.unlabeledTrackersWarningText);
        this.addRenderableWidget(this.unlabeledTrackersConfirmationText);
        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(this.cancelButton);

        if (VRState.VR_RUNNING) {
            this.yaw = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.hmd.getYawRad();
        }
    }

    @Override
    public void removed() {
        if (!this.calibrated) {
            // restore previous state when canceling
            ClientDataHolderVR.getInstance().vrSettings.fbtCalibrated = this.wasFbtCalibrated;
            ClientDataHolderVR.getInstance().vrSettings.fbtExtendedCalibrated = this.wasFbtExtendedCalibrated;
            for (int i = 0; i < this.oldFbtOffsets.length; i++) {
                ClientDataHolderVR.getInstance().vrSettings.fbtOffsets[i].set(this.oldFbtOffsets[i]);
                ClientDataHolderVR.getInstance().vrSettings.fbtRotations[i].set(this.oldFbtRotations[i]);
            }
            ClientDataHolderVR.getInstance().vrSettings.saveOptions();
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.calibrationText.visible = !this.calibrated;
        this.unlabeledTrackersWarningText.visible = !this.calibrated && this.usingUnlabeledTrackers;
        this.unlabeledTrackersConfirmationText.visible = this.calibrated && this.usingUnlabeledTrackers;

        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (!this.calibrated || !this.usingUnlabeledTrackers) {
            // arm overlay
            GuiHelper.renderOutline(poseStack, this.width / 2 - 64, this.height - 32 - 96,
                48, 16, 0xFFFFFFFF);
            GuiHelper.renderOutline(poseStack, this.width / 2 + 16, this.height - 32 - 96,
                48, 16, 0xFFFFFFFF);

            poseStack.pushPose();

            // move to screen center and scale
            poseStack.translate(this.width / 2F, this.height - 32F, 0);
            poseStack.scale(4, -4, 4);
            poseStack.mulPose(com.mojang.math.Vector3f.YP.rotation(Mth.PI));

            if (VRState.VR_RUNNING) {
                poseStack.mulPose(com.mojang.math.Vector3f.YP.rotation(
                    this.yaw - ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.hmd.getYawRad()));
            }

            Vec3i color = this.leftHandAtPosition && this.rightHandAtPosition ? COLOR_ACTIVE : COLOR_INACTIVE;

            // body overlay
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // legs
            RenderHelper.renderBox(builder, new Vec3(2, 0, 0), new Vec3(2, 12, 0),
                4, 4, color, ALPHA, poseStack);
            RenderHelper.renderBox(builder, new Vec3(-2, 0, 0), new Vec3(-2, 12, 0),
                4, 4, color, ALPHA, poseStack);
            // body
            RenderHelper.renderBox(builder, new Vec3(0, 12, 0), new Vec3(0, 24, 0),
                8, 4, color, ALPHA, poseStack);

            // head
            RenderHelper.renderBox(builder, new Vec3(0, 24, 0), new Vec3(0, 32, 0),
                8, 8, color, ALPHA, poseStack);

            // arms
            RenderHelper.renderBox(builder,
                new Vec3(6, 22, 0).subtract(this.leftHand.x * 2F, this.leftHand.y * 2F, this.leftHand.z * 2F),
                new Vec3(6, 22, 0).add(this.leftHand.x * 10F, this.leftHand.y * 10F, this.leftHand.z * 10F),
                4, 4, this.leftHandAtPosition ? COLOR_ACTIVE : COLOR_INACTIVE, ALPHA, poseStack);
            RenderHelper.renderBox(builder,
                new Vec3(-6, 22, 0).subtract(this.rightHand.x * 2F, this.rightHand.y * 2F, this.rightHand.z * 2F),
                new Vec3(-6, 22, 0).add(this.rightHand.x * 10F, this.rightHand.y * 10F, this.rightHand.z * 10F),
                4, 4, this.rightHandAtPosition ? COLOR_ACTIVE : COLOR_INACTIVE, ALPHA, poseStack);

            BufferUploader.drawWithShader(builder.end());
            poseStack.popPose();
        }
    }

    @Override
    public void tick() {
        if (!VRState.VR_RUNNING) {
            this.rightHand.set(MathUtils.DOWN);
            this.leftHand.set(MathUtils.DOWN);
            return;
        }

        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        Vector3f hmdPosAvg = dataHolder.vr.hmdPivotHistory.averagePosition(0.5D);

        float height = hmdPosAvg.y / AutoCalibration.DEFAULT_HEIGHT;
        float scale = height * 0.9375F;

        int main = dataHolder.vrSettings.reverseHands ? 1 : 0;

        this.rightHand = dataHolder.vrPlayer.vrdata_room_post.getController(main).getPositionF()
            .sub(hmdPosAvg.x, 1.375F * scale, hmdPosAvg.z)
            .rotateY(this.yaw)
            .add(scale * 0.375F, 0F, 0F)
            .normalize();
        this.leftHand = dataHolder.vrPlayer.vrdata_room_post.getController(1 - main).getPositionF()
            .sub(hmdPosAvg.x, 1.375F * scale, hmdPosAvg.z)
            .rotateY(this.yaw)
            .add(-scale * 0.375F, 0F, 0F)
            .normalize();

        boolean rightHandNew = this.rightHand.dot(MathUtils.RIGHT) > 0.9F;
        boolean leftHandNew = this.leftHand.dot(MathUtils.LEFT) > 0.9F;

        if (!this.rightHandAtPosition && rightHandNew) {
            dataHolder.vr.triggerHapticPulse(ControllerType.RIGHT, 0.01F, 100, 1F);
        }

        if (!this.leftHandAtPosition && leftHandNew) {
            dataHolder.vr.triggerHapticPulse(ControllerType.LEFT, 0.01F, 100, 1F);
        }

        this.rightHandAtPosition = rightHandNew;
        this.leftHandAtPosition = leftHandNew;

        if (VRState.VR_RUNNING) {
            if (this.calibrated && this.usingUnlabeledTrackers) {
                dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).setEnabled(false);
            } else {
                dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract)
                    .setEnabled(this.leftHandAtPosition && this.rightHandAtPosition);

                if (VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.LEFT) &&
                    VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.RIGHT) &&
                    VivecraftVRMod.INSTANCE.keyVRInteract.consumeClick())
                {
                    AutoCalibration.calibrateManual();
                    dataHolder.vr.calibrateFBT(this.yaw + Mth.PI);
                    dataHolder.vrSettings.unlabeledTrackersUsed = this.usingUnlabeledTrackers;
                    dataHolder.vrSettings.saveOptions();
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.fbtcalibrationsuccess"));
                    this.calibrated = true;
                    if (!this.usingUnlabeledTrackers) {
                        this.minecraft.setScreen(this.parent);
                    } else {
                        this.cancelButton.setMessage(Component.translatable("vivecraft.gui.ok"));
                        this.resetButton.visible = true;
                    }
                }
            }
        }
    }
}
