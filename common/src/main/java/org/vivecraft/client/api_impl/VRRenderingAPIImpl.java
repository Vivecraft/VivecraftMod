package org.vivecraft.client.api_impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.VRRenderingAPI;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.common.utils.MathUtils;

public class VRRenderingAPIImpl implements VRRenderingAPI {

    public static final VRRenderingAPIImpl INSTANCE = new VRRenderingAPIImpl();

    private VRRenderingAPIImpl() {
    }

    @Override
    public boolean isVanillaRenderPass() {
        return RenderPassType.isVanilla();
    }

    @Override
    public RenderPass getCurrentRenderPass() {
        return ClientDataHolderVR.getInstance().currentPass;
    }

    @Override
    public boolean isFirstRenderPass() {
        return ClientDataHolderVR.getInstance().isFirstPass;
    }

    @Override
    public Matrix4f getRenderPassMatrix(RenderPass pass) {
        if (!VRState.VR_RUNNING || pass == RenderPass.VANILLA || pass == RenderPass.MIRROR || pass == RenderPass.GUI) {
            Matrix4f m = new Matrix4f();
            m.multiply(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            return m;
        } else {
            return MathUtils.toMcMat4(
                ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(pass).getMatrix());
        }
    }

    @Override
    public Vec3 getHandRenderPos(InteractionHand hand) {
        return RenderHelper.getControllerRenderPos(hand.ordinal());
    }

    @Override
    public void setupRenderingAtHand(InteractionHand hand, PoseStack stack) {
        RenderHelper.setupRenderingAtController(hand.ordinal(), stack);
    }
}
