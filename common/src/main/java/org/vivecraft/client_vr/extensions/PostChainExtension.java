package org.vivecraft.client_vr.extensions;

import java.io.IOException;

public interface PostChainExtension {
    /**
     * updates sub PostChains to be for the correct passes
     */
    void vivecraft$updatePasses() throws IOException;
}
