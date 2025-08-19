package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.InteractModule;

/**
 * Allows an InteractModule to render visual debug information
 */
public interface DebugRenderModule extends InteractModule {
    /**
     * Renders debug elements to visualize the module's state.
     * Only called when the InteractTracker is set to render debug elements in the debug settings.
     * <br>
     * This is called every frame, so modules should make sure that stuff is not {@code null}, since this could be called before {@link InteractModule#isActive(LocalPlayer, InteractionHand, Vec3)} is called.
     *
     * @param isActive if the given module is currently one of active InteractModules
     */
    void renderDebug(boolean isActive);
}
