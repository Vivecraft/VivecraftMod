package org.vivecraft.client.extensions;

public interface RenderTargetExtension {

    /**
     * sets if a combined depth/stencil should be used
     *
     * @param stencil if a stencil should be added
     */
    void vivecraft$setStencil(boolean stencil);

    /**
     * @return if the RenderTarget has a stencil added by vivecraft active
     */
    boolean vivecraft$hasStencil();

    /**
     * sets if mipmaps should be used for sampling
     *
     * @param mipmaps if mipmaps should be used
     */
    void vivecraft$setMipmaps(boolean mipmaps);

    /**
     * @return if the RenderTarget is set to use mipmaps
     */
    boolean vivecraft$hasMipmaps();
}
