package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * return projMAD(gbufferProjectionInverse, screenPos) / (screenPos.z * gbufferProjectionInverse[2].w + gbufferProjectionInverse[3].w);
 */
public class EbinProjInvPatch extends Patch {
    public EbinProjInvPatch() {
        super(
            """
                return projMAD(gbufferProjectionInverse, screenPos) / (screenPos.z * gbufferProjectionInverse[2].w + gbufferProjectionInverse[3].w);
                """, """
                vec4 vivecraft_$3 = $2 * vec4($3, 1.0);
                return vivecraft_$3.xyz / vivecraft_$3.w;""",
            "return\\s+(\\w+)\\((\\w+),(\\w+)\\)/\\(\\3\\.z\\*\\2\\[2]\\.w\\+\\2\\[3]\\.w\\);");
    }
}
