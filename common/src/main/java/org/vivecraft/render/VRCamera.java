package org.vivecraft.render;

import com.mojang.math.Axis;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.api.VRData;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class VRCamera extends Camera
{
    public void setup(BlockGetter pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks)
    {
        this.initialized = true;
        this.level = pLevel;
        this.entity = pRenderViewEntity;
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolder dataholder = ClientDataHolder.getInstance();
        RenderPass renderpass = dataholder.currentPass;

//        if (Shaders.isShadowPass && renderpass != RenderPass.THIRD && renderpass != RenderPass.CAMERA)
        if (false && renderpass != RenderPass.THIRD && renderpass != RenderPass.CAMERA)
        {
            renderpass = RenderPass.CENTER;
        }

        VRData.VRDevicePose eye = dataholder.vrPlayer.vrdata_world_render.getEye(renderpass);
        this.setPosition(eye.getPosition());
        this.xRot = -eye.getPitch();
        this.yRot = eye.getYaw();
        this.forwards.set((float)eye.getDirection().x, (float)eye.getDirection().y, (float)eye.getDirection().z);
        Vec3 vec3 = eye.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        this.up.set((float)vec3.x, (float)vec3.y, (float)vec3.z);
        eye.getCustomVector(new Vec3(1.0D, 0.0D, 0.0D));
        this.left.set((float)vec3.x, (float)vec3.y, (float)vec3.z);
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.mul(Axis.YP.rotationDegrees(-this.yRot));
        this.rotation.mul(Axis.XP.rotationDegrees(this.xRot));
    }

    public void tick()
    {
    }

    public boolean isDetached()
    {
        return false;
    }
}
