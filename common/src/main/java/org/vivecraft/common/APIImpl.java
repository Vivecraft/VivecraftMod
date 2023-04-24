package org.vivecraft.common;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api_beta.VivecraftAPI;

public final class APIImpl implements VivecraftAPI {

    public static final APIImpl INSTANCE = new APIImpl();

    private APIImpl() {
    }

    @Override
    public boolean isVrPlayer(Player player) {
        return false;
    }
}
