package org.vivecraft.client_vr.extensions.vulkan;

import java.util.Set;

public interface VulkanInstanceExtension {

    /**
     * @return set of all supported instance extensions
     */
    Set<String> vivecraft$getAvailableExtensions();

    /**
     * @return the vulkan api version the instance was created with
     */
    int vivecraft$getApiVersion();
}
