package org.vivecraft.client_vr.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import static org.joml.Math.toRadians;
import static org.vivecraft.client_vr.VRState.dh;


public class XRCamera extends net.minecraft.client.Camera {
    @Override
    public void setup(BlockGetter pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            super.setup(pLevel, pRenderViewEntity, pThirdPerson, pThirdPersonReverse, pPartialTicks);
            return;
        }
        this.initialized = true;
        this.level = pLevel;
        this.entity = pRenderViewEntity;
        VRDevicePose eye = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass);
        Vector3fc eyePos = eye.getPosition(new Vector3f());
        this.setPosition(eyePos.x(), eyePos.y(), eyePos.z());
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.getLookVector().set(eye.getDirection(new Vector3f()));
        Vector3f vec3 = eye.getCustomVector(new Vector3f(0.0F, 1.0F, 0.0F));
        this.getUpVector().set(vec3);
        eye.getCustomVector(vec3.set(1.0F, 0.0F, 0.0F));
        this.getLeftVector().set(vec3);
        this.rotation().set(0.0F, 0.0F, 0.0F, 1.0F).rotateY(toRadians(-this.yRot)).rotateX(toRadians(this.xRot));
    }

    @Override
    public void tick() {
        if (RenderPassType.isVanilla()) {
            super.tick();
        }
    }

    @Override
    public boolean isDetached() {
        return (RenderPassType.isVanilla() ?
                super.isDetached() :
                switch (dh.currentPass) {
                    case THIRD -> {
                        yield dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON;
                    }
                    case CAMERA -> {
                        yield true;
                    }
                    default -> {
                        yield dh.vrSettings.shouldRenderSelf;
                    }
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
