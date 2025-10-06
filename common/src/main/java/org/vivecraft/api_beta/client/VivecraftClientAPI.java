package org.vivecraft.api_beta.client;

import com.google.common.annotations.Beta;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.client.VRRenderingAPI;

/**
 * @deprecated since 1.3.0, use {@link VRClientAPI} and {@link VRRenderingAPI} instead
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public interface VivecraftClientAPI {

    @Deprecated(since = "1.3.0", forRemoval = true)
    VivecraftClientAPI INSTANCE = new VivecraftClientAPI() {
        @Override
        public boolean isVrInitialized() {
            return VRClientAPI.instance().isVRInitialized();
        }

        @Override
        public boolean isVrActive() {
            return VRClientAPI.instance().isVRActive();
        }

        @Override
        public boolean isVanillaRenderPass() {
            return VRRenderingAPI.instance().isVanillaRenderPass();
        }
    };

    /**
     * @deprecated since 1.3.0, use {@link VRClientAPI#instance()} and {@link VRRenderingAPI#instance()} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    static VivecraftClientAPI getInstance() {
        return INSTANCE;
    }

    /**
     * @deprecated since 1.3.0, use {@link VRClientAPI#isVRInitialized()} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    boolean isVrInitialized();

    /**
     * @deprecated since 1.3.0, use {@link VRClientAPI#isVRActive()} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    boolean isVrActive();

    /**
     * @deprecated since 1.3.0, use {@link VRRenderingAPI#isVanillaRenderPass()} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Beta
    boolean isVanillaRenderPass();
}
