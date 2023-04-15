package org.vivecraft.client_xr.render_pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.client_vr.VRTextureTarget;

import java.io.IOException;

public class WorldRenderPass implements AutoCloseable {

    private static final Minecraft mc = Minecraft.getInstance();

    public static WorldRenderPass stereoXR;
    public static WorldRenderPass center;
    public static WorldRenderPass mixedReality;
    public static WorldRenderPass leftTelescope;
    public static WorldRenderPass rightTelescope;
    public static WorldRenderPass camera;


    public final VRTextureTarget target;
    public final PostChain transparencyChain;
    public final PostChain outlineChain;

    public WorldRenderPass(VRTextureTarget target) throws IOException {
        this.target = target;
        if (Minecraft.useShaderTransparency()) {
            this.transparencyChain = createPostChain(new ResourceLocation("shaders/post/vrtransparency.json"), this.target);
        } else {
            this.transparencyChain = null;
        }
        this.outlineChain = createPostChain(new ResourceLocation("shaders/post/entity_outline.json"), this.target);
    }

    private static PostChain createPostChain(ResourceLocation resourceLocation, RenderTarget target) throws IOException {
        PostChain postchain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), target, resourceLocation);
        postchain.resize(target.viewWidth, target.viewHeight);
        return postchain;
    }

    @Override
    public void close() {
        this.target.destroyBuffers();
        if (this.transparencyChain != null) {
            this.transparencyChain.close();
        }
        this.outlineChain.close();
    }
}
