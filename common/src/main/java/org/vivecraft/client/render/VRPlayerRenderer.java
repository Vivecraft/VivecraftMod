package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;

import java.util.UUID;

public class VRPlayerRenderer extends PlayerRenderer {
    private static final LayerDefinition VRLayerDef = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VRLayerDef_arms = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VRLayerDef_slim = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);
    private static final LayerDefinition VRLayerDef_arms_slim = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, boolean seated) {
        super(context, slim);
        this.model = seated ?
            new VRPlayerModel<>(slim ? VRLayerDef_slim.bakeRoot() : VRLayerDef.bakeRoot(), slim) :
            new VRPlayerModel_WithArms<>(slim ? VRLayerDef_arms_slim.bakeRoot() : VRLayerDef_arms.bakeRoot(), slim);

        this.addLayer(new HMDLayer(this));
    }

    @Override
    public void render(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        VRPlayersClient.RotInfo rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(player.getUUID());

        if (rotInfo != null) {
            poseStack.scale(rotInfo.heightScale, rotInfo.heightScale, rotInfo.heightScale);
            super.render(player, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.scale(1.0F, 1.0F / rotInfo.heightScale, 1.0F);
        }
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer player, float partialTicks) {
        // idk why we do this anymore
        // this changes the offset to only apply when swimming, instead of crouching
        return player.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
    }

    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        // no crouch hip movement when roomscale crawling
        this.getModel().crouching &= !player.isVisuallySwimming();

        if (player == Minecraft.getInstance().player && this.getModel() instanceof VRPlayerModel_WithArms<?> armsModel && ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA && ClientDataHolderVR.getInstance().cameraTracker.isQuickMode() && ClientDataHolderVR.getInstance().grabScreenShot) {
            // player hands block the camera, so disable them for the screenshot
            armsModel.leftHand.visible = false;
            armsModel.rightHand.visible = false;
            armsModel.leftSleeve.visible = false;
            armsModel.rightSleeve.visible = false;
        }
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        UUID uuid = player.getUUID();
        if (ClientDataHolderVR.getInstance().currentPass != RenderPass.GUI && VRPlayersClient.getInstance().isTracked(uuid)) {
            VRPlayersClient.RotInfo rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(uuid);
            rotationYaw = (float) Math.toDegrees(rotInfo.getBodyYawRadians());
        }

        //vanilla below here
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTicks);
    }
}
