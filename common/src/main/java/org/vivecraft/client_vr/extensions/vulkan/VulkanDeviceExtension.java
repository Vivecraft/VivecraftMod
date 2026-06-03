package org.vivecraft.client_vr.extensions.vulkan;

import java.util.Set;

public interface VulkanDeviceExtension {

    /**
     * @return set of all supported device extensions
     */
    Set<String> vivecraft$getAvailableDeviceExtensions();
}
