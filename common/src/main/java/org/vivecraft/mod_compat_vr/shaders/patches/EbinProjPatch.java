package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * return (diagonal2(gbufferProjection) * viewSpacePosition.xy + gbufferProjection[3].xy) / -viewSpacePosition.z * 0.5 + 0.5;
 */
public class EbinProjPatch extends Patch {
    public EbinProjPatch() {
        super("""
                return (diagonal2(gbufferProjection) * viewSpacePosition.xy + gbufferProjection[3].xy) / -viewSpacePosition.z * 0.5 + 0.5;
                """, """
                vec4 vivecraft_pos = $2 * vec4($3, 1.0);
                return (vivecraft_pos.xy / vivecraft_pos.w) * 0.5 + 0.5;""",
            "return\\s+\\((diagonal2|diag2)\\((\\w+)\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy\\)/-\\3\\.z\\*0\\.5\\+0\\.5;");
    }
}
