//package org.vivecraft.render;
//
//import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Supplier;
//import net.minecraft.client.renderer.RenderStateShard;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.resources.ResourceLocation;
//import net.optifine.util.CompoundKey;
//
//public abstract class VRRenderType extends RenderStateShard
//{
//    private static Map<CompoundKey, RenderType> RENDER_TYPES;
//
//    public VRRenderType(String p_110161_, Runnable p_110162_, Runnable p_110163_)
//    {
//        super(p_110161_, p_110162_, p_110163_);
//    }
//
//    private static RenderType getRenderType(String p_getRenderType_0_, ResourceLocation p_getRenderType_1_, Supplier<RenderType> p_getRenderType_2_)
//    {
//        CompoundKey compoundkey = new CompoundKey(p_getRenderType_0_, p_getRenderType_1_);
//        return getRenderType(compoundkey, p_getRenderType_2_);
//    }
//
//    private static RenderType getRenderType(CompoundKey p_getRenderType_0_, Supplier<RenderType> p_getRenderType_1_)
//    {
//        if (RENDER_TYPES == null)
//        {
//            RENDER_TYPES = new HashMap<>();
//        }
//
//        RenderType rendertype = RENDER_TYPES.get(p_getRenderType_0_);
//
//        if (rendertype != null)
//        {
//            return rendertype;
//        }
//        else
//        {
//            rendertype = p_getRenderType_1_.get();
//            RENDER_TYPES.put(p_getRenderType_0_, rendertype);
//            return rendertype;
//        }
//    }
//
////    public static RenderType getTextNoCull(ResourceLocation locationIn)
////    {
////        return getRenderType("text_nocull", locationIn, () ->
////        {
////            return RenderType.create("text_nocull", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false)).setAlphaState(DEFAULT_ALPHA).setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setCullState(NO_CULL).createCompositeState(false));
////        });
////    }
//}
