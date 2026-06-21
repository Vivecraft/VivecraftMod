package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class VulkanHelper implements GraphicsHelper {

    protected abstract VkCommandBuffer allocateAndBeginCommandBuffer();

    protected abstract void endCommandBuffer(VkCommandBuffer commandBuffer);

    @Override
    public abstract long getTextureHandle(GpuTexture texture);

    @Override
    public void genMipmaps(GpuTexture texture) {
        long vkImage = getTextureHandle(texture);

        VkCommandBuffer blitCommandBuffer = this.allocateAndBeginCommandBuffer();

        // transfer base level to src optimal
        transitionImageLayoutTo(blitCommandBuffer, vkImage,
            0, 1,
            VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
            VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

        for (int i = 1; i < texture.getMipLevels(); i++) {
            // transition the target layer to dst optimal
            transitionImageLayoutTo(blitCommandBuffer, vkImage,
                i, 1,
                VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                0, VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                0, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            // blit
            blitTexture(blitCommandBuffer,
                vkImage, i - 1, 0, 0, texture.getWidth(i - 1), texture.getHeight(i - 1),
                vkImage, i, 0, 0, texture.getWidth(i), texture.getHeight(i));

            // transition the source layer to src optimal for next layer
            transitionImageLayoutTo(blitCommandBuffer, vkImage,
                i, 1,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        }

        // every mip is now in src optimal, transfer all mips at once back into the genreal layout
        transitionImageLayoutTo(blitCommandBuffer, vkImage,
            0, texture.getMipLevels(),
            VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_GENERAL,
            VK10.VK_ACCESS_TRANSFER_READ_BIT, VK10.VK_ACCESS_SHADER_READ_BIT,
            VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

        this.endCommandBuffer(blitCommandBuffer);
    }

    /**
     * blits the source image/mip rectangle to the target image/mip rectangle, with linear interpolation
     *
     * @param commandBuffer commandbuffer to submit the calls to
     * @param sourceImage   source image handle
     * @param sourceMip     mip level of the source image to copy frome
     * @param sourceX       source X position to copy from
     * @param sourceY       source Y position to copy from
     * @param sourceWidth   width of the source rectangle to copy from
     * @param sourceHeight  height of the source rectangle to copy from
     * @param targetImage   target image handle
     * @param targetMip     mip level of the target image to copy to
     * @param targetX       target X position to copy to
     * @param targetY       target Y position to copy to
     * @param targetWidth   width of the target rectangle to copy to
     * @param targetHeight  height of the target rectangle to copy to
     */
    private void blitTexture(
        VkCommandBuffer commandBuffer,
        long sourceImage, int sourceMip, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
        long targetImage, int targetMip, int targetX, int targetY, int targetWidth, int targetHeight)
    {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkOffset3D.Buffer srcOffsets = VkOffset3D.calloc(2, stack);
            srcOffsets.x(sourceX)
                .y(sourceY)
                .z(0);
            srcOffsets.position(1);
            srcOffsets.x(sourceX + sourceWidth)
                .y(sourceY + sourceHeight)
                .z(1);
            srcOffsets.position(0);

            VkOffset3D.Buffer dstOffsets = VkOffset3D.calloc(2, stack);
            dstOffsets.x(targetX)
                .y(targetY)
                .z(0);
            dstOffsets.position(1);
            dstOffsets.x(targetX + targetWidth)
                .y(targetY + targetHeight)
                .z(1);
            dstOffsets.position(0);

            VkImageSubresourceLayers srcSubresource = VkImageSubresourceLayers.calloc(stack);
            srcSubresource.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            srcSubresource.mipLevel(sourceMip);
            srcSubresource.baseArrayLayer(0);
            srcSubresource.layerCount(1);

            VkImageSubresourceLayers dstSubresource = VkImageSubresourceLayers.calloc(stack);
            dstSubresource.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            dstSubresource.mipLevel(targetMip);
            dstSubresource.baseArrayLayer(0);
            dstSubresource.layerCount(1);

            VkImageBlit.Buffer blitRegion = VkImageBlit.calloc(1, stack);
            blitRegion.srcSubresource(srcSubresource);
            blitRegion.srcOffsets(srcOffsets);
            blitRegion.dstSubresource(dstSubresource);
            blitRegion.dstOffsets(dstOffsets);

            VK12.vkCmdBlitImage(commandBuffer,
                sourceImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                targetImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                blitRegion, VK10.VK_FILTER_LINEAR);
        }
    }

    /**
     * transitions the layout of the given image to the specified one
     *
     * @param commandBuffer command buffer to add the layout barrier to
     * @param vkImage       image to changethe layout of
     * @param baseMip       mip level to transition
     * @param numberOfMips  count of mips that should be transitioned (including the base mip)
     * @param oldLayout     cuurrent layout of the image
     * @param newLayout     new layout of the image
     * @param srcAccessMask access bits of what has been done with the image so far
     * @param dstAccessMask access bits of what the intent of the image is now
     * @param srcStageMask  stage bits of what has been done with the image so far
     * @param dstStageMask  stage bits of what the intent of the image is now
     */
    protected void transitionImageLayoutTo(
        VkCommandBuffer commandBuffer, long vkImage, int baseMip, int numberOfMips, int oldLayout, int newLayout,
        int srcAccessMask, int dstAccessMask, int srcStageMask, int dstStageMask)
    {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default();
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcAccessMask(srcAccessMask);
            barrier.dstAccessMask(dstAccessMask);
            barrier.srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED);
            barrier.image(vkImage);
            VkImageSubresourceRange subresourceRange = barrier.subresourceRange();
            subresourceRange.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            subresourceRange.baseMipLevel(baseMip);
            subresourceRange.levelCount(numberOfMips);
            subresourceRange.baseArrayLayer(0);
            subresourceRange.layerCount(1);

            VK12.vkCmdPipelineBarrier(commandBuffer, srcStageMask, dstStageMask, 0, null, null, barrier);
        }
    }

    @Override
    public String checkError(String errorSection) {
        // can't check errors like that on vulkan
        return "";
    }

    @Override
    public boolean isStencil() {
        return false;
    }

    @Override
    public void setStencil(boolean state) {}

    @Override
    public void flush() {}

    @Override
    public boolean flipEyeVertically() {
        return true;
    }

    /**
     * checks that the given extensions are supported. throws a RenderConfigException if the are not supported, or if they are not enabled
     *
     * @param instanceExtensions instance extension that are needed
     * @param deviceExtensions   device extension that are needed
     * @throws RenderConfigException thrown if something is missing
     */
    public void checkExtensionSupport(
        List<String> instanceExtensions, List<String> deviceExtensions) throws RenderConfigException
    {
        // get all supported extensions
        Set<String> availableDeviceExtensions = this.getAvailableDeviceExtensions();
        this.getAvailableInstanceExtensions();

        Set<String> availableInstanceExtensions = this.getAvailableInstanceExtensions();

        Set<String> missingExtensions = new HashSet<>();
        for (String extension : instanceExtensions) {
            if (!availableInstanceExtensions.contains(extension)) {
                missingExtensions.add("Instance extension: " + extension);
            }
        }

        for (String extension : deviceExtensions) {
            if (!availableDeviceExtensions.contains(extension)) {
                missingExtensions.add("Device extension: " + extension);
            }
        }

        if (!missingExtensions.isEmpty()) {
            //  unsupported extensions, abort with unsupported
            MutableComponent error = Component.translatable("vivecraft.messages.vulkanunsupported");

            for (String ext : missingExtensions) {
                error.append(Component.literal("\n" + ext));
            }
            throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblegpu"), error);
        }
        // all extensions supported, check if they are already enabled
        Set<String> enabledExtensions = this.getEnabledExtensions();
        for (String extension : instanceExtensions) {
            if (!enabledExtensions.contains(extension + " (I)")) {
                missingExtensions.add("Instance extension: " + extension);
            }
        }

        for (String extension : deviceExtensions) {
            if (!enabledExtensions.contains(extension + " (D)")) {
                missingExtensions.add("Device extension: " + extension);
            }
        }
        if (!missingExtensions.isEmpty()) {
            VRSettings.LOGGER.info(
                "Vivecraft: Not all needed Vulkan Extensions are enabled, game restart required. Not enabled Extensions:\n{}",
                String.join("\n", missingExtensions));
            throw new RenderConfigException(Component.translatable("vivecraft.messages.vulkanrestarttitle"),
                Component.translatable("vivecraft.messages.vulkanrestart"));
        }
    }

    protected abstract Set<String> getAvailableInstanceExtensions();

    protected abstract Set<String> getAvailableDeviceExtensions();

    protected abstract Set<String> getEnabledExtensions();

    public abstract long getDevicePointer();

    public abstract long getPhysicalDevicePointer();

    public abstract long getQueuePointer();

    public abstract int getQueueFamilyIndex();

    public abstract long getInstancePointer();
}
