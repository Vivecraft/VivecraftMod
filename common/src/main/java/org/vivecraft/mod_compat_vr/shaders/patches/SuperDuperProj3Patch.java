package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * vec2 clipCoord = vec2(projection[0].x, projection[1].y) * viewPos.xy;
 * return 0.5 - vec3(clipCoord.xy / viewPos.z, projection[3].z / viewPos.z + projection[2].z) * 0.5;
 */
public class SuperDuperProj3Patch extends Patch {
    public SuperDuperProj3Patch() {
        super("""
                    vec2 clipCoord = vec2(projection[0].x, projection[1].y) * viewPos.xy;
                    return 0.5 - vec3(clipCoord.xy / viewPos.z, projection[3].z / viewPos.z + projection[2].z) * 0.5;
                """, """
                vec4 $1 = $2 * vec4($3, 1);
                return ($1.xyz / $1.w) * 0.5 + 0.5;""",
            "vec2\\s+(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;return\\s+0\\.5-vec3\\(\\1\\.xy/\\3\\.z,\\2\\[3]\\.z/\\3\\.z\\+\\2\\[2]\\.z\\)(\\*0\\.5|/2\\.0);");
    }
}
