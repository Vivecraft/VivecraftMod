package org.vivecraft.client_xr.render_pass;

import com.mojang.blaze3d.pipeline.MainTarget;
import net.minecraft.client.Minecraft;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.RenderPass;

public class RenderPassManager {
    private static final Minecraft mc = Minecraft.getInstance();

    public static RenderPassManager INSTANCE;

    public final MainTarget vanillaRenderTarget;
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
        ClientDataHolderVR.getInstance().currentPass = RenderPass.GUI;
        renderPassType = RenderPassType.GUI_ONLY;
        mc.mainRenderTarget = GuiHandler.guiFramebuffer;
    }

    public static void setVanillaRenderPass() {
        ClientDataHolderVR.getInstance().currentPass = null;
        renderPassType = RenderPassType.VANILLA;
        mc.mainRenderTarget = INSTANCE.vanillaRenderTarget;
    }
}
