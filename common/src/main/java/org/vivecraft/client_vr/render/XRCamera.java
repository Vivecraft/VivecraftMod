package org.vivecraft.client_vr.render;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassType;

/**
 * an extension of the Camera, to correctly set up the camera position for the current pass
 */
public class XRCamera extends Camera {
    /**
     * override to position the camera for the current pass
     * @param level rendered level
     * @param entity camera entity
     * @param detached third or first person
     * @param thirdPersonReverse front or back third person
     * @param partialTick current partial tick
     */
    @Override
    public void setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick) {
        if (RenderPassType.isVanilla()) {
            super.setup(level, entity, detached, thirdPersonReverse, partialTick);
            return;
        }
        this.initialized = true;
        this.level = level;
        this.entity = entity;
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        RenderPass renderpass = dataholder.currentPass;

        VRData.VRDevicePose eye = dataholder.vrPlayer.getVRDataWorld().getEye(renderpass);
        if (renderpass == RenderPass.CENTER && dataholder.vrSettings.displayMirrorCenterSmooth > 0.0F) {
            this.setPosition(RenderHelper.getSmoothCameraPosition(renderpass, dataholder.vrPlayer.getVRDataWorld()));
        } else {
            this.setPosition(eye.getPosition());
        }
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.getLookVector().set((float) eye.getDirection().x, (float) eye.getDirection().y, (float) eye.getDirection().z);
        Vec3 up = eye.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        this.getUpVector().set((float) up.x, (float) up.y, (float) up.z);
        Vec3 left = eye.getCustomVector(new Vec3(1.0D, 0.0D, 0.0D));
        this.getLeftVector().set((float) left.x, (float) left.y, (float) left.z);

        this.rotation().set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation().mul(Axis.YP.rotationDegrees(-this.yRot));
        this.rotation().mul(Axis.XP.rotationDegrees(this.xRot));
    }

    /**
     * TODO: do we need to skip that? that just smooths the eye height for the regular camera
     */
    @Override
    public void tick() {
        if (RenderPassType.isVanilla()) {
            super.tick();
        }
    }

    /**
     * the detached state is used to check if the player should be rendered, we only want that in external passes
     * @return if the camera is not first person
     */
    @Override
    public boolean isDetached() {
        if (RenderPassType.isVanilla()) {
            return super.isDetached();
        }
        boolean renderSelf = ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA ||
            (ClientDataHolderVR.getInstance().currentPass == RenderPass.THIRD &&
                ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            );
        return renderSelf || ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf;
    }

    /**
     * gets the fluid state of the camera
     * we override this because some mods call this, when querying the sunrise color in the menu world, where the level is null
      */
    @Override
    public FogType getFluidInCamera() {
        if (this.level == null) {
            return FogType.NONE;
        } else {
            return super.getFluidInCamera();
        }
    }
}
