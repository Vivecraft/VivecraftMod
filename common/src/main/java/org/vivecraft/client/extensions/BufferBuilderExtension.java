package org.vivecraft.client.extensions;

/**
 * this is an extension to be able to free buffers of custom created BufferBuilders and get their size,
 * since vanilla doesn't have that capability
 */
public interface BufferBuilderExtension {
    /**
     * frees the underlying buffer
     */
    void vivecraft$freeBuffer();

    /**
     * @return size of the underlying buffer
     */
    int vivecraft$getBufferSize();
}
