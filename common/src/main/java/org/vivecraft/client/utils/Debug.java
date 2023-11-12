package org.vivecraft.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.common.utils.Utils;

import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.pow;

public class Debug {
    Vec3 root;
    Quaternionf rotation;
    public static boolean isEnabled = true;
    static Polygon cross = new Polygon(6);
    static Polygon arrowHead = new Polygon(8);
    private static final DebugRendererManual renderer = new DebugRendererManual();

    public Debug(Vec3 root) {
        this.root = root;
        this.rotation = new Quaternionf();
    }

    public Debug(Vec3 root, Quaternionf rotation) {
        this.root = root;
        this.rotation = rotation;
    }

    public void drawPoint(Vec3 point, Color color) {
        point = Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) point.x, (float) point.y, (float) point.z), new Vector3f()));
        Vec3 vec3 = this.root.add(point);
        Polygon debug$polygon = cross.offset(vec3);

        for (int i = 0; i < debug$polygon.colors.length; ++i) {
            if (debug$polygon.colors[i] == null) {
                debug$polygon.colors[i] = color;
            }
        }

        renderer.toDraw.add(debug$polygon);
    }

    public void drawVector(Vec3 start, Vec3 direction, Color color) {
        Polygon debug$polygon = new Polygon(2);
        start = Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) start.x, (float) start.y, (float) start.z), new Vector3f()));
        direction = Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) direction.x, (float) direction.y, (float) direction.z), new Vector3f()));
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(start).add(direction);
        debug$polygon.colors[1] = color;
        Vec3 normalized = direction.normalize();
        Vector3f to = new Vector3f((float) normalized.x(), (float) normalized.y(), (float) normalized.z());
        Vector3f vector3 = Utils.up.cross(to, new Vector3f());
        float f = (float) (Math.sqrt(pow(Utils.up.length(), 2.0D) * pow(to.length(), 2.0D)) + (double) Utils.up.dot(to));
        Quaternionf quaternion1 = (new Quaternionf(vector3.x, vector3.y, vector3.z, f));
        Quaternionf dest = new Quaternionf();
        float f4 = Math.sqrt(quaternion1.lengthSquared());

        if (f4 > 0.0F) {
            dest.normalize();
        } else {
            dest.identity();
        }

        Quaternionf quaternion = dest;
        Polygon debug$polygon1 = arrowHead.rotated(quaternion).offset(this.root.add(start).add(direction));

        for (int i = 0; i < debug$polygon1.colors.length; ++i) {
            if (debug$polygon1.colors[i] == null) {
                debug$polygon1.colors[i] = color;
            }
        }

        renderer.toDraw.add(debug$polygon);
        renderer.toDraw.add(debug$polygon1);
    }

    public void drawLine(Vec3 start, Vec3 end, Color color) {
        start = Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) start.x, (float) start.y, (float) start.z), new Vector3f()));
        end = Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) end.x, (float) end.y, (float) end.z), new Vector3f()));
        Polygon debug$polygon = new Polygon(2);
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(end);
        debug$polygon.colors[1] = color;
        renderer.toDraw.add(debug$polygon);
    }

    public void drawBoundingBox(AABB box, Color color) {
        Polygon debug$polygon = new Polygon(16);
        Vec3[] avec3 = new Vec3[4];
        Vec3[] avec31 = new Vec3[4];
        int i = 0;
        avec3[0] = new Vec3(box.minX, box.minY, box.minZ);
        avec3[1] = new Vec3(box.minX, box.minY, box.maxZ);
        avec3[2] = new Vec3(box.maxX, box.minY, box.maxZ);
        avec3[3] = new Vec3(box.maxX, box.minY, box.minZ);
        avec31[0] = new Vec3(box.minX, box.maxY, box.minZ);
        avec31[1] = new Vec3(box.minX, box.maxY, box.maxZ);
        avec31[2] = new Vec3(box.maxX, box.maxY, box.maxZ);
        avec31[3] = new Vec3(box.maxX, box.maxY, box.minZ);

        for (int j = 0; j < 4; ++j) {
            avec3[j] = this.root.add(Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) avec3[j].x, (float) avec3[j].y, (float) avec3[j].z), new Vector3f())));
            avec31[j] = this.root.add(Utils.convertToVec3(this.rotation.transformUnit(new Vector3f((float) avec31[j].x, (float) avec31[j].y, (float) avec31[j].z), new Vector3f())));
        }

        for (int k = 0; k < 5; ++k) {
            if (k == 0) {
                debug$polygon.colors[i] = new Color(0, 0, 0, 0);
            } else {
                debug$polygon.colors[i] = color;
            }

            debug$polygon.vertices[i] = avec3[k % 4];
            ++i;
        }

        for (int l = 0; l < 5; ++l) {
            debug$polygon.colors[i] = color;
            debug$polygon.vertices[i] = avec31[l % 4];
            ++i;
        }

        for (int i1 = 1; i1 < 4; ++i1) {
            debug$polygon.vertices[i] = avec3[i1];
            debug$polygon.colors[i] = new Color(0, 0, 0, 0);
            ++i;
            debug$polygon.vertices[i] = avec31[i1];
            debug$polygon.colors[i] = color;
            ++i;
        }

        renderer.toDraw.add(debug$polygon);
    }

    public static DebugRendererManual getRenderer() {
        return renderer;
    }

    static {
        cross.colors[0] = new Color(0, 0, 0, 0);
        cross.vertices[0] = new Vec3(0.0D, -0.1D, 0.0D);
        cross.vertices[1] = new Vec3(0.0D, 0.1D, 0.0D);
        cross.colors[2] = new Color(0, 0, 0, 0);
        cross.vertices[2] = new Vec3(0.0D, 0.0D, -0.1D);
        cross.vertices[3] = new Vec3(0.0D, 0.0D, 0.1D);
        cross.colors[4] = new Color(0, 0, 0, 0);
        cross.vertices[4] = new Vec3(-0.1D, 0.0D, 0.0D);
        cross.vertices[5] = new Vec3(0.1D, 0.0D, 0.0D);
        arrowHead.colors[0] = new Color(0, 0, 0, 0);
        arrowHead.vertices[0] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[1] = new Vec3(-0.05D, -0.05D, 0.0D);
        arrowHead.colors[2] = new Color(0, 0, 0, 0);
        arrowHead.vertices[2] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[3] = new Vec3(0.05D, -0.05D, 0.0D);
        arrowHead.colors[4] = new Color(0, 0, 0, 0);
        arrowHead.vertices[4] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[5] = new Vec3(0.0D, -0.05D, -0.05D);
        arrowHead.colors[6] = new Color(0, 0, 0, 0);
        arrowHead.vertices[6] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[7] = new Vec3(0.0D, -0.05D, 0.05D);
    }

    public static class DebugRendererManual implements DebugRenderer.SimpleDebugRenderer {
        public boolean manualClearing = false;
        ArrayList<Polygon> toDraw = new ArrayList<>();

        public void render(float partialTicks, long finishTimeNano) {
            Player player = Minecraft.getInstance().player;
            double d0 = player.xOld + (player.getX() - player.xOld) * (double) partialTicks;
            double d1 = player.yOld + (player.getY() - player.yOld) * (double) partialTicks;
            double d2 = player.zOld + (player.getZ() - player.zOld) * (double) partialTicks;
            // GlStateManager.lineWidth(5.0F);
            //GlStateManager._disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            //bufferbuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

            for (Polygon debug$polygon : this.toDraw) {
                for (int i = 0; i < debug$polygon.vertices.length; ++i) {
                    this.renderVertex(bufferbuilder, debug$polygon.vertices[i], debug$polygon.colors[i], d0, d1, d2);
                }
            }

            tesselator.end();
            RenderSystem.depthMask(true);
            //GlStateManager._enableLighting();
            RenderSystem.enableDepthTest();

            if (!this.manualClearing) {
                this.toDraw.clear();
            }
        }

        public void clear() {
            this.toDraw.clear();
        }

        void renderVertex(BufferBuilder buffer, Vec3 vert, Color color, double offX, double offY, double offZ) {
            buffer.vertex(vert.x - offX, vert.y - offY, vert.z - offZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, double pCamX, double p_113510_, double pCamY) {
        }
    }

    static class Polygon {
        Vec3[] vertices;
        Color[] colors;

        public Polygon(int size) {
            this.vertices = new Vec3[size];
            this.colors = new Color[size];
        }

        public Polygon offset(Vec3 offset) {
            Polygon debug$polygon = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i) {
                debug$polygon.vertices[i] = this.vertices[i].add(offset);
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }

        public Polygon rotated(Quaternionf quat) {
            Polygon debug$polygon = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i) {
                debug$polygon.vertices[i] = Utils.convertToVec3(quat.transformUnit(new Vector3f((float) this.vertices[i].x, (float) this.vertices[i].y, (float) this.vertices[i].z), new Vector3f()));
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }
    }
}
