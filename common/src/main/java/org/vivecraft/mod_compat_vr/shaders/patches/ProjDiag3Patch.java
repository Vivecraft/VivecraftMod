package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches all of these
 * diagonal3(m) * (v) + (m)[3].xyz
 * diagonal3(mat) * v + mat[3].xyz
 * (diagonal3(m) * v) + m[3].xyz;
 * vec3(projection[0].x, projection[1].y, projection[2].z) * viewPosition + projection[3].xyz
 */
public class ProjDiag3Patch extends Patch {
    public ProjDiag3Patch() {
        super("""
                diagonal3(m) * (v) + (m)[3].xyz
                diagonal3(mat) * v + mat[3].xyz
                (diagonal3(m) * v) + m[3].xyz;
                vec3(projection[0].x, projection[1].y, projection[2].z) * viewPosition + projection[3].xyz
                """, """
                ($1 * vec4($2$3, 1.0)).xyz""",
            "(?:diagonal3|diag3|vec3)\\((\\w+)(?:\\[0]\\.x,\\1\\[1]\\.y,\\1\\[2]\\.z)?\\)\\*(?:(\\w+)|\\((\\w+)\\))\\+(?:\\(\\1\\)|\\1)\\[3]\\.xyz",
            "\\((?:diagonal3|diag3|vec3)\\((\\w+)(?:\\[0]\\.x,\\1\\[1]\\.y,\\1\\[2]\\.z)?\\)\\*(?:(\\w+)|\\((\\w+)\\))\\)\\+(?:\\(\\1\\)|\\1)\\[3]\\.xyz");
    }
}
