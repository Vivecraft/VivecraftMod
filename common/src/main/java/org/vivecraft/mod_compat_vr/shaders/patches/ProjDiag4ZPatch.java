package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * pos     = pos.xyzz * diag4(gl_ProjectionMatrix) + vec4(0.0, 0.0, gl_ProjectionMatrix[3].z, 0.0);
 */
public class ProjDiag4ZPatch extends Patch {
    public ProjDiag4ZPatch() {
        super(
            """
                pos     = pos.xyzz * diag4(gl_ProjectionMatrix) + vec4(0.0, 0.0, gl_ProjectionMatrix[3].z, 0.0);
                """, """
                $1 = $3 * vec4($1.xyz, 1.0);""",
            "(\\w+)=\\1\\.xyzz\\*(diag4|diagonal4)\\((\\w+)\\)\\+vec4\\(0\\.0,0\\.0,\\3\\[3]\\.z,0\\.0\\);");
    }
}
