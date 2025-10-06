package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * vec3 viewpos    = vec3(vec2(projInv[0].x, projInv[1].y)*screenpos.xy + projInv[3].xy, projInv[3].z);
 * viewpos    /= projInv[2].w*screenpos.z + projInv[3].w;
 */
public class RreProjInvPatch extends Patch {
    public RreProjInvPatch() {
        super(
            """
                vec3 viewpos    = vec3(vec2(projInv[0].x, projInv[1].y)*screenpos.xy + projInv[3].xy, projInv[3].z);
                viewpos    /= projInv[2].w*screenpos.z + projInv[3].w;
                """, """
                vec4 vivecraft_$1 = $2 * vec4($3.xyz, 1.0);
                vec3 $1 = vivecraft_$1.xyz / vivecraft_$1.w;""",
            "vec3(\\w+)=vec3\\(vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy,\\2\\[3]\\.z\\);\\1/=\\2\\[2]\\.w\\*\\3\\.z\\+\\2\\[3]\\.w;");
    }
}
