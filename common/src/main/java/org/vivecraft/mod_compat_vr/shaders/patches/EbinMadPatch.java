package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches:
 * vec4(projMAD(gl_ProjectionMatrix, viewSpacePosition), viewSpacePosition.z * gl_ProjectionMatrix[2].w);
 */
public class EbinMadPatch extends Patch {
    public EbinMadPatch() {
        super("""
                vec4(projMAD(gl_ProjectionMatrix, viewSpacePosition), viewSpacePosition.z * gl_ProjectionMatrix[2].w);
                """, """
                $2 * vec4($3, 1.0)""",
            "vec4\\((\\w+)\\((\\w+),(\\w+)\\),\\3\\.z\\*\\2\\[2]\\.w\\)");
    }
}
