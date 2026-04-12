package org.vivecraft.mod_compat_vr.pehkui;

import net.minecraft.world.entity.Entity;
import org.vivecraft.Xloader;

public class PehkuiHelper {

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("pehkui");
    }

    /**
     * gets the current eye height scale of the give Entity
     *
     * @param entity      Entity to get the eye height scale for
     * @param partialTick current partial tick
     * @return scale of the entities eye height
     */
    public static float getEntityEyeHeightScale(Entity entity, float partialTick) {
        // TODO 26.1 use reflection
        return 1;//ScaleUtils.getEyeHeightScale(entity, partialTick);
    }

    /**
     * gets the current bounding box scale of the give Entity
     *
     * @param entity      Entity to get the bounding box scale for
     * @param partialTick current partial tick
     * @return scale of the entities bounding box
     */
    public static float getEntityBbScale(Entity entity, float partialTick) {
        // TODO 26.1 use reflection
        return 1;//ScaleUtils.getBoundingBoxHeightScale(entity, partialTick);
    }
}
