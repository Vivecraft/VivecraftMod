package org.vivecraft.client.extensions;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;

public interface CustomFeatureRendererExtension {
    void vivecraft$renderLate(
        final SubmitNodeCollection nodeCollection, final MultiBufferSource.BufferSource bufferSource);
}
