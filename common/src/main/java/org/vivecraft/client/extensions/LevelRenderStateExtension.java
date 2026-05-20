package org.vivecraft.client.extensions;


import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;

public interface LevelRenderStateExtension {

    /**
     * sets the outline state for the interact outline
     */
    void vivecraft$setInteractOutlineState(int controller, BlockOutlineRenderState interactOutline);

    /**
     * gets the states of the interact outline
     */
    BlockOutlineRenderState[] vivecraft$getInteractOutlineStates();

    /**
     * gets the VR render state object
     */
    VRRenderState vivecraft$getVRRenderState();
}
