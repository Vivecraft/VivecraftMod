package org.vivecraft.client_vr.render.renderstates;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.Xevents;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.helpers.VRWidgetHelper;

public class VRRenderState {

    public RenderPass currentPass;
    public final CrosshairRenderState crosshairState = new CrosshairRenderState();
    public final CameraWidgetRenderState thirdCamWidgetState = new CameraWidgetRenderState();
    public final CameraWidgetRenderState screenCamWidgetState = new CameraWidgetRenderState();

    public boolean firstPersonFire;
    public float fireHeight;
    public float fireYaw;

    public boolean renderHands;
    public boolean handsSecond;
    public boolean menuHandMain;
    public boolean menuHandOff;

    public boolean occludeGui;

    public void extract(LocalPlayer player, float partialTick) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();

        this.currentPass = dataHolder.currentPass;

        this.firstPersonFire = this.currentPass != RenderPass.THIRD && this.currentPass != RenderPass.CAMERA &&
            !player.isSpectator() && player.isOnFire() &&
            !Xevents.INSTANCE.renderFireOverlay(player, new PoseStack());
        this.fireHeight = (float) (dataHolder.vrPlayer.vrdata_world_render.getHeadPivot().y -
            ((GameRendererExtension) mc.gameRenderer).vivecraft$getRveY()
        );
        this.fireYaw = dataHolder.vrPlayer.vrdata_world_render.getBodyYaw();

        this.renderHands = VRArmHelper.shouldRenderHands();
        // render hands in second pass when gui is open
        this.handsSecond = RadialHandler.isShowing() || KeyboardHandler.SHOWING || mc.screen != null;

        this.occludeGui = VREffectsHelper.shouldOccludeGui();

        VREffectsHelper.extractCrosshairState(this.crosshairState);
        VRWidgetHelper.extractVRThirdPersonCamWidget(this.thirdCamWidgetState);
        VRWidgetHelper.extractVRHandheldCameraWidget(this.screenCamWidgetState);
        DebugRenderHelper.extractDebug(partialTick);
    }

}
