package org.vivecraft.client_xr.render_pass;

import com.mojang.blaze3d.pipeline.MainTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mixin.accessor.client.MinecraftAccessor;
import org.vivecraft.mixin.accessor.client.renderer.GameRendererAccessor;

public class RenderPassManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final MinecraftAccessor mcac = (MinecraftAccessor) mc;

    public static RenderPassManager INSTANCE;

    public final MainTarget vanillaRenderTarget;
    public PostChain vanillaOutlineChain;
    public PostChain vanillaPostEffect;
    public PostChain vanillaTransparencyChain;
    public static RenderPassType renderPassType = RenderPassType.VANILLA;
    public static WorldRenderPass wrp;

    public RenderPassManager(MainTarget vanillaRenderTarget) {
        this.vanillaRenderTarget = vanillaRenderTarget;
    }

    public static void setWorldRenderPass(WorldRenderPass wrp) {
        RenderPassManager.wrp = wrp;
        renderPassType = RenderPassType.WORLD_ONLY;
        mcac.setMainRenderTarget(wrp.target);
        if (mc.gameRenderer != null) {
            ((GameRendererAccessor) mc.gameRenderer).setPostEffect(wrp.postEffect);
        }
    }

    public static void setGUIRenderPass() {
        ClientDataHolderVR.getInstance().currentPass = RenderPass.GUI;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.GUI_ONLY;
        mcac.setMainRenderTarget(GuiHandler.guiFramebuffer);
    }

    public static void setVanillaRenderPass() {
        ClientDataHolderVR.getInstance().currentPass = null;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.VANILLA;
        mcac.setMainRenderTarget(INSTANCE.vanillaRenderTarget);
        if (mc.gameRenderer != null) {
            ((GameRendererAccessor) mc.gameRenderer).setPostEffect(INSTANCE.vanillaPostEffect);
        }
    }
}
