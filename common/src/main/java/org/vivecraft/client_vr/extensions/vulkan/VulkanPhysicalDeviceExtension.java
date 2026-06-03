package org.vivecraft.client_vr.extensions.vulkan;

import java.util.Set;

public interface VulkanPhysicalDeviceExtension {

    /**
     * @return set of all supported device extensions
     */
    Set<String> vivecraft$getAvailableExtensions();
}
