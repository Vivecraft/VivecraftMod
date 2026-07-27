package org.vivecraft.client_vr.render.helpers.graphics.vulkan;

import com.mojang.blaze3d.vulkan.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.ImageFormat;
import org.vivecraft.client_vr.render.helpers.graphics.RawTexture;

import javax.annotation.Nullable;
import java.nio.LongBuffer;

public class VulkanRawTexture implements RawTexture {

    protected int width;
    protected int height;
    protected ImageFormat format;

    protected final long vkImage;
    protected final int layer;
    @Nullable
    private final Long vmaAllocation;
    protected int currentLayout;

    private VulkanRawTexture(
        String name, int width, int height, ImageFormat format, long vkImage, int layer, @Nullable Long vmaAllocation,
        int vkImageLayout)
    {
        this.width = width;
        this.height = height;
        this.format = format;

        this.vkImage = vkImage;
        this.layer = layer;
        this.vmaAllocation = vmaAllocation;
        VulkanHelper.getVulkanDevice().instance().debug()
            .setObjectName(VulkanHelper.getVulkanDevice().vkDevice(), VK10.VK_OBJECT_TYPE_IMAGE, this.vkImage, name);
        this.currentLayout = vkImageLayout;
    }

    /**
     * creates a RawTexture that holds a reference to an externally created texture
     *
     * @param name     name of the texture
     * @param width    width of the external texture
     * @param height   height of the external texture
     * @param format   image format of the external texture
     * @param vkImage  handle to the external texture
     * @param vkLayout layout the external texture has currently
     * @return a wrapper to the external texture
     */
    public static VulkanRawTexture link(
        String name, int width, int height, ImageFormat format, long vkImage, int layer, int vkLayout)
    {
        return new VulkanRawTexture(name, width, height, format, vkImage, layer, null, vkLayout);
    }


    /**
     * creates a RawTexture of the give dimensions and format
     *
     * @param name   name of the texture
     * @param width  width of the texture to create
     * @param height height of the texture to create
     * @param format image format of the texture to create
     * @return the created texture
     */
    public static VulkanRawTexture create(String name, int width, int height, ImageFormat format) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc(stack).sType$Default();
            imageCreateInfo.imageType(VK10.VK_IMAGE_TYPE_2D);
            imageCreateInfo.extent().set(width, height, 1);
            imageCreateInfo.mipLevels(1);
            imageCreateInfo.arrayLayers(1);
            imageCreateInfo.format(GraphicsHelper.INSTANCE.formatToAPI(format));
            imageCreateInfo.tiling(VK10.VK_IMAGE_TILING_OPTIMAL);
            imageCreateInfo.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
            imageCreateInfo.usage(VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT |
                VK10.VK_IMAGE_USAGE_SAMPLED_BIT);
            imageCreateInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
            imageCreateInfo.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
            imageCreateInfo.flags(0);
            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack);
            allocationCreateInfo.usage(Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE);
            LongBuffer imageHandlePtr = stack.callocLong(1);
            PointerBuffer allocationHandlePtr = stack.callocPointer(1);
            VulkanUtils.crashIfFailure(
                VulkanHelper.getVulkanDevice(),
                Vma.vmaCreateImage(VulkanHelper.getVulkanDevice().vma(), imageCreateInfo, allocationCreateInfo,
                    imageHandlePtr,
                    allocationHandlePtr, null),
                "Failed to create image"
            );

            VulkanRawTexture texture = new VulkanRawTexture(name, width, height, format, imageHandlePtr.get(0), 0,
                allocationHandlePtr.get(0), VK10.VK_IMAGE_LAYOUT_UNDEFINED);

            texture.transitionLayoutTo(VulkanHelper.getVulkanDevice().createCommandEncoder().textureInitCommandBuffer(),
                VK10.VK_IMAGE_LAYOUT_GENERAL, 0, VK10.VK_ACCESS_TRANSFER_READ_BIT | VK10.VK_ACCESS_TRANSFER_WRITE_BIT);
            return texture;
        }
    }

    protected void transitionLayoutTo(
        VkCommandBuffer commandBuffer, int newLayout, int srcAccessMask, int dstAccessMask)
    {
        ((VulkanHelper) GraphicsHelper.INSTANCE).transitionImageLayoutTo(commandBuffer,
            this.vkImage, 0, 1,
            this.currentLayout, newLayout,
            srcAccessMask, dstAccessMask,
            VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        this.currentLayout = newLayout;
    }

    @Override
    public long getHandle() {
        return this.vkImage;
    }

    @Override
    public void destroy() {
        if (this.vmaAllocation != null) {
            Vma.vmaDestroyImage(VulkanHelper.getVulkanDevice().vma(), this.vkImage, this.vmaAllocation);
        }
    }
}
