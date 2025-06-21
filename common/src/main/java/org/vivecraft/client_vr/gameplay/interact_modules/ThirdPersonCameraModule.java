package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

public class ThirdPersonCameraModule implements HeldInteractModule {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vivecraft",
        "third_person_camera");

    private final ClientDataHolderVR dh;

    public ThirdPersonCameraModule(ClientDataHolderVR dh) {
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
        if (this.dh.vrSettings.mixedRealityRenderCameraModel &&
            (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ))
        {
            VRData.VRDevicePose camData = this.dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.THIRD);
            Vec3 camPos = camData.getPosition();

            Vector3f offset = camData.getCustomVector(MathUtils.BACK)
                .mul(0.15F * this.dh.vrPlayer.vrdata_world_pre.worldScale);
            offset.add(
                camData.getCustomVector(MathUtils.DOWN).mul(0.05F * this.dh.vrPlayer.vrdata_world_pre.worldScale));

            camPos = camPos.subtract(offset.x, offset.y, offset.z);

            return handPosition.distanceTo(camPos) < 0.15F * this.dh.vrPlayer.vrdata_world_pre.worldScale;
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
}
