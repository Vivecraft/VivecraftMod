package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.gl.program.ProgramUniforms",
    "net.irisshaders.iris.gl.program.ProgramUniforms"
})
public class IrisProgramUniformsMixin {

    @Shadow(remap = false)
    int lastFrame;
    @Unique
    private RenderPass vivecraft$lastPass;
    @Unique
    private int vivecraft$actualFrame;


    // modify the frame counter on RenderPasChange, so perFrame Uniforms are recalculated
    @ModifyVariable(method = "update", at = @At(value = "STORE"), remap = false)
    private int vivecraft$checkNewFrame(int currentFrame) {
        if (!RenderPassType.isVanilla()) {
            vivecraft$actualFrame = currentFrame;
            if (lastFrame == currentFrame && vivecraft$lastPass != ClientDataHolderVR.getInstance().currentPass) {
                currentFrame--;
            }
            vivecraft$lastPass = ClientDataHolderVR.getInstance().currentPass;
        }
        return currentFrame;
    }

    // restore actual frame counter, so stuff doesn't get messed up
    @ModifyVariable(method = "update", at = @At(value = "LOAD", ordinal = 1), remap = false)
    private int vivecraft$restoreFrame(int currentFrame) {
        if (!RenderPassType.isVanilla()) {
            return vivecraft$actualFrame;
        } else {
            return currentFrame;
        }
        //return !RenderPassType.isVanilla() ? actualFrame : currentFrame;
    }
}
