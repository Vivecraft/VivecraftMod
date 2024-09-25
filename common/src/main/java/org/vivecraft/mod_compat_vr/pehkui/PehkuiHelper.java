package org.vivecraft.mod_compat_vr.pehkui;

import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

public class PehkuiHelper {
    public static float getEntityScale(Entity entity, float tickDelta) {
        return ScaleUtils.getEyeHeightScale(entity, tickDelta);
    }

    public static float getEntityBbScale(Entity entity, float tickDelta) {
        return ScaleUtils.getBoundingBoxHeightScale(entity, tickDelta);
    }
}
