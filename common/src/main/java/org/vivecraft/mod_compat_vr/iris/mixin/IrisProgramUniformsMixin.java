package org.vivecraft.mod_compat_vr.iris.mixin;

import net.coderbot.iris.gl.program.ProgramUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(ProgramUniforms.class)
public class IrisProgramUniformsMixin {

    @Shadow(remap = false)
    int lastFrame;
    private RenderPass lastPass;
    private int actualFrame;


    // modify the frame counter on RenderPasChange, so perFrame Uniforms are recalculated
    @ModifyVariable(method = "update", at = @At(value = "STORE"), remap = false)
    private int checkNewFrame(int currentFrame) {
        if (!RenderPassType.isVanilla()) {
            actualFrame = currentFrame;
            if (lastFrame == currentFrame && lastPass != ClientDataHolderVR.getInstance().currentPass) {
                currentFrame--;
            }
            lastPass = ClientDataHolderVR.getInstance().currentPass;
        }
        return currentFrame;
    }

    // restore actual frame counter, so stuff doesn't get messed up
    @ModifyVariable(method = "update", at = @At(value = "LOAD", ordinal = 1), remap = false)
    private int restoreFrame(int currentFrame) {
        if (!RenderPassType.isVanilla()){
            return actualFrame;
        }else {
            return currentFrame;
        }
        //return !RenderPassType.isVanilla() ? actualFrame : currentFrame;
    }
}
