package org.vivecraft.api_beta;

import com.google.common.annotations.Beta;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.common.api_impl.APIImpl;

import javax.annotation.Nullable;

@Beta
public interface VivecraftAPI {

    /**
     * @return The Vivecraft API instance for interacting with Vivecraft's API.
     */
    static VivecraftAPI getInstance() {
        return APIImpl.INSTANCE;
    }

    /**
     * Check whether a given player is currently in VR.
     *
     * @param player The player to check the VR status of.
     * @return true if the player is in VR.
     */
    @Beta
    boolean isVRPlayer(Player player);

    /**
     * Returns the VR data for the given player. Will return null if the player isn't in VR,
     * or if being called from the client, and the client has yet to receive any data for the player.
     * @param player Player to get the VR data of.
     * @return The VR data for a player, or null if the player isn't in VR or no data has been received for said player.
     */
    @Beta
    @Nullable
    VRData getVRData(Player player);
}
