package net.irisshaders.iris.uniforms;

import org.joml.Matrix4f;

public class CapturedRenderingState {
    public static final CapturedRenderingState INSTANCE =  new CapturedRenderingState();
    public Matrix4f getGbufferProjection() {
        return null;
    }
}
