package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches all of these
 * diagonal4(mat) * v.xyzz + mat[3];
 * v.xyzz * diagonal4(mat) + mat[3];
 * vec4(m[0].x, m[1].y, m[2].zw) * pos.xyzz + m[3];
 * iProjDiag * p3.xyzz + gbufferProjectionInverse[3];
 */
public class ProjDiag4Patch extends Patch {
    public ProjDiag4Patch() {
        super(
            """
                diagonal4(mat) * v.xyzz + mat[3];
                v.xyzz * diagonal4(mat) + mat[3];
                vec4(m[0].x, m[1].y, m[2].zw) * pos.xyzz + m[3];
                iProjDiag * p3.xyzz + gbufferProjectionInverse[3];
                """, """
                $10 * vec4($6, 1.0);""",
            "((((diagonal4|diag4)\\(\\w+\\))|\\w+|vec4\\((\\w+)\\[0]\\.x,\\5\\[1]\\.y,\\5\\[2]\\.zw\\))\\*\\s*)?(\\w+)\\.xyzz(\\*((diagonal4|diag4)\\(\\w+\\)))?\\+(\\w+)\\[3];");
    }
}
