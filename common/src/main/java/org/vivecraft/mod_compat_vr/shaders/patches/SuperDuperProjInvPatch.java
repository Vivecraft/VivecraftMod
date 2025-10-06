package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * vec3 viewPos = vec3(vec2(projectionInverse[0].x, projectionInverse[1].y) * (screenPos.xy * 2.0 - 1.0), -1);
 * return viewPos / (projectionInverse[2].w * (screenPos.z * 2.0 - 1.0) + projectionInverse[3].w);
 */
public class SuperDuperProjInvPatch extends Patch {
    public SuperDuperProjInvPatch() {
        super(
            """
                vec3 viewPos = vec3(vec2(projectionInverse[0].x, projectionInverse[1].y) * (screenPos.xy * 2.0 - 1.0), -1);
                return viewPos / (projectionInverse[2].w * (screenPos.z * 2.0 - 1.0) + projectionInverse[3].w);
                """, """
                vec4 $1 = $2 * vec4($3 * 2.0 - 1.0, 1.0);
                return $1.xyz / $1.w;""",
            "vec3\\s+(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*\\((\\w+)\\.xy\\*2\\.0-1\\.0\\),-1\\.0\\);return\\s+\\1/\\(\\2\\[2]\\.w\\*\\(\\3\\.z\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.w\\);");
    }
}
