package org.vivecraft.client.extensions;

import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.render.VRPlayerRenderData;

public interface EntityRenderStateExtension {
    /**
     * @return the RotInfo of the entity this state is for
     */
    ClientVRPlayers.RotInfo vivecraft$getRotInfo();

    /**
     * set the RotInfo of the entity this state is for
     */
    void vivecraft$setRotInfo(ClientVRPlayers.RotInfo rotInfo);

    /**
     * @return if the entity, this state is for, is the first person player and in VR
     */
    boolean vivecraft$isFirstPersonPlayer();

    /**
     * set if the entity, this state is for, is the first person player
     */
    void vivecraft$setFirstPersonPlayer(boolean firstPersonPlayer);

    /**
     * @return the players scale, including any other mods scaling
     */
    float vivecraft$getTotalScale();

    /**
     * set the players scale
     */
    void vivecraft$setTotalScale(float totalScale);

    /**
     * @return the render data needed to animate the vr player model
     */
    VRPlayerRenderData vivecraft$getVRRenderData();

    /**
     * set render data
     */
    void vivecraft$setVRRenderData(VRPlayerRenderData data);
}
