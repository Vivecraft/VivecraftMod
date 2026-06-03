package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.vivecraft.client_vr.extensions.vulkan.VulkanDeviceExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanInstanceExtension;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VulkanHelper implements GraphicsHelper {

    private VulkanDevice getVulkanDevice() {
        if (RenderSystem.getDevice().backend instanceof VulkanDevice vulkanDevice) {
            return vulkanDevice;
        } else {
            throw new IllegalArgumentException("Vivecraft: not a vulkan device in vulkan context");
        }
    }

    @Override
    public long getTextureHandle(GpuTexture texture) {
        if (texture instanceof VulkanGpuTexture vulkanTexture) {
            return vulkanTexture.vkImage();
        }
        throw new IllegalArgumentException("Vivecraft: not a vulkan texture in vulkan context");
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
        VulkanDevice vulkanDevice = getVulkanDevice();
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
        if (!missingExtensions.isEmpty()) {
            VRSettings.LOGGER.info(
                "Vivecraft: Not all needed Vulkan Extensions are enabled, game restart required. Not enabled Extensions:\n{}",
                String.join("\n", missingExtensions));
            throw new RenderConfigException(Component.translatable("vivecraft.messages.vulkanrestarttitle"),
                Component.translatable("vivecraft.messages.vulkanrestart"));
        }
    }

    public long getDevicePointer() {
        return getVulkanDevice().vkDevice().address();
    }

    public long getPhysicalDevicePointer() {
        return getVulkanDevice().vkDevice().getPhysicalDevice().address();
    }

    public long getQueuePointer() {
        return getVulkanDevice().graphicsQueue().vkQueue().address();
    }

    public int getQueueFamilyIndex() {
        return getVulkanDevice().graphicsQueue().queueFamilyIndex();
    }

    public long getInstancePointer() {
        return getVulkanDevice().instance().vkInstance().address();
    }
}
