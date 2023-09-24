package org.vivecraft.client_xr.render_pass;

import org.vivecraft.client_vr.VRTextureTarget;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

import static org.vivecraft.client_vr.VRState.mc;

import static net.minecraft.client.Minecraft.ON_OSX;
import static net.minecraft.client.Minecraft.useShaderTransparency;

public class WorldRenderPass implements AutoCloseable {

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
        this.transparencyChain = (useShaderTransparency() ?
            createPostChain(new ResourceLocation("shaders/post/vrtransparency.json"), this.target) :
            null
        );
        this.outlineChain = createPostChain(new ResourceLocation("shaders/post/entity_outline.json"), this.target);
    }

    private static PostChain createPostChain(ResourceLocation resourceLocation, RenderTarget target) throws IOException {
        PostChain postchain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), target, resourceLocation);
        postchain.resize(target.viewWidth, target.viewHeight);
        return postchain;
    }

    public void resize(int width, int height) {
        target.resize(width, height, ON_OSX);
        outlineChain.resize(width, height);
        if (transparencyChain != null) {
            transparencyChain.resize(width, height);
        }
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
