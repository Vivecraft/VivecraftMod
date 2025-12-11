package org.vivecraft.client_vr.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.resources.Identifier;

import java.util.Set;

public interface LevelTargetBundleExtension {
    Identifier OCCLUDED_TARGET_ID = Identifier.fromNamespaceAndPath("vivecraft", "vroccluded");
    Identifier UNOCCLUDED_TARGET_ID = Identifier.fromNamespaceAndPath("vivecraft", "vrunoccluded");
    Identifier HANDS_TARGET_ID = Identifier.fromNamespaceAndPath("vivecraft", "vrhands");

    /**
     * list of targets for the VR fabulous shader
     */
    Set<Identifier> VR_TARGETS = Set.of(
        LevelTargetBundle.MAIN_TARGET_ID, LevelTargetBundle.TRANSLUCENT_TARGET_ID,
        LevelTargetBundle.ITEM_ENTITY_TARGET_ID, LevelTargetBundle.PARTICLES_TARGET_ID,
        LevelTargetBundle.WEATHER_TARGET_ID, LevelTargetBundle.CLOUDS_TARGET_ID, OCCLUDED_TARGET_ID,
        UNOCCLUDED_TARGET_ID, HANDS_TARGET_ID);

    /**
     * @return RenderTarget used for occluded stuff when using fabulous graphics
     */
    ResourceHandle<RenderTarget> vivecraft$getOccluded();

    /**
     * @return RenderTarget used for unoccluded stuff when using fabulous graphics
     */
    ResourceHandle<RenderTarget> vivecraft$getUnoccluded();

    /**
     * @return RenderTarget used for the hands when using fabulous graphics
     */
    ResourceHandle<RenderTarget> vivecraft$getHands();
}
