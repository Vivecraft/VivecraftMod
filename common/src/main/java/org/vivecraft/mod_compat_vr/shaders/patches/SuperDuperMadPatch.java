package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * gl_Position.xyz = getMatScale(mat3(gl_ProjectionMatrix)) * vertexViewPos;
 * gl_Position.z += gl_ProjectionMatrix[3].z;
 * gl_Position.w = -vertexViewPos.z;
 */
public class SuperDuperMadPatch extends Patch {
    public SuperDuperMadPatch() {
        super(
            """
                gl_Position.xyz = getMatScale(mat3(gl_ProjectionMatrix)) * vertexViewPos;
                gl_Position.z += gl_ProjectionMatrix[3].z;
                gl_Position.w = -vertexViewPos.z;
                """, """
                $1 = $3 * vec4($4, 1.0);""",
            "(\\w+)\\.xyz=(\\w+)\\(mat3\\((\\w+)\\)\\)\\*(\\w+);\\1\\.z\\+=\\3\\[3]\\.z;\\1\\.w=-\\4\\.z;");
    }
}
