package org.vivecraft.client_vr.render;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.common.utils.MathUtils;

/**
 * an extension of the Camera, to correctly set up the camera position for the current pass
 */
public class XRCamera extends Camera {
    /**
     * override to position the camera for the current pass
     *
     * @param level              rendered level
     * @param entity             camera entity
     * @param detached           third or first person
     * @param thirdPersonReverse front or back third person
     * @param partialTick        current partial tick
     */
    @Override
    public void setup(
        BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick)
    {
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
        this.setPosition(eye.getPosition());
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.getLookVector().set(eye.getDirection());

        Matrix4f rotation = eye.getMatrix();
        rotation.transformDirection(MathUtils.UP, this.getUpVector());
        rotation.transformDirection(MathUtils.RIGHT, this.getLeftVector());

        this.rotation().setFromNormalized(rotation);
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
     *
     * @return if the camera is not first person
     */
    @Override
    public boolean isDetached() {
        if (RenderPassType.isVanilla()) {
            return super.isDetached();
        }
        boolean renderSelf = RenderPass.renderPlayer(ClientDataHolderVR.getInstance().currentPass);
        // don't render the player in first person when sleeping
        renderSelf &= !(RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            getEntity() instanceof LivingEntity && ((LivingEntity) getEntity()).isSleeping()
        );
        return renderSelf;
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
