package org.vivecraft.mod_compat_vr.shaders;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.shaders.patches.*;

import java.util.Set;

public class ShaderPatcher {

    private static final Set<Patch> PATCHES = Set.of(
        new ProjDiag4ZPatch(),
        new SuperDuperMadPatch(),
        new ProjDiag4Patch(),
        new EbinMadPatch(),
        new RreProjInvPatch(),
        new SuperDuperProjInvPatch(),
        new EbinProjInvPatch(),
        new MakeupProjInvPatch(),
        new SuperDuperProj2Patch(),
        new SuperDuperLineProj2Patch(),
        new SuperDuperProj3Patch(),
        new EbinProjPatch(),
        new ProjDiag3Patch(),
        new MakeupProjPatch()
    );

    /**
     * patches known incompatibilities with VR
     *
     * @param shader shader code to patch
     * @return patched shader code
     */
    public static String patchShader(String shader) {
        if (ClientDataHolderVR.getInstance().vrSettings.shaderPatching) {
            for (Patch patch : PATCHES) {
                shader = patch.patch(shader);
            }
        }
        return shader;
    }

    public static Set<Patch> getPatches() {
        return PATCHES;
    }
}
