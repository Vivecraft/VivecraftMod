package org.vivecraft.client.api_impl;

import org.vivecraft.api_beta.client.VivecraftClientAPI;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_xr.render_pass.RenderPassType;

public final class ClientAPIImpl implements VivecraftClientAPI {

    public static final ClientAPIImpl INSTANCE = new ClientAPIImpl();

    private ClientAPIImpl() {
    }

    @Override
    public VRData getPreTickRoomData() throws IllegalStateException {
        if (!isVrActive()) {
            throw new IllegalStateException();
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.asVRData();
    }

    @Override
    public VRData getPostTickRoomData() throws IllegalStateException {
        if (!isVrActive()) {
            throw new IllegalStateException();
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.asVRData();
    }

    @Override
    public VRData getPreTickWorldData() throws IllegalStateException {
        if (!isVrActive()) {
            throw new IllegalStateException();
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre.asVRData();
    }

    @Override
    public VRData getPostTickWorldData() throws IllegalStateException {
        if (!isVrActive()) {
            throw new IllegalStateException();
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.asVRData();
    }

    @Override
    public VRData getWorldRenderData() throws IllegalStateException {
        if (!isVrActive()) {
            throw new IllegalStateException();
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.asVRData();
    }

    @Override
    public boolean isVrInitialized() {
        return VRState.vrInitialized;
    }

    @Override
    public boolean isVrActive() {
        return VRState.vrRunning;
    }

    @Override
    public boolean isVanillaRenderPass() {
        return RenderPassType.isVanilla();
    }

    @Override
    public float getWorldScale() {
        return ClientDataHolderVR.getInstance().vrPlayer.worldScale;
    }
}
