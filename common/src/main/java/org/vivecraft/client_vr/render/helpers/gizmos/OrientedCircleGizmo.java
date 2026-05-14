package org.vivecraft.client_vr.render.helpers.gizmos;

import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.common.utils.MathUtils;

public record OrientedCircleGizmo(Vec3 pos, Vector3fc direction, float radius, GizmoStyle style) implements Gizmo {
    private static final int CIRCLE_VERTICES = 20;
    private static final float SEGMENT_SIZE_RADIANS = (float) (Math.PI / 10.0);

    public void emit(GizmoPrimitives primitives, float alphaMultiplier) {
        if (this.style.hasStroke() || this.style.hasFill()) {
            Vec3[] points = new Vec3[CIRCLE_VERTICES + 1];
            Vector3f offset = MathUtils.getPerpendicularVec(this.direction).mul(this.radius);

            for (int i = 0; i < CIRCLE_VERTICES; ++i) {
                Vec3 point = this.pos.add(offset.x, offset.y, offset.z);
                offset.rotateAxis(SEGMENT_SIZE_RADIANS, this.direction.x(), this.direction.y(), this.direction.z());
                points[i] = point;
            }

            points[CIRCLE_VERTICES] = points[0];
            if (this.style.hasFill()) {
                int color = this.style.multipliedFill(alphaMultiplier);
                primitives.addTriangleFan(points, color);
            }

            if (this.style.hasStroke()) {
                int color = this.style.multipliedStroke(alphaMultiplier);

                for (int i = 0; i < CIRCLE_VERTICES; ++i) {
                    primitives.addLine(points[i], points[i + 1], color, this.style.strokeWidth());
                }
            }
        }
    }
}
