package org.vivecraft.client_vr.extensions;

public interface BlockModelWrapperExtension {
    /**
     * set if any of the parents of this model use the flat generated model
     *
     * @param generated if any of the parents of this model use the flat generated model
     */
    void vivecraft$setGenerated(boolean generated);

    /**
     * @return if any of the parents of this model use the flat generated model
     */
    boolean vivecraft$isGenerated();
}
