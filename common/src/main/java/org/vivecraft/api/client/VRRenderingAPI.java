package org.vivecraft.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.client.api_impl.VRRenderingAPIImpl;

/**
 * The main interface for interacting with Vivecraft from rendering code. For other client-side code, one should use
 * {@link VRClientAPI}.
 *
 * @since 1.3.0
 */
public interface VRRenderingAPI {

    /**
     * Gets the API instance for interacting with Vivecraft for rendering.
     *
     * @return The Vivecraft API instance for interacting with Vivecraft's rendering API.
     * @since 1.3.0
     */
    static VRRenderingAPI instance() {
        return VRRenderingAPIImpl.INSTANCE;
    }

    /**
     * Gets whether the current render pass is a vanilla render pass. This method's return value is only valid if this
     * method was called while rendering.
     *
     * @return Whether the current render pass is a vanilla render pass.
     * @since 1.3.0
     */
    boolean isVanillaRenderPass();

    /**
     * Gets the current render pass. This method's return value is only valid if this method was called while rendering.
     *
     * @return The current render pass Vivecraft is performing.
     * @since 1.3.0
     */
    RenderPass getCurrentRenderPass();

    /**
     * Returns if the current render pass is the first render pass for this render cycle. This method's return value is
     * only valid if this method was called while rendering.
     *
     * @return Whether the current render pass is the first one performed for this render cycle.
     * @since 1.3.0
     */
    boolean isFirstRenderPass();

    /**
     * Gets the position that the provided {@link InteractionHand} renders at. Unlike
     * {@link VRPose#getHand(InteractionHand)} from {@link VRClientAPI#getWorldRenderPose()},
     * this returns a reasonable, default value for seated mode.
     *
     * @param hand The hand to get the rendering position of.
     * @return The rendering position for the provided hand.
     * @since 1.3.0
     */
    Vec3 getHandRenderPos(InteractionHand hand);

    /**
     * Sets the provided {@link PoseStack} to render at the position of and with the rotation of the provided
     * {@link InteractionHand}, this assumes the given {@code stack} to be set to an identity.
     *
     * @param hand  The hand to set the PoseStack to.
     * @param stack The PoseStack to be set.
     * @since 1.3.0
     */
    void setupRenderingAtHand(InteractionHand hand, PoseStack stack);

    /**
     * Sets the provided {@link Matrix4f} to render at the position of and with the rotation of the provided
     * {@link InteractionHand}, this assumes the given {@code matrix} to be an identity.
     *
     * @param hand   The hand to set the Matrix4f to.
     * @param matrix The Matrix4f to be set.
     * @since 1.3.0
     * @since Minecraft 1.20.5
     */
    void setupRenderingAtHand(InteractionHand hand, Matrix4f matrix);
}
