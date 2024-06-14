package org.vivecraft.client_vr.render;

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


public class XRCamera extends Camera {
    public void setup(BlockGetter pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            super.setup(pLevel, pRenderViewEntity, pThirdPerson, pThirdPersonReverse, pPartialTicks);
            return;
        }
        this.initialized = true;
        this.level = pLevel;
        this.entity = pRenderViewEntity;
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
        Vec3 fwd = eye.getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
        this.getLookVector().set((float) fwd.x, (float) fwd.y, (float) fwd.z);
        Vec3 up = eye.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        this.getUpVector().set((float) up.x, (float) up.y, (float) up.z);
        Vec3 left = eye.getCustomVector(new Vec3(-1.0D, 0.0D, 0.0D));
        this.getLeftVector().set((float) left.x, (float) left.y, (float) left.z);

        this.rotation().rotationYXZ(
            3.1415927F - this.yRot * 0.017453292F,
            -this.xRot * 0.017453292F,
            0.0F);
    }

    public void tick() {
        if (RenderPassType.isVanilla()) {
            super.tick();
        }
    }

    @Override
    public boolean isDetached() {
        if (RenderPassType.isVanilla()) {
            return super.isDetached();
        }
        boolean renderSelf = ClientDataHolderVR.getInstance().currentPass == RenderPass.THIRD && ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA;
        return renderSelf || ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf;
    }

    // some mods call this, when querying the sunrise color in the menu world
    @Override
    public FogType getFluidInCamera() {
        if (this.level == null) {
            return FogType.NONE;
        } else {
            return super.getFluidInCamera();
        }
    }
}
