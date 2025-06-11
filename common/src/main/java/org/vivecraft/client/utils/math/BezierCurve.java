package org.vivecraft.client.utils.math;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BezierCurve {
    public ArrayList<Node> nodes = new ArrayList<>();

    // Whether we should draw another curve from the last Node to the first
    boolean circular;

    public BezierCurve(Node[] nodes, boolean circular) {
        this.nodes.addAll(Arrays.asList(nodes));
        this.circular = circular;
    }

    public BezierCurve(boolean circular) {
        this.circular = circular;
    }

    Vec3 getIntermediate(Node n1, Node n2, double perc) {
        Vec3 p0 = n1.vertex;
        Vec3 p1 = n1.controlOut;
        Vec3 p2 = n2.controlIn;
        Vec3 p3 = n2.vertex;
        return p0.scale(Math.pow(1.0D - perc, 3.0D))
            .add(p1.scale(3.0D * Math.pow(1.0D - perc, 2.0D) * perc))
            .add(p2.scale(3.0D * (1.0D - perc) * Math.pow(perc, 2.0D)))
            .add(p3.scale(Math.pow(perc, 3.0D)));
    }

    /**
     * returns the intermediate Point on the path at {@code perc}%
     * of the path. This assumes equidistant Nodes, meaning close distances have
     * the same amount of intermediate Vertices as longer paths between Nodes.
     *
     * @param perc position on Path in interval [0,1] (inclusive interval)
     */
    public Vec3 getPointOnPath(double perc) {
        // first node is counted as another virtual node if we are circular
        int nodeCount = this.circular ? this.nodes.size() : this.nodes.size() - 1;

        double exactIndex = perc * (double) nodeCount;
        int lowerIndex = (int) Math.floor(exactIndex) % this.nodes.size();
        int upperIndex = (int) Math.ceil(exactIndex) % this.nodes.size();

        if (lowerIndex == upperIndex) {
            return (this.nodes.get(lowerIndex)).vertex;
        } else {
            Node node1 = this.nodes.get(lowerIndex);
            Node node2 = this.nodes.get(upperIndex);
            return this.getIntermediate(node1, node2, exactIndex - lowerIndex);
        }
    }

    public Vec3[] getLinearInterpolation(int verticesPerNode) {
        if (this.nodes.isEmpty()) {
            return new Vec3[0];
        } else {
            int totalVertices = verticesPerNode * (this.circular ? this.nodes.size() : this.nodes.size() - 1) + 1;
            Vec3[] out = new Vec3[totalVertices];

            for (int i = 0; i < totalVertices; i++) {
                double perc = (double) i / (double) Math.max(1, totalVertices - 1);
                out[i] = this.getPointOnPath(perc);
            }

            return out;
        }
    }

    public void render(int vertexCount, Color c, float partialTick) {
        Player player = Minecraft.getInstance().player;
        double x = player.xOld + (player.getX() - player.xOld) * partialTick;
        double y = player.yOld + (player.getY() - player.yOld) * partialTick;
        double z = player.zOld + (player.getZ() - player.zOld) * partialTick;
        // GlStateManager._disableLighting();
        RenderSystem.depthMask(false);
        BufferBuilder buffer = Tesselator.getInstance()
            .begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3[] avec3 = this.getLinearInterpolation(vertexCount / this.nodes.size());

        for (int i = 0; i < avec3.length; i++) {
            this.renderVertex(buffer, avec3[i], c, x, y, z);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        // GlStateManager._enableLighting();
        RenderSystem.depthMask(true);
    }

    void renderVertex(BufferBuilder buffer, Vec3 vert, Color color, double offX, double offY, double offZ) {
        buffer.addVertex((float) (vert.x - offX), (float) (vert.y - offY), (float) (vert.z - offZ))
            .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static class Node {
        Vec3 vertex;
        Vec3 controlIn;
        Vec3 controlOut;

        public Node(Vec3 vertex, Vec3 controlIn, Vec3 controlOut) {
            this.vertex = vertex;
            this.controlIn = controlIn;
            this.controlOut = controlOut;
        }

        public Node(Vec3 vertex, Vec3 controlDir, double controlLenIn, double controlLenOut) {
            this(vertex,
                vertex.add(controlDir.normalize().scale(-controlLenIn)),
                vertex.add(controlDir.normalize().scale(controlLenOut)));
        }
    }
}
