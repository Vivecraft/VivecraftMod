package org.vivecraft.client_vr.render;

import com.mojang.math.Vector3f;
import net.minecraft.world.level.material.FogType;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
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

        VRData.VRDevicePose eye = dataholder.vrPlayer.vrdata_world_render.getEye(renderpass);
        this.setPosition(eye.getPosition());
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.getLookVector().set((float) eye.getDirection().x, (float) eye.getDirection().y, (float) eye.getDirection().z);
        Vec3 vec3 = eye.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        this.getUpVector().set((float) vec3.x, (float) vec3.y, (float) vec3.z);
        eye.getCustomVector(new Vec3(1.0D, 0.0D, 0.0D));
        this.getLeftVector().set((float) vec3.x, (float) vec3.y, (float) vec3.z);
        this.rotation().set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation().mul(Vector3f.YP.rotationDegrees(-this.yRot));
        this.rotation().mul(Vector3f.XP.rotationDegrees(this.xRot));
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
