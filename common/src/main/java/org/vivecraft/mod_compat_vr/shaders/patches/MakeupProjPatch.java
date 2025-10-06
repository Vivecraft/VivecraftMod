package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches:
 * final_pos = vec2(gbufferPreviousProjection[0].x, gbufferPreviousProjection[1].y) * prev_view_pos.xy + gbufferPreviousProjection[3].xy;
 * texcoord_past = (final_pos / -prev_view_pos.z) * 0.5 + 0.5;
 */
public class MakeupProjPatch extends Patch {
    public MakeupProjPatch() {
        super("""
                final_pos = vec2(gbufferPreviousProjection[0].x, gbufferPreviousProjection[1].y) * prev_view_pos.xy + gbufferPreviousProjection[3].xy;
                texcoord_past = (final_pos / -prev_view_pos.z) * 0.5 + 0.5;""", """
                vec4 viveScreenPos = $2 * vec4($3, 1.0);
                $1 = viveScreenPos.xy;
                $4 = viveScreenPos.xy / viveScreenPos.w * 0.5 + 0.5;
                """,
            "(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy\\+\\2\\[3]\\.xy;(\\w+)=\\(\\1/-\\3\\.z\\)\\*0\\.5\\+0\\.5;");
    }
}
