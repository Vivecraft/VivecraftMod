package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.data.ViveItems;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import javax.annotation.Nullable;

public class ArmsRenderState {
    public boolean renderHands;
    public boolean handsSecond;
    public boolean menuHandMain;
    public boolean menuHandOff;
    public Vec3 mainHandWorldPos;
    public Vec3 offHandWorldPos;
    public final Matrix4f mainHandWorldRot = new Matrix4f();
    public final Matrix4f offHandWorldRot = new Matrix4f();
    public ItemStack mainHandRenderItem = ItemStack.EMPTY;
    public boolean skipMainHandItemRendering;
    public ItemStack offHandRenderItem = ItemStack.EMPTY;
    public boolean skipOffHandItemRendering;
    public int headLight;
    public int rawHeadLightCoords;

    public void extract(@Nullable LocalPlayer player, Vec3 headPos) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();

        this.renderHands = VRArmHelper.shouldRenderHands();
        // render hands in second pass when gui is open
        this.handsSecond = RadialHandler.isShowing() || KeyboardHandler.SHOWING || mc.screen != null;
        this.menuHandMain = dataHolder.menuHandMain;
        this.menuHandOff = dataHolder.menuHandOff;
        this.mainHandWorldPos = RenderHelper.setupRenderingAtController(0, this.mainHandWorldRot.identity(), false);
        this.offHandWorldPos = RenderHelper.setupRenderingAtController(1, this.offHandWorldRot.identity(), false);
        this.headLight = player == null ? 15 :
            Math.max(ShadersHelper.ShaderLight(),
                player.level().getMaxLocalRawBrightness(BlockPos.containing(headPos)));
        this.rawHeadLightCoords =
            player != null ? LevelRenderer.getLightCoords(player.level(), BlockPos.containing(headPos)) :
                LightCoordsUtil.FULL_BRIGHT;
        this.mainHandRenderItem = VRArmHelper.extractHandRenderItem(player, InteractionHand.MAIN_HAND);
        this.offHandRenderItem = VRArmHelper.extractHandRenderItem(player, InteractionHand.OFF_HAND);

        // don't render claws with model arms
        this.skipMainHandItemRendering = player != null && dataHolder.vrSettings.shouldRenderSelf &&
            dataHolder.vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE &&
            (dataHolder.climbTracker.isClimbeyClimb() || ViveItems.isClimbingClaws(player.getMainHandItem()));
        this.skipOffHandItemRendering = player != null && dataHolder.vrSettings.shouldRenderSelf &&
            dataHolder.vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE &&
            (dataHolder.climbTracker.isClimbeyClimb() || ViveItems.isClimbingClaws(player.getOffhandItem()));
    }
}
