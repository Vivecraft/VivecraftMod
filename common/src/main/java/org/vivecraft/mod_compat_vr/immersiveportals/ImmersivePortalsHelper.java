package org.vivecraft.mod_compat_vr.immersiveportals;

import org.vivecraft.Xloader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.portal.PortalPlaceholderBlock;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

public class ImmersivePortalsHelper {

    public static boolean isLoaded() {
        return Xloader.isModLoaded("immersive_portals");
    }

    /**
     * @return if the renderpass is for a portal
     */
    public static boolean isRenderingPortal() {
        return PortalRendering.isRendering();
    }

    /**
     * @return if the player should be rendered in a portal
     */
    public static boolean shouldRenderSelf() {
        return IPGlobal.renderYourselfInPortal && isRenderingPortal();
    }

    public static boolean rayIntersectsPortal(Level level, Vec3 start, Vec3 end) {
        return !IPMcHelper.rayTracePortals(level, start, end, true, p -> true).isEmpty();
    }

    public static boolean positionContainsPortalBlock(Level level, Vec3 pos) {
        return level.getBlockState(BlockPos.containing(pos)).is(PortalPlaceholderBlock.instance);
    }
}
