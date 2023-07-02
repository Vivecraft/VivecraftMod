package org.vivecraft.mod_compat_vr.iris;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import org.vivecraft.client_xr.render_pass.RenderPassManager;

import java.io.IOException;

public class IrisHelper {


    public static void setShadersActive(boolean bl) {
        IrisApi.getInstance().getConfig().setShadersEnabledAndApply(bl);
    }

    public static void reload() {
        try {
            RenderPassManager.setVanillaRenderPass();
            Iris.reload();
        } catch (IOException e) {
            System.err.println("Error reloading shaders on Frame Buffer reinit");
            e.printStackTrace();
        }

    }

    public static boolean hasWaterEffect() {
        return Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderUnderwaterOverlay).orElse(true);
    }

    public static boolean isShaderActive(){
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
