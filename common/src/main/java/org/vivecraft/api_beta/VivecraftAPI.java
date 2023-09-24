package org.vivecraft.api_beta;

import org.vivecraft.common.APIImpl;

import com.google.common.annotations.Beta;

import net.minecraft.world.entity.player.Player;

@Beta
public interface VivecraftAPI {

    static VivecraftAPI getInstance() {
        return APIImpl.INSTANCE;
    }

    @Beta
    boolean isVRPlayer(Player player);
}
