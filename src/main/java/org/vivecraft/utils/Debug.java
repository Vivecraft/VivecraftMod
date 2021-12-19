package org.vivecraft.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

public class Debug
{
    Vec3 root;
    Quaternion rotation;
    public static boolean isEnabled = true;
    static Debug.Polygon cross = new Debug.Polygon(6);
    static Debug.Polygon arrowHead = new Debug.Polygon(8);
    private static Debug.DebugRendererManual renderer = new Debug.DebugRendererManual();

    public Debug(Vec3 root)
    {
        this.root = root;
        this.rotation = new Quaternion();
    }

    public Debug(Vec3 root, Quaternion rotation)
    {
        this.root = root;
        this.rotation = rotation;
    }

    public void drawPoint(Vec3 point, Color color)
    {
        point = this.rotation.multiply(point);
        Vec3 vec3 = this.root.add(point);
        Debug.Polygon debug$polygon = cross.offset(vec3);

        for (int i = 0; i < debug$polygon.colors.length; ++i)
        {
            if (debug$polygon.colors[i] == null)
            {
                debug$polygon.colors[i] = color;
            }
        }

        renderer.toDraw.add(debug$polygon);
    }

    public void drawVector(Vec3 start, Vec3 direction, Color color)
    {
        Debug.Polygon debug$polygon = new Debug.Polygon(2);
        start = this.rotation.multiply(start);
        direction = this.rotation.multiply(direction);
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(start).add(direction);
        debug$polygon.colors[1] = color;
        Quaternion quaternion = Quaternion.createFromToVector(new Vector3(0.0F, 1.0F, 0.0F), new Vector3(direction.normalize()));
        Debug.Polygon debug$polygon1 = arrowHead.rotated(quaternion).offset(this.root.add(start).add(direction));

        for (int i = 0; i < debug$polygon1.colors.length; ++i)
        {
            if (debug$polygon1.colors[i] == null)
            {
                debug$polygon1.colors[i] = color;
            }
        }

        renderer.toDraw.add(debug$polygon);
        renderer.toDraw.add(debug$polygon1);
    }

    public void drawLine(Vec3 start, Vec3 end, Color color)
    {
        start = this.rotation.multiply(start);
        end = this.rotation.multiply(end);
        Debug.Polygon debug$polygon = new Debug.Polygon(2);
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(end);
        debug$polygon.colors[1] = color;
        renderer.toDraw.add(debug$polygon);
    }

    public void drawBoundingBox(AABB box, Color color)
    {
        Debug.Polygon debug$polygon = new Debug.Polygon(16);
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

        for (int j = 0; j < 4; ++j)
        {
            avec3[j] = this.root.add(this.rotation.multiply(avec3[j]));
            avec31[j] = this.root.add(this.rotation.multiply(avec31[j]));
        }

        for (int k = 0; k < 5; ++k)
        {
            if (k == 0)
            {
                debug$polygon.colors[i] = new Color(0, 0, 0, 0);
            }
            else
            {
                debug$polygon.colors[i] = color;
            }

            debug$polygon.vertices[i] = avec3[k % 4];
            ++i;
        }

        for (int l = 0; l < 5; ++l)
        {
            debug$polygon.colors[i] = color;
            debug$polygon.vertices[i] = avec31[l % 4];
            ++i;
        }

        for (int i1 = 1; i1 < 4; ++i1)
        {
            debug$polygon.vertices[i] = avec3[i1];
            debug$polygon.colors[i] = new Color(0, 0, 0, 0);
            ++i;
            debug$polygon.vertices[i] = avec31[i1];
            debug$polygon.colors[i] = color;
            ++i;
        }

        renderer.toDraw.add(debug$polygon);
    }

    public static Debug.DebugRendererManual getRenderer()
    {
        return renderer;
    }

    static
    {
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

    public static class DebugRendererManual implements DebugRenderer.SimpleDebugRenderer
    {
        public boolean manualClearing = false;
        ArrayList<Debug.Polygon> toDraw = new ArrayList<>();

        public void render(float partialTicks, long finishTimeNano)
        {
            Player player = Minecraft.getInstance().player;
            double d0 = player.xOld + (player.getX() - player.xOld) * (double)partialTicks;
            double d1 = player.yOld + (player.getY() - player.yOld) * (double)partialTicks;
            double d2 = player.zOld + (player.getZ() - player.zOld) * (double)partialTicks;
           // GlStateManager.lineWidth(5.0F);
            GlStateManager._disableTexture();
            //GlStateManager._disableLighting();
            GlStateManager._depthMask(false);
            GlStateManager._disableDepthTest();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            //bufferbuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

            for (Debug.Polygon debug$polygon : this.toDraw)
            {
                for (int i = 0; i < debug$polygon.vertices.length; ++i)
                {
                    this.renderVertex(bufferbuilder, debug$polygon.vertices[i], debug$polygon.colors[i], d0, d1, d2);
                }
            }

            tesselator.end();
            GlStateManager._depthMask(true);
            GlStateManager._enableTexture();
            //GlStateManager._enableLighting();
            GlStateManager._enableDepthTest();

            if (!this.manualClearing)
            {
                this.toDraw.clear();
            }
        }

        public void clear()
        {
            this.toDraw.clear();
        }

        void renderVertex(BufferBuilder buffer, Vec3 vert, Color color, double offX, double offY, double offZ)
        {
            buffer.vertex(vert.x - offX, vert.y - offY, vert.z - offZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }

        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, double pCamX, double p_113510_, double pCamY)
        {
        }
    }

    static class Polygon
    {
        Vec3[] vertices;
        Color[] colors;

        public Polygon(int size)
        {
            this.vertices = new Vec3[size];
            this.colors = new Color[size];
        }

        public Debug.Polygon offset(Vec3 offset)
        {
            Debug.Polygon debug$polygon = new Debug.Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i)
            {
                debug$polygon.vertices[i] = this.vertices[i].add(offset);
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }

        public Debug.Polygon rotated(Quaternion quat)
        {
            Debug.Polygon debug$polygon = new Debug.Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i)
            {
                debug$polygon.vertices[i] = quat.multiply(new Vector3(this.vertices[i])).toVector3d();
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }
    }
}
