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
    boolean circular;

    public BezierCurve(Node[] nodes, boolean circular) {
        this.nodes.addAll(Arrays.asList(nodes));
        this.circular = circular;
    }

    public BezierCurve(boolean circular) {
        this.circular = circular;
    }

    Vec3 getIntermediate(Node n1, Node n2, double perc) {
        Vec3 vec3 = n1.vertex;
        Vec3 vec31 = n1.controlOut;
        Vec3 vec32 = n2.controlIn;
        Vec3 vec33 = n2.vertex;
        return vec3.scale(Math.pow(1.0D - perc, 3.0D)).add(vec31.scale(3.0D * Math.pow(1.0D - perc, 2.0D) * perc)).add(vec32.scale(3.0D * (1.0D - perc) * Math.pow(perc, 2.0D))).add(vec33.scale(Math.pow(perc, 3.0D)));
    }

    public Vec3 getPointOnPath(double perc) {
        int i = this.circular ? this.nodes.size() : this.nodes.size() - 1;
        double d0 = perc * (double) i;
        int j = (int) Math.floor(d0) % this.nodes.size();
        int k = (int) Math.ceil(d0) % this.nodes.size();

        if (j == k) {
            return (this.nodes.get(j)).vertex;
        } else {
            Node beziercurve$node = this.nodes.get(j);
            Node beziercurve$node1 = this.nodes.get(k);
            return this.getIntermediate(beziercurve$node, beziercurve$node1, d0 - (double) j);
        }
    }

    public Vec3[] getLinearInterpolation(int verticesPerNode) {
        if (this.nodes.size() == 0) {
            return new Vec3[0];
        } else {
            int i = verticesPerNode * (this.circular ? this.nodes.size() : this.nodes.size() - 1) + 1;
            Vec3[] avec3 = new Vec3[i];

            for (int j = 0; j < i; ++j) {
                double d0 = (double) j / (double) Math.max(1, i - 1);
                avec3[j] = this.getPointOnPath(d0);
            }

            return avec3;
        }
    }

    public void render(int vertexCount, Color c, float partialTicks) {
        Player player = Minecraft.getInstance().player;
        double d0 = player.xOld + (player.getX() - player.xOld) * (double) partialTicks;
        double d1 = player.yOld + (player.getY() - player.yOld) * (double) partialTicks;
        double d2 = player.zOld + (player.getZ() - player.zOld) * (double) partialTicks;
        //GlStateManager._disableLighting();
        RenderSystem.depthMask(false);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Vec3[] avec3 = this.getLinearInterpolation(vertexCount / this.nodes.size());

        for (int i = 0; i < avec3.length; ++i) {
            this.renderVertex(bufferbuilder, avec3[i], c, d0, d1, d2);
        }

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        //GlStateManager._enableLighting();
        RenderSystem.depthMask(true);
    }

    void renderVertex(BufferBuilder buffer, Vec3 vert, Color color, double offX, double offY, double offZ) {
        buffer.addVertex((float) (vert.x - offX), (float) (vert.y - offY), (float) (vert.z - offZ)).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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
            this(vertex, vertex.add(controlDir.normalize().scale(-controlLenIn)), vertex.add(controlDir.normalize().scale(controlLenOut)));
        }
    }
}
