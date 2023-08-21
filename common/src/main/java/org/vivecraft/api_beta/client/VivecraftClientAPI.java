package org.vivecraft.api_beta.client;

import com.google.common.annotations.Beta;
import org.vivecraft.client.api_impl.ClientAPIImpl;

public interface VivecraftClientAPI {

    static VivecraftClientAPI getInstance() {
        return ClientAPIImpl.INSTANCE;
    }

    boolean isVrInitialized();

    boolean isVrActive();

    @Beta
    boolean isVanillaRenderPass();
}
