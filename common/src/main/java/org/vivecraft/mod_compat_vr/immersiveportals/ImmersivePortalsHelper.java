package org.vivecraft.mod_compat_vr.immersiveportals;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

public class ImmersivePortalsHelper {
    public static boolean isRenderingPortal() {
        return PortalRendering.isRendering();
    }

    public static boolean shouldRenderSelf() {
        return IPGlobal.renderYourselfInPortal && isRenderingPortal();
    }

    public static boolean isStandingInPortal() {
        Player player = Minecraft.getInstance().player;
        return !player.level().getEntities(player, AABB.ofSize(player.position(), 0.1, 0.1, 0.1),
            (entity -> entity instanceof Portal)).isEmpty();
    }
}
