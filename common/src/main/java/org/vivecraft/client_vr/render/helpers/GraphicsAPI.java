package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;
import org.vivecraft.client_vr.render.helpers.vulkan.VulkanHelper;

public abstract class GraphicsAPI {

    protected static GraphicsAPI INSTANCE = determinePlatform();

    private static GraphicsAPI determinePlatform() {
        int api = GLFW.glfwGetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CLIENT_API);
        if (api == GLFW.GLFW_OPENGL_API) {
            return new OpenGLHelper();
        } else if (api == GLFW.GLFW_NO_API) {
            return new VulkanHelper();
        } else {
            throw new RuntimeException("Vivecraft: unsupported graphics API: " + api);
        }
    }

    public static GraphicsAPI getInstance() {
        return INSTANCE;
    }

    /**
     * checks if there were any graphics api errors since this was last called
     *
     * @param context name of the section that is checked, this gets logged if there are any errors
     * @return error string if there was one
     */
    public abstract String checkError(String context);

    public abstract Type type();

    public abstract boolean isStencilEnabled();

    public abstract void enableStencil();

    public abstract void disableStencil();

    public abstract void flushPreSubmit();

    public abstract void flushPostSubmit();

    public abstract void changeTexturePurpose(GpuTexture texture, TexturePurpose purpose);

    public abstract long getImageHandle(GpuTexture texture);

    public abstract int getFormat(TextureFormat format);

    public enum TexturePurpose {
        RENDER,
        TRANSFER
    }

    public enum Type {
        OPENGL,
        VULKAN
    }
}
