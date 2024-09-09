package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

public class VRTextureTarget extends RenderTarget {

    private final String name;
    public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter, boolean useStencil) {
        super(usedepth);
        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTextid(texid);
        ((RenderTargetExtension) this).vivecraft$isLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setUseStencil(useStencil);
        this.resize(width, height, onMac);
        if (useStencil) {
            Xplat.enableRenderTargetStencil(this);
        }
        this.setClearColor(0, 0, 0, 0);
    }

    public VRTextureTarget(String name, int width, int height, int colorid, int index) {
        super(true);
        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        this.resize(width, height, Minecraft.ON_OSX);
        ((RenderTargetExtension) this).vivecraft$setColorid(colorid);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferId);
        GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorid, 0, index);
        this.setClearColor(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("\n");
        if (this.name != null) {
            stringbuilder.append("Name:   " + this.name).append("\n");
        }
        stringbuilder.append("Size:   " + this.viewWidth + " x " + this.viewHeight).append("\n");
        stringbuilder.append("FB ID:  " + this.frameBufferId).append("\n");
        stringbuilder.append("Tex ID: " + this.colorTextureId).append("\n");
        return stringbuilder.toString();
    }
}
