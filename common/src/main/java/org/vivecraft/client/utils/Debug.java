package org.vivecraft.client.utils;

import org.vivecraft.common.utils.color.Color;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer.SimpleDebugRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

import static java.lang.Math.pow;
import static org.joml.Math.*;

public class Debug
{
    Vec3 root;
    Quaternionf rotation;
    public static boolean isEnabled = true;
    static Polygon cross = new Polygon(6);
    static Polygon arrowHead = new Polygon(8);
    private static final DebugRendererManual renderer = new DebugRendererManual();

    public Debug(Vec3 root)
    {
        this.root = root;
        this.rotation = new Quaternionf();
    }

    public Debug(Vec3 root, Quaternionf rotation)
    {
        this.root = root;
        this.rotation = rotation;
    }

    public void drawPoint(Vec3 point, Color color)
    {
        point = convertToVec3(this.rotation.transformUnit(convertToVector3f(point), new Vector3f()));
        Vec3 vec3 = this.root.add(point);
        Polygon debug$polygon = cross.offset(vec3);

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
        Polygon debug$polygon = new Polygon(2);
        start = convertToVec3(this.rotation.transformUnit(convertToVector3f(start), new Vector3f()));
        direction = convertToVec3(this.rotation.transformUnit(convertToVector3f(direction), new Vector3f()));
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(start).add(direction);
        debug$polygon.colors[1] = color;
        // TODO JOML probably has functions for this
        Vec3 from = convertToVec3(up);
        Vec3 to = direction.normalize();
        float f = (float)(sqrt(pow(from.length(), 2.0D) * pow(to.length(), 2.0D)) + from.dot(to));
        Vec3 vector3 = from.cross(to);
        Quaternionf quaternion = new Quaternionf((float)vector3.x, (float)vector3.y, (float)vector3.z, f).normalize(new Quaternionf());
        //
        Polygon debug$polygon1 = arrowHead.rotated(quaternion).offset(this.root.add(start).add(direction));

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
        start = convertToVec3(this.rotation.transformUnit(convertToVector3f(start), new Vector3f()));
        end = convertToVec3(this.rotation.transformUnit(convertToVector3f(end), new Vector3f()));
        Polygon debug$polygon = new Polygon(2);
        debug$polygon.vertices[0] = this.root.add(start);
        debug$polygon.colors[0] = new Color(0, 0, 0, 0);
        debug$polygon.vertices[1] = this.root.add(end);
        debug$polygon.colors[1] = color;
        renderer.toDraw.add(debug$polygon);
    }

    public void drawBoundingBox(AABB box, Color color)
    {
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

        for (int j = 0; j < 4; ++j)
        {
            avec3[j] = this.root.add(convertToVec3(this.rotation.transformUnit(convertToVector3f(avec3[j]), new Vector3f())));
            avec31[j] = this.root.add(convertToVec3(this.rotation.transformUnit(convertToVector3f(avec31[j]), new Vector3f())));
        }

        for (int k = 0; k < 5; ++k)
        {
            if (k == 0)
            {
                debug$polygon.colors[i] = new Color(Color.OFF);
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
            debug$polygon.colors[i] = new Color(Color.OFF);
            ++i;
            debug$polygon.vertices[i] = avec31[i1];
            debug$polygon.colors[i] = color;
            ++i;
        }

        renderer.toDraw.add(debug$polygon);
    }

    public static DebugRendererManual getRenderer()
    {
        return renderer;
    }

    static
    {
        cross.colors[0] = new Color(Color.OFF);
        cross.vertices[0] = new Vec3(0.0D, -0.1D, 0.0D);
        cross.vertices[1] = new Vec3(0.0D, 0.1D, 0.0D);
        cross.colors[2] = new Color(Color.OFF);
        cross.vertices[2] = new Vec3(0.0D, 0.0D, -0.1D);
        cross.vertices[3] = new Vec3(0.0D, 0.0D, 0.1D);
        cross.colors[4] = new Color(Color.OFF);
        cross.vertices[4] = new Vec3(-0.1D, 0.0D, 0.0D);
        cross.vertices[5] = new Vec3(0.1D, 0.0D, 0.0D);
        arrowHead.colors[0] = new Color(Color.OFF);
        arrowHead.vertices[0] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[1] = new Vec3(-0.05D, -0.05D, 0.0D);
        arrowHead.colors[2] = new Color(Color.OFF);
        arrowHead.vertices[2] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[3] = new Vec3(0.05D, -0.05D, 0.0D);
        arrowHead.colors[4] = new Color(Color.OFF);
        arrowHead.vertices[4] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[5] = new Vec3(0.0D, -0.05D, -0.05D);
        arrowHead.colors[6] = new Color(Color.OFF);
        arrowHead.vertices[6] = new Vec3(0.0D, 0.0D, 0.0D);
        arrowHead.vertices[7] = new Vec3(0.0D, -0.05D, 0.05D);
    }

    public static class DebugRendererManual implements SimpleDebugRenderer
    {
        public boolean manualClearing = false;
        ArrayList<Polygon> toDraw = new ArrayList<>();

        public void render(float partialTicks, long finishTimeNano)
        {
            double d0 = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * (double)partialTicks;
            double d1 = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * (double)partialTicks;
            double d2 = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * (double)partialTicks;
           // GlStateManager.lineWidth(5.0F);
            //GlStateManager._disableLighting();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            //bufferbuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

            for (Polygon debug$polygon : this.toDraw)
            {
                for (int i = 0; i < debug$polygon.vertices.length; ++i)
                {
                    this.renderVertex(bufferbuilder, debug$polygon.vertices[i], debug$polygon.colors[i], d0, d1, d2);
                }
            }

            tesselator.end();
            RenderSystem.depthMask(true);
            //GlStateManager._enableLighting();
            RenderSystem.enableDepthTest();

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
            buffer.vertex(vert.x - offX, vert.y - offY, vert.z - offZ).color(color.R(), color.G(), color.B(), color.A()).endVertex();
        }

        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, double camX, double camY, double camZ)
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

        public Polygon offset(Vec3 offset)
        {
            Polygon debug$polygon = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i)
            {
                debug$polygon.vertices[i] = this.vertices[i].add(offset);
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }

        public Polygon rotated(Quaternionf quat)
        {
            Polygon debug$polygon = new Polygon(this.vertices.length);

            for (int i = 0; i < this.vertices.length; ++i)
            {
                debug$polygon.vertices[i] = convertToVec3(
                    quat.transformUnit(
                        new Vector3f().set(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z),
                        new Vector3f()
                    )
                );
                debug$polygon.colors[i] = this.colors[i];
            }

            return debug$polygon;
        }
    }
}
