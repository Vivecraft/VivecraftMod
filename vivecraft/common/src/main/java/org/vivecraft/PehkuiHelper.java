package org.vivecraft;

import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

public class PehkuiHelper {
    public static float getPlayerScale(Entity player, float tickDelta) {
        return ScaleUtils.getEyeHeightScale(player, tickDelta);
    }
    public static float getPlayerBbScale(Entity player, float tickDelta) {
        return ScaleUtils.getBoundingBoxHeightScale(player, tickDelta);
    }

}
