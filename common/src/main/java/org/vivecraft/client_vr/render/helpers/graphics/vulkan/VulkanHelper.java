package org.vivecraft.client_vr.render.helpers.graphics.vulkan;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import com.mojang.blaze3d.vulkan.VulkanUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Vector2ic;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.vulkan.VulkanDeviceExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanInstanceExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanQueueExtension;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.ImageFormat;
import org.vivecraft.client_vr.render.helpers.graphics.RawTexture;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VulkanHelper implements GraphicsHelper {

    private Set<ImageFormat> supportedFormats = null;

    protected static VulkanDevice getVulkanDevice() {
        if (RenderSystem.getDevice().backend instanceof VulkanDevice vulkanDevice) {
            return vulkanDevice;
        } else {
            throw new IllegalArgumentException("Vivecraft: not a vulkan device in vulkan context");
        }
    }

    protected static VulkanGpuTexture getVulkanTexture(GpuTexture texture) {
        if (texture instanceof VulkanGpuTexture vulkanTexture) {
            return vulkanTexture;
        }
        throw new IllegalArgumentException("Vivecraft: not a vulkan texture in vulkan context");
    }

    @Override
    public Type getType() {
        return Type.VULKAN;
    }

    @Override
    public long getTextureHandle(GpuTexture texture) {
        return getVulkanTexture(texture).vkImage();
    }

    @Override
    public void genMipmaps(GpuTexture texture) {
        VulkanGpuTexture vulkanTexture = getVulkanTexture(texture);

        VkCommandBuffer blitCommandBuffer = getVulkanDevice().createCommandEncoder()
            .allocateAndBeginTransientCommandBuffer();

        // transfer base level to src optimal
        transitionImageLayoutTo(blitCommandBuffer, vulkanTexture.vkImage(),
            0, 1,
            VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
            VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

        for (int i = 1; i < texture.getMipLevels(); i++) {
            // transition the target layer to dst optimal
            transitionImageLayoutTo(blitCommandBuffer, vulkanTexture.vkImage(),
                i, 1,
                VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                0, VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                0, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            // blit
            blitTexture(blitCommandBuffer, VK10.VK_FILTER_LINEAR,
                vulkanTexture.vkImage(), i - 1, 0, 0, 0, vulkanTexture.getWidth(i - 1), vulkanTexture.getHeight(i - 1),
                vulkanTexture.vkImage(), i, 0, 0, 0, vulkanTexture.getWidth(i), vulkanTexture.getHeight(i));

            // transition the source layer to src optimal for next layer
            transitionImageLayoutTo(blitCommandBuffer, vulkanTexture.vkImage(),
                i, 1,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
        }

        // every mip is now in src optimal, transfer all mips at once back into the general layout
        transitionImageLayoutTo(blitCommandBuffer, vulkanTexture.vkImage(),
            0, vulkanTexture.getMipLevels(),
            VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_GENERAL,
            VK10.VK_ACCESS_TRANSFER_READ_BIT, VK10.VK_ACCESS_SHADER_READ_BIT,
            VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

        VulkanUtils.crashIfFailure(getVulkanDevice(), VK12.vkEndCommandBuffer(blitCommandBuffer),
            "Failed to end VkCommandBuffer");
        getVulkanDevice().createCommandEncoder().execute(blitCommandBuffer);
    }

    /**
     * blits the source image/mip rectangle to the target image/mip rectangle, with linear interpolation
     *
     * @param commandBuffer commandbuffer to submit the calls to
     * @param filter        filter mode to use for the blit
     * @param sourceImage   source image handle
     * @param sourceMip     mip level of the source image to copy from
     * @param sourceLayer   layer of a multi layer image to copy from
     * @param sourceX       source X position to copy from
     * @param sourceY       source Y position to copy from
     * @param sourceWidth   width of the source rectangle to copy from
     * @param sourceHeight  height of the source rectangle to copy from
     * @param targetImage   target image handle
     * @param targetMip     mip level of the target image to copy to
     * @param targetLayer   layer of a multi layer image to copy to
     * @param targetX       target X position to copy to
     * @param targetY       target Y position to copy to
     * @param targetWidth   width of the target rectangle to copy to
     * @param targetHeight  height of the target rectangle to copy to
     */
    private void blitTexture(
        VkCommandBuffer commandBuffer, int filter,
        long sourceImage, int sourceMip, int sourceLayer, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
        long targetImage, int targetMip, int targetLayer, int targetX, int targetY, int targetWidth, int targetHeight)
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
            srcSubresource.baseArrayLayer(sourceLayer);
            srcSubresource.layerCount(1);

            VkImageSubresourceLayers dstSubresource = VkImageSubresourceLayers.calloc(stack);
            dstSubresource.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            dstSubresource.mipLevel(targetMip);
            dstSubresource.baseArrayLayer(targetLayer);
            dstSubresource.layerCount(1);

            VkImageBlit.Buffer blitRegion = VkImageBlit.calloc(1, stack);
            blitRegion.srcSubresource(srcSubresource);
            blitRegion.srcOffsets(srcOffsets);
            blitRegion.dstSubresource(dstSubresource);
            blitRegion.dstOffsets(dstOffsets);

            VK12.vkCmdBlitImage(commandBuffer,
                sourceImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                targetImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                blitRegion, filter);
        }
    }

    /**
     * does a raw memory copy of the source image/mip rectangle to the target image/mip rectangle,
     *
     * @param commandBuffer commandbuffer to submit the calls to
     * @param sourceImage   source image handle
     * @param sourceMip     mip level of the source image to copy from
     * @param sourceLayer   layer of a multi layer image to copy from
     * @param sourceX       source X position to copy from
     * @param sourceY       source Y position to copy from
     * @param targetImage   target image handle
     * @param targetMip     mip level of the target image to copy to
     * @param targetLayer   layer of a multi layer image to copy to
     * @param targetX       target X position to copy to
     * @param targetY       target Y position to copy to
     * @param width         width of the rectangle to copy to
     * @param height        height of the rectangle to copy to
     */
    private void copyTexture(
        VkCommandBuffer commandBuffer,
        long sourceImage, int sourceMip, int sourceLayer, int sourceX, int sourceY,
        long targetImage, int targetMip, int targetLayer, int targetX, int targetY,
        int width, int height)
    {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkOffset3D.Buffer src = VkOffset3D.calloc(1, stack);
            src.x(sourceX)
                .y(sourceY)
                .z(0);

            VkOffset3D.Buffer dst = VkOffset3D.calloc(1, stack);
            dst.x(targetX)
                .y(targetY)
                .z(0);

            VkExtent3D.Buffer size = VkExtent3D.calloc(1, stack);
            size.width(width)
                .height(height)
                .depth(1);

            VkImageSubresourceLayers srcSubresource = VkImageSubresourceLayers.calloc(stack);
            srcSubresource.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            srcSubresource.mipLevel(sourceMip);
            srcSubresource.baseArrayLayer(sourceLayer);
            srcSubresource.layerCount(1);

            VkImageSubresourceLayers dstSubresource = VkImageSubresourceLayers.calloc(stack);
            dstSubresource.aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            dstSubresource.mipLevel(targetMip);
            dstSubresource.baseArrayLayer(targetLayer);
            dstSubresource.layerCount(1);

            VkImageCopy.Buffer copyRegion = VkImageCopy.calloc(1, stack);
            copyRegion.srcSubresource(srcSubresource);
            copyRegion.srcOffset(src.get(0));
            copyRegion.dstSubresource(dstSubresource);
            copyRegion.dstOffset(dst.get());
            copyRegion.extent(size.get(0));

            VK12.vkCmdCopyImage(commandBuffer,
                sourceImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                targetImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                copyRegion);
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

    @Override
    public void blitTextures(RenderTarget[] sources, RawTexture... targets) {
        if (targets == null || sources.length != targets.length) {
            throw new IllegalArgumentException("Vivecraft: sources.length != targets.length");
        }

        VkCommandBuffer blitCommandBuffer = getVulkanDevice().createCommandEncoder()
            .allocateAndBeginTransientCommandBuffer();
        for (int i = 0; i < sources.length; ++i) {
            VulkanGpuTexture source = getVulkanTexture(sources[i].getColorTexture());
            VulkanRawTexture target = ((VulkanRawTexture) targets[i]);

            // transition image layouts
            // transfer source to src optimal
            transitionImageLayoutTo(blitCommandBuffer, source.vkImage(),
                0, 1,
                VK10.VK_IMAGE_LAYOUT_GENERAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            // transfer target layout
            int oldTargetLayout = target.currentLayout;
            target.transitionLayoutTo(blitCommandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK10.VK_ACCESS_TRANSFER_READ_BIT, VK10.VK_ACCESS_TRANSFER_WRITE_BIT);

            if (source.getFormat() == GpuFormat.RGBA8_UNORM && target.format == ImageFormat.R8G8B8A8_SRGB) {
                // use a memory copy to skip srgb conversion
                copyTexture(blitCommandBuffer,
                    source.vkImage(), 0, 0, 0, 0,
                    target.getHandle(), 0, target.layer, 0, 0,
                    target.width, target.height);
            } else {
                blitTexture(blitCommandBuffer, VK10.VK_FILTER_NEAREST,
                    source.vkImage(), 0, 0, 0, 0, source.getWidth(0), source.getHeight(0),
                    target.getHandle(), 0, target.layer, 0, 0, target.width, target.height);
            }

            // transition image layouts back
            transitionImageLayoutTo(blitCommandBuffer, source.vkImage(),
                0, 1,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK10.VK_IMAGE_LAYOUT_GENERAL,
                VK10.VK_ACCESS_TRANSFER_READ_BIT, VK10.VK_ACCESS_SHADER_READ_BIT,
                VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
            target.transitionLayoutTo(blitCommandBuffer, oldTargetLayout,
                VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT);
        }
        VulkanUtils.crashIfFailure(getVulkanDevice(), VK12.vkEndCommandBuffer(blitCommandBuffer),
            "Failed to end VkCommandBuffer");
        getVulkanDevice().createCommandEncoder().execute(blitCommandBuffer);
    }

    @Override
    public Set<ImageFormat> supportedImageFormats() {
        if (this.supportedFormats == null) {
            Set<ImageFormat> supported = new HashSet<>();
            for (ImageFormat format : ImageFormat.values()) {
                if (formatToAPI(format) == -1) continue;
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    VkPhysicalDeviceImageFormatInfo2 formatInfo = VkPhysicalDeviceImageFormatInfo2.calloc(stack)
                        .sType$Default();
                    VkImageFormatProperties2 supportedProperties = VkImageFormatProperties2.calloc(stack)
                        .sType$Default();

                    // set needed properties
                    formatInfo.format(formatToAPI(format));
                    formatInfo.type(VK10.VK_IMAGE_TYPE_2D);
                    formatInfo.tiling(VK10.VK_IMAGE_TILING_OPTIMAL);
                    formatInfo.usage(VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT |
                        VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT |
                        VK10.VK_IMAGE_USAGE_SAMPLED_BIT);
                    formatInfo.flags(0);

                    int res = VK11.vkGetPhysicalDeviceImageFormatProperties2(getVulkanDevice().vkDevice()
                        .getPhysicalDevice(), formatInfo, supportedProperties);
                    if (res == VK10.VK_SUCCESS) {
                        supported.add(format);
                    }
                }
            }
            // make unmodifiable
            this.supportedFormats = Collections.unmodifiableSet(supported);
        }

        return this.supportedFormats;
    }

    /**
     * checks that the given extensions are supported. throws a RenderConfigException if they are not supported, or if they are not enabled
     *
     * @param instanceExtensions     instance extension that are needed
     * @param deviceExtensions       device extension that are needed
     * @param requiredApiVersion     minimum required Vulkan Api version
     * @param vkPhysicalDeviceHandle physical device the game should run on, ignored if it is {@code 0}
     * @throws RenderConfigException thrown if something is missing
     */
    public void checkCompatibility(
        List<String> instanceExtensions, List<String> deviceExtensions, @Nullable Vector2ic requiredApiVersion,
        long vkPhysicalDeviceHandle) throws RenderConfigException
    {
        VulkanDevice vulkanDevice = getVulkanDevice();

        boolean wrongDevice = false;
        VkPhysicalDevice requiredDevice = null;
        String requiredDeviceUUID = "";

        if (vkPhysicalDeviceHandle != 0) {
            // first check if the device is the correct one
            requiredDevice = new VkPhysicalDevice(vkPhysicalDeviceHandle, getInstance());
            String currentDeviceUUID = VulkanStaticHelper.getDeviceUUID(getPhysicalDevice());
            requiredDeviceUUID = VulkanStaticHelper.getDeviceUUID(requiredDevice);

            wrongDevice = !currentDeviceUUID.equals(requiredDeviceUUID);
        }

        // check api version
        boolean wrongApiVersion = false;
        boolean unsupportedApiVersion = false;

        if (requiredApiVersion != null) {
            // check if the minimum version is supported by the gpu
            Vector2ic maxVersion = getMaxSupportedApiVersion();
            unsupportedApiVersion = maxVersion.x() < requiredApiVersion.x() ||
                (maxVersion.x() == requiredApiVersion.x() && maxVersion.y() < requiredApiVersion.y());

            Vector2ic currentVersion = getCurrentVulkanVersion();
            wrongApiVersion = currentVersion.x() < requiredApiVersion.x() ||
                (currentVersion.x() == requiredApiVersion.x() && currentVersion.y() < requiredApiVersion.y());
        }

        // get all supported extensions
        Set<String> availableDeviceExtensions = ((VulkanDeviceExtension) vulkanDevice).vivecraft$getAvailableDeviceExtensions();

        Set<String> availableInstanceExtensions = ((VulkanInstanceExtension) vulkanDevice.instance()).vivecraft$getAvailableExtensions();

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

        // remember the requirements for the next launch
        ClientDataHolderVR.getInstance().vrSettings.requiredVulkanInstanceExtensions = String.join(" ",
            instanceExtensions);
        ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceExtensions = String.join(" ", deviceExtensions);
        if (requiredApiVersion != null) {
            ClientDataHolderVR.getInstance().vrSettings.requiredVulkanMinAPIVersion =
                String.valueOf(VulkanStaticHelper.packVulkanVersion(requiredApiVersion.x(), requiredApiVersion.y()));
        } else {
            ClientDataHolderVR.getInstance().vrSettings.requiredVulkanMinAPIVersion = "";
        }
        ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceUUID = requiredDeviceUUID;
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();

        // first throw if we are running on the wrong device, in that case all other checks are irrelevant
        if (wrongDevice && requiredDevice != null) {
            String currentDeviceName = VulkanStaticHelper.getDeviceName(getPhysicalDevice());
            String requiredDeviceName = VulkanStaticHelper.getDeviceName(requiredDevice);

            throw new RenderConfigException(Component.translatable("vivecraft.messages.vulkanwronggputitle"),
                Component.translatable("vivecraft.messages.vulkanwronggpu",
                    Component.literal(currentDeviceName).withStyle(ChatFormatting.GOLD),
                    Component.literal(requiredDeviceName).withStyle(ChatFormatting.GREEN)));
        }

        // if we are on the right device, throw if the minimum api version is not supported
        if (unsupportedApiVersion) {
            Vector2ic maxVersion = getMaxSupportedApiVersion();
            throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblegpu"),
                Component.translatable("vivecraft.messages.vulkanunsupportedapi",
                    Component.literal(RenderSystem.getDevice().getDeviceInfo().name()).withStyle(ChatFormatting.GOLD),
                    Component.literal(maxVersion.x() + "." + maxVersion.y()).withStyle(ChatFormatting.RED),
                    Component.literal(requiredApiVersion.x() + "." + requiredApiVersion.y())
                        .withStyle(ChatFormatting.GREEN)));
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
        Set<String> enabledExtensions = vulkanDevice.getDeviceInfo().underlyingExtensions();
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

        // if all the above are fine, check that the requirements are met, extensions and set api version
        if (wrongApiVersion || !missingExtensions.isEmpty()) {
            if (wrongApiVersion) {
                VRSettings.LOGGER.info("Vivecraft: vulkan api version not set correctly, requesting a restart.");
            }
            if (!missingExtensions.isEmpty()) {
                VRSettings.LOGGER.info(
                    "Vivecraft: Not all needed Vulkan Extensions are enabled, game restart required. Not enabled Extensions:\n{}",
                    String.join("\n", missingExtensions));
            }
            throw new RenderConfigException(Component.translatable("vivecraft.messages.vulkanrestarttitle"),
                Component.translatable("vivecraft.messages.vulkanrestart"));
        }
    }

    public Vector2ic getMaxSupportedApiVersion() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.calloc(stack);
            VK10.vkGetPhysicalDeviceProperties(getPhysicalDevice(), deviceProperties);

            return VulkanStaticHelper.parseVulkanVersion(
                Math.min(VK.getInstanceVersionSupported(), deviceProperties.apiVersion()));
        }
    }

    public Vector2ic getCurrentVulkanVersion() {
        return VulkanStaticHelper.parseVulkanVersion(
            ((VulkanInstanceExtension) getVulkanDevice().instance()).vivecraft$getApiVersion());
    }

    public VkDevice getDevice() {
        return getVulkanDevice().vkDevice();
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return getVulkanDevice().vkDevice().getPhysicalDevice();
    }

    public long getQueuePointer() {
        return getVulkanDevice().graphicsQueue().vkQueue().address();
    }

    public int getQueueFamilyIndex() {
        return getVulkanDevice().graphicsQueue().queueFamilyIndex();
    }

    public int getQueueIndex() {
        return ((VulkanQueueExtension) (Object) getVulkanDevice().graphicsQueue()).vivecraft$getQueueIndex();
    }

    public VkInstance getInstance() {
        return getVulkanDevice().instance().vkInstance();
    }

    @Override
    public int formatToAPI(ImageFormat format) {
        return switch (format) {
            case R8G8B8_SRGB -> VK10.VK_FORMAT_R8G8B8_SRGB;
            case R8G8B8A8_UNORM -> VK10.VK_FORMAT_R8G8B8A8_UNORM;
            case R8G8B8A8_SRGB -> VK10.VK_FORMAT_R8G8B8A8_SRGB;
            case B8G8R8A8_UNORM -> VK10.VK_FORMAT_B8G8R8A8_UNORM;
            case B8G8R8A8_SRGB -> VK10.VK_FORMAT_B8G8R8A8_SRGB;
            case R10G10B10A2_UINT -> VK10.VK_FORMAT_A2R10G10B10_UINT_PACK32;
            case B10G10R10A2_UNORM -> VK10.VK_FORMAT_A2B10G10R10_UNORM_PACK32;
            case R16G16B16_UNORM -> VK10.VK_FORMAT_R16G16B16_UNORM;
            case R16G16B16_SFLOAT -> VK10.VK_FORMAT_R16G16B16_SFLOAT;
            case R16G16B16A16_UNORM -> VK10.VK_FORMAT_R16G16B16A16_UNORM;
            case R16G16B16A16_SFLOAT -> VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
            case R32G32B32_SFLOAT -> VK10.VK_FORMAT_R32G32B32_SFLOAT;
            case R32G32B32A32_SFLOAT -> VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
        };
    }

    @Override
    @Nullable
    public ImageFormat formatFromAPI(int format) {
        return switch (format) {
            case VK10.VK_FORMAT_R8G8B8_SRGB -> ImageFormat.R8G8B8_SRGB;
            case VK10.VK_FORMAT_R8G8B8A8_UNORM -> ImageFormat.R8G8B8A8_UNORM;
            case VK10.VK_FORMAT_R8G8B8A8_SRGB -> ImageFormat.R8G8B8A8_SRGB;
            case VK10.VK_FORMAT_B8G8R8A8_UNORM -> ImageFormat.B8G8R8A8_UNORM;
            case VK10.VK_FORMAT_B8G8R8A8_SRGB -> ImageFormat.B8G8R8A8_SRGB;
            case VK10.VK_FORMAT_A2R10G10B10_UINT_PACK32 -> ImageFormat.R10G10B10A2_UINT;
            case VK10.VK_FORMAT_A2B10G10R10_UNORM_PACK32 -> ImageFormat.B10G10R10A2_UNORM;
            case VK10.VK_FORMAT_R16G16B16_UNORM -> ImageFormat.R16G16B16_UNORM;
            case VK10.VK_FORMAT_R16G16B16_SFLOAT -> ImageFormat.R16G16B16_SFLOAT;
            case VK10.VK_FORMAT_R16G16B16A16_UNORM -> ImageFormat.R16G16B16A16_UNORM;
            case VK10.VK_FORMAT_R16G16B16A16_SFLOAT -> ImageFormat.R16G16B16A16_SFLOAT;
            case VK10.VK_FORMAT_R32G32B32_SFLOAT -> ImageFormat.R32G32B32_SFLOAT;
            case VK10.VK_FORMAT_R32G32B32A32_SFLOAT -> ImageFormat.R32G32B32A32_SFLOAT;
            default -> null;
        };
    }
}
