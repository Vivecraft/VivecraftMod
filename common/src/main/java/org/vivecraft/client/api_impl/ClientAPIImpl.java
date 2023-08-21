package org.vivecraft.client.api_impl;

import org.vivecraft.api_beta.client.VivecraftClientAPI;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_xr.render_pass.RenderPassType;

public final class ClientAPIImpl implements VivecraftClientAPI {

    public static final ClientAPIImpl INSTANCE = new ClientAPIImpl();

    private ClientAPIImpl() {
    }

    /**
     * @return Whether VR support is initialized.
     */
    @Override
    public boolean isVrInitialized() {
        return VRState.vrInitialized;
    }

    /**
     * @return Whether the client is actively in VR.
     */
    @Override
    public boolean isVrActive() {
        return VRState.vrRunning;
    }

    /**
     * @return Whether the current render pass is a vanilla render pass.
     */
    @Override
    public boolean isVanillaRenderPass() {
        return RenderPassType.isVanilla();
    }
}
