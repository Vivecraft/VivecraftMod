package org.vivecraft.client_xr.render_pass;

import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.RenderPass;

import com.mojang.blaze3d.pipeline.MainTarget;

import net.minecraft.client.renderer.PostChain;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class RenderPassManager {

    public static RenderPassManager INSTANCE;

    public final MainTarget vanillaRenderTarget;
    public PostChain vanillaOutlineChain;
    public PostChain vanillaTransparencyChain;
    public static RenderPassType renderPassType = RenderPassType.VANILLA;
    public static WorldRenderPass wrp;

    public RenderPassManager(MainTarget vanillaRenderTarget) {
        this.vanillaRenderTarget = vanillaRenderTarget;
    }

    public static void setWorldRenderPass(WorldRenderPass wrp) {
        RenderPassManager.wrp = wrp;
        renderPassType = RenderPassType.WORLD_ONLY;
        mc.mainRenderTarget = wrp.target;
    }

    public static void setGUIRenderPass() {
        dh.currentPass = RenderPass.GUI;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.GUI_ONLY;
        mc.mainRenderTarget = GuiHandler.guiFramebuffer;
    }

    public static void setVanillaRenderPass() {
        dh.currentPass = RenderPass.VANILLA;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.VANILLA;
        mc.mainRenderTarget = INSTANCE.vanillaRenderTarget;
    }
}
