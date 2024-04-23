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
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import java.awt.*;
import java.util.ArrayList;

public class Debug {
    Vec3 root;
    Quaternion rotation;
    public static boolean isEnabled = true;
    static Polygon cross = new Polygon(6);
    static Polygon arrowHead = new Polygon(8);
    private static final DebugRendererManual renderer = new DebugRendererManual();

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

    public Debug(Vec3 root) {
        this.root = root;
        this.rotation = new Quaternion();
    }

    public Debug(Vec3 root, Quaternion rotation) {
        this.root = root;
        this.rotation = rotation;
    }

    public void drawPoint(Vec3 point, Color color) {
        point = this.rotation.multiply(point);
        Vec3 global = this.root.add(point);
        Polygon poly = cross.offset(global);

        for (int i = 0; i < poly.colors.length; i++) {
            if (poly.colors[i] == null) {
                poly.colors[i] = color;
            }
        }

        renderer.toDraw.add(poly);
    }

    public void drawVector(Vec3 start, Vec3 direction, Color color) {
        Polygon poly = new Polygon(2);

        start = this.rotation.multiply(start);
        direction = this.rotation.multiply(direction);

        poly.vertices[0] = this.root.add(start);
        poly.colors[0] = new Color(0, 0, 0, 0);

        poly.vertices[1] = this.root.add(start).add(direction);
        poly.colors[1] = color;

        Quaternion rot = Quaternion.createFromToVector(new Vector3(0.0F, 1.0F, 0.0F),
            new Vector3(direction.normalize()));
        Polygon arrow = arrowHead.rotated(rot).offset(this.root.add(start).add(direction));

        for (int i = 0; i < arrow.colors.length; i++) {
            if (arrow.colors[i] == null) {
                arrow.colors[i] = color;
            }
        }

        renderer.toDraw.add(poly);
        renderer.toDraw.add(arrow);
    }

    public void drawLine(Vec3 start, Vec3 end, Color color) {
        start = this.rotation.multiply(start);
        end = this.rotation.multiply(end);

        Polygon poly = new Polygon(2);

        poly.vertices[0] = this.root.add(start);
        poly.colors[0] = new Color(0, 0, 0, 0);

        poly.vertices[1] = this.root.add(end);
        poly.colors[1] = color;

        renderer.toDraw.add(poly);
    }

    public void drawBoundingBox(AABB box, Color color) {
        Polygon poly = new Polygon(16);
        Vec3[] lower = new Vec3[4];
        Vec3[] upper = new Vec3[4];
        int index = 0;

        lower[0] = new Vec3(box.minX, box.minY, box.minZ);
        lower[1] = new Vec3(box.minX, box.minY, box.maxZ);
        lower[2] = new Vec3(box.maxX, box.minY, box.maxZ);
        lower[3] = new Vec3(box.maxX, box.minY, box.minZ);

        upper[0] = new Vec3(box.minX, box.maxY, box.minZ);
        upper[1] = new Vec3(box.minX, box.maxY, box.maxZ);
        upper[2] = new Vec3(box.maxX, box.maxY, box.maxZ);
        upper[3] = new Vec3(box.maxX, box.maxY, box.minZ);

        for (int j = 0; j < 4; j++) {
            lower[j] = this.root.add(this.rotation.multiply(lower[j]));
            upper[j] = this.root.add(this.rotation.multiply(upper[j]));
        }

        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                poly.colors[index] = new Color(0, 0, 0, 0);
            } else {
                poly.colors[index] = color;
            }

            poly.vertices[index] = lower[i % 4];
            index++;
        }

        for (int i = 0; i < 5; i++) {
            poly.colors[index] = color;
            poly.vertices[index] = upper[i % 4];
            index++;
        }

        for (int i = 1; i < 4; i++) {
            poly.vertices[index] = lower[i];
            poly.colors[index] = new Color(0, 0, 0, 0);
            index++;

            poly.vertices[index] = upper[i];
            poly.colors[index] = color;
            index++;
        }

        renderer.toDraw.add(poly);
    }

    static class Polygon {
        Vec3[] vertices;
        Color[] colors;

        public Polygon(int size) {
            this.vertices = new Vec3[size];
            this.colors = new Color[size];
        }

        public Polygon offset(Vec3 offset) {
            Polygon poly = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; i++) {
                poly.vertices[i] = this.vertices[i].add(offset);
                poly.colors[i] = this.colors[i];
            }

            return poly;
        }

        public Polygon rotated(Quaternion quat) {
            Polygon poly = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; i++) {
                poly.vertices[i] = quat.multiply(new Vector3(this.vertices[i])).toVector3d();
                poly.colors[i] = this.colors[i];
            }

            return poly;
        }
    }

    public static DebugRendererManual getRenderer() {
        return renderer;
    }

    public static class DebugRendererManual implements DebugRenderer.SimpleDebugRenderer {
        public boolean manualClearing = false;
        private final ArrayList<Polygon> toDraw = new ArrayList<>();

        public void render(float partialTicks, long finishTimeNano) {
            Player player = Minecraft.getInstance().player;
            double x = player.xOld + (player.getX() - player.xOld) * partialTicks;
            double y = player.yOld + (player.getY() - player.yOld) * partialTicks;
            double z = player.zOld + (player.getZ() - player.zOld) * partialTicks;
            // GlStateManager.lineWidth(5.0F);
            //GlStateManager._disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            //buffer.begin(3, DefaultVertexFormat.POSITION_COLOR);

            for (Polygon polygon : this.toDraw) {
                for (int i = 0; i < polygon.vertices.length; i++) {
                    this.renderVertex(buffer, polygon.vertices[i], polygon.colors[i],  x, y, z);
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

        @Override
        public void clear() {
            this.toDraw.clear();
        }

        void renderVertex(BufferBuilder buffer, Vec3 vert, Color color, double offX, double offY, double offZ) {
            buffer.vertex(vert.x - offX, vert.y - offY, vert.z - offZ)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ) {
        }
    }
}
