package org.vivecraft.client.extensions;

import org.vivecraft.client.ClientVRPlayers;

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
     * @return if the entity, this state is for, is the main player
     */
    float vivecraft$getTotalScale();

    /**
     * set if the entity, this state is for, is the main player
     */
    void vivecraft$setTotalScale(float totalScale);
}
