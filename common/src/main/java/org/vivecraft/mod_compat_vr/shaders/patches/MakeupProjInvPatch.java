package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches:
 * curr_view_pos = vec3(vec2(gbufferProjectionInverse[0].x, gbufferProjectionInverse[1].y) * (texcoord * 2.0 - 1.0) + gbufferProjectionInverse[3].xy, gbufferProjectionInverse[3].z);
 * curr_view_pos /= (gbufferProjectionInverse[2].w * (z_depth * 2.0 - 1.0) + gbufferProjectionInverse[3].w);
 */
public class MakeupProjInvPatch extends Patch {
    public MakeupProjInvPatch() {
        super("""
                    curr_view_pos =
                    vec3(vec2(gbufferProjectionInverse[0].x, gbufferProjectionInverse[1].y) * (texcoord * 2.0 - 1.0) + gbufferProjectionInverse[3].xy, gbufferProjectionInverse[3].z);
                    curr_view_pos /= (gbufferProjectionInverse[2].w * (z_depth * 2.0 - 1.0) + gbufferProjectionInverse[3].w);
                """, """
                vec4 viveViewPos = $2 * vec4(vec3($3, $4) * 2.0 - 1.0, 1.0);
                $1 = viveViewPos.xyz / viveViewPos.w;""",
            "(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*\\((\\w+)\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.xy,\\2\\[3]\\.z\\);\\1/=\\(\\2\\[2]\\.w\\*\\((\\w+)\\*2\\.0-1\\.0\\)\\+\\2\\[3]\\.w\\);");
    }
}
