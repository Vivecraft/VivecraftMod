package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

import javax.annotation.Nullable;

public class ThirdPersonCameraModule implements DebugRenderModule, HeldInteractModule {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("vivecraft",
        "third_person_camera");

    private static final float INTERACT_DIST = 0.15F;

    private final ClientDataHolderVR dh;

    // stored for the debug view
    private Vec3 camPos;

    public ThirdPersonCameraModule(ClientDataHolderVR dh) {
        this.dh = dh;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // after bow, but before default priority stuff
        return 750;
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        if (this.dh.vrSettings.mixedRealityRenderCameraModel &&
            (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ))
        {
            VRData.VRDevicePose camData = this.dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.THIRD);

            Vector3f offset = camData.getCustomVector(MathUtils.BACK)
                .mul(0.15F * this.dh.vrPlayer.vrdata_world_pre.worldScale)
                .add(camData.getCustomVector(MathUtils.DOWN).mul(0.05F * this.dh.vrPlayer.vrdata_world_pre.worldScale));

            this.camPos = camData.getPosition().subtract(offset.x, offset.y, offset.z);

            return handPosition.distanceTo(this.camPos) < INTERACT_DIST * this.dh.vrPlayer.vrdata_world_pre.worldScale;
        }
        return false;
    }

    @Override
    public boolean onPress(@Nullable LocalPlayer player, InteractionHand hand) {
        VRHotkeys.startMovingThirdPersonCam(hand.ordinal(), VRHotkeys.Triggerer.INTERACTION);
        return true;
    }

    @Override
    public void onRelease(@Nullable LocalPlayer player, InteractionHand hand) {
        if (VRHotkeys.isMovingThirdPersonCam() &&
            VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.INTERACTION &&
            VRHotkeys.getMovingThirdPersonCamController() == hand.ordinal())
        {
            VRHotkeys.stopMovingThirdPersonCam();
        }
    }

    public boolean isActive() {
        return this.dh.interactTracker.isActiveModule(this);
    }

    @Override
    public void renderDebug(boolean isActive) {
        if (this.dh.vrSettings.mixedRealityRenderCameraModel &&
            (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ) && this.camPos != null)
        {
            VRData world = this.dh.vrPlayer.getVRDataWorld();
            // origin offset since the camera is room relative
            Vec3 cam = this.camPos.subtract(this.dh.vrPlayer.vrdata_world_pre.origin).add(world.origin);
            DebugRenderHelper.renderSphere(cam, INTERACT_DIST * world.worldScale,
                isActive ? MathUtils.GREEN_INT : MathUtils.RED_INT);
        }
    }
}
