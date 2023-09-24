package org.vivecraft.api_beta.client;

import org.vivecraft.client.ClientAPIImpl;

import com.google.common.annotations.Beta;

public interface VivecraftClientAPI {

    static VivecraftClientAPI getInstance() {
        return ClientAPIImpl.INSTANCE;
    }

    boolean isVrInitialized();

    boolean isVrActive();

    @Beta
    boolean isVanillaRenderPass();
}
