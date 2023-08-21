package org.vivecraft.api_beta.client;

import com.google.common.annotations.Beta;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.client.api_impl.ClientAPIImpl;

public interface VivecraftClientAPI {

    static VivecraftClientAPI getInstance() {
        return ClientAPIImpl.INSTANCE;
    }

    /**
     * Gets data representing the devices as they exist in the room before the game tick. This is effectively
     * the latest polling data from the VR devices.
     * @return Data representing the devices in the room pre-tick.
     * @throws IllegalStateException Thrown when the local player isn't in VR.
     */
    @Beta
    VRData getPreTickRoomData() throws IllegalStateException;

    /**
     * Gets data representing the devices as they exist in the room after the game tick.
     * @return Data representing the devices in the room post-tick.
     * @throws IllegalStateException Thrown when the local player isn't in VR.
     */
    @Beta
    VRData getPostTickRoomData() throws IllegalStateException;

    /**
     * Gets data representing the devices as they exist in Minecraft coordinates before the game tick.
     * This is the same as {@link #getPreTickRoomData()} with translation to Minecraft's coordinates as of the last
     * tick.
     * @return Data representing the devices in Minecraft space pre-tick.
     * @throws IllegalStateException Thrown when the local player isn't in VR.
     */
    @Beta
    VRData getPreTickWorldData() throws IllegalStateException;

    /**
     * Gets data representing the devices as they exist in Minecraft coordinates after the game tick.
     * This is the data sent to the server, and also used to calculate the data in {@link #getWorldRenderData()}.
     * If you're unsure which {@link VRData} method to use, you likely want to use this one.
     * @return Data representing the devices in Minecraft space post-tick.
     * @throws IllegalStateException Thrown when the local player isn't in VR.
     */
    @Beta
    VRData getPostTickWorldData() throws IllegalStateException;

    /**
     * Gets data representing the devices as they exist in Minecraft coordinates after the game tick interpolated for
     * rendering.
     * This is the same data as {@link #getPostTickWorldData()}, however it is interpolated for rendering.
     * @return Data representing the devices in Minecraft space post-tick interpolated for rendering.
     * @throws IllegalStateException Thrown when the local player isn't in VR.
     */
    @Beta
    VRData getWorldRenderData() throws IllegalStateException;

    /**
     * @return Whether VR support is initialized.
     */
    boolean isVrInitialized();

    /**
     * @return Whether the client is actively in VR.
     */
    boolean isVrActive();

    /**
     * @return Whether the current render pass is a vanilla render pass.
     */
    @Beta
    boolean isVanillaRenderPass();

    /**
     * @return The currently active world scale.
     */
    @Beta
    float getWorldScale();
}
