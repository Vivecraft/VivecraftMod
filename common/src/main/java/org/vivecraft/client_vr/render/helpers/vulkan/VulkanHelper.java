package org.vivecraft.client_vr.render.helpers.vulkan;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.vivecraft.client_vr.render.helpers.GraphicsAPI;
import org.vivecraft.mod_compat_vr.cinnabar.CinnabarHelper;

public class VulkanHelper extends GraphicsAPI {

    public VulkanHelper() {
        GraphicsAPI.INSTANCE = this;
    }

    @Override
    public String checkError(String context) {
        return "";
    }

    @Override
    public Type type() {
        return Type.VULKAN;
    }

    @Override
    public boolean isStencilEnabled() {
        return false;
    }

    @Override
    public void enableStencil() {

    }

    @Override
    public void disableStencil() {

    }

    @Override
    public void flushPreSubmit() {
        if (CinnabarHelper.isLoaded()) {
            CinnabarHelper.flushCommandBuffers();
        } else {
            throw new RuntimeException("Game is running on Vulkan, but unsupported vulkan mod.");
        }
    }

    @Override
    public void flushPostSubmit() {}

    @Override
    public void changeTexturePurpose(GpuTexture texture, TexturePurpose purpose) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default();
            if (purpose == TexturePurpose.RENDER) {
                barrier.oldLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
            } else if (purpose == TexturePurpose.TRANSFER) {
                barrier.oldLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
                barrier.newLayout(VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
            }
            barrier.srcAccessMask(VK10.VK_ACCESS_MEMORY_READ_BIT | VK10.VK_ACCESS_MEMORY_WRITE_BIT);
            barrier.dstAccessMask(VK10.VK_ACCESS_MEMORY_READ_BIT | VK10.VK_ACCESS_MEMORY_WRITE_BIT);
            barrier.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.image(getImageHandle(texture));
            VkImageSubresourceRange subresourceRange = barrier.subresourceRange();
            subresourceRange.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            subresourceRange.baseMipLevel(0);
            subresourceRange.levelCount(1);
            subresourceRange.baseArrayLayer(0);
            subresourceRange.layerCount(1);

            VK10.vkCmdPipelineBarrier(getCommandBuffer(), VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, null, null, barrier);
        }
    }

    private VkCommandBuffer getCommandBuffer() {
        if (CinnabarHelper.isLoaded()) {
            return CinnabarHelper.getCommandBuffer();
        } else {
            throw new RuntimeException("Game is running on Vulkan, but unsupported vulkan mod.");
        }
    }

    public long getImageHandle(GpuTexture texture) {
        if (CinnabarHelper.isLoaded()) {
            return CinnabarHelper.getImageHandle(texture);
        } else {
            throw new RuntimeException("Game is running on Vulkan, but unsupported vulkan mod.");
        }
    }

    @Override
    public int getFormat(TextureFormat format) {
        return switch (format) {
            case RGBA8 -> VK10.VK_FORMAT_R8G8B8A8_UNORM;
            case RED8 -> VK10.VK_FORMAT_R8_UNORM;
            case RED8I -> VK10.VK_FORMAT_R8_SINT;
            case DEPTH32 -> VK10.VK_FORMAT_D32_SFLOAT;
            default -> throw new IllegalArgumentException("Invalid TextureFormat: " + format);
        };
    }

    public VulkanDeviceData getDeviceData() {
        if (CinnabarHelper.isLoaded()) {
            return CinnabarHelper.getDeviceData();
        } else {
            throw new RuntimeException("Game is running on Vulkan, but unsupported vulkan mod.");
        }
    }

    public record VulkanDeviceData(long device, long physicalDevice, long instance, long queue, int queueFamilyIndex) {}
}
