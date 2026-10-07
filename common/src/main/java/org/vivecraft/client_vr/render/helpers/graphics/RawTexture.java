package org.vivecraft.client_vr.render.helpers.graphics;

public interface RawTexture {

    static RawTexture create() {
        return null;
    }

    long getHandle();

    /**
     * deletes this texture
     */
    void destroy();
}
