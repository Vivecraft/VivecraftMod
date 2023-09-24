package org.vivecraft.client_vr.render;

import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import org.joml.Quaternionf;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;


public class XRCamera extends net.minecraft.client.Camera {
    public void setup(BlockGetter pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            super.setup(pLevel, pRenderViewEntity, pThirdPerson, pThirdPersonReverse, pPartialTicks);
            return;
        }
        this.initialized = true;
        this.level = pLevel;
        this.entity = pRenderViewEntity;
        VRDevicePose eye = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass);
        this.setPosition(eye.getPosition());
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.getLookVector().set((float) eye.getDirection().x, (float) eye.getDirection().y, (float) eye.getDirection().z);
        Vec3 vec3 = eye.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        this.getUpVector().set((float) vec3.x, (float) vec3.y, (float) vec3.z);
        eye.getCustomVector(new Vec3(1.0D, 0.0D, 0.0D));
        this.getLeftVector().set((float) vec3.x, (float) vec3.y, (float) vec3.z);
        this.rotation().set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation().mul(new Quaternionf().rotationY(toRadians(-this.yRot)));
        this.rotation().mul(new Quaternionf().rotationX(toRadians(this.xRot)));
    }

    public void tick() {
        if (RenderPassType.isVanilla()) {
            super.tick();
        }
    }

    @Override
    public boolean isDetached() {
        return (RenderPassType.isVanilla() ?
            super.isDetached() :
            switch (dh.currentPass){
                case THIRD -> dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON;
                case CAMERA -> true;
                default -> dh.vrSettings.shouldRenderSelf;
            }
        );
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
