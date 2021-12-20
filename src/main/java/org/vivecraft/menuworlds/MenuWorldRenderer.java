package org.vivecraft.menuworlds;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Matrix4f;
import com.mojang.serialization.MapCodec;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL43;
import org.vivecraft.reflection.MCReflection;
import org.vivecraft.render.GLUtils;
import org.vivecraft.settings.VRSettings;

public class MenuWorldRenderer
{
    private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation END_SKY_TEXTURES = new ResourceLocation("textures/environment/end_sky.png");
    private Minecraft mc;
    private DimensionSpecialEffects dimensionInfo;
    private FakeBlockAccess blockAccess;
    private final DynamicTexture lightmapTexture;
    private final NativeImage lightmapColors;
    private final ResourceLocation locationLightMap;
    private boolean lightmapUpdateNeeded;
    private float torchFlickerX;
    private float torchFlickerDX;
    private int counterInWater;
    public long time = 1000L;
    private VertexBuffer[] vertexBuffers;
    private VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;
    private int renderDistance;
    private int renderDistanceChunks;
    public MenuWorldRenderer.MenuCloudRenderer cloudRenderer;
    public MenuWorldRenderer.MenuFogRenderer fogRenderer;
    public Set<TextureAtlasSprite> visibleTextures = new HashSet<>();
    private Random rand;
    private boolean ready;
    private boolean lol;

    public MenuWorldRenderer()
    {
        this.mc = Minecraft.getInstance();
        this.lightmapTexture = new DynamicTexture(16, 16, false);
        this.locationLightMap = this.mc.getTextureManager().register("lightMap", this.lightmapTexture);
        this.lightmapColors = this.lightmapTexture.getPixels();
        Builder<VertexFormatElement> builder = ImmutableList.builder();
        builder.add(new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3));
       // this.vertexBufferFormat = new VertexFormat(builder.build());
        this.cloudRenderer = new MenuWorldRenderer.MenuCloudRenderer(this.mc);
        this.fogRenderer = new MenuWorldRenderer.MenuFogRenderer(this);
        this.rand = new Random();
        this.rand.nextInt();
    }

    public void init()
    {
        if (this.mc.vrSettings.menuWorldSelection == VRSettings.MenuWorld.NONE)
        {
            System.out.println("Main menu worlds disabled.");
            return;
        }

        try
        {
            InputStream inputstream = MenuWorldDownloader.getRandomWorld();

            if (inputstream != null)
            {
                System.out.println("Initializing main menu world renderer...");
                this.loadRenderers();
                System.out.println("Loading world data...");
                this.setWorld(MenuWorldExporter.loadWorld(inputstream));
                System.out.println("Building geometry...");
                this.prepare();
                this.mc.gameRenderer.menuWorldFastTime = (new Random()).nextInt(10) == 0;
            }
            else
            {
                System.out.println("Failed to load any main menu world, falling back to old menu room");
            }
        }
        catch (Throwable throwable)
        {
            if (!(throwable instanceof OutOfMemoryError) && !(throwable.getCause() instanceof OutOfMemoryError))
            {
                System.out.println("Exception thrown when loading main menu world, falling back to old menu room");
            }
            else
            {
                System.out.println("OutOfMemoryError while loading main menu world. Low heap size or 32-bit Java?");
            }

            throwable.printStackTrace();
            this.destroy();
            this.setWorld(null);
        }
    }

    public void render()
    {/*
        this.prepare();
        RenderSystem.shadeModel(7425);
        GL11.glPushClientAttrib(2);
        this.enableLightmap();
        GL43.glPushMatrix();
        AbstractTexture abstracttexture = this.mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        this.mc.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        int i = this.blockAccess.getGround();

        if (this.lol)
        {
            i += 100;
        }

        GL43.glTranslatef((float)(-this.blockAccess.getXSize() / 2), (float)(-i), (float)(-this.blockAccess.getZSize() / 2));
        Matrix4f matrix4f = GLUtils.getViewModelMatrix();
        GlStateManager._disableBlend();
        GlStateManager.disableAlphaTest();
        this.drawBlockLayer(RenderType.solid(), matrix4f);
        GlStateManager.enableAlphaTest();
        abstracttexture.setFilter(false, this.mc.options.mipmapLevels > 0);
        this.drawBlockLayer(RenderType.cutoutMipped(), matrix4f);
        abstracttexture.restoreLastBlurMipmap();
        abstracttexture.setFilter(false, false);
        this.drawBlockLayer(RenderType.cutout(), matrix4f);
        abstracttexture.restoreLastBlurMipmap();
        GlStateManager._enableBlend();
        RenderSystem.depthMask(false);
        this.drawBlockLayer(RenderType.translucent(), matrix4f);
        this.drawBlockLayer(RenderType.tripwire(), matrix4f);
        RenderSystem.depthMask(true);
        DefaultVertexFormat.BLOCK_VANILLA.clearBufferState();
        GL43.glPopMatrix();
        this.disableLightmap();
        GL11.glPopClientAttrib();
    */}

    private void drawBlockLayer(RenderType layer, Matrix4f matrix)
    {/*
        VertexBuffer vertexbuffer = this.vertexBuffers[layer.ordinal()];
        vertexbuffer.bind();
        DefaultVertexFormat.BLOCK_VANILLA.setupBufferState(0L);
        vertexbuffer.draw(matrix, 7);
    */}

    public void prepare()
    {/*
        if (this.vertexBuffers == null)
        {
            AmbientOcclusionStatus ambientocclusionstatus = this.mc.options.ambientOcclusion;
            this.mc.options.ambientOcclusion = AmbientOcclusionStatus.MAX;
            boolean flag = Shaders.shaderPackLoaded;
            Shaders.shaderPackLoaded = false;
            LiquidBlockRenderer.skipStupidGoddamnChunkBoundaryClipping = true;
            DefaultVertexFormat.updateVertexFormats();
            ItemBlockRenderTypes.setFancy(true);
            TextureUtils.resourcesReloaded(Config.getResourceManager());
            this.visibleTextures.clear();
            this.lol = this.rand.nextInt(1000) == 0;

            try
            {
                List<RenderType> list = RenderType.chunkBufferLayers();
                this.vertexBuffers = new VertexBuffer[list.size()];
                Random random = new Random();
                BlockRenderDispatcher blockrenderdispatcher = this.mc.getBlockRenderer();
                PoseStack posestack = new PoseStack();

                for (int i = 0; i < this.vertexBuffers.length; ++i)
                {
                    RenderType rendertype = list.get(i);
                    System.out.println("Layer: " + rendertype.getName());
                    BufferBuilder bufferbuilder = new BufferBuilder(41943040);
                    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK_VANILLA);
                    bufferbuilder.setBlockLayer(rendertype);
                    int j = 0;

                    for (int k = 0; k < this.blockAccess.getXSize(); ++k)
                    {
                        for (int l = 0; l < this.blockAccess.getYSize(); ++l)
                        {
                            for (int i1 = 0; i1 < this.blockAccess.getZSize(); ++i1)
                            {
                                BlockPos blockpos = new BlockPos(k, l, i1);
                                BlockState blockstate = this.blockAccess.getBlockState(blockpos);

                                if (blockstate != null)
                                {
                                    FluidState fluidstate = blockstate.getFluidState();

                                    if (!fluidstate.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidstate) == rendertype && blockrenderdispatcher.renderLiquid(blockpos, this.blockAccess, bufferbuilder, new MenuWorldRenderer.FluidStateWrapper(fluidstate)))
                                    {
                                        ++j;
                                    }

                                    if (blockstate.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.getChunkRenderType(blockstate) == rendertype)
                                    {
                                        posestack.pushPose();
                                        posestack.translate((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());

                                        if (blockrenderdispatcher.renderBatched(blockstate, blockpos, this.blockAccess, posestack, bufferbuilder, true, random))
                                        {
                                            ++j;
                                        }

                                        posestack.popPose();
                                    }
                                }
                            }
                        }
                    }

                    System.out.println("Built " + j + " blocks.");

                    if (rendertype.isNeedsSorting())
                    {
                        bufferbuilder.sortQuads((float)(this.blockAccess.getXSize() / 2), (float)this.blockAccess.getGround(), (float)(this.blockAccess.getXSize() / 2));
                    }

                    bufferbuilder.end();
                    this.vertexBuffers[i] = new VertexBuffer(bufferbuilder.getVertexFormat());
                    this.vertexBuffers[i].upload(bufferbuilder);
                }

                this.copyVisibleTextures();
                this.ready = true;
            }
            finally
            {
                this.mc.options.ambientOcclusion = ambientocclusionstatus;
                LiquidBlockRenderer.skipStupidGoddamnChunkBoundaryClipping = false;

                if (flag)
                {
                    Shaders.shaderPackLoaded = flag;
                    TextureUtils.resourcesReloaded(Config.getResourceManager());
                }

                DefaultVertexFormat.updateVertexFormats();
            }
        }
    */}

    public void destroy()
    {
        if (this.vertexBuffers != null)
        {
            for (VertexBuffer vertexbuffer : this.vertexBuffers)
            {
                if (vertexbuffer != null)
                {
                    vertexbuffer.close();
                }
            }

            this.vertexBuffers = null;
        }

        this.ready = false;
    }

    public void tick()
    {
        this.updateTorchFlicker();

        if (this.areEyesInFluid(FluidTags.WATER))
        {
            int i = 1;
            this.counterInWater = Mth.clamp(this.counterInWater + i, 0, 600);
        }
        else if (this.counterInWater > 0)
        {
            this.areEyesInFluid(FluidTags.WATER);
            this.counterInWater = Mth.clamp(this.counterInWater - 10, 0, 600);
        }
    }

    public FakeBlockAccess getWorld()
    {
        return this.blockAccess;
    }

    public void setWorld(FakeBlockAccess blockAccess)
    {
        this.blockAccess = blockAccess;

        if (blockAccess != null)
        {
            this.dimensionInfo = blockAccess.getDimensionReaderInfo();
            this.lightmapUpdateNeeded = true;
            this.renderDistance = blockAccess.getXSize() / 2;
            this.renderDistanceChunks = this.renderDistance / 16;
        }
    }

    public void loadRenderers() throws Exception
    {
        this.generateSky();
        this.generateSky2();
        this.generateStars();
    }

    public boolean isReady()
    {
        return this.ready;
    }

    private void copyVisibleTextures()
    {
    }

    public void pushVisibleTextures()
    {
    }

    public void renderSky(float x, float y, float z, int pass)
    {/*
        if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.END)
        {
            this.renderSkyEnd();
        }
        else if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.NORMAL)
        {
            GlStateManager._disableTexture();
            Vec3 vec3 = this.getSkyColor(x, y, z);
            float f = (float)vec3.x;
            float f1 = (float)vec3.y;
            float f2 = (float)vec3.z;
            RenderSystem.color3f(f, f1, f2);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            GlStateManager._depthMask(false);
            GlStateManager._enableFog();
            RenderSystem.color3f(f, f1, f2);

            if (Config.isSkyEnabled())
            {
                this.skyVBO.bind();
                GlStateManager._enableClientState(32884);
                GlStateManager._vertexPointer(3, 5126, 12, 0L);
                this.skyVBO.draw(GLUtils.getViewModelMatrix(), 7);
                VertexBuffer.unbind();
                GlStateManager._disableClientState(32884);
            }

            GlStateManager._disableFog();
            GlStateManager.disableAlphaTest();
            GlStateManager._enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Lighting.turnOff();
            float[] afloat = this.dimensionInfo.getSunriseColor(this.getCelestialAngle(), 0.0F);

            if (afloat != null && Config.isSunMoonEnabled())
            {
                GlStateManager._disableTexture();
                GlStateManager._shadeModel(7425);
                GL43.glPushMatrix();
                GlStateManager._rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager._rotatef(Mth.sin(this.getCelestialAngleRadians()) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager._rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                float f3 = afloat[0];
                float f4 = afloat[1];
                float f5 = afloat[2];
                bufferbuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(0.0D, 100.0D, 0.0D).color(f3, f4, f5, afloat[3]).endVertex();
                int i = 16;

                for (int j = 0; j <= 16; ++j)
                {
                    float f6 = (float)j * ((float)Math.PI * 2F) / 16.0F;
                    float f7 = Mth.sin(f6);
                    float f8 = Mth.cos(f6);
                    bufferbuilder.vertex((double)(f7 * 120.0F), (double)(f8 * 120.0F), (double)(-f8 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tesselator.end();
                GL43.glPopMatrix();
                GlStateManager._shadeModel(7424);
            }

            GlStateManager._enableTexture();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GL43.glPushMatrix();
            float f10 = 1.0F;
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, f10);
            GlStateManager._rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager._rotatef(this.getCelestialAngle() * 360.0F, 1.0F, 0.0F, 0.0F);
            float f11 = 30.0F;

            if (Config.isSunTexture())
            {
                this.mc.getTextureManager().bind(SUN_TEXTURES);
                bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex((double)(-f11), 100.0D, (double)(-f11)).uv(0.0F, 0.0F).endVertex();
                bufferbuilder.vertex((double)f11, 100.0D, (double)(-f11)).uv(1.0F, 0.0F).endVertex();
                bufferbuilder.vertex((double)f11, 100.0D, (double)f11).uv(1.0F, 1.0F).endVertex();
                bufferbuilder.vertex((double)(-f11), 100.0D, (double)f11).uv(0.0F, 1.0F).endVertex();
                tesselator.end();
            }

            f11 = 20.0F;

            if (Config.isMoonTexture())
            {
                this.mc.getTextureManager().bind(MOON_PHASES_TEXTURES);
                int k = this.getMoonPhase();
                int l = k % 4;
                int i1 = k / 4 % 2;
                float f13 = (float)(l + 0) / 4.0F;
                float f14 = (float)(i1 + 0) / 2.0F;
                float f15 = (float)(l + 1) / 4.0F;
                float f9 = (float)(i1 + 1) / 2.0F;
                bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex((double)(-f11), -100.0D, (double)f11).uv(f15, f9).endVertex();
                bufferbuilder.vertex((double)f11, -100.0D, (double)f11).uv(f13, f9).endVertex();
                bufferbuilder.vertex((double)f11, -100.0D, (double)(-f11)).uv(f13, f14).endVertex();
                bufferbuilder.vertex((double)(-f11), -100.0D, (double)(-f11)).uv(f15, f14).endVertex();
                tesselator.end();
            }

            GlStateManager._disableTexture();
            float f12 = this.getStarBrightness() * f10;

            if (f12 > 0.0F && Config.isStarsEnabled())
            {
                GlStateManager.color4f(f12, f12, f12, f12);
                this.starVBO.bind();
                GlStateManager._enableClientState(32884);
                GlStateManager._vertexPointer(3, 5126, 12, 0L);
                this.starVBO.draw(GLUtils.getViewModelMatrix(), 7);
                VertexBuffer.unbind();
                GlStateManager._disableClientState(32884);
            }

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager._disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager._enableFog();
            GL43.glPopMatrix();
            GlStateManager._disableTexture();
            RenderSystem.color3f(0.0F, 0.0F, 0.0F);
            double d0 = (double)y - this.blockAccess.getHorizon();

            if (d0 < 0.0D)
            {
                GL43.glPushMatrix();
                GlStateManager._translatef(0.0F, 12.0F, 0.0F);
                this.sky2VBO.bind();
                GlStateManager._enableClientState(32884);
                GlStateManager._vertexPointer(3, 5126, 12, 0L);
                this.sky2VBO.draw(GLUtils.getViewModelMatrix(), 7);
                VertexBuffer.unbind();
                GlStateManager._disableClientState(32884);
                GL43.glPopMatrix();
            }

            if (this.dimensionInfo.hasGround())
            {
                RenderSystem.color3f(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            }
            else
            {
                RenderSystem.color3f(f, f1, f2);
            }

            if (this.renderDistanceChunks <= 4)
            {
                RenderSystem.color3f(this.fogRenderer.red, this.fogRenderer.green, this.fogRenderer.blue);
            }

            GL43.glPushMatrix();
            GlStateManager._translatef(0.0F, -((float)(d0 - 16.0D)), 0.0F);

            if (Config.isSkyEnabled())
            {
                this.sky2VBO.bind();
                GlStateManager._enableClientState(32884);
                GlStateManager._vertexPointer(3, 5126, 12, 0L);
                this.sky2VBO.draw(GLUtils.getViewModelMatrix(), 7);
                VertexBuffer.unbind();
                GlStateManager._disableClientState(32884);
            }

            GL43.glPopMatrix();
            GlStateManager._enableTexture();
            GlStateManager._depthMask(true);
        }
    */}

    private void renderSkyEnd()
    {/*
        if (Config.isSkyEnabled())
        {
            GlStateManager._disableFog();
            GlStateManager.disableAlphaTest();
            GlStateManager._enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Lighting.turnOff();
            GlStateManager._depthMask(false);
            this.mc.getTextureManager().bind(END_SKY_TEXTURES);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            for (int i = 0; i < 6; ++i)
            {
                GL43.glPushMatrix();

                if (i == 1)
                {
                    GlStateManager._rotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2)
                {
                    GlStateManager._rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3)
                {
                    GlStateManager._rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4)
                {
                    GlStateManager._rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5)
                {
                    GlStateManager._rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                int j = 40;
                int k = 40;
                int l = 40;

                if (Config.isCustomColors())
                {
                    Vec3 vec3 = new Vec3((double)j / 255.0D, (double)k / 255.0D, (double)l / 255.0D);
                    j = (int)(vec3.x * 255.0D);
                    k = (int)(vec3.y * 255.0D);
                    l = (int)(vec3.z * 255.0D);
                }

                bufferbuilder.vertex(-100.0D, -100.0D, -100.0D).uv(0.0F, 0.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(-100.0D, -100.0D, 100.0D).uv(0.0F, 16.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(100.0D, -100.0D, 100.0D).uv(16.0F, 16.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(100.0D, -100.0D, -100.0D).uv(16.0F, 0.0F).color(j, k, l, 255).endVertex();
                tesselator.end();
                GL43.glPopMatrix();
            }

            GlStateManager._depthMask(true);
            GlStateManager._enableTexture();
            GlStateManager.enableAlphaTest();
        }
    */}

    public void renderClouds(int pass, double x, double y, double z)
    {/*
        float f = this.dimensionInfo.getCloudHeight();

        if (!Float.isNaN(f))
        {
            Vec3 vec3 = this.getCloudColour();
            this.cloudRenderer.prepareToRender(this.mc.getFrameTime(), vec3);
            GL43.glPushMatrix();
            GlStateManager._disableCull();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            float f1 = 12.0F;
            float f2 = 4.0F;
            double d0 = (double)((float)this.mc.tickCounter + this.mc.getFrameTime());
            double d1 = (x + d0 * (double)0.03F) / 12.0D;
            double d2 = z / 12.0D + (double)0.33F;
            float f3 = f - (float)y + 0.33F;
            f3 = f3 + (float)this.mc.options.ofCloudsHeight * 128.0F;
            int i = Mth.floor(d1 / 2048.0D);
            int j = Mth.floor(d2 / 2048.0D);
            d1 = d1 - (double)(i * 2048);
            d2 = d2 - (double)(j * 2048);
            this.mc.getTextureManager().bind(CLOUDS_TEXTURES);
            GlStateManager._enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            float f4 = (float)vec3.x;
            float f5 = (float)vec3.y;
            float f6 = (float)vec3.z;

            if (pass != 2)
            {
                float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
                float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
                float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
                f4 = f7;
                f5 = f8;
                f6 = f9;
            }

            float f26 = f4 * 0.9F;
            float f27 = f5 * 0.9F;
            float f28 = f6 * 0.9F;
            float f10 = f4 * 0.7F;
            float f11 = f5 * 0.7F;
            float f12 = f6 * 0.7F;
            float f13 = f4 * 0.8F;
            float f14 = f5 * 0.8F;
            float f15 = f6 * 0.8F;
            float f16 = 0.00390625F;
            float f17 = (float)Mth.floor(d1) * 0.00390625F;
            float f18 = (float)Mth.floor(d2) * 0.00390625F;
            float f19 = (float)(d1 - (double)Mth.floor(d1));
            float f20 = (float)(d2 - (double)Mth.floor(d2));
            int k = 8;
            int l = 4;
            float f21 = 9.765625E-4F;
            GlStateManager._scalef(12.0F, 1.0F, 12.0F);

            for (int i1 = 0; i1 < 2; ++i1)
            {
                if (i1 == 0)
                {
                    GlStateManager._colorMask(false, false, false, false);
                }
                else
                {
                    switch (pass)
                    {
                        case 0:
                            GlStateManager._colorMask(false, true, true, true);
                            break;

                        case 1:
                            GlStateManager._colorMask(true, false, false, true);
                            break;

                        case 2:
                            GlStateManager._colorMask(true, true, true, true);
                    }
                }

                this.cloudRenderer.renderGlList((float)x, (float)y, (float)z);
            }

            if (this.cloudRenderer.shouldUpdateGlList((float)y))
            {
                this.cloudRenderer.startUpdateGlList();

                for (int l1 = -3; l1 <= 4; ++l1)
                {
                    for (int j1 = -3; j1 <= 4; ++j1)
                    {
                        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
                        float f22 = (float)(l1 * 8);
                        float f23 = (float)(j1 * 8);
                        float f24 = f22 - f19;
                        float f25 = f23 - f20;

                        if (f3 > -5.0F)
                        {
                            bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        }

                        if (f3 <= 5.0F)
                        {
                            bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 8.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 8.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 0.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                            bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 4.0F - 9.765625E-4F), (double)(f25 + 0.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        }

                        if (l1 > -1)
                        {
                            for (int k1 = 0; k1 < 8; ++k1)
                            {
                                bufferbuilder.vertex((double)(f24 + (float)k1 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).uv((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)k1 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + 8.0F)).uv((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)k1 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + 0.0F)).uv((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)k1 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).uv((f22 + (float)k1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            }
                        }

                        if (l1 <= 1)
                        {
                            for (int i2 = 0; i2 < 8; ++i2)
                            {
                                bufferbuilder.vertex((double)(f24 + (float)i2 + 1.0F - 9.765625E-4F), (double)(f3 + 0.0F), (double)(f25 + 8.0F)).uv((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)i2 + 1.0F - 9.765625E-4F), (double)(f3 + 4.0F), (double)(f25 + 8.0F)).uv((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)i2 + 1.0F - 9.765625E-4F), (double)(f3 + 4.0F), (double)(f25 + 0.0F)).uv((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + (float)i2 + 1.0F - 9.765625E-4F), (double)(f3 + 0.0F), (double)(f25 + 0.0F)).uv((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            }
                        }

                        if (j1 > -1)
                        {
                            for (int j2 = 0; j2 < 8; ++j2)
                            {
                                bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + (float)j2 + 0.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 4.0F), (double)(f25 + (float)j2 + 0.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + (float)j2 + 0.0F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + (float)j2 + 0.0F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            }
                        }

                        if (j1 <= 1)
                        {
                            for (int k2 = 0; k2 < 8; ++k2)
                            {
                                bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 4.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 8.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).uv((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                                bufferbuilder.vertex((double)(f24 + 0.0F), (double)(f3 + 0.0F), (double)(f25 + (float)k2 + 1.0F - 9.765625E-4F)).uv((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            }
                        }

                        tesselator.end();
                    }
                }

                this.cloudRenderer.endUpdateGlList((float)x, (float)y, (float)z);
            }

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager._disableBlend();
            GlStateManager._enableCull();
            GL43.glPopMatrix();
        }
    */}

    public float getCelestialAngle()
    {
        return this.blockAccess.dimensionType().timeOfDay(this.time);
    }

    public float getCelestialAngleRadians()
    {
        float f = this.getCelestialAngle();
        return f * ((float)Math.PI * 2F);
    }

    public int getMoonPhase()
    {
        return this.blockAccess.dimensionType().moonPhase(this.time);
    }

    public float getSunBrightness()
    {
        float f = this.getCelestialAngle();
        float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * 1.0D);
        f1 = (float)((double)f1 * 1.0D);
        return f1 * 0.8F + 0.2F;
    }

    public float getStarBrightness()
    {
        float f = this.getCelestialAngle();
        float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    public Vec3 getSkyColor(float x, float y, float z)
    {
        float f = this.getCelestialAngle();
        float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        int i = Mth.floor(x);
        int j = Mth.floor(y);
        int k = Mth.floor(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        Biome biome = this.blockAccess.getBiome(blockpos);
        int l = biome.getSkyColor();
        float f2 = (float)(l >> 16 & 255) / 255.0F;
        float f3 = (float)(l >> 8 & 255) / 255.0F;
        float f4 = (float)(l & 255) / 255.0F;
        f2 = f2 * f1;
        f3 = f3 * f1;
        f4 = f4 * f1;
        return new Vec3((double)f2, (double)f3, (double)f4);
    }

    public Vec3 getSkyColor(Vec3 pos)
    {
        return this.getSkyColor((float)pos.x, (float)pos.y, (float)pos.z);
    }

    public Vec3 getFogColor(Vec3 pos)
    {
        float f = Mth.clamp(Mth.cos(this.getCelestialAngle() * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        Vec3 vec3 = pos.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        return CubicSampler.gaussianSampleVec3(vec3, (x, y, z) ->
        {
            return this.dimensionInfo.getBrightnessDependentFogColor(Vec3.fromRGB24(this.blockAccess.getBiomeManager().getNoiseBiomeAtQuart(x, y, z).getFogColor()), f);
        });
    }

    public Vec3 getCloudColour()
    {
        float f = this.getCelestialAngle();
        float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        float f2 = 1.0F;
        float f3 = 1.0F;
        float f4 = 1.0F;
        f2 = f2 * (f1 * 0.9F + 0.1F);
        f3 = f3 * (f1 * 0.9F + 0.1F);
        f4 = f4 * (f1 * 0.85F + 0.15F);
        return new Vec3((double)f2, (double)f3, (double)f4);
    }

    private void generateSky() throws Exception
    {/*
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        if (this.skyVBO != null)
        {
            this.skyVBO.close();
        }

        this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderSky(bufferbuilder, 16.0F, false);
        bufferbuilder.end();
        this.skyVBO.upload(bufferbuilder);
    */}

    private void generateSky2() throws Exception
    {/*
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        if (this.sky2VBO != null)
        {
            this.sky2VBO.close();
        }

        this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderSky(bufferbuilder, -16.0F, true);
        bufferbuilder.end();
        this.sky2VBO.upload(bufferbuilder);
    */}

    private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX)
    {/*
        int i = 64;
        int j = 6;
        bufferBuilderIn.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        int k = (this.renderDistance / 64 + 1) * 64 + 64;

        for (int l = -k; l <= k; l += 64)
        {
            for (int i1 = -k; i1 <= k; i1 += 64)
            {
                float f = (float)l;
                float f1 = (float)(l + 64);

                if (reverseX)
                {
                    f1 = (float)l;
                    f = (float)(l + 64);
                }

                bufferBuilderIn.vertex((double)f, (double)posY, (double)i1).endVertex();
                bufferBuilderIn.vertex((double)f1, (double)posY, (double)i1).endVertex();
                bufferBuilderIn.vertex((double)f1, (double)posY, (double)(i1 + 64)).endVertex();
                bufferBuilderIn.vertex((double)f, (double)posY, (double)(i1 + 64)).endVertex();
            }
        }
    */}

    private void generateStars() throws Exception
    {/*
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        if (this.starVBO != null)
        {
            this.starVBO.close();
        }

        this.starVBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderStars(bufferbuilder);
        bufferbuilder.end();
        this.starVBO.upload(bufferbuilder);
    */}

    private void renderStars(BufferBuilder bufferBuilderIn)
    {
        Random random = new Random(10842L);
        bufferBuilderIn.begin(Mode.QUADS, DefaultVertexFormat.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d17 = 0.0D;
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d20 = 0.0D;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    bufferBuilderIn.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    public void disableLightmap()
    {
        GlStateManager._activeTexture(33986);
        GlStateManager._disableTexture();
        GlStateManager._activeTexture(33984);
    }

    public void enableLightmap()
    {/*
        GlStateManager._activeTexture(33986);
        GL43.glMatrixMode(5890);
        GL43.glLoadIdentity();
        float f = 0.00390625F;
        GlStateManager._scalef(f, f, f);
        GlStateManager._translatef(8.0F, 8.0F, 8.0F);
        GL43.glMatrixMode(5888);
        this.mc.getTextureManager().bind(this.locationLightMap);
        GlStateManager._texParameter(3553, 10241, 9729);
        GlStateManager._texParameter(3553, 10240, 9729);
        GlStateManager._texParameter(3553, 10242, 33071);
        GlStateManager._texParameter(3553, 10243, 33071);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager._enableTexture();
        GlStateManager._activeTexture(33984);
    */}

    public void updateTorchFlicker()
    {
        this.torchFlickerDX = (float)((double)this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.torchFlickerDX = (float)((double)this.torchFlickerDX * 0.9D);
        this.torchFlickerX += this.torchFlickerDX - this.torchFlickerX;
        this.lightmapUpdateNeeded = true;
    }

    public void updateLightmap()
    {
        if (this.lightmapUpdateNeeded)
        {
            float f = this.getSunBrightness();
            float f1 = f * 0.95F + 0.05F;
            float f2 = 0.0F;

            for (int i = 0; i < 16; ++i)
            {
                for (int j = 0; j < 16; ++j)
                {
                    float f3 = this.blockAccess.dimensionType().brightness(i) * f1;
                    float f4 = this.blockAccess.dimensionType().brightness(j) * (this.torchFlickerX * 0.1F + 1.5F);
                    float f5 = f3 * (f * 0.65F + 0.35F);
                    float f6 = f3 * (f * 0.65F + 0.35F);
                    float f7 = f4 * ((f4 * 0.6F + 0.4F) * 0.6F + 0.4F);
                    float f8 = f4 * (f4 * f4 * 0.6F + 0.4F);
                    float f9 = f5 + f4;
                    float f10 = f6 + f7;
                    float f11 = f3 + f8;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;
                    f11 = f11 * 0.96F + 0.03F;

                    if (this.dimensionInfo.forceBrightLightmap())
                    {
                        f9 = 0.22F + f4 * 0.75F;
                        f10 = 0.28F + f7 * 0.75F;
                        f11 = 0.25F + f8 * 0.75F;
                    }

                    if (f2 > 0.0F)
                    {
                        float f12 = 1.0F / f9;

                        if (f12 > 1.0F / f10)
                        {
                            f12 = 1.0F / f10;
                        }

                        if (f12 > 1.0F / f11)
                        {
                            f12 = 1.0F / f11;
                        }

                        f9 = f9 * (1.0F - f2) + f9 * f12 * f2;
                        f10 = f10 * (1.0F - f2) + f10 * f12 * f2;
                        f11 = f11 * (1.0F - f2) + f11 * f12 * f2;
                    }

                    if (f9 > 1.0F)
                    {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F)
                    {
                        f10 = 1.0F;
                    }

                    if (f11 > 1.0F)
                    {
                        f11 = 1.0F;
                    }

                    float f16 = (float)this.mc.options.gamma;
                    float f13 = 1.0F - f9;
                    float f14 = 1.0F - f10;
                    float f15 = 1.0F - f11;
                    f13 = 1.0F - f13 * f13 * f13 * f13;
                    f14 = 1.0F - f14 * f14 * f14 * f14;
                    f15 = 1.0F - f15 * f15 * f15 * f15;
                    f9 = f9 * (1.0F - f16) + f13 * f16;
                    f10 = f10 * (1.0F - f16) + f14 * f16;
                    f11 = f11 * (1.0F - f16) + f15 * f16;
                    f9 = f9 * 0.96F + 0.03F;
                    f10 = f10 * 0.96F + 0.03F;
                    f11 = f11 * 0.96F + 0.03F;

                    if (f9 > 1.0F)
                    {
                        f9 = 1.0F;
                    }

                    if (f10 > 1.0F)
                    {
                        f10 = 1.0F;
                    }

                    if (f11 > 1.0F)
                    {
                        f11 = 1.0F;
                    }

                    if (f9 < 0.0F)
                    {
                        f9 = 0.0F;
                    }

                    if (f10 < 0.0F)
                    {
                        f10 = 0.0F;
                    }

                    if (f11 < 0.0F)
                    {
                        f11 = 0.0F;
                    }

                    int k = 255;
                    int l = (int)(f9 * 255.0F);
                    int i1 = (int)(f10 * 255.0F);
                    int j1 = (int)(f11 * 255.0F);
                    this.lightmapColors.setPixelRGBA(j, i, -16777216 | j1 << 16 | i1 << 8 | l);
                }
            }

            this.lightmapTexture.upload();
            this.lightmapUpdateNeeded = false;
        }
    }

    public void setupFogColor(boolean black)
    {
        this.fogRenderer.applyFog(black);
    }

    public float getWaterBrightness()
    {
        if (!this.areEyesInFluid(FluidTags.WATER))
        {
            return 0.0F;
        }
        else
        {
            float f = 600.0F;
            float f1 = 100.0F;

            if ((float)this.counterInWater >= 600.0F)
            {
                return 1.0F;
            }
            else
            {
                float f2 = Mth.clamp((float)this.counterInWater / 100.0F, 0.0F, 1.0F);
                float f3 = (float)this.counterInWater < 100.0F ? 0.0F : Mth.clamp(((float)this.counterInWater - 100.0F) / 500.0F, 0.0F, 1.0F);
                return f2 * 0.6F + f3 * 0.39999998F;
            }
        }
    }

    public boolean areEyesInFluid(Tag<Fluid> tagIn)
    {
        if (this.blockAccess == null)
        {
            return false;
        }
        else
        {
            Vec3 vec3 = this.getEyePos();
            BlockPos blockpos = new BlockPos(vec3);
            FluidState fluidstate = this.blockAccess.getFluidState(blockpos);
            return this.isFluidTagged(fluidstate, tagIn) && vec3.y < (double)((float)blockpos.getY() + (float)fluidstate.getAmount() + 0.11111111F);
        }
    }

    public Vec3 getEyePos()
    {
        Vec3 vec3 = this.mc.vrPlayer.vrdata_room_post.hmd.getPosition();
        return this.blockAccess == null ? vec3 : new Vec3(vec3.x + (double)(this.blockAccess.getXSize() / 2), vec3.y + (double)this.blockAccess.getGround(), vec3.z + (double)(this.blockAccess.getZSize() / 2));
    }

    private boolean isFluidTagged(Fluid fluid, Tag<Fluid> tag)
    {
        if (tag == FluidTags.WATER)
        {
            return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
        }
        else if (tag != FluidTags.LAVA)
        {
            return false;
        }
        else
        {
            return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
        }
    }

    private boolean isFluidTagged(FluidState fluidState, Tag<Fluid> tag)
    {
        return this.isFluidTagged(fluidState.getType(), tag);
    }

    private static class FluidStateWrapper extends FluidState
    {
        private final FluidState fluidState;

        public FluidStateWrapper(FluidState fluidState)
        {
            super(fluidState.getType(), fluidState.getValues(), (MapCodec)MCReflection.StateHolder_mapCodec.get(fluidState));
            this.fluidState = fluidState;
        }

        public boolean is(Tag<Fluid> pTag)
        {
            if (pTag == FluidTags.WATER)
            {
                return this.getType() == Fluids.WATER || this.getType() == Fluids.FLOWING_WATER;
            }
            else if (pTag != FluidTags.LAVA)
            {
                return this.fluidState.is(pTag);
            }
            else
            {
                return this.getType() == Fluids.LAVA || this.getType() == Fluids.FLOWING_LAVA;
            }
        }
    }

    public static class MenuCloudRenderer
    {
        private Minecraft mc;
        private boolean updated = false;
        float partialTicks;
        private int glListClouds = -1;
        private int cloudTickCounterUpdate = 0;
        private double cloudPlayerX = 0.0D;
        private double cloudPlayerY = 0.0D;
        private double cloudPlayerZ = 0.0D;
        private Vec3 color;
        private Vec3 lastColor;

        public MenuCloudRenderer(Minecraft p_i23_1_)
        {
            this.mc = p_i23_1_;
            //this.glListClouds = GLUtils.generateDisplayLists(1);
        }

        public void prepareToRender(float partialTicks, Vec3 color)
        {
            this.partialTicks = partialTicks;
            this.lastColor = this.color;
            this.color = color;
        }

        public boolean shouldUpdateGlList(float posY)
        {
            if (!this.updated)
            {
                return true;
            }
            else if (this.mc.tickCounter >= this.cloudTickCounterUpdate + 100)
            {
                return true;
            }
            else if (!this.color.equals(this.lastColor) && this.mc.tickCounter >= this.cloudTickCounterUpdate + 1)
            {
                return true;
            }
            else
            {
                boolean flag = this.cloudPlayerY < 128.0D + this.mc.options.ofCloudsHeight * 128.0D;
                boolean flag1 = (double)posY < 128.0D + this.mc.options.ofCloudsHeight * 128.0D;
                return flag1 != flag;
            }
        }

        public void startUpdateGlList()
        {
            GL11.glNewList(this.glListClouds, GL11.GL_COMPILE);
        }

        public void endUpdateGlList(float x, float y, float z)
        {/*
            GL11.glEndList();
            this.cloudTickCounterUpdate = this.mc.tickCounter;
            this.cloudPlayerX = (double)x;
            this.cloudPlayerY = (double)y;
            this.cloudPlayerZ = (double)z;
            this.updated = true;
            GlStateManager._clearCurrentColor();
        */}

        public void renderGlList(float x, float y, float z)
        {/*
            double d0 = (double)((float)(this.mc.tickCounter - this.cloudTickCounterUpdate) + this.partialTicks);
            float f = (float)((double)x - this.cloudPlayerX + d0 * 0.03D);
            float f1 = (float)((double)y - this.cloudPlayerY);
            float f2 = (float)((double)z - this.cloudPlayerZ);
            GL43.glPushMatrix();
            GlStateManager._translatef(-f / 12.0F, -f1, -f2 / 12.0F);
            GL12.glCallList(this.glListClouds);
            GL43.glPopMatrix();
            GlStateManager._clearCurrentColor();
        */}

        public void reset()
        {
            this.updated = false;
        }
    }

    public static class MenuFogRenderer
    {
        private final float[] blackBuffer = new float[4];
        private final float[] buffer = new float[4];
        public float red;
        public float green;
        public float blue;
        private float lastRed = -1.0F;
        private float lastGreen = -1.0F;
        private float lastBlue = -1.0F;
        private int lastWaterFogColor = -1;
        private int waterFogColor = -1;
        private long waterFogUpdateTime = -1L;
        private MenuWorldRenderer menuWorldRenderer;
        private Minecraft mc;

        public MenuFogRenderer(MenuWorldRenderer menuWorldRenderer)
        {
            this.menuWorldRenderer = menuWorldRenderer;
            this.mc = Minecraft.getInstance();
            this.blackBuffer[0] = 0.0F;
            this.blackBuffer[1] = 0.0F;
            this.blackBuffer[2] = 0.0F;
            this.blackBuffer[3] = 1.0F;
        }

        public void updateFogColor()
        {
            Vec3 vec3 = this.menuWorldRenderer.getEyePos();

            if (this.menuWorldRenderer.areEyesInFluid(FluidTags.WATER))
            {
                this.updateWaterFog(this.menuWorldRenderer.getWorld());
            }
            else if (this.menuWorldRenderer.areEyesInFluid(FluidTags.LAVA))
            {
                this.red = 0.6F;
                this.green = 0.1F;
                this.blue = 0.0F;
                this.waterFogUpdateTime = -1L;
            }
            else
            {
                this.updateSurfaceFog();
                this.waterFogUpdateTime = -1L;
            }

            double d0 = vec3.y * this.menuWorldRenderer.getWorld().getVoidFogYFactor();

            if (d0 < 1.0D)
            {
                if (d0 < 0.0D)
                {
                    d0 = 0.0D;
                }

                d0 = d0 * d0;
                this.red = (float)((double)this.red * d0);
                this.green = (float)((double)this.green * d0);
                this.blue = (float)((double)this.blue * d0);
            }

            if (this.menuWorldRenderer.areEyesInFluid(FluidTags.WATER))
            {
                float f = this.menuWorldRenderer.getWaterBrightness();
                float f1 = 1.0F / this.red;

                if (f1 > 1.0F / this.green)
                {
                    f1 = 1.0F / this.green;
                }

                if (f1 > 1.0F / this.blue)
                {
                    f1 = 1.0F / this.blue;
                }

                this.red = this.red * (1.0F - f) + this.red * f1 * f;
                this.green = this.green * (1.0F - f) + this.green * f1 * f;
                this.blue = this.blue * (1.0F - f) + this.blue * f1 * f;
            }

            GlStateManager._clearColor(this.red, this.green, this.blue, 0.0F);
        }

        private void updateSurfaceFog()
        {
            float f = 0.25F + 0.75F * (float)this.menuWorldRenderer.renderDistanceChunks / 32.0F;
            f = 1.0F - (float)Math.pow((double)f, 0.25D);
            Vec3 vec3 = this.menuWorldRenderer.getEyePos();
            Vec3 vec31 = this.menuWorldRenderer.getSkyColor(vec3);
            float f1 = (float)vec31.x;
            float f2 = (float)vec31.y;
            float f3 = (float)vec31.z;
            Vec3 vec32 = this.menuWorldRenderer.getFogColor(vec3);
            this.red = (float)vec32.x;
            this.green = (float)vec32.y;
            this.blue = (float)vec32.z;

            if (this.menuWorldRenderer.renderDistanceChunks >= 4)
            {
                double d0 = Mth.sin(this.menuWorldRenderer.getCelestialAngleRadians()) > 0.0F ? -1.0D : 1.0D;
                Vec3 vec33 = new Vec3(d0, 0.0D, 0.0D);
                float f4 = (float)this.mc.vrPlayer.vrdata_room_post.hmd.getDirection().dot(vec33);

                if (f4 < 0.0F)
                {
                    f4 = 0.0F;
                }

                if (f4 > 0.0F)
                {
                    float[] afloat = this.menuWorldRenderer.dimensionInfo.getSunriseColor(this.menuWorldRenderer.getCelestialAngle(), 0.0F);

                    if (afloat != null)
                    {
                        f4 = f4 * afloat[3];
                        this.red = this.red * (1.0F - f4) + afloat[0] * f4;
                        this.green = this.green * (1.0F - f4) + afloat[1] * f4;
                        this.blue = this.blue * (1.0F - f4) + afloat[2] * f4;
                    }
                }
            }

            this.red += (f1 - this.red) * f;
            this.green += (f2 - this.green) * f;
            this.blue += (f3 - this.blue) * f;
        }

        private void updateWaterFog(LevelReader worldIn)
        {
            long i = Util.getMillis();
            int j = worldIn.getBiome(new BlockPos(this.menuWorldRenderer.getEyePos())).getWaterFogColor();

            if (this.waterFogUpdateTime < 0L)
            {
                this.lastWaterFogColor = j;
                this.waterFogColor = j;
                this.waterFogUpdateTime = i;
            }

            int k = this.lastWaterFogColor >> 16 & 255;
            int l = this.lastWaterFogColor >> 8 & 255;
            int i1 = this.lastWaterFogColor & 255;
            int j1 = this.waterFogColor >> 16 & 255;
            int k1 = this.waterFogColor >> 8 & 255;
            int l1 = this.waterFogColor & 255;
            float f = Mth.clamp((float)(i - this.waterFogUpdateTime) / 5000.0F, 0.0F, 1.0F);
            float f1 = (float)j1 + (float)(k - j1) * f;
            float f2 = (float)k1 + (float)(l - k1) * f;
            float f3 = (float)l1 + (float)(i1 - l1) * f;
            this.red = f1 / 255.0F;
            this.green = f2 / 255.0F;
            this.blue = f3 / 255.0F;

            if (this.lastWaterFogColor != j)
            {
                this.lastWaterFogColor = j;
                this.waterFogColor = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
                this.waterFogUpdateTime = i;
            }
        }

        public void setupFog(int startCoords)
        {/*
            this.applyFog(false);
            float f = (float)this.menuWorldRenderer.renderDistance;
            Vec3 vec3 = this.menuWorldRenderer.getEyePos();
            GlStateManager._normal3f(0.0F, -1.0F, 0.0F);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f1 = -1.0F;

            if (f1 >= 0.0F)
            {
                GlStateManager._fogDensity(f1);
            }
            else if (this.menuWorldRenderer.areEyesInFluid(FluidTags.WATER))
            {
                RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
                float f2 = 0.05F - this.menuWorldRenderer.getWaterBrightness() * this.menuWorldRenderer.getWaterBrightness() * 0.03F;
                Biome biome = this.menuWorldRenderer.getWorld().getBiome(new BlockPos(vec3));

                if (biome.getBiomeCategory() == Biome.BiomeCategory.SWAMP)
                {
                    f2 += 0.005F;
                }

                GlStateManager._fogDensity(f2);
            }
            else if (this.menuWorldRenderer.areEyesInFluid(FluidTags.LAVA))
            {
                RenderSystem.fogMode(GlStateManager.FogMode.EXP);
                GlStateManager._fogDensity(2.0F);
            }
            else
            {
                RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);

                if (startCoords == -1)
                {
                    GlStateManager._fogStart(0.0F);
                    GlStateManager._fogEnd(f);
                }
                else
                {
                    GlStateManager._fogStart(f * Config.getFogStart());
                    GlStateManager._fogEnd(f);
                }

                if (GL.getCapabilities().GL_NV_fog_distance)
                {
                    GlStateManager._fogi(34138, 34139);
                }

                if (this.menuWorldRenderer.dimensionInfo.isFoggyAt((int)vec3.x, (int)vec3.z))
                {
                    GlStateManager._fogStart(f * 0.05F);
                    GlStateManager._fogEnd(f);
                }
            }

            GlStateManager._enableColorMaterial();
            GlStateManager._enableFog();
            GlStateManager._colorMaterial(1028, 4608);
        */}

        public void applyFog(boolean blackIn)
        {/*
            if (blackIn)
            {
                GlStateManager.m_84273_(2918, this.blackBuffer);
            }
            else
            {
                GlStateManager.m_84273_(2918, this.getFogBuffer());
            }
        */}

        private float[] getFogBuffer()
        {
            if (this.lastRed != this.red || this.lastGreen != this.green || this.lastBlue != this.blue)
            {
                this.buffer[0] = this.red;
                this.buffer[1] = this.green;
                this.buffer[2] = this.blue;
                this.buffer[3] = 1.0F;
                this.lastRed = this.red;
                this.lastGreen = this.green;
                this.lastBlue = this.blue;

                if (Config.isShaders())
                {
                    Shaders.setFogColor(this.red, this.green, this.blue);
                }
            }

            return this.buffer;
        }
    }
}
