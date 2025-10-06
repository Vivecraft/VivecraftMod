package org.vivecraft.mod_compat_vr.shaders.patches;

/**
 * patches
 * vec2 vertexClipCoordStart = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosStart.xy;
 * vec2 vertexClipCoordEnd = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosEnd.xy;
 * ...
 * float vertexViewDepth = linePosStart.z * 0.99609375;
 */
public class SuperDuperLineProj2Patch extends Patch {
    public SuperDuperLineProj2Patch() {
        super(
            """
                vec2 vertexClipCoordStart = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosStart.xy;
                vec2 vertexClipCoordEnd = vec2(projectionMatrix[0].x, projectionMatrix[1].y) * linePosEnd.xy;
                float vertexViewDepth = linePosStart.z * 0.99609375; // don't patch
                """, """
                vec2 $1 = ($2 * vec4($3, 1.0)).xy * 0.99609375;
                vec2 $4 = ($2 * vec4($5, 1.0)).xy * 0.99609375;
                $6""",
            "vec2\\s+(\\w+)=vec2\\((\\w+)\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;vec2\\s+(\\w+)=vec2\\(\\2\\[0]\\.x,\\2\\[1]\\.y\\)\\*(\\w+)\\.xy;(((.|\\s)+?)\\3\\.z\\*0\\.99609375;)");
    }
}
