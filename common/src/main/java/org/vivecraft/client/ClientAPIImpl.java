package org.vivecraft.client;

import org.vivecraft.api_beta.client.VivecraftClientAPI;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import static org.vivecraft.client_vr.VRState.vrInitialized;
import static org.vivecraft.client_vr.VRState.vrRunning;

public final class ClientAPIImpl implements VivecraftClientAPI {

    public static final ClientAPIImpl INSTANCE = new ClientAPIImpl();

    private ClientAPIImpl() {
    }

    @Override
    public boolean isVrInitialized() {
        return vrInitialized;
    }

    @Override
    public boolean isVrActive() {
        return vrRunning;
    }

    @Override
    public boolean isVanillaRenderPass() {
        return RenderPassType.isVanilla();
    }
}
