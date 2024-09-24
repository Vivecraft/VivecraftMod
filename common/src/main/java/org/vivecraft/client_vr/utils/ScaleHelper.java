package org.vivecraft.client_vr.utils;

import net.minecraft.world.entity.LivingEntity;
import org.vivecraft.client.Xplat;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

public class ScaleHelper {

    public static float getEntityScale(LivingEntity livingEntity, float tickDelta) {
        float scale = livingEntity.getScale();
        if (Xplat.isModLoaded("pehkui")) {
            scale *= PehkuiHelper.getEntityScale(livingEntity, tickDelta);
        }
        return scale;
    }

    public static float getEntityBbScale(LivingEntity livingEntity, float tickDelta) {
        float scale = livingEntity.getScale();
        if (Xplat.isModLoaded("pehkui")) {
            scale *= PehkuiHelper.getEntityBbScale(livingEntity, tickDelta);
        }
        return scale;
    }
}
