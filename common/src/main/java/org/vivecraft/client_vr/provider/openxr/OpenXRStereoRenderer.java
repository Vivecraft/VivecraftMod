package org.vivecraft.client_vr.provider.openxr;

import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

public class OpenXRStereoRenderer extends VRRenderer {
    public OpenXRStereoRenderer(MCVR vr) {
        super(vr);
    }

    @Override
    public void createRenderTexture(int var1, int var2) {

    }

    @Override
    public Matrix4f getProjectionMatrix(int var1, float var2, float var3) {
        return null;
    }

    @Override
    public void endFrame() throws RenderConfigException {

    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        return null;
    }
}
