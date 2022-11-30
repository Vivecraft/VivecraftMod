package org.vivecraft.api.render;// Optifine
//package org.vivecraft.render;
//
//import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Matrix3f;
//import com.mojang.math.Matrix4f;
//import net.minecraft.client.renderer.block.ModelBlockRenderer;
//import net.minecraft.client.renderer.block.model.BakedQuad;
//import net.minecraft.core.Vec3i;
//
//public class SpecialItemRenderer
//{
//    public static void addQuad(VertexConsumer buffer, PoseStack.Pose matrixEntryIn, BakedQuad quadIn, float redIn, float greenIn, float blueIn, float alphaIn, int combinedLightIn, int combinedOverlayIn)
//    {
//        addQuad(buffer, matrixEntryIn, quadIn, getTempFloat4(1.0F, 1.0F, 1.0F, 1.0F), redIn, greenIn, blueIn, alphaIn, getTempInt4(combinedLightIn, combinedLightIn, combinedLightIn, combinedLightIn), combinedOverlayIn, false);
//    }
//
//    private static void addQuad(VertexConsumer buffer, PoseStack.Pose matrixEntryIn, BakedQuad quadIn, float[] colorMuls, float redIn, float greenIn, float blueIn, float alphaIn, int[] combinedLightsIn, int combinedOverlayIn, boolean mulColor)
//    {
//        int[] aint = buffer.isMultiTexture() ? quadIn.getVertexDataSingle() : quadIn.getVertices();
//        buffer.putSprite(quadIn.getSprite());
//        boolean flag = ModelBlockRenderer.isSeparateAoLightValue();
//        Vec3i vec3i = quadIn.getDirection().getNormal();
//        float f = (float)vec3i.getX();
//        float f1 = (float)vec3i.getY();
//        float f2 = (float)vec3i.getZ();
//        Matrix4f matrix4f = matrixEntryIn.pose();
//        Matrix3f matrix3f = matrixEntryIn.normal();
//        float f3 = matrix3f.getTransformX(f, f1, f2);
//        float f4 = matrix3f.getTransformY(f, f1, f2);
//        float f5 = matrix3f.getTransformZ(f, f1, f2);
//        int i = 8;
//        int j = DefaultVertexFormat.BLOCK.getIntegerSize();
//        int k = aint.length / j;
//
//        for (int l = 0; l < k; ++l)
//        {
//            int i1 = l * j;
//            float f6 = Float.intBitsToFloat(aint[i1 + 0]);
//            float f7 = Float.intBitsToFloat(aint[i1 + 1]);
//            float f8 = Float.intBitsToFloat(aint[i1 + 2]);
//            float f9 = flag ? 1.0F : colorMuls[l];
//            float f10;
//            float f11;
//            float f12;
//
//            if (mulColor)
//            {
//                int j1 = aint[i1 + 3];
//                float f13 = (float)(j1 & 255) / 255.0F;
//                float f14 = (float)(j1 >> 8 & 255) / 255.0F;
//                float f15 = (float)(j1 >> 16 & 255) / 255.0F;
//                f10 = f13 * f9 * redIn;
//                f11 = f14 * f9 * greenIn;
//                f12 = f15 * f9 * blueIn;
//            }
//            else
//            {
//                f10 = f9 * redIn;
//                f11 = f9 * greenIn;
//                f12 = f9 * blueIn;
//            }
//
//            int k1 = combinedLightsIn[l];
//            float f18 = Float.intBitsToFloat(aint[i1 + 4]);
//            float f19 = Float.intBitsToFloat(aint[i1 + 5]);
//            float f20 = matrix4f.getTransformX(f6, f7, f8, 1.0F);
//            float f16 = matrix4f.getTransformY(f6, f7, f8, 1.0F);
//            float f17 = matrix4f.getTransformZ(f6, f7, f8, 1.0F);
//
//            if (flag)
//            {
//                buffer.vertex(f20, f16, f17, f10, f11, f12, colorMuls[l], f18, f19, combinedOverlayIn, k1, f3, f4, f5);
//            }
//            else
//            {
//                buffer.vertex(f20, f16, f17, f10, f11, f12, alphaIn, f18, f19, combinedOverlayIn, k1, f3, f4, f5);
//            }
//        }
//    }
//
//    private static float[] getTempFloat4(float p_getTempFloat4_1_, float p_getTempFloat4_2_, float p_getTempFloat4_3_, float p_getTempFloat4_4_)
//    {
//        return new float[] {p_getTempFloat4_1_, p_getTempFloat4_2_, p_getTempFloat4_3_, p_getTempFloat4_4_};
//    }
//
//    private static int[] getTempInt4(int p_getTempInt4_1_, int p_getTempInt4_2_, int p_getTempInt4_3_, int p_getTempInt4_4_)
//    {
//        return new int[] {p_getTempInt4_1_, p_getTempInt4_2_, p_getTempInt4_3_, p_getTempInt4_4_};
//    }
//}
