package org.vivecraft.mod_compat_vr.sable;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import org.vivecraft.Xloader;

public class SableHelper {

    /**
     * Sable adds a skylight scale to the vanilla shader, without setting this the menuworlöd would be black
     * @param shaderInstance shader instace to set the uniform on
     */
    public static void setSkylightFactor(ShaderInstance shaderInstance) {
        if (Xloader.isModLoaded("sable")) {
            final Uniform sableSkyLightScale = shaderInstance.getUniform("SableSkyLightScale");
            if (sableSkyLightScale != null) {
                sableSkyLightScale.set(1.0F);
            }
        }
    }
}
