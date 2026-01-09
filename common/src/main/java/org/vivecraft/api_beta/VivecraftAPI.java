package org.vivecraft.api_beta;

import com.google.common.annotations.Beta;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;
import org.vivecraft.common.api_impl.VRAPIImpl;

/**
 * @deprecated since 1.3.0, use {@link VRAPI} instead
 */
@Deprecated(since = "1.3.0", forRemoval = true)
@Beta
public interface VivecraftAPI {

    @Deprecated(since = "1.3.0", forRemoval = true)
    VivecraftAPI INSTANCE = new VRAPIImpl.LegacyApi();

    /**
     * @deprecated since 1.3.0, use {@link VRAPI#instance()} instead
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    static VivecraftAPI getInstance() {
        return INSTANCE;
    }

    /**
     * @deprecated since 1.3.0, use {@link VRAPI#isVRPlayer(Player)} instead
     */
    @Beta
    @Deprecated(since = "1.3.0", forRemoval = true)
    boolean isVRPlayer(Player player);
}
