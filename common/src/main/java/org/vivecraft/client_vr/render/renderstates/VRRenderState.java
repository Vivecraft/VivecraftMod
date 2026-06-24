package org.vivecraft.client_vr.render.renderstates;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.vivecraft.Xevents;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.helpers.VRWidgetHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import javax.annotation.Nullable;

public class VRRenderState {

    public RenderPass currentPass;

    public float worldScale;
    public boolean inMenuRoom;

    public float partialTick;

    public boolean inWater;
    public boolean inBlock;

    public Vec3 headPos;

    // first person fire
    public boolean firstPersonFire;
    public float fireHeight;
    public float fireYaw;

    // player shadow
    @Nullable
    public Vec3 shadowPos;
    public final Vector2f shadowSize = new Vector2f();

    // block outline
    public VRSettings.RenderPointerElement showOutline;

    // hands
    public final ArmsRenderState armsState = new ArmsRenderState();

    public final TeleportRenderState teleportState = new TeleportRenderState();

    // uis
    public boolean occludeGui;
    public float uiOpacity;
    public UiMode uiRenderMode = UiMode.MENU;
    public boolean uiAfterWorld;
    public float uiWidth;
    public float uiHeight;
    public boolean noHudFog;

    public Keyboard keyboardType = Keyboard.NONE;
    public final PhysicalKeyboard.KeyboardState physicalKeyboardState = new PhysicalKeyboard.KeyboardState();
    public final ScreenRenderState keyboardState = new ScreenRenderState();

    public boolean renderGui;
    public final ScreenRenderState guiState = new ScreenRenderState();
    public boolean radialShowing;
    public final ScreenRenderState radialState = new ScreenRenderState();

    public final CrosshairRenderState crosshairState = new CrosshairRenderState();
    public final CameraWidgetRenderState thirdCamWidgetState = new CameraWidgetRenderState();
    public final CameraWidgetRenderState screenCamWidgetState = new CameraWidgetRenderState();

    public final PostProcessRenderState postProcessState = new PostProcessRenderState();

    public void extract(@Nullable LocalPlayer player, float partialTick, SubmitNodeStorage submitNodeStorage) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();
        VRData worldData = dataHolder.vrPlayer.getVRDataWorld();

        this.currentPass = dataHolder.currentPass;

        this.inMenuRoom = MethodHolder.isInMenuRoom();

        // depends on render pass
        this.occludeGui = VREffectsHelper.shouldOccludeGui();
        this.armsState.renderHands = VRArmHelper.shouldRenderHands();

        // overlay status
        this.inBlock = false;
        this.inWater = false;

        if (!this.inMenuRoom && player != null && !player.isSpectator() && player.isAlive()) {
            Vec3 cameraPos = worldData.getEye(this.currentPass).getPosition();
            Pair<BlockState, BlockPos> block = VREffectsHelper.getNearOpaqueBlock(cameraPos, 0.02);

            this.inBlock = block != null &&
                !Xevents.INSTANCE.renderBlockOverlay(player, new PoseStack(), block.getLeft(), block.getRight(),
                    submitNodeStorage);
            this.inWater =
                player.isEyeInFluid(FluidTags.WATER) &&
                    !Xevents.INSTANCE.renderWaterOverlay(player, new PoseStack(), submitNodeStorage);
        }

        DebugRenderHelper.extractDebug(partialTick);

        // everything after this just needs to be done once per frame
        if (!dataHolder.isFirstPass) return;

        this.partialTick = partialTick;
        this.worldScale = worldData.worldScale;
        this.headPos = worldData.hmd.getPosition();

        this.postProcessState.extract(this.partialTick, this.inWater);

        // first person effects
        this.firstPersonFire = player != null && !player.isSpectator() && player.isOnFire() &&
            !Xevents.INSTANCE.renderFireOverlay(player, new PoseStack(), submitNodeStorage);
        this.fireHeight = (float) (worldData.getHeadPivot().y -
            ((GameRendererExtension) mc.gameRenderer).vivecraft$getRveY()
        );
        this.fireYaw = worldData.getBodyYaw();

        // player shadow
        if (player == null || !player.isAlive() ||
            player.isPassenger() || player != mc.getCameraEntity() ||
            // no indicator when swimming/crawling
            ((PlayerExtension) player).vivecraft$getRoomYOffsetFromPose() < 0.0D ||
            !dataHolder.vrSettings.vrShowBlueCircleBuddy)
        {
            this.shadowPos = null;
        } else {
            this.shadowPos = ((GameRendererExtension) mc.gameRenderer).vivecraft$getRvePos(partialTick)
                .add(0.0D, 0.005D, 0.0D);
            AABB aabb = player.getBoundingBox();
            this.shadowSize.set((float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ));
        }

        // outline
        if (dataHolder.blockModule.isActive(0)) {
            // no block outline when the main arm has interaction
            this.showOutline = VRSettings.RenderPointerElement.NEVER;
        } else if (dataHolder.teleportTracker.isAiming() ||
            dataHolder.vrSettings.renderBlockOutlineMode == VRSettings.RenderPointerElement.NEVER)
        {
            // don't render outline when aiming with tp, or the user disabled it
            this.showOutline = VRSettings.RenderPointerElement.NEVER;
        } else if (dataHolder.vrSettings.renderBlockOutlineMode ==
            VRSettings.RenderPointerElement.ALWAYS)
        {
            // skip vanilla check and always render the outline
            this.showOutline = VRSettings.RenderPointerElement.ALWAYS;
        } else {
            // use the vanilla behaviour
            this.showOutline = null;
        }

        // hands
        this.armsState.extract(player, this.headPos);
        this.teleportState.extract(player);

        // HUDs
        this.uiOpacity = 1F;
        this.uiAfterWorld = ShadersHelper.isShaderActive() &&
            dataHolder.vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.AFTER_SHADER;
        if (mc.level == null) {
            this.uiRenderMode = UiMode.MENU;
        } else if (!ShadersHelper.isShaderActive() ||
            dataHolder.vrSettings.shaderGUIRender != VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID)
        {
            this.uiRenderMode = UiMode.TRANSLUCENT;
        } else {
            this.uiRenderMode = UiMode.CUTOUT;
        }

        this.uiWidth = mc.getWindow().getGuiScaledWidth();
        this.uiHeight = mc.getWindow().getGuiScaledHeight();

        if (!this.inMenuRoom) {
            if (mc.gui.screen() == null) {
                this.uiOpacity = dataHolder.vrSettings.hudOpacity;
            }
            if (player != null && player.isShiftKeyDown()) {
                this.uiOpacity *= 0.75F;
            }
        }
        this.noHudFog = !this.inMenuRoom && mc.gui.screen() != null;

        // check if the main player renders, we need the arm position for the gui if it exists
        // this is a stupid workaround to get the position before the player actually renders
        for (EntityRenderState entityState : mc.gameRenderer.gameRenderState().levelRenderState.entityRenderStates) {
            if (entityState instanceof AvatarRenderState avatarState) {
                ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) entityState).vivecraft$getRotInfo();
                if (rotInfo != null && ((EntityRenderStateExtension) entityState).vivecraft$isFirstPersonPlayer()) {
                    // this is the main player
                    if (mc.getEntityRenderDispatcher()
                        .getRenderer(avatarState) instanceof LivingEntityRenderer livingRenderer)
                    {
                        livingRenderer.getModel().setupAnim(avatarState);
                    }
                }
            }
        }

        // gui render position first, that also sets up the scale
        GuiHandler.extractGui(this.guiState);

        // keyboard
        if (KeyboardHandler.SHOWING && !dataHolder.bowTracker.isDrawing()) {
            this.keyboardState.worldPos = VRPlayer.roomToWorldPos(KeyboardHandler.POS_ROOM, worldData);
            this.keyboardState.worldRotation.rotationY(worldData.rotation_radians).mul(KeyboardHandler.ROTATION_ROOM);
            this.keyboardState.scale = GuiHandler.GUI_SCALE * this.worldScale;

            if (dataHolder.vrSettings.physicalKeyboard) {
                KeyboardHandler.PHYSICAL_KEYBOARD.extract(this.physicalKeyboardState);
                this.keyboardType = Keyboard.PHYSICAL;
            } else {
                this.keyboardType = Keyboard.POINTER;
            }
        } else {
            this.keyboardType = Keyboard.NONE;
        }


        // radial
        this.radialShowing = RadialHandler.isShowing();
        if (this.radialShowing) {
            this.radialState.worldPos = VRPlayer.roomToWorldPos(RadialHandler.POS_ROOM, worldData);
            this.radialState.worldRotation.rotationY(worldData.rotation_radians).mul(RadialHandler.ROTATION_ROOM);
            this.radialState.scale = GuiHandler.GUI_SCALE * this.worldScale;
        }

        // if the gui should even render
        this.renderGui = (mc.gui.screen() != null || this.keyboardType == Keyboard.NONE) &&
            !dataHolder.bowTracker.isDrawing() &&
            (mc.gui.screen() != null || !mc.gui.hud.isHidden()) &&
            !this.radialShowing;

        // ui lights in world
        if (player != null && !this.inMenuRoom) {
            int minLight = ShadersHelper.ShaderLight();
            int headLightCoords = ClientUtils.getCombinedLightWithMin((ClientLevel) player.level(),
                BlockPos.containing(this.headPos), minLight);

            // assign the head brighness if either the head or the ui is in a block
            this.guiState.lightCoords =
                this.inBlock || VREffectsHelper.isInsideOpaqueBlock(this.guiState.worldPos) ? headLightCoords :
                    ClientUtils.getCombinedLightWithMin((ClientLevel) player.level(),
                        BlockPos.containing(this.guiState.worldPos), minLight);
            this.radialState.lightCoords =
                !this.radialShowing || this.inBlock || VREffectsHelper.isInsideOpaqueBlock(this.radialState.worldPos) ?
                    headLightCoords : ClientUtils.getCombinedLightWithMin((ClientLevel) player.level(),
                    BlockPos.containing(this.radialState.worldPos), minLight);
            this.keyboardState.lightCoords = this.keyboardType == Keyboard.NONE || this.inBlock ||
                VREffectsHelper.isInsideOpaqueBlock(this.keyboardState.worldPos) ? headLightCoords :
                ClientUtils.getCombinedLightWithMin((ClientLevel) player.level(),
                    BlockPos.containing(this.keyboardState.worldPos), minLight);
        } else {
            this.guiState.lightCoords = this.radialState.lightCoords = this.keyboardState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }

        VREffectsHelper.extractCrosshairState(this.crosshairState, player);
        VRWidgetHelper.extractVRThirdPersonCamWidget(this.thirdCamWidgetState, player);
        VRWidgetHelper.extractVRHandheldCameraWidget(this.screenCamWidgetState, player);
        DebugRenderHelper.extractDebug(partialTick);
    }

    public enum Keyboard {
        NONE,
        PHYSICAL,
        POINTER
    }

    public enum UiMode {
        MENU,
        CUTOUT,
        TRANSLUCENT
    }
}
