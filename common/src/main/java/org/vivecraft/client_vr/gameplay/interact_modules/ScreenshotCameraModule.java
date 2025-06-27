package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.common.utils.MathUtils;

public class ScreenshotCameraModule implements DebugRenderModule, HeldInteractModule {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vivecraft", "screenshot_camera");

    private static final float INTERACT_DIST = 0.11F;

    private final ClientDataHolderVR dh;

    // stored for the debug view
    private Vec3 camPos;

    public ScreenshotCameraModule(ClientDataHolderVR dh) {
        this.dh = dh;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // after bow, but before default priority stuff
        return 750;
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        if (this.dh.cameraTracker.isVisible() && !this.dh.cameraTracker.isQuickMode()) {
            VRData.VRDevicePose camData = this.dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.CAMERA);

            Vector3f offset = camData.getCustomVector(MathUtils.BACK)
                .mul(0.08F * this.dh.vrPlayer.vrdata_world_pre.worldScale);

            this.camPos = camData.getPosition().subtract(offset.x, offset.y, offset.z);

            return handPosition.distanceTo(this.camPos) < INTERACT_DIST * this.dh.vrPlayer.vrdata_world_pre.worldScale;
        }
        return false;
    }

    @Override
    public boolean onPress(@Nullable LocalPlayer player, InteractionHand hand) {
        this.dh.cameraTracker.startMoving(hand.ordinal());
        return true;
    }

    @Override
    public void onRelease(@Nullable LocalPlayer player, InteractionHand hand) {
        if (this.dh.cameraTracker.isMoving() && this.dh.cameraTracker.getMovingController() == hand.ordinal() &&
            !this.dh.cameraTracker.isQuickMode())
        {
            this.dh.cameraTracker.stopMoving();
        }
    }

    public boolean isActive() {
        return this.dh.interactTracker.isActiveModule(this);
    }

    @Override
    public void renderDebug(boolean isActive) {
        if (this.dh.cameraTracker.isVisible() && !this.dh.cameraTracker.isQuickMode() && this.camPos != null) {
            VRData world = this.dh.vrPlayer.getVRDataWorld();
            // no origin offset, since the camera is world relative
            DebugRenderHelper.renderSphere(
                MathUtils.subtractToVector3f(this.camPos, world.getEye(this.dh.currentPass).getPosition()),
                INTERACT_DIST * world.worldScale, isActive ? MathUtils.GREEN : MathUtils.RED);
        }
    }
}
