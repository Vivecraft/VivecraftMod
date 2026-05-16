package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.settings.GuiOtherHUDSettings;
import org.vivecraft.client.gui.settings.GuiRenderOpticsSettings;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client.utils.StencilHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.renderstates.CrosshairRenderState;
import org.vivecraft.client_vr.render.renderstates.ScreenRenderState;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mixin.client_vr.renderer.GameRendererAccessor;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.Stream;

public class VREffectsHelper {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Identifier SCOPE_TEXTURE = Identifier.withDefaultNamespace(
        "textures/misc/spyglass_scope.png");

    /**
     * checks if the given position is inside a block that blocks vision
     *
     * @param pos position to check
     * @return if vision is blocked
     */
    public static boolean isInsideOpaqueBlock(Vec3 pos) {
        if (MC.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(pos);
            return MC.level.getBlockState(blockpos).isSolidRender();
        }
    }

    /**
     * check if the given position is 'dist' near a block that blocks vision
     *
     * @param pos  position to check
     * @param dist distance where it should still count as inside the block
     * @return null if there is no block, else a tuple containing
     * BlockState and BlockPos of the blocking block
     */
    public static Tuple<BlockState, BlockPos> getNearOpaqueBlock(Vec3 pos, double dist) {
        if (MC.level == null) {
            return null;
        } else {
            AABB aabb = new AABB(pos.subtract(dist, dist, dist), pos.add(dist, dist, dist));
            Stream<BlockPos> stream = BlockPos.betweenClosedStream(aabb).filter((bp) ->
                MC.level.getBlockState(bp).isSolidRender());
            Optional<BlockPos> optional = stream.findFirst();
            return optional.map(blockPos -> new Tuple<>(MC.level.getBlockState(blockPos), blockPos)).orElse(null);
        }
    }

    /**
     * checks if the given entity is the active VR player and the camera entity
     *
     * @param entity Entity to check if it is the first person Player
     * @return if the given entity is the active VR player and the camera entity
     */
    public static boolean isFirstPersonPlayer(Entity entity) {
        return VRState.VR_RUNNING && entity == MC.player && entity == MC.getCameraEntity();
    }

    /**
     * checks if the given {@code entity} is the main player entity and if it should be rendered
     *
     * @param entity Entity to check
     * @return if the {@code entity} is the main player and is rendering in first person
     */
    public static boolean isRenderingFirstPersonPlayer(Entity entity) {
        return isFirstPersonPlayer(entity) && isFirstPersonEntityPass();
    }

    /**
     * checks if the given {@code renderState} is from the main player entity and if it should be rendered
     *
     * @param renderState EntityRenderState to check
     * @return if the {@code renderState} belongs to the main player and is rendering in first person
     */
    public static boolean isRenderingFirstPersonPlayer(EntityRenderState renderState) {
        return ((EntityRenderStateExtension) renderState).vivecraft$isFirstPersonPlayer() && isFirstPersonEntityPass();
    }

    /**
     * @return if the current pass is first person and should render the main player entity
     */
    public static boolean isFirstPersonEntityPass() {
        return DATA_HOLDER.vrSettings.shouldRenderSelf &&
            RenderPass.isFirstPerson(DATA_HOLDER.currentPass) &&
            !ShadersHelper.isRenderingShadows() &&
            !(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal());
    }

    /**
     * draws the spyglass view of the given controller
     *
     * @param output    SubmitNodeCollector to submit the render calls to
     * @param poseStack PoseStack for positioning
     * @param c         controller index for the scope
     */
    public static void drawScopeFB(SubmitNodeCollector output, PoseStack poseStack, int c) {
        poseStack.pushPose();

        GpuTextureView scopeView;

        if (c == 0) {
            scopeView = DATA_HOLDER.vrRenderer.telescopeFramebufferR.getColorTextureView();
        } else {
            scopeView = DATA_HOLDER.vrRenderer.telescopeFramebufferL.getColorTextureView();
        }

        // size of the back of the spyglass 2/16
        float scale = 0.125F;

        float alpha = TelescopeTracker.viewPercent(c);
        // draw spyglass view
        RenderHelper.submitSizedQuadFullbright(720.0F, 720.0F, scale, new float[]{alpha, alpha, alpha, 1},
            poseStack, VRRenderTypes.entitySolidNoCardinalLight(scopeView, false), output, 1);

        // draw spyglass overlay
        // slight offset to not cause z fighting
        poseStack.translate(0.0F, 0.0F, 0.00001F);
        // get light at the controller position
        int light = LevelRenderer.getLightCoords(MC.level, BlockPos.containing(
            DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c).getPosition()));
        // draw the overlay, and flip it vertically
        RenderHelper.submitSizedQuadWithLightmap(720.0F, 720.0F, scale, light, poseStack,
            RenderTypes.entityTranslucent(SCOPE_TEXTURE), true, output, 2);

        poseStack.popPose();
    }

    private static boolean WAS_STENCIL_ON;

    /**
     * enables stencil test, and draws the stencil, if enabled for the current RenderPass
     */
    public static void drawEyeStencil() {
        if (DATA_HOLDER.vrSettings.vrUseStencil) {
            if (StencilHelper.stencilBufferSupported()) {
                WAS_STENCIL_ON = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
                if (WAS_STENCIL_ON && !DATA_HOLDER.showedStencilMessage &&
                    DATA_HOLDER.vrSettings.showChatMessageStencil)
                {
                    DATA_HOLDER.showedStencilMessage = true;
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.stencil",
                        Component.translatable("vivecraft.messages.3options",
                                Component.translatable("options.title"),
                                Component.translatable("vivecraft.options.screen.main"),
                                Component.translatable("vivecraft.options.screen.stereorendering"))
                            .withStyle(style -> style.withClickEvent(
                                    new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN,
                                        new GuiRenderOpticsSettings(null)))
                                .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatable("vivecraft.messages.openSettings")))
                                .withColor(ChatFormatting.GREEN)
                                .withItalic(true)),
                        Component.translatable("vivecraft.messages.3options",
                                Component.translatable("options.title"),
                                Component.translatable("vivecraft.options.screen.main"),
                                Component.translatable("vivecraft.options.screen.guiother"))
                            .withStyle(style -> style.withClickEvent(
                                    new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN,
                                        new GuiOtherHUDSettings(null)))
                                .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatable("vivecraft.messages.openSettings")))
                                .withColor(ChatFormatting.GREEN)
                                .withItalic(true))
                    ));
                }
            }

            // don't touch the stencil if we don't use it
            // stencil only for left/right VR view
            if ((DATA_HOLDER.currentPass == RenderPass.LEFT || DATA_HOLDER.currentPass == RenderPass.RIGHT) &&
                (!ImmersivePortalsHelper.isLoaded() || !ImmersivePortalsHelper.isRenderingPortal()))
            {
                DATA_HOLDER.vrRenderer.doStencil(false);
            }
        }
    }

    /**
     * disables the stencil pass if it was enabled by us
     */
    public static void disableStencilTest() {
        // if we did enable the stencil test, disable it
        if (StencilHelper.stencilBufferSupported() && !WAS_STENCIL_ON) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }

    // textures for the panorama menu
    private static final Identifier CUBE_FRONT = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_0.png");
    private static final Identifier CUBE_RIGHT = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_1.png");
    private static final Identifier CUBE_BACK = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_2.png");
    private static final Identifier CUBE_LEFT = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_3.png");
    private static final Identifier CUBE_UP = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_4.png");
    private static final Identifier CUBE_DOWN = Identifier.withDefaultNamespace(
        "textures/gui/title/background/panorama_5.png");
    private static final Identifier DIRT = Identifier.withDefaultNamespace(
        "textures/block/dirt.png");
    private static final Identifier GRASS = Identifier.withDefaultNamespace(
        "textures/block/grass_block_top.png");

    /**
     * renders a 100^3 cubemap and a dirt/grass floor
     *
     * @param output    SubmitNodeCollector to output to
     * @param poseStack PoseStack to use for positioning
     * @param order     order to render at
     * @return order to render the next thing at
     */
    public static int renderMenuPanorama(SubmitNodeCollector output, PoseStack poseStack, int order) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            MC.getMainRenderTarget().getColorTexture(), 0xFF000000,
            MC.getMainRenderTarget().getDepthTexture(), 1.0);

        poseStack.pushPose();

        // translate by half of the cube size
        poseStack.translate(-50F, -50F, -50.0F);

        // down
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_DOWN), (pose, consumer) -> {
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(0, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 0, 100)
                .setUv(0, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 0, 100)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 0, 0)
                .setUv(1, 0).setColor(255, 255, 255, 255);
        });

        // up
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_UP), (pose, consumer) -> {
            consumer.addVertex(pose, 0, 100, 100)
                .setUv(0, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 100, 0)
                .setUv(0, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 0)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 100)
                .setUv(1, 0).setColor(255, 255, 255, 255);
        });

        // left
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_LEFT), (pose, consumer) -> {
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 100, 0)
                .setUv(1, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 100, 100)
                .setUv(0, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 0, 100)
                .setUv(0, 1).setColor(255, 255, 255, 255);
        });

        // right
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_RIGHT), (pose, consumer) -> {
            consumer.addVertex(pose, 100, 0, 0)
                .setUv(0, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 0, 100)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 100)
                .setUv(1, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 0)
                .setUv(0, 0).setColor(255, 255, 255, 255);
        });

        // front
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_FRONT), (pose, consumer) -> {
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(0, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 0, 0)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 0)
                .setUv(1, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 100, 0)
                .setUv(0, 0).setColor(255, 255, 255, 255);
        });

        // back
        output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(CUBE_BACK), (pose, consumer) -> {
            consumer.addVertex(pose, 0, 0, 100)
                .setUv(1, 1).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 0, 100, 100)
                .setUv(1, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 100, 100)
                .setUv(0, 0).setColor(255, 255, 255, 255);
            consumer.addVertex(pose, 100, 0, 100)
                .setUv(0, 1).setColor(255, 255, 255, 255);
        });

        poseStack.popPose();

        order++;

        // render floor
        Vector2fc area = DATA_HOLDER.vr.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }
        // render two floors, grass at room size, and dirt a bit bigger
        for (int i = 0; i < 2; i++) {
            float width = area.x() + i * 2;
            float length = area.y() + i * 2;

            poseStack.pushPose();

            int r, g, b;
            if (i == 0) {
                // plains grass color, but a bit darker
                r = 114;
                g = 148;
                b = 70;
            } else {
                r = g = b = 128;
            }

            // offset so the floor is centered
            poseStack.translate(-width * 0.5F, 0.0F, -length * 0.5F);

            final int repeat = 4; // texture wraps per meter
            int offset = i;
            output.order(order).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(i == 0 ? GRASS : DIRT),
                (pose, consumer) -> {
                    consumer
                        .addVertex(pose, 0, 0.005f * -offset, 0)
                        .setUv(0, 0)
                        .setColor(r, g, b, 255);
                    consumer
                        .addVertex(pose, 0, 0.005f * -offset, length)
                        .setUv(0, repeat * length)
                        .setColor(r, g, b, 255);
                    consumer
                        .addVertex(pose, width, 0.005f * -offset, length)
                        .setUv(repeat * width, repeat * length)
                        .setColor(r, g, b, 255);
                    consumer
                        .addVertex(pose, width, 0.005f * -offset, 0)
                        .setUv(repeat * width, 0)
                        .setColor(r, g, b, 255);
                });
            poseStack.popPose();
        }
        return order;
    }

    /**
     * renders a dirt cube, slightly bigger than the room size
     *
     * @param output    SubmitNodeCollector to output to
     * @param poseStack PoseStack to use for positioning
     * @param order     order to render at
     * @return order to render the next thing at
     */
    public static int renderJrbuddasAwesomeMainMenuRoomNew(SubmitNodeCollector output, PoseStack poseStack, int order) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
            MC.getMainRenderTarget().getColorTexture(), 0xFF000000,
            MC.getMainRenderTarget().getDepthTexture(), 1.0);

        int repeat = 4; // texture wraps per meter
        float height = 2.5F;
        float oversize = 1.3F; // how much bigger than the room

        Vector2fc area = DATA_HOLDER.vr.getPlayAreaSize();
        if (area == null) {
            area = new Vector2f(2, 2);
        }

        float width = area.x() + oversize;
        float length = area.y() + oversize;

        float r, g, b, a;
        r = g = b = 0.8f;
        a = 1.0f;

        poseStack.pushPose();

        // offset so the room is centered
        poseStack.translate(-width * 0.5F, 0.0F, -length * 0.5F);

        output.order(order++).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(DIRT), (pose, consumer) -> {
            // floor
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, 0, length)
                .setUv(0, repeat * length).setColor(r, g, b, a);
            consumer.addVertex(pose, width, 0, length)
                .setUv(repeat * width, repeat * length).setColor(r, g, b, a);
            consumer.addVertex(pose, width, 0, 0)
                .setUv(repeat * width, 0).setColor(r, g, b, a);

            // ceiling
            consumer.addVertex(pose, 0, height, length)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, height, 0)
                .setUv(0, repeat * length).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, 0)
                .setUv(repeat * width, repeat * length).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, length)
                .setUv(repeat * width, 0).setColor(r, g, b, a);

            // left
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, height, 0)
                .setUv(0, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, height, length)
                .setUv(repeat * length, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, 0, length)
                .setUv(repeat * length, 0).setColor(r, g, b, a);

            // right
            consumer.addVertex(pose, width, 0, 0)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, width, 0, length)
                .setUv(repeat * length, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, length)
                .setUv(repeat * length, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, 0)
                .setUv(0, repeat * height).setColor(r, g, b, a);

            // front
            consumer.addVertex(pose, 0, 0, 0)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, width, 0, 0)
                .setUv(repeat * width, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, 0)
                .setUv(repeat * width, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, height, 0)
                .setUv(0, repeat * height).setColor(r, g, b, a);

            // back
            consumer.addVertex(pose, 0, 0, length)
                .setUv(0, 0).setColor(r, g, b, a);
            consumer.addVertex(pose, 0, height, length)
                .setUv(0, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, width, height, length)
                .setUv(repeat * width, repeat * height).setColor(r, g, b, a);
            consumer.addVertex(pose, width, 0, length)
                .setUv(repeat * width, 0).setColor(r, g, b, a);
        });

        poseStack.popPose();
        return order;
    }

    /**
     * renders the loaded menuworld and a room floor quad
     *
     * @param output    SubmitNodeCollector to output to
     * @param poseStack PoseStack to use for positioning
     * @param order     order to render at
     * @return order to render the next thing at
     */
    public static int renderTechjarsAwesomeMainMenuRoom(SubmitNodeCollector output, PoseStack poseStack, int order) {
        // transfer the rotation
        RenderSystem.getModelViewStack().pushMatrix().mul(poseStack.last().pose());

        try {
            // use irl time for sky, or fast forward
            int tzOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
            DATA_HOLDER.menuWorldRenderer.time = DATA_HOLDER.menuWorldRenderer.fastTime ?
                (long) (DATA_HOLDER.menuWorldRenderer.ticks * 10L + 10.0F * ClientUtils.getCurrentPartialTick()) :
                (long) ((System.currentTimeMillis() + tzOffset - 21600000) / 86400000D * 24000D);

            // clear sky
            DATA_HOLDER.menuWorldRenderer.fogRenderer.setupFogColor();
            DATA_HOLDER.menuWorldRenderer.fogRenderer.updateFog();

            RenderSystem.getDevice().createCommandEncoder()
                .clearColorAndDepthTextures(MC.getMainRenderTarget().getColorTexture(),
                    ARGB.colorFromFloat(0.0f, DATA_HOLDER.menuWorldRenderer.fogRenderer.fogColor.x,
                        DATA_HOLDER.menuWorldRenderer.fogRenderer.fogColor.y,
                        DATA_HOLDER.menuWorldRenderer.fogRenderer.fogColor.z),
                    MC.getMainRenderTarget().getDepthTexture(), 1.0);

            DATA_HOLDER.menuWorldRenderer.updateLightmap();
            // render world
            DATA_HOLDER.menuWorldRenderer.render(RenderSystem.getModelViewStack());

            // render room floor
            Vector2fc area = DATA_HOLDER.vr.getPlayAreaSize();
            if (area == null) {
                area = new Vector2f(2, 2);
            }

            float width = area.x();
            float length = area.y();

            float sun = DATA_HOLDER.menuWorldRenderer.getSkyDarken();

            poseStack.pushPose();

            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);

            output.order(order++).submitCustomGeometry(poseStack, VRRenderTypes.guiTextured(DIRT), (pose, consumer) -> {
                consumer
                    .addVertex(pose, 0, 0.005f, 0)
                    .setUv(0, 0)
                    .setColor(sun, sun, sun, 0.3f);
                consumer
                    .addVertex(pose, 0, 0.005f, length)
                    .setUv(0, 4 * length)
                    .setColor(sun, sun, sun, 0.3f);
                consumer
                    .addVertex(pose, width, 0.005f, length)
                    .setUv(4 * width, 4 * length)
                    .setColor(sun, sun, sun, 0.3f);
                consumer
                    .addVertex(pose, width, 0.005f, 0)
                    .setUv(4 * width, 0)
                    .setColor(sun, sun, sun, 0.3f);
            });

            poseStack.popPose();
        } finally {
            // reset stacks
            RenderSystem.getModelViewStack().popMatrix();
        }
        return order;
    }

    /**
     * renders the menu environment, aswell as hands, screen and keyboard
     *
     * @param featureRenderer FeatureRenderDispatcher to render with
     * @param output          SubmitNodeCollector to out put to
     * @param levelState      level state to get the camera state from
     */
    public static void renderMenuRoom(
        FeatureRenderDispatcher featureRenderer, SubmitNodeCollector output, LevelRenderState levelState)
    {
        // clear depth for menu environment
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(MC.mainRenderTarget.getDepthTexture(), 1.0);

        VRRenderState vrState = ((LevelRenderStateExtension) levelState).vivecraft$getVRRenderState();
        CameraRenderState cameraState = levelState.cameraRenderState;

        RenderSystem.getModelViewStack().pushMatrix().identity();
        RenderHelper.applyVRModelView(vrState.currentPass, RenderSystem.getModelViewStack());
        RenderSystem.backupProjectionMatrix();
        VRShaders.setUndistortedProj(cameraState.projectionMatrix);
        RenderSystem.setProjectionMatrix(VRShaders.UNDISTORTED_PROJ_BUFFER, ProjectionType.PERSPECTIVE);
        PoseStack poseStack = new PoseStack();

        int order = 0;

        order = renderMenuEnvironment(output, cameraState, poseStack, order);
        order = renderGuiLayer(output, cameraState, vrState, poseStack, true, order);

        if (vrState.keyboardType != VRRenderState.Keyboard.NONE) {
            if (vrState.keyboardType == VRRenderState.Keyboard.PHYSICAL) {
                order = renderPhysicalKeyboard(output, cameraState, vrState, poseStack, order);
            } else {
                order = renderScreen(output, cameraState, vrState, vrState.keyboardState, KeyboardHandler.FRAMEBUFFER,
                    true, true, poseStack, order);
            }
        }

        if (vrState.currentPass != RenderPass.CAMERA &&
            (vrState.currentPass != RenderPass.THIRD || DATA_HOLDER.vrSettings.mixedRealityRenderHands))
        {
            order = VRArmHelper.renderVRHands(output, vrState, cameraState, poseStack, true, true, true, true, order);
        }
        featureRenderer.renderAllFeatures();

        ((LevelRendererExtension) MC.levelRenderer).vivecraft$renderGizmos(poseStack, cameraState,
            RenderSystem.getModelViewStack());

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    /**
     * renders the current menu environment
     *
     * @param output      SubmitNodeCollector to output to
     * @param cameraState sate of the camera for the position
     * @param poseStack   PoseStack to use for positioning
     * @param order       order to render at
     * @return order to render the next thing at
     */

    public static int renderMenuEnvironment(
        SubmitNodeCollector output, CameraRenderState cameraState, PoseStack poseStack, int order)
    {
        // MAIN MENU ENVIRONMENT
        poseStack.pushPose();
        Vec3 eye = cameraState.pos;
        poseStack.translate((float) (DATA_HOLDER.vrPlayer.vrdata_world_render.origin.x - eye.x),
            (float) (DATA_HOLDER.vrPlayer.vrdata_world_render.origin.y - eye.y),
            (float) (DATA_HOLDER.vrPlayer.vrdata_world_render.origin.z - eye.z));

        // remove world rotation or the room doesn't align with the screen
        poseStack.mulPose(Axis.YN.rotation(-DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians));

        if (DATA_HOLDER.menuWorldRenderer.isReady()) {
            try {
                order = renderTechjarsAwesomeMainMenuRoom(output, poseStack, order);
            } catch (Exception e) {
                VRSettings.LOGGER.error(
                    "Vivecraft: Error rendering main menu world, unloading to prevent more errors: ", e);
                DATA_HOLDER.menuWorldRenderer.destroy();
            }
        } else {
            if (DATA_HOLDER.vrSettings.menuWorldFallbackPanorama) {
                order = renderMenuPanorama(output, poseStack, order);
            } else {
                order = renderJrbuddasAwesomeMainMenuRoomNew(output, poseStack, order);
            }
        }
        poseStack.popPose();
        return order;
    }

    /**
     * renders the vivecraft stuff into separate buffers for the fabulous settings
     * this includes hands, vr shadow, gui, camera widgets and other stuff
     *
     * @param featureRender FeatureRenderDispatcher to render with
     * @param output        SubmitNodeCollector to output to
     * @param levelState    LevelRenderState to getthe vr renderstate and camera state from
     * @param poseStack     PoseStack to use for positioning
     * @param targets       RenderTarget bundle that holds the framebuffers for rendering
     */
    public static void renderVRFabulous(
        FeatureRenderDispatcher featureRender, SubmitNodeCollector output, LevelRenderState levelState,
        PoseStack poseStack, LevelTargetBundle targets)
    {
        VRRenderState vrState = ((LevelRenderStateExtension) levelState).vivecraft$getVRRenderState();
        if (vrState.currentPass == RenderPass.SCOPEL || vrState.currentPass == RenderPass.SCOPER) {
            // skip for spyglass
            return;
        }

        int order = 0;

        Profiler.get().push("VR");
        renderCrosshairAtDepth(output, vrState.crosshairState, levelState.cameraRenderState, poseStack, order);
        // render stuff
        featureRender.renderAllFeatures();
        MC.renderBuffers().bufferSource().endBatch();

        // switch to VR Occluded buffer, and copy main depth for occlusion
        LevelTargetBundleExtension extTargets = (LevelTargetBundleExtension) targets;

        RenderSystem.getDevice().createCommandEncoder()
            .clearColorTexture(extTargets.vivecraft$getOccluded().get().getColorTexture(), 0x00000000);
        extTargets.vivecraft$getOccluded().get().copyDepthFrom(targets.main.get());

        RenderSystem.outputColorTextureOverride = extTargets.vivecraft$getOccluded().get().getColorTextureView();
        RenderSystem.outputDepthTextureOverride = extTargets.vivecraft$getOccluded().get().getDepthTextureView();

        order = 0;
        if (vrState.occludeGui) {
            order = renderGuiAndShadow(output, vrState, levelState.cameraRenderState, poseStack, false, false, order);
            order = VRArmHelper.renderVRHands(output, vrState, levelState.cameraRenderState, poseStack,
                vrState.armsState.renderHands && vrState.armsState.menuHandMain,
                vrState.armsState.renderHands && vrState.armsState.menuHandOff, true, true, order);
        }

        // render stuff
        featureRender.renderAllFeatures();
        MC.renderBuffers().bufferSource().endBatch();

        // switch to VR UnOccluded buffer, no depth copy
        RenderSystem.getDevice().createCommandEncoder()
            .clearColorAndDepthTextures(extTargets.vivecraft$getUnoccluded().get().getColorTexture(), 0x00000000,
                extTargets.vivecraft$getUnoccluded().get().getDepthTexture(), 1.0);
        RenderSystem.outputColorTextureOverride = extTargets.vivecraft$getUnoccluded().get().getColorTextureView();
        RenderSystem.outputDepthTextureOverride = extTargets.vivecraft$getUnoccluded().get().getDepthTextureView();

        order = 0;
        if (!vrState.occludeGui) {
            order = renderGuiAndShadow(output, vrState, levelState.cameraRenderState, poseStack, false, false, order);
        }

        order = renderVRSelfEffects(output, vrState, levelState.cameraRenderState, poseStack, order);
        VRWidgetHelper.renderVRThirdPersonCamWidget(output, levelState.cameraRenderState, vrState.thirdCamWidgetState,
            poseStack);
        VRWidgetHelper.renderVRHandheldCameraWidget(output, levelState.cameraRenderState, vrState.screenCamWidgetState,
            poseStack);

        if (!vrState.occludeGui) {
            order = VRArmHelper.renderVRHands(output, vrState, levelState.cameraRenderState, poseStack,
                vrState.armsState.renderHands && vrState.armsState.menuHandMain,
                vrState.armsState.renderHands && vrState.armsState.menuHandOff, true, true, order);
        }

        // render stuff
        featureRender.renderAllFeatures();
        MC.renderBuffers().bufferSource().endBatch();

        // switch to VR hands buffer
        RenderSystem.getDevice().createCommandEncoder()
            .clearColorTexture(extTargets.vivecraft$getHands().get().getColorTexture(), 0x00000000);
        extTargets.vivecraft$getHands().get().copyDepthFrom(targets.main.get());
        RenderSystem.outputColorTextureOverride = extTargets.vivecraft$getHands().get().getColorTextureView();
        RenderSystem.outputDepthTextureOverride = extTargets.vivecraft$getHands().get().getDepthTextureView();

        order = 0;
        order = VRArmHelper.renderVRHands(output, vrState, levelState.cameraRenderState, poseStack,
            vrState.armsState.renderHands && !vrState.armsState.menuHandMain,
            vrState.armsState.renderHands && !vrState.armsState.menuHandOff, false, false, order);

        // render stuff
        featureRender.renderAllFeatures();
        MC.renderBuffers().bufferSource().endBatch();

        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        Profiler.get().pop();
    }

    /**
     * renders the vivecraft stuff, for fast and fancy setting, separated into 2 passes
     * one before and one after translucents.
     * this includes hands, vr shadow, gui, camera widgets and other stuff
     *
     * @param output         SubmitNodeCollector to output to
     * @param levelState     LevelRenderState to getthe vr renderstate and camera state from
     * @param poseStack      PoseStack to use for positioning
     * @param secondPassOnly if true, only renders the screen and hands
     */
    public static void renderVrFast(
        SubmitNodeCollector output, LevelRenderState levelState, PoseStack poseStack, boolean secondPassOnly)
    {
        VRRenderState vrState = ((LevelRenderStateExtension) levelState).vivecraft$getVRRenderState();
        if (vrState.currentPass == RenderPass.SCOPEL || vrState.currentPass == RenderPass.SCOPER) {
            // skip for spyglass
            return;
        }

        Profiler.get().push("render VR");
        int order = 0;
        if (!secondPassOnly) {
            order = renderCrosshairAtDepth(output, vrState.crosshairState, levelState.cameraRenderState, poseStack,
                order);

            // item renderers can't be ordered, should be fine though
            VRWidgetHelper.renderVRThirdPersonCamWidget(output, levelState.cameraRenderState,
                vrState.thirdCamWidgetState, poseStack);
            VRWidgetHelper.renderVRHandheldCameraWidget(output, levelState.cameraRenderState,
                vrState.screenCamWidgetState, poseStack);

            if (!vrState.armsState.handsSecond) {
                order = VRArmHelper.renderVRHands(output, vrState, levelState.cameraRenderState, poseStack,
                    vrState.armsState.renderHands, vrState.armsState.renderHands, vrState.armsState.menuHandMain,
                    vrState.armsState.menuHandOff, order);
            }

            order = renderVRSelfEffects(output, vrState, levelState.cameraRenderState, poseStack, order);
        }

        if (secondPassOnly || !vrState.uiAfterWorld) {
            order = renderGuiAndShadow(output, vrState, levelState.cameraRenderState, poseStack, !vrState.occludeGui,
                true,
                order);

            if (vrState.armsState.handsSecond) {
                order = VRArmHelper.renderVRHands(output, vrState, levelState.cameraRenderState, poseStack,
                    vrState.armsState.renderHands, vrState.armsState.renderHands, vrState.armsState.menuHandMain,
                    vrState.armsState.menuHandOff, order);
            }
        }
        Profiler.get().pop();
    }

    /**
     * @return if the gui should be occluded
     */
    public static boolean shouldOccludeGui() {
        if (RenderPass.isThirdPerson(DATA_HOLDER.currentPass)) {
            return true;
        } else {
            return DATA_HOLDER.vrSettings.hudOcclusion &&
                !MethodHolder.isInMenuRoom() &&
                MC.screen == null &&
                !KeyboardHandler.SHOWING &&
                !RadialHandler.isShowing() &&
                !isInsideOpaqueBlock(MC.gameRenderer.getMainCamera().position());
        }
    }

    /**
     * renders the guis (current screen/hud, radial and keyboard) and player shadow in the correct order
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param depthAlways if the depth test should be disabled
     * @param shadowFirst if the player shadow should be rendered first
     * @param order       order to render at
     * @return order to render the next thing at
     */
    private static int renderGuiAndShadow(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean depthAlways, boolean shadowFirst, int order)
    {
        if (shadowFirst) {
            order = VREffectsHelper.renderVrShadow(output, vrState, cameraState, poseStack, depthAlways, order);
        }

        order = renderGuiLayer(output, cameraState, vrState, poseStack, depthAlways, order);

        if (!shadowFirst) {
            order = VREffectsHelper.renderVrShadow(output, vrState, cameraState, poseStack, depthAlways, order);
        }

        if (vrState.keyboardType != VRRenderState.Keyboard.NONE) {
            if (vrState.keyboardType == VRRenderState.Keyboard.PHYSICAL) {
                order = renderPhysicalKeyboard(output, cameraState, vrState, poseStack, order);
            } else {
                order = renderScreen(output, cameraState, vrState, vrState.keyboardState, KeyboardHandler.FRAMEBUFFER,
                    depthAlways, true, poseStack, order);
            }
        }

        if (vrState.radialShowing) {
            order = renderScreen(output, cameraState, vrState, vrState.radialState, RadialHandler.FRAMEBUFFER,
                depthAlways,
                true, poseStack, order);
        }
        return order;
    }

    /**
     * renders the player position indicator
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param depthAlways if the depth test should be disabled
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderVrShadow(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        boolean depthAlways, int order)
    {
        if (RenderPass.isThirdPerson(vrState.currentPass)) return order;

        if (vrState.shadowPos != null) {
            Profiler.get().push("vr shadow");

            Vec3 pos = vrState.shadowPos.subtract(cameraState.pos);

            order = RenderHelper.renderFlatQuad(pos, vrState.shadowSize.x(), vrState.shadowSize.y(),
                0.0F, 0, 0, 0, 64, poseStack, depthAlways, output, order);
            Profiler.get().pop();
        }
        return order;
    }

    /**
     * renders effects around the player, includes burning animation and totem of undying
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param order       order to render at
     * @return order to render the next thing at
     */
    private static int renderVRSelfEffects(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        int order)
    {
        // only render the fire in first person, other views have the burning entity
        if (vrState.firstPersonFire && vrState.currentPass != RenderPass.THIRD &&
            vrState.currentPass != RenderPass.CAMERA)
        {
            order = VREffectsHelper.renderFireInFirstPerson(output, vrState, cameraState, poseStack, order);
        }

        // totem of undying
        // can't be ordered
        ((GameRendererAccessor) MC.gameRenderer).getScreenEffectRenderer()
            .renderItemActivationAnimation(poseStack, vrState.partialTick, output);
        return order;
    }

    /**
     * renders the fire when the player is burning
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderFireInFirstPerson(
        SubmitNodeCollector output, VRRenderState vrState, CameraRenderState cameraState, PoseStack poseStack,
        int order)
    {
        poseStack.pushPose();
        poseStack.translate(
            vrState.headPos.x - cameraState.pos.x,
            vrState.headPos.y - cameraState.pos.y,
            vrState.headPos.z - cameraState.pos.z
        );

        TextureAtlasSprite fireSprite = MC.getAtlasManager().get(ModelBakery.FIRE_1);

        if (OptifineHelper.isOptifineLoaded()) {
            OptifineHelper.markTextureAsActive(fireSprite);
        }

        // code adapted from net.minecraft.client.renderer.ScreenEffectRenderer.renderFire

        float uMin = fireSprite.getU0();
        float uMax = fireSprite.getU1();

        float vMin = fireSprite.getV0();
        float vMax = fireSprite.getV1();

        float width = 0.3F;

        RenderType renderType = VRRenderTypes.guiTextured(fireSprite.atlasLocation(),
            // with depthtest in third
            !RenderPass.isThirdPerson(vrState.currentPass));

        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0F - vrState.fireYaw));
            poseStack.translate(0.0D, -vrState.fireHeight, 0.0D);

            RenderHelper.submitLateCustomGeometry(output.order(order), poseStack, renderType,
                (pose, consumer) -> {
                    consumer.addVertex(pose, -width, 0.0F, -width)
                        .setUv(uMax, vMax).setColor(1.0F, 1.0F, 1.0F, 0.9F);
                    consumer.addVertex(pose, width, 0.0F, -width)
                        .setUv(uMin, vMax).setColor(1.0F, 1.0F, 1.0F, 0.9F);
                    consumer.addVertex(pose, width, vrState.fireHeight, -width)
                        .setUv(uMin, vMin).setColor(1.0F, 1.0F, 1.0F, 0.9F);
                    consumer.addVertex(pose, -width, vrState.fireHeight, -width)
                        .setUv(uMax, vMin).setColor(1.0F, 1.0F, 1.0F, 0.9F);
                });

            poseStack.popPose();
        }
        poseStack.popPose();
        return order + 1;
    }

    /**
     * renders the physical touch keyboard
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderPhysicalKeyboard(
        SubmitNodeCollector output, CameraRenderState cameraState, VRRenderState vrState, PoseStack poseStack,
        int order)
    {
        poseStack.pushPose();
        Profiler.get().push("renderPhysicalKeyboard");

        Profiler.get().push("applyPhysicalKeyboardModelView");

        // offset from eye to keyboard pos
        poseStack.translate(vrState.keyboardState.worldPos.x - cameraState.pos.x,
            vrState.keyboardState.worldPos.y - cameraState.pos.y,
            vrState.keyboardState.worldPos.z - cameraState.pos.z);

        poseStack.mulPose(vrState.keyboardState.worldRotation);

        poseStack.scale(vrState.worldScale, vrState.worldScale, vrState.worldScale);

        // pop apply modelview
        Profiler.get().pop();

        order = KeyboardHandler.PHYSICAL_KEYBOARD.render(output, vrState.physicalKeyboardState, poseStack, order);

        // pop render
        Profiler.get().pop();
        poseStack.popPose();
        return order;
    }

    /**
     * Renders the given RenderTarget into the world at the given location.
     *
     * @param output      SubmitNodeCollector to submit the rendercall to
     * @param cameraState state of the camera
     * @param vrState     VR state
     * @param screenState state of the screen
     * @param framebuffer RenderTarget to render into the world
     * @param depthAlways if the depth test should be disabled
     * @param noFog       disables fog, used to render menus without for in lava
     * @param poseStack   PoseStack to use for positioning
     */
    public static int renderScreen(
        SubmitNodeCollector output, CameraRenderState cameraState, VRRenderState vrState, ScreenRenderState screenState,
        RenderTarget framebuffer, boolean depthAlways, boolean noFog, PoseStack poseStack, int order)
    {
        Profiler.get().push("render screen");

        poseStack.pushPose();

        // position
        poseStack.translate(screenState.worldPos.x - cameraState.pos.x,
            screenState.worldPos.y - cameraState.pos.y,
            screenState.worldPos.z - cameraState.pos.z);
        poseStack.mulPose(screenState.worldRotation);
        poseStack.scale(screenState.scale, screenState.scale, screenState.scale);

        float[] color = new float[]{1.0F, 1.0F, 1.0F, vrState.uiOpacity};

        order = switch (vrState.uiRenderMode) {
            case TRANSLUCENT -> RenderHelper.submitSizedQuadWithLightmap(vrState.uiWidth, vrState.uiHeight, 1.5F,
                screenState.lightCoords, color, poseStack,
                VRRenderTypes.entityTranslucentNoCardinalLightLinear(framebuffer.getColorTextureView(), depthAlways,
                    noFog), false, output, order);
            case CUTOUT -> RenderHelper.submitSizedQuadWithLightmap(vrState.uiWidth, vrState.uiHeight, 1.5F,
                screenState.lightCoords, color, poseStack,
                VRRenderTypes.entityCutoutNoCardinalLightLinear(framebuffer.getColorTextureView(), depthAlways, noFog),
                false, output, order);
            case MENU ->
                RenderHelper.drawSizedQuad(vrState.uiWidth, vrState.uiHeight, 1.5F, color, poseStack, framebuffer,
                    depthAlways, output, order);
        };

        Profiler.get().pop();
        poseStack.popPose();
        return order;
    }

    /**
     * renders the GUI/HUD buffer into the world
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     * @param poseStack   PoseStack to use for positioning
     * @param depthAlways if the depth test should be disabled
     * @param order       order to render at
     * @return order to render the next thing at
     */
    public static int renderGuiLayer(
        SubmitNodeCollector output, CameraRenderState cameraState, VRRenderState vrState, PoseStack poseStack,
        boolean depthAlways, int order)
    {
        if (!vrState.renderGui) return order;

        Profiler.get().push("GuiLayer");
        order = renderScreen(output, cameraState, vrState, vrState.guiState, GuiHandler.GUI_FRAMEBUFFER, depthAlways,
            vrState.noHudFog, poseStack, order);

        Profiler.get().pop();
        return order;
    }

    /**
     * if the face is inside a block, this renders a black square, and rerenders the gui and hands
     *
     * @param output      SubmitNodeCollector to output to
     * @param vrState     VR render state
     * @param cameraState camera render state for the position
     */
    public static void renderFaceOverlay(
        SubmitNodeCollector output, FeatureRenderDispatcher featureRenderDispatcher, CameraRenderState cameraState,
        VRRenderState vrState)
    {
        if (vrState.inBlock) {
            PoseStack poseStack = new PoseStack();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(VRShaders.UNDISTORTED_PROJ_BUFFER, ProjectionType.PERSPECTIVE);
            int order = renderFaceInBlock(output, poseStack, 0);

            // because this runs after the gameRenderer, the ModelViewStack is reset, so use the posestack instead
            RenderHelper.applyVRModelView(vrState.currentPass, poseStack.last().pose());

            order = renderGuiAndShadow(output, vrState, cameraState, poseStack, true, true, order);

            order = VRArmHelper.renderVRHands(output, vrState, cameraState, poseStack, true, true, true, true, order);
            featureRenderDispatcher.renderAllFeatures();
            MC.renderBuffers().bufferSource().endBatch();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    /**
     * renders a fullscreen black quad, to block the screen
     *
     * @param output    SubmitNodeCollector to output to
     * @param poseStack PoseStack to use for positioning
     * @param order     order to render at
     * @return order to render the next thing at
     */
    public static int renderFaceInBlock(SubmitNodeCollector output, PoseStack poseStack, int order) {
        RenderType renderType = VRRenderTypes.quads(true);
        RenderHelper.submitLateCustomGeometry(output.order(order++), poseStack, renderType,
            (pose, consumer) -> {
                // render a big quad 2 meters in front
                consumer.addVertex(-100.F, -100.F, -2.0F).setColor(0, 0, 0, 255);
                consumer.addVertex(100.F, -100.F, -2.0F).setColor(0, 0, 0, 255);
                consumer.addVertex(100.F, 100.F, -2.0F).setColor(0, 0, 0, 255);
                consumer.addVertex(-100.F, 100.F, -2.0F).setColor(0, 0, 0, 255);
            });
        return order;
    }

    /**
     * @return if the crosshair should be rendered
     */
    private static boolean shouldRenderCrosshair() {
        if (DATA_HOLDER.viewOnly) {
            return false;
        } else if (MC.level == null) {
            return false;
        } else if (MC.screen != null) {
            return false;
        } else if (DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.NEVER ||
            (DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.WITH_HUD &&
                MC.options.hideGui
            ))
        {
            return false;
        } else if (!RenderPass.isFirstPerson(DATA_HOLDER.currentPass)) {
            // it doesn't look very good
            return false;
        } else if (KeyboardHandler.SHOWING) {
            return false;
        } else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
            return false;
        } else if (GuiHandler.GUI_POS_ROOM != null) {
            // don't show it, when a screen is open, or a popup
            return false;
        } else if (DATA_HOLDER.bowTracker.isNotched()) {
            return false;
        } else if (
            DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).isEnabledRaw(ControllerType.RIGHT) ||
                VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.RIGHT))
        {
            return false;
        } else if (
            DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab).isEnabledRaw(ControllerType.RIGHT) ||
                VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT))
        {
            return false;
        } else if (DATA_HOLDER.teleportTracker.isAiming()) {
            return false;
        } else if (DATA_HOLDER.climbTracker.isGrabbingLadder(0)) {
            return false;
        } else {
            return !(DATA_HOLDER.vrPlayer.worldScale > 15.0F);
        }
    }

    public static void extractCrosshairState(CrosshairRenderState crosshairRenderState, @Nullable LocalPlayer player) {
        crosshairRenderState.shouldRender = player != null && shouldRenderCrosshair();
        if (!crosshairRenderState.shouldRender) return;

        Profiler.get().push("extract crosshair");

        Vec3 crosshairRenderPos = DATA_HOLDER.vrPlayer.crossVec;
        Vec3 crossDistance = crosshairRenderPos.subtract(
            DATA_HOLDER.vrPlayer.vrdata_world_render.getAim().getPosition());

        // scooch closer a bit for light calc.
        crosshairRenderState.pos = crosshairRenderPos.add(crossDistance.normalize().scale(-0.01D));

        Matrix4f rotation = crosshairRenderState.rotation.identity();

        if (MC.hitResult != null && MC.hitResult.getType() == HitResult.Type.BLOCK) {
            // if there is a block hit, make the crosshair parallel to the block
            BlockHitResult blockhitresult = (BlockHitResult) MC.hitResult;

            switch (blockhitresult.getDirection()) {
                case DOWN -> {
                    rotation.rotate(
                        Axis.YP.rotationDegrees(-DATA_HOLDER.vrPlayer.vrdata_world_render.getAim().getYaw()));
                    rotation.rotate(Axis.XP.rotationDegrees(-90.0F));
                }
                case UP -> {
                    rotation.rotate(
                        Axis.YP.rotationDegrees(-DATA_HOLDER.vrPlayer.vrdata_world_render.getAim().getYaw()));
                    rotation.rotate(Axis.XP.rotationDegrees(90.0F));
                }
                case WEST -> rotation.rotate(Axis.YP.rotationDegrees(90.0F));
                case EAST -> rotation.rotate(Axis.YP.rotationDegrees(-90.0F));
                case SOUTH -> rotation.rotate(Axis.YP.rotationDegrees(180.0F));
            }
        } else {
            // if there is no block hit, make it face the controller
            rotation.rotate(
                Axis.YP.rotationDegrees(-DATA_HOLDER.vrPlayer.vrdata_world_render.getAim().getYaw()));
            rotation.rotate(
                Axis.XP.rotationDegrees(-DATA_HOLDER.vrPlayer.vrdata_world_render.getAim().getPitch()));
        }

        float scale = (float) (0.125F * DATA_HOLDER.vrSettings.crosshairScale *
            Math.sqrt(DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale)
        );
        if (DATA_HOLDER.vrSettings.crosshairScalesWithDistance) {
            float depthScale = 0.3F + 0.2F * (float) crossDistance.length();
            scale *= depthScale;
        }
        crosshairRenderState.scale = scale;

        crosshairRenderState.light = LevelRenderer.getLightCoords(player.level(),
            BlockPos.containing(crosshairRenderState.pos));

        // white crosshair, with blending
        if (MC.hitResult == null || MC.hitResult.getType() == HitResult.Type.MISS) {
            crosshairRenderState.brightness = 0.5F;
        } else {
            crosshairRenderState.brightness = 1.0F;
        }

        Profiler.get().pop();
    }

    /**
     * renders the crosshair
     *
     * @param output         SubmitNodeCollector to output to
     * @param cameraState    camera render state for the position
     * @param poseStack      PoseStack to use for positioning
     * @param crosshairState crosshair renderstate to use for rendering
     * @param order          order to render at
     * @return order to render the next thing at
     */
    public static int renderCrosshairAtDepth(
        SubmitNodeCollector output, CrosshairRenderState crosshairState, CameraRenderState cameraState,
        PoseStack poseStack, int order)
    {
        if (!crosshairState.shouldRender) return order;

        Profiler.get().push("submit crosshair");

        poseStack.pushPose();
        poseStack.translate(crosshairState.pos.x - cameraState.pos.x,
            crosshairState.pos.y - cameraState.pos.y,
            crosshairState.pos.z - cameraState.pos.z);
        poseStack.mulPose(crosshairState.rotation);
        poseStack.scale(crosshairState.scale, crosshairState.scale, crosshairState.scale);

        TextureAtlasSprite crosshairSprite = MC.getAtlasManager().getAtlasOrThrow(AtlasIds.GUI)
            .getSprite(Gui.CROSSHAIR_SPRITE);

        float brightness = crosshairState.brightness;
        int light = crosshairState.light;
        // after regular geometry when unoccluded
        order += (crosshairState.occlude ? 0 : 1);

        // not late, this should render before translucents
        output.order(order).submitCustomGeometry(poseStack,
            VRRenderTypes.crosshairWorld(crosshairSprite.atlasLocation(), !crosshairState.occlude),
            (pose, consumer) -> {
                consumer.addVertex(pose, -1.0F, 1.0F, 0.0F)
                    .setColor(brightness, brightness, brightness, 1.0F)
                    .setUv(crosshairSprite.getU1(), crosshairSprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(0.0F, 0.0F, 1.0F);
                consumer.addVertex(pose, 1.0F, 1.0F, 0.0F)
                    .setColor(brightness, brightness, brightness, 1.0F)
                    .setUv(crosshairSprite.getU0(), crosshairSprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(0.0F, 0.0F, 1.0F);
                consumer.addVertex(pose, 1.0F, -1.0F, 0.0F)
                    .setColor(brightness, brightness, brightness, 1.0F)
                    .setUv(crosshairSprite.getU0(), crosshairSprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(0.0F, 0.0F, 1.0F);
                consumer.addVertex(pose, -1.0F, -1.0F, 0.0F)
                    .setColor(brightness, brightness, brightness, 1.0F)
                    .setUv(crosshairSprite.getU1(), crosshairSprite.getV1())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(0.0F, 0.0F, 1.0F);
            });

        poseStack.popPose();
        Profiler.get().pop();
        return order;
    }
}
