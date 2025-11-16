package org.vivecraft.client_vr.extensions;

import net.minecraft.world.entity.player.PlayerModelType;
import org.vivecraft.client_vr.render.VRArmRenderer;

import java.util.Map;

public interface EntityRenderDispatcherVRExtension {

    /**
     * @return map of VR arm renderers
     */
    Map<PlayerModelType, VRArmRenderer> vivecraft$getArmSkinMap();
}
