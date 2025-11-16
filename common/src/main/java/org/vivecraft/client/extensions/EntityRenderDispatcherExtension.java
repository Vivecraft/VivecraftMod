package org.vivecraft.client.extensions;

import net.minecraft.world.entity.player.PlayerModelType;
import org.vivecraft.client.render.VRPlayerRenderer;

import java.util.Map;

public interface EntityRenderDispatcherExtension {
    /**
     * @return map of VR player renderers with the vanilla model
     */
    Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRVanilla();

    /**
     * @return map of VR player renderers with split arms
     */
    Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRArms();

    /**
     * @return map of VR player renderers with split arms and legs
     */
    Map<PlayerModelType, VRPlayerRenderer> vivecraft$getSkinMapVRLegs();
}
