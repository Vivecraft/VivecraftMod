package org.vivecraft.client_vr.menuworlds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.Util;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.BufferBuilderExtension;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mixin.client.renderer.RenderStateShardAccessor;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MenuWorldRenderer {
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation CLOUDS_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/clouds.png");
    private static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");

    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");

    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");

    private final Minecraft mc;
    private DimensionSpecialEffects dimensionInfo;
    private FakeBlockAccess blockAccess;
    private final DynamicTexture lightTexture;
    private final NativeImage lightPixels;
    private final ResourceLocation lightTextureLocation;
    private boolean lightmapUpdateNeeded;
    private float blockLightRedFlicker;
    private int waterVisionTime;

    public int ticks = 0;
    public long time = 1000;
    public boolean fastTime;
    private HashMap<RenderType, List<VertexBuffer>> vertexBuffers;
    private VertexBuffer starVBO;
    private VertexBuffer skyBuffer;
    private VertexBuffer darkBuffer;
    private VertexBuffer cloudVBO;
    private int renderDistance;
    private int renderDistanceChunks;
    public MenuFogRenderer fogRenderer;
    public Set<TextureAtlasSprite> animatedSprites;
    private final Random rand;
    private boolean ready;
    private CloudStatus prevCloudsType;
    private int prevCloudX;
    private int prevCloudY;
    private int prevCloudZ;
    private Vec3 prevCloudColor;
    private boolean generateClouds = true;
    private int skyFlashTime;
    private float rainLevel;
    private float thunderLevel;

    private float worldRotation;

    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    private CompletableFuture<FakeBlockAccess> getWorldTask;

    public int renderMaxTime = 40;
    public Vec3i segmentSize = new Vec3i(64, 64, 64);

    private boolean building = false;
    private boolean shadersEnabled = false;
    private long buildStartTime;
    private Map<Pair<RenderType, BlockPos>, BufferBuilder> bufferBuilders;
    private Map<Pair<RenderType, BlockPos>, BlockPos.MutableBlockPos> currentPositions;
    private Map<Pair<RenderType, BlockPos>, Integer> blockCounts;
    private Map<Pair<RenderType, BlockPos>, Long> renderTimes;
    private final List<CompletableFuture<Void>> builderFutures = new ArrayList<>();
    private final Queue<Thread> builderThreads = new ConcurrentLinkedQueue<>();
    private Throwable builderError;

    private static boolean firstRenderDone;

    public MenuWorldRenderer() {
        this.mc = Minecraft.getInstance();
        this.lightTexture = new DynamicTexture(16, 16, false);
        this.lightTextureLocation = mc.getTextureManager().register("vivecraft_light_map", this.lightTexture);
        this.lightPixels = this.lightTexture.getPixels();
        this.fogRenderer = new MenuFogRenderer(this);
        this.rand = new Random();
        this.rand.nextInt(); // toss some bits in the bin
    }

    public void init() {
        if (ClientDataHolderVR.getInstance().vrSettings.menuWorldSelection == VRSettings.MenuWorld.NONE) {
            //VRSettings.logger.info("Main menu worlds disabled.");
            return;
        }

        try {
            VRSettings.logger.info("MenuWorlds: Initializing main menu world renderer...");
            loadRenderers();
            getWorldTask = CompletableFuture.supplyAsync(() -> {
                try (InputStream inputStream = MenuWorldDownloader.getRandomWorld()) {
                    VRSettings.logger.info("MenuWorlds: Loading world data...");
                    return inputStream != null ? MenuWorldExporter.loadWorld(inputStream) : null;
                } catch (Exception e) {
                    VRSettings.logger.error("Exception thrown when loading main menu world, falling back to old menu room. \n {}", e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }, Util.backgroundExecutor());
        } catch (Exception e) {
            VRSettings.logger.error("Exception thrown when initializing main menu world renderer, falling back to old menu room. \n {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkTask() {
        if (getWorldTask == null || !getWorldTask.isDone()) {
            return;
        }

        try {
            FakeBlockAccess world = getWorldTask.get();
            if (world != null) {
                setWorld(world);
                prepare();
            } else {
                VRSettings.logger.warn("Failed to load any main menu world, falling back to old menu room");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getWorldTask = null;
        }
    }

    public void render(Matrix4fStack poseStack) {

        // temporarily disable fabulous to render the menu world
        GraphicsStatus current = mc.options.graphicsMode().get();
        if (current == GraphicsStatus.FABULOUS) {
            mc.options.graphicsMode().set(GraphicsStatus.FANCY);
        }

        turnOnLightLayer();

        poseStack.pushMatrix();

        //rotate World
        poseStack.rotate(Axis.YP.rotationDegrees(worldRotation));

        // small offset to center on source block, and add the partial block offset, this shouldn't be too noticable on the fog
        poseStack.translate(-0.5F, -blockAccess.getGround() + (int) blockAccess.getGround(), -0.5F);

        // not sure why this needs to be rotated twice, but it works
        Vec3 offset = new Vec3(0.5, -blockAccess.getGround() + (int) blockAccess.getGround(), 0.5).yRot(worldRotation * 0.0174533f);
        Vec3 eyePosition = getEyePos().add(offset).yRot(-worldRotation * 0.0174533f);

        fogRenderer.levelFogColor();

        renderSky(poseStack, eyePosition);

        fogRenderer.setupFog(FogRenderer.FogMode.FOG_TERRAIN);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();

        Matrix4f projection = RenderSystem.getProjectionMatrix();

        RenderSystem.disableBlend();

        renderChunkLayer(RenderType.solid(), poseStack, projection);
        renderChunkLayer(RenderType.cutoutMipped(), poseStack, projection);
        renderChunkLayer(RenderType.cutout(), poseStack, projection);

        RenderSystem.enableBlend();

        float cloudHeight = this.dimensionInfo.getCloudHeight();
        if (OptifineHelper.isOptifineLoaded()) {
            cloudHeight += OptifineHelper.getCloudHeight() * 128.0;
        }

        if (eyePosition.y + blockAccess.getGround() + blockAccess.getMinBuildHeight() < cloudHeight) {
            renderClouds(poseStack, eyePosition.x, eyePosition.y + blockAccess.getGround() + blockAccess.getMinBuildHeight(), eyePosition.z);
        }

        renderChunkLayer(RenderType.translucent(), poseStack, projection);
        renderChunkLayer(RenderType.tripwire(), poseStack, projection);

        if (eyePosition.y + blockAccess.getGround() + blockAccess.getMinBuildHeight() >= cloudHeight) {
            renderClouds(poseStack, eyePosition.x, eyePosition.y + blockAccess.getGround() + blockAccess.getMinBuildHeight(), eyePosition.z);
        }

        RenderSystem.depthMask(false);
        renderSnowAndRain(poseStack, eyePosition.x, 0, eyePosition.z);
        RenderSystem.depthMask(true);

        poseStack.popMatrix();
        turnOffLightLayer();
        mc.options.graphicsMode().set(current);
    }

    private void renderChunkLayer(RenderType layer, Matrix4f modelView, Matrix4f Projection) {
        List<VertexBuffer> buffers = vertexBuffers.get(layer);
        if (buffers.size() == 0) {
            return;
        }

        layer.setupRenderState();
        ShaderInstance shaderInstance = RenderSystem.getShader();
        shaderInstance.apply();
        turnOnLightLayer();
        for (VertexBuffer vertexBuffer : buffers) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(modelView, Projection, shaderInstance);
        }
        turnOffLightLayer();
    }

    public void prepare() {
        if (vertexBuffers == null && !building) {
            VRSettings.logger.info("MenuWorlds: Building geometry...");

            // random offset to make the player fly
            if (rand.nextInt(1000) == 0) {
                blockAccess.setGroundOffset(100);
            }
            fastTime = new Random().nextInt(10) == 0;

            animatedSprites = ConcurrentHashMap.newKeySet();
            blockCounts = new ConcurrentHashMap<>();
            renderTimes = new ConcurrentHashMap<>();
            if (IrisHelper.isIrisLoaded() && IrisHelper.isShaderActive()) {
                shadersEnabled = true;
                mc.gui.getChat().addMessage(Component.literal("Vivecraft: temporarily disabling shaders to build Menuworld"));
                IrisHelper.toggleShaders(mc, false);
            }

            try {
                vertexBuffers = new HashMap<>();
                bufferBuilders = new HashMap<>();
                currentPositions = new HashMap<>();

                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    vertexBuffers.put(layer, new LinkedList<>());

                    for (int x = -blockAccess.getXSize() / 2; x < blockAccess.getXSize() / 2; x += segmentSize.getX()) {
                        for (int y = (int) -blockAccess.getGround(); y < blockAccess.getYSize() - (int) blockAccess.getGround(); y += segmentSize.getY()) {
                            for (int z = -blockAccess.getZSize() / 2; z < blockAccess.getZSize() / 2; z += segmentSize.getZ()) {
                                BlockPos pos = new BlockPos(x, y, z);
                                Pair<RenderType, BlockPos> pair = Pair.of(layer, pos);

                                // 32768 yields most efficient memory use for some reason
                                BufferBuilder vertBuffer = new BufferBuilder(new ByteBufferBuilder(32768), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

                                bufferBuilders.put(pair, vertBuffer);
                                currentPositions.put(pair, pos.mutable());
                            }
                        }
                    }
                }
            } catch (OutOfMemoryError e) {
                VRSettings.logger.error("OutOfMemoryError while building main menu world. Low system memory or 32-bit Java?");
                destroy();
                return;
            }

            buildStartTime = Utils.milliTime();
            building = true;
        }
    }

    public boolean isBuilding() {
        return building;
    }

    public void buildNext() {
        if (!builderFutures.stream().allMatch(CompletableFuture::isDone) || builderError != null) {
            return;
        }
        builderFutures.clear();

        if (currentPositions.entrySet().stream().allMatch(entry -> entry.getValue().getY() >= Math.min(segmentSize.getY() + entry.getKey().getRight().getY(), blockAccess.getYSize() - (int) blockAccess.getGround()))) {
            finishBuilding();
            return;
        }

        long startTime = Utils.milliTime();
        for (var pair : bufferBuilders.keySet()) {
            if (currentPositions.get(pair).getY() < Math.min(segmentSize.getY() + pair.getRight().getY(), blockAccess.getYSize() - (int) blockAccess.getGround())) {
                if (firstRenderDone || !SodiumHelper.isLoaded() || !SodiumHelper.hasIssuesWithParallelBlockBuilding()) {
                    // generate the data in parallel
                    builderFutures.add(CompletableFuture.runAsync(() -> buildGeometry(pair, startTime, renderMaxTime), Util.backgroundExecutor()));
                } else {
                    // generate first data in series to avoid weird class loading error
                    buildGeometry(pair, startTime, renderMaxTime);
                    if (blockCounts.getOrDefault(pair, 0) > 0) {
                        firstRenderDone = true;
                    }
                }
            }
        }

        CompletableFuture.allOf(builderFutures.toArray(new CompletableFuture[0])).thenRunAsync(this::handleError, Util.backgroundExecutor());
    }

    private void buildGeometry(Pair<RenderType, BlockPos> pair, long startTime, int maxTime) {
        if (Utils.milliTime() - startTime >= maxTime) {
            return;
        }

        RenderType layer = pair.getLeft();
        BlockPos offset = pair.getRight();
        builderThreads.add(Thread.currentThread());
        long realStartTime = Utils.milliTime();

        try {
            PoseStack thisPose = new PoseStack();
            int renderDistSquare = (renderDistance + 1) * (renderDistance + 1);
            BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
            BufferBuilder vertBuffer = bufferBuilders.get(pair);
            BlockPos.MutableBlockPos pos = currentPositions.get(pair);
            RandomSource randomSource = RandomSource.create();

            int count = 0;
            while (Utils.milliTime() - startTime < maxTime && pos.getY() < Math.min(segmentSize.getY() + offset.getY(), blockAccess.getYSize() - (int) blockAccess.getGround()) && building) {
                // only build blocks not obscured by fog
                if (Mth.abs(pos.getY()) <= renderDistance + 1 && Mth.lengthSquared(pos.getX(), pos.getZ()) <= renderDistSquare) {
                    BlockState state = blockAccess.getBlockState(pos);
                    if (state != null) {
                        FluidState fluidState = state.getFluidState();
                        if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == layer) {
                            for (var sprite : Xplat.getFluidTextures(blockAccess, pos, fluidState)) {
                                if (sprite != null && sprite.contents().getUniqueFrames().sum() > 1) {
                                    animatedSprites.add(sprite);
                                }
                            }
                            blockRenderer.renderLiquid(pos, blockAccess, vertBuffer, state, new FluidStateWrapper(fluidState));
                            count++;
                        }

                        if (state.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.getChunkRenderType(state) == layer) {
                            for (var quad : mc.getModelManager().getBlockModelShaper().getBlockModel(state).getQuads(state, null, randomSource)) {
                                if (quad.getSprite().contents().getUniqueFrames().sum() > 1) {
                                    animatedSprites.add(quad.getSprite());
                                }
                            }
                            thisPose.pushPose();
                            thisPose.translate(pos.getX(), pos.getY(), pos.getZ());
                            blockRenderer.renderBatched(state, pos, blockAccess, thisPose, vertBuffer, true, randomSource);
                            count++;
                            thisPose.popPose();
                        }
                    }
                }

                // iterate the position
                pos.setX(pos.getX() + 1);
                if (pos.getX() >= Math.min(segmentSize.getX() + offset.getX(), blockAccess.getXSize() / 2)) {
                    pos.setX(offset.getX());
                    pos.setZ(pos.getZ() + 1);
                    if (pos.getZ() >= Math.min(segmentSize.getZ() + offset.getZ(), blockAccess.getZSize() / 2)) {
                        pos.setZ(offset.getZ());
                        pos.setY(pos.getY() + 1);
                    }
                }
            }

            //VRSettings.logger.info("MenuWorlds: Built segment of " + count + " blocks in " + ((RenderStateShardAccessor)layer).getName() + " layer.");
            blockCounts.put(pair, blockCounts.getOrDefault(pair, 0) + count);
            renderTimes.put(pair, renderTimes.getOrDefault(pair, 0L) + (Utils.milliTime() - realStartTime));

            if (pos.getY() >= Math.min(segmentSize.getY() + offset.getY(), blockAccess.getYSize() - (int) blockAccess.getGround())) {
                VRSettings.logger.debug("MenuWorlds: Built {} blocks on {} layer at {},{},{} in {} ms",
                    blockCounts.get(pair),
                    ((RenderStateShardAccessor) layer).getName(),
                    offset.getX(), offset.getY(), offset.getZ(),
                    renderTimes.get(pair));
            }
        } catch (Throwable e) { // Only effective way of preventing crash on poop computers with low heap size
            builderError = e;
        } finally {
            builderThreads.remove(Thread.currentThread());
        }
    }

    private void finishBuilding() {
        building = false;

        // Sort buffers from nearest to furthest
        var entryList = new ArrayList<>(bufferBuilders.entrySet());
        entryList.sort(Comparator.comparing(entry -> entry.getKey().getRight(), (posA, posB) -> {
            Vec3i center = new Vec3i(segmentSize.getX() / 2, segmentSize.getY() / 2, segmentSize.getZ() / 2);
            double distA = posA.offset(center).distSqr(BlockPos.ZERO);
            double distB = posB.offset(center).distSqr(BlockPos.ZERO);
            return Double.compare(distA, distB);
        }));

        int totalMemory = 0, count = 0;
        try (ByteBufferBuilder builder = new ByteBufferBuilder(32768)) {
            for (var entry : entryList) {
                RenderType layer = entry.getKey().getLeft();
                BufferBuilder bufferBuilder = entry.getValue();
                MeshData meshData = bufferBuilder.build();
                if (meshData != null) {
                    if (layer == RenderType.translucent()) {
                        meshData.sortQuads(builder, VertexSorting.byDistance(0, Mth.frac(blockAccess.getGround()), 0));
                    }
                    uploadGeometry(layer, meshData);
                    count++;
                }
                totalMemory += ((BufferBuilderExtension) bufferBuilder).vivecraft$getBufferSize();
                ((BufferBuilderExtension) bufferBuilder).vivecraft$freeBuffer();

            }
        }

        bufferBuilders = null;
        currentPositions = null;
        ready = true;
        VRSettings.logger.info("MenuWorlds: Built {} blocks in {} ms ({} ms CPU time)",
            blockCounts.values().stream().reduce(Integer::sum).orElse(0),
            Utils.milliTime() - buildStartTime,
            renderTimes.values().stream().reduce(Long::sum).orElse(0L));
        VRSettings.logger.info("MenuWorlds: Used {} temporary buffers ({} MiB), uploaded {} non-empty buffers",
            entryList.size(),
            totalMemory / 1048576,
            count);
        if (shadersEnabled) {
            shadersEnabled = false;
            IrisHelper.toggleShaders(mc, true);
        }
    }

    public boolean isOnBuilderThread() {
        return builderThreads.contains(Thread.currentThread());
    }

    private void handleError() {
        if (builderError == null) {
            return;
        }
        if (builderError instanceof OutOfMemoryError || builderError.getCause() instanceof OutOfMemoryError) {
            VRSettings.logger.error("OutOfMemoryError while building main menu world. Low system memory or 32-bit Java?");
        } else {
            VRSettings.logger.error("Exception thrown when building main menu world, falling back to old menu room. \n {}", builderError.getMessage());
        }
        builderError.printStackTrace();
        destroy();
        setWorld(null);
        builderError = null;
    }

    private void uploadGeometry(RenderType layer, MeshData meshData) {
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(meshData);
        VertexBuffer.unbind();
        vertexBuffers.get(layer).add(buffer);
    }

    public void cancelBuilding() {
        building = false;
        builderFutures.forEach(CompletableFuture::join);
        builderFutures.clear();
        if (bufferBuilders != null) {
            for (BufferBuilder vertBuffer : bufferBuilders.values()) {
                ((BufferBuilderExtension) vertBuffer).vivecraft$freeBuffer();
            }
            bufferBuilders = null;
        }
        currentPositions = null;
    }

    public void destroy() {
        cancelBuilding();
        if (vertexBuffers != null) {
            for (List<VertexBuffer> buffers : vertexBuffers.values()) {
                for (VertexBuffer vertexBuffer : buffers) {
                    if (vertexBuffer != null) {
                        vertexBuffer.close();
                    }
                }
            }
            vertexBuffers = null;
        }
        animatedSprites = null;
        ready = false;
    }

    public void completeDestroy() {
        destroy();
        if (starVBO != null) {
            starVBO.close();
        }
        if (skyBuffer != null) {
            skyBuffer.close();
        }
        if (darkBuffer != null) {
            darkBuffer.close();
        }
        if (cloudVBO != null) {
            cloudVBO.close();
        }
        ready = false;
    }

    public void tick() {
        ticks++;
        this.updateTorchFlicker();

        if (this.areEyesInFluid(FluidTags.WATER)) {
            int i = 1; //this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.areEyesInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (SodiumHelper.isLoaded() && animatedSprites != null) {
            for (TextureAtlasSprite sprite : animatedSprites) {
                SodiumHelper.markTextureAsActive(sprite);
            }
        }
        if (OptifineHelper.isOptifineLoaded()) {
            for (TextureAtlasSprite sprite : animatedSprites) {
                OptifineHelper.markTextureAsActive(sprite);
            }
        }
    }

    public FakeBlockAccess getLevel() {
        return blockAccess;
    }

    public void setWorld(FakeBlockAccess blockAccess) {
        this.blockAccess = blockAccess;
        if (blockAccess != null) {
            this.dimensionInfo = blockAccess.getDimensionReaderInfo();
            this.lightmapUpdateNeeded = true;
            this.renderDistance = blockAccess.getXSize() / 2;
            this.renderDistanceChunks = this.renderDistance / 16;
            rainLevel = blockAccess.getRain() ? 1.0F : 0.0F;
            thunderLevel = blockAccess.getThunder() ? 1.0F : 0.0F;

            worldRotation = blockAccess.getRotation();
        }
    }

    public void loadRenderers() {
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
                float h = Mth.sqrt(f * f + g * g);
                this.rainSizeX[i << 5 | j] = -g / h;
                this.rainSizeZ[i << 5 | j] = f / h;
            }
        }

        this.generateLightSky();
        this.generateDarkSky();
        this.createStars();
    }

    public boolean isReady() {
        return ready;
    }

    // VanillaFix support
    @SuppressWarnings("unchecked")
    private void copyVisibleTextures() {
		/*if (Reflector.VFTemporaryStorage.exists()) {
			if (Reflector.VFTemporaryStorage_texturesUsed.exists()) {
				visibleTextures.addAll((Collection<TextureAtlasSprite>)Reflector.getFieldValue(Reflector.VFTemporaryStorage_texturesUsed));
			} else if (Reflector.VFTextureAtlasSprite_needsAnimationUpdate.exists()) {
				for (TextureAtlasSprite texture : (Collection<TextureAtlasSprite>)MCReflection.TextureMap_listAnimatedSprites.get(mc.getTextureMapBlocks())) {
					if (Reflector.callBoolean(texture, Reflector.VFTextureAtlasSprite_needsAnimationUpdate))
						visibleTextures.add(texture);
				}
			}
		}*/
    }

    @SuppressWarnings("unchecked")
    public void pushVisibleTextures() {
		/*if (Reflector.VFTemporaryStorage.exists()) {
			if (Reflector.VFTemporaryStorage_texturesUsed.exists()) {
				Collection<TextureAtlasSprite> coll = (Collection<TextureAtlasSprite>)Reflector.getFieldValue(Reflector.VFTemporaryStorage_texturesUsed);
				coll.addAll(visibleTextures);
			} else if (Reflector.VFTextureAtlasSprite_markNeedsAnimationUpdate.exists()) {
				for (TextureAtlasSprite texture : visibleTextures)
					Reflector.call(texture, Reflector.VFTextureAtlasSprite_markNeedsAnimationUpdate);
			}
		}*/
    }
    // End VanillaFix support

    public void renderSky(Matrix4fStack poseStack, Vec3 position) {
        if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.END) {
            this.renderEndSky(poseStack);
        } else if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
            RenderSystem.setShader(GameRenderer::getPositionShader);
            fogRenderer.setupFog(FogRenderer.FogMode.FOG_SKY);
            ShaderInstance skyShader = RenderSystem.getShader();
            //RenderSystem.disableTexture();

            Vec3 skyColor = this.getSkyColor(position);

            if (OptifineHelper.isOptifineLoaded()) {
                skyColor = OptifineHelper.getCustomSkyColor(skyColor, blockAccess, position.x, position.y, position.z);
            }

            fogRenderer.levelFogColor();

            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1.0f);


            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled()) {
                this.skyBuffer.bind();
                this.skyBuffer.drawWithShader(poseStack, RenderSystem.getProjectionMatrix(), skyShader);
                VertexBuffer.unbind();
            }

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            float[] sunriseColor = this.dimensionInfo.getSunriseColor(this.getTimeOfDay(), 0); // calcSunriseSunsetColors

            if (sunriseColor != null && (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled())) {
                //RenderSystem.disableTexture();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                poseStack.pushMatrix();

                poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
                poseStack.rotate(Axis.ZP.rotationDegrees(Mth.sin(this.getSunAngle()) < 0.0f ? 180.0f : 0.0f));
                poseStack.rotate(Axis.ZP.rotationDegrees(90.0f));

                Matrix4f modelView = poseStack;
                BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder
                    .addVertex(modelView, 0.0f, 100.0f, 0.0f)
                    .setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]);

                for (int j = 0; j <= 16; ++j) {
                    float f6 = (float) j * ((float) Math.PI * 2F) / 16.0F;
                    float f7 = Mth.sin(f6);
                    float f8 = Mth.cos(f6);
                    bufferBuilder
                        .addVertex(modelView, f7 * 120.0F, f8 * 120.0F, -f8 * 40.0F * sunriseColor[3])
                        .setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0F);
                }

                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                poseStack.popMatrix();
            }

            //RenderSystem.enableTexture();

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            poseStack.pushMatrix();

            float f10 = 1.0F - getRainLevel();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f10);
            poseStack.rotate(Axis.YP.rotationDegrees(-90.0f));
            Matrix4f modelView = poseStack;

            //if (OptifineHelper.isOptifineLoaded()) {
            // needs a full Level
            //CustomSky.renderSky(this.world, poseStack, Minecraft.getInstance().getFrameTime());
            //}

            poseStack.rotate(Axis.XP.rotationDegrees(this.getTimeOfDay() * 360.0f));

            float size = 30.0F;
            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, SUN_LOCATION);
                BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferBuilder.addVertex(modelView, -size, 100.0F, -size).setUv(0.0F, 0.0F);
                bufferBuilder.addVertex(modelView, size, 100.0F, -size).setUv(1.0F, 0.0F);
                bufferBuilder.addVertex(modelView, size, 100.0F, size).setUv(1.0F, 1.0F);
                bufferBuilder.addVertex(modelView, -size, 100.0F, size).setUv(0.0F, 1.0F);
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            }

            size = 20.0F;
            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()) {
                RenderSystem.setShaderTexture(0, MOON_LOCATION);
                int moonPhase = this.getMoonPhase();
                int l = moonPhase % 4;
                int i1 = moonPhase / 4 % 2;
                float u0 = (float) (l) / 4.0F;
                float v0 = (float) (i1) / 2.0F;
                float u1 = (float) (l + 1) / 4.0F;
                float v1 = (float) (i1 + 1) / 2.0F;
                BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferBuilder.addVertex(modelView, -size, -100.0f, size).setUv(u0, v1);
                bufferBuilder.addVertex(modelView, size, -100.0f, size).setUv(u1, v1);
                bufferBuilder.addVertex(modelView, size, -100.0f, -size).setUv(u1, v0);
                bufferBuilder.addVertex(modelView, -size, -100.0f, -size).setUv(u0, v0);
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            }

            //GlStateManager.disableTexture();

            float starBrightness = this.getStarBrightness() * f10;

            if (starBrightness > 0.0F && (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isStarsEnabled()) /*&& !CustomSky.hasSkyLayers(this.world)*/) {
                RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
                fogRenderer.setupNoFog();
                this.starVBO.bind();
                this.starVBO.drawWithShader(poseStack, RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
                VertexBuffer.unbind();
                fogRenderer.setupFog(FogRenderer.FogMode.FOG_SKY);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();

            poseStack.popMatrix();
            //RenderSystem.disableTexture();

            double horizonDistance = position.y - this.blockAccess.getHorizon();

            if (horizonDistance < 0.0D) {
                RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
                poseStack.pushMatrix();
                poseStack.translate(0.0f, 12.0f, 0.0f);
                this.darkBuffer.bind();
                this.darkBuffer.drawWithShader(poseStack, RenderSystem.getProjectionMatrix(), skyShader);
                VertexBuffer.unbind();
                poseStack.popMatrix();
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.depthMask(true);
        }
    }

    private void renderEndSky(Matrix4fStack poseStack) {
        if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, END_SKY_LOCATION);

            for (int i = 0; i < 6; ++i) {
                poseStack.pushMatrix();
                switch (i) {
                    case 1 -> poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
                    case 2 -> poseStack.rotate(Axis.XP.rotationDegrees(-90.0f));
                    case 3 -> poseStack.rotate(Axis.XP.rotationDegrees(180.0f));
                    case 4 -> poseStack.rotate(Axis.ZP.rotationDegrees(90.0f));
                    case 5 -> poseStack.rotate(Axis.ZP.rotationDegrees(-90.0f));
                }

                BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

                int r = 40;
                int g = 40;
                int b = 40;

                if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isCustomColors()) {
                    Vec3 newSkyColor = new Vec3((double) r / 255.0D, (double) g / 255.0D, (double) b / 255.0D);
                    newSkyColor = OptifineHelper.getCustomSkyColorEnd(newSkyColor);
                    r = (int) (newSkyColor.x * 255.0D);
                    g = (int) (newSkyColor.y * 255.0D);
                    b = (int) (newSkyColor.z * 255.0D);
                }
                bufferBuilder.addVertex(poseStack, -100.0f, -100.0f, -100.0f).setUv(0.0f, 0.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(poseStack, -100.0f, -100.0f, 100.0f).setUv(0.0f, 16.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(poseStack, 100.0f, -100.0f, 100.0f).setUv(16.0f, 16.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(poseStack, 100.0f, -100.0f, -100.0f).setUv(16.0f, 0.0f).setColor(r, g, b, 255);
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                poseStack.popMatrix();
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

    public void renderClouds(Matrix4fStack poseStack, double x, double y, double z) {
        float cloudHeight = this.dimensionInfo.getCloudHeight();

        if (!Float.isNaN(cloudHeight) && this.mc.options.getCloudsType() != CloudStatus.OFF) {
            // setup clouds

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);

            float cloudSizeXZ = 12.0f;
            float cloudSizeY = 4.0f;
            double cloudOffset = ((float) ticks + mc.getTimer().getGameTimeDeltaPartialTick(false)) * 0.03f;
            double cloudX = (x + cloudOffset) / 12.0;
            double cloudY = cloudHeight - y + 0.33;
            if (OptifineHelper.isOptifineLoaded()) {
                cloudY = cloudY + OptifineHelper.getCloudHeight() * 128.0;
            }

            double cloudZ = z / 12.0 + 0.33;
            cloudX -= Mth.floor(cloudX / 2048.0) * 2048;
            cloudZ -= Mth.floor(cloudZ / 2048.0) * 2048;
            float cloudXfract = (float) (cloudX - (double) Mth.floor(cloudX));
            float cloudYfract = (float) (cloudY / 4.0 - (double) Mth.floor(cloudY / 4.0)) * 4.0f;
            float cloudZfract = (float) (cloudZ - (double) Mth.floor(cloudZ));

            Vec3 cloudColor = this.getCloudColour();
            int cloudXfloor = (int) Math.floor(cloudX);
            int cloudYfloor = (int) Math.floor(cloudY / 4.0);
            int cloudZfloor = (int) Math.floor(cloudZ);
            if (cloudXfloor != this.prevCloudX ||
                cloudYfloor != this.prevCloudY ||
                cloudZfloor != this.prevCloudZ ||
                this.mc.options.getCloudsType() != this.prevCloudsType ||
                this.prevCloudColor.distanceToSqr(cloudColor) > 2.0E-4) {
                this.prevCloudX = cloudXfloor;
                this.prevCloudY = cloudYfloor;
                this.prevCloudZ = cloudZfloor;
                this.prevCloudColor = cloudColor;
                this.prevCloudsType = this.mc.options.getCloudsType();
                this.generateClouds = true;
            }
            if (this.generateClouds) {
                this.generateClouds = false;
                if (this.cloudVBO != null) {
                    this.cloudVBO.close();
                }
                this.cloudVBO = new VertexBuffer(VertexBuffer.Usage.STATIC);
                this.cloudVBO.bind();
                this.cloudVBO.upload(this.buildClouds(Tesselator.getInstance(), cloudX, cloudY, cloudZ, cloudColor));
                VertexBuffer.unbind();
            }

            // render
            RenderSystem.setShader(GameRenderer::getRendertypeCloudsShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            fogRenderer.levelFogColor();
            poseStack.pushMatrix();
            poseStack.scale(12.0f, 1.0f, 12.0f);
            poseStack.translate(-cloudXfract, cloudYfract, -cloudZfract);
            if (this.cloudVBO != null) {
                this.cloudVBO.bind();
                // probably rendered twice, so only the front faces are there?
                for (int w = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1; w < 2; ++w) {
                    if (w == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }
                    this.cloudVBO.drawWithShader(poseStack, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                }
                VertexBuffer.unbind();
            }
            poseStack.popMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private MeshData buildClouds(Tesselator tesselator, double cloudX, double cloudY, double cloudZ, Vec3 cloudColor) {
        float l = (float) Mth.floor(cloudX) * 0.00390625f;
        float m = (float) Mth.floor(cloudZ) * 0.00390625f;
        float redTop = (float) cloudColor.x;
        float greenTop = (float) cloudColor.y;
        float blueTop = (float) cloudColor.z;
        float redX = redTop * 0.9f;
        float greenX = greenTop * 0.9f;
        float blueX = blueTop * 0.9f;
        float redBottom = redTop * 0.7f;
        float greenBottom = greenTop * 0.7f;
        float blueBottom = blueTop * 0.7f;
        float redZ = redTop * 0.8f;
        float greenZ = greenTop * 0.8f;
        float blueZ = blueTop * 0.8f;
        RenderSystem.setShader(GameRenderer::getRendertypeCloudsShader);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float z = (float) Math.floor(cloudY / 4.0) * 4.0f;
        if (this.prevCloudsType == CloudStatus.FANCY) {
            for (int aa = -3; aa <= 4; ++aa) {
                for (int ab = -3; ab <= 4; ++ab) {
                    int ae;
                    float ac = aa * 8;
                    float ad = ab * 8;
                    if (z > -5.0f) {
                        bufferBuilder.addVertex(ac + 0.0f, z + 0.0f, ad + 8.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redBottom, greenBottom, blueBottom, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 0.0f, ad + 8.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redBottom, greenBottom, blueBottom, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 0.0f, ad + 0.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redBottom, greenBottom, blueBottom, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 0.0f, z + 0.0f, ad + 0.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redBottom, greenBottom, blueBottom, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                    }
                    if (z <= 5.0f) {
                        bufferBuilder.addVertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, 1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, 1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, 1.0f, 0.0f);
                        bufferBuilder.addVertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, 1.0f, 0.0f);
                    }
                    if (aa > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.addVertex(ac + (float) ae + 0.0f, z + 0.0f, ad + 8.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(-1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 0.0f, z + 4.0f, ad + 8.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(-1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 0.0f, z + 4.0f, ad + 0.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(-1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 0.0f, z + 0.0f, ad + 0.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(-1.0f, 0.0f, 0.0f);
                        }
                    }
                    if (aa <= 1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.addVertex(ac + (float) ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 8.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 8.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 0.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(1.0f, 0.0f, 0.0f);
                            bufferBuilder.addVertex(ac + (float) ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 0.0f).setUv((ac + (float) ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).setColor(redX, greenX, blueX, 0.8f).setNormal(1.0f, 0.0f, 0.0f);
                        }
                    }
                    if (ab > -1) {
                        for (ae = 0; ae < 8; ++ae) {
                            bufferBuilder.addVertex(ac + 0.0f, z + 4.0f, ad + (float) ae + 0.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, -1.0f);
                            bufferBuilder.addVertex(ac + 8.0f, z + 4.0f, ad + (float) ae + 0.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, -1.0f);
                            bufferBuilder.addVertex(ac + 8.0f, z + 0.0f, ad + (float) ae + 0.0f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, -1.0f);
                            bufferBuilder.addVertex(ac + 0.0f, z + 0.0f, ad + (float) ae + 0.0f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, -1.0f);
                        }
                    }
                    if (ab > 1) {
                        continue;
                    }
                    for (ae = 0; ae < 8; ++ae) {
                        bufferBuilder.addVertex(ac + 0.0f, z + 4.0f, ad + (float) ae + 1.0f - 9.765625E-4f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, 1.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 4.0f, ad + (float) ae + 1.0f - 9.765625E-4f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, 1.0f);
                        bufferBuilder.addVertex(ac + 8.0f, z + 0.0f, ad + (float) ae + 1.0f - 9.765625E-4f).setUv((ac + 8.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, 1.0f);
                        bufferBuilder.addVertex(ac + 0.0f, z + 0.0f, ad + (float) ae + 1.0f - 9.765625E-4f).setUv((ac + 0.0f) * 0.00390625f + l, (ad + (float) ae + 0.5f) * 0.00390625f + m).setColor(redZ, greenZ, blueZ, 0.8f).setNormal(0.0f, 0.0f, 1.0f);
                    }
                }
            }
        } else {
            boolean aa = true;
            int ab = 32;
            for (int af = -32; af < 32; af += 32) {
                for (int ag = -32; ag < 32; ag += 32) {
                    bufferBuilder.addVertex(af, z, ag + 32).setUv((float) (af) * 0.00390625f + l, (float) (ag + 32) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                    bufferBuilder.addVertex(af + 32, z, ag + 32).setUv((float) (af + 32) * 0.00390625f + l, (float) (ag + 32) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                    bufferBuilder.addVertex(af + 32, z, ag).setUv((float) (af + 32) * 0.00390625f + l, (float) (ag) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                    bufferBuilder.addVertex(af, z, ag).setUv((float) (af) * 0.00390625f + l, (float) (ag) * 0.00390625f + m).setColor(redTop, greenTop, blueTop, 0.8f).setNormal(0.0f, -1.0f, 0.0f);
                }
            }
        }
        return bufferBuilder.buildOrThrow();
    }

    private void renderSnowAndRain(Matrix4fStack poseStack, double inX, double inY, double inZ) {
        if (getRainLevel() <= 0.0f) {
            return;
        }

        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(poseStack);
        RenderSystem.applyModelViewMatrix();

        int xFloor = Mth.floor(inX);
        int yFloor = Mth.floor(inY);
        int zFloor = Mth.floor(inZ);

        BufferBuilder bufferBuilder = null;
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int rainDistance = 5;
        if (Minecraft.useFancyGraphics()) {
            rainDistance = 10;
        }
        RenderSystem.depthMask(true);
        int count = -1;
        float rainAnimationTime = this.ticks + mc.getTimer().getGameTimeDeltaPartialTick(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        turnOnLightLayer();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int rainZ = zFloor - rainDistance; rainZ <= zFloor + rainDistance; ++rainZ) {
            for (int rainX = xFloor - rainDistance; rainX <= xFloor + rainDistance; ++rainX) {
                int q = (rainZ - zFloor + 16) * 32 + rainX - xFloor + 16;
                double r = (double) this.rainSizeX[q] * 0.5;
                double s = (double) this.rainSizeZ[q] * 0.5;
                mutableBlockPos.set(rainX, inY, rainZ);
                Biome biome = blockAccess.getBiome(mutableBlockPos).value();
                if (!biome.hasPrecipitation()) {
                    continue;
                }

                int blockingHeight = blockAccess.getHeightBlocking(rainX, rainZ);
                int lower = Math.max(yFloor - rainDistance, blockingHeight);
                int upper = Math.max(yFloor + rainDistance, blockingHeight);

                if (lower == upper) {
                    // no rain
                    continue;
                }
                int rainY = Math.max(blockingHeight, yFloor);

                RandomSource randomSource = RandomSource.create(rainX * rainX * 3121L + rainX * 45238971L ^ rainZ * rainZ * 418711L + rainZ * 13761L);
                mutableBlockPos.setY(lower);
                Biome.Precipitation precipitation = biome.getPrecipitationAt(mutableBlockPos);
                if (precipitation == Biome.Precipitation.NONE) {
                    continue;
                }

                mutableBlockPos.setY(rainY);

                double localX = rainX + 0.5;
                double localZ = rainZ + 0.5;
                float distance = (float) Math.sqrt(localX * localX + localZ * localZ) / (float) rainDistance;
                float blend;
                float xOffset = 0;
                float yOffset = 0;

                int skyLight = blockAccess.getBrightness(LightLayer.SKY, mutableBlockPos) << 4;
                int blockLight = blockAccess.getBrightness(LightLayer.BLOCK, mutableBlockPos) << 4;

                if (precipitation == Biome.Precipitation.RAIN) {
                    if (count != 0) {
                        if (count >= 0) {
                            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                        }
                        count = 0;
                        RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                        bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }

                    blend = ((1.0f - distance * distance) * 0.5f + 0.5f);
                    int x = this.ticks + rainX * rainX * 3121 + rainX * 45238971 + rainZ * rainZ * 418711 + rainZ * 13761 & 0x1F;
                    yOffset = -((float) x + mc.getTimer().getGameTimeDeltaPartialTick(false)) / 32.0f * (3.0f + randomSource.nextFloat());
                } else if (precipitation == Biome.Precipitation.SNOW) {
                    if (count != 1) {
                        if (count >= 0) {
                            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                        }
                        count = 1;
                        RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                        bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }

                    blend = ((1.0f - distance * distance) * 0.3f + 0.5f);
                    xOffset = (float) (randomSource.nextDouble() + (double) rainAnimationTime * 0.01 * (double) ((float) randomSource.nextGaussian()));
                    float ae = -((float) (this.ticks & 0x1FF) + mc.getTimer().getGameTimeDeltaPartialTick(false)) / 512.0f;
                    float af = (float) (randomSource.nextDouble() + (double) (rainAnimationTime * (float) randomSource.nextGaussian()) * 0.001);
                    yOffset = ae + af;

                    //snow is brighter
                    skyLight = (skyLight * 3 + 240) / 4;
                    blockLight = (blockLight * 3 + 240) / 4;
                } else {
                    continue;
                }
                bufferBuilder
                    .addVertex((float) (localX - r),  upper - (float) inY, (float) (localZ - s))
                    .setUv(0.0f + xOffset, (float) lower * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                bufferBuilder
                    .addVertex((float) (localX + r),  upper - (float) inY, (float) (localZ + s))
                    .setUv(1.0f + xOffset, (float) lower * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                bufferBuilder
                    .addVertex((float) (localX + r), lower - (float) inY, (float) (localZ + s))
                    .setUv(1.0f + xOffset, (float) upper * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                bufferBuilder
                    .addVertex((float) (localX - r),  lower - (float) inY, (float) (localZ - s))
                    .setUv(0.0f + xOffset, (float) upper * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
            }
        }
        if (count >= 0) {
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        }
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        turnOffLightLayer();
    }

    public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int i = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
        int j = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
        return i << 20 | j << 4;
    }

    public float getTimeOfDay() {
        return this.blockAccess.dimensionType().timeOfDay(time);
    }

    public float getSunAngle() {
        float dayTime = this.getTimeOfDay();
        return dayTime * ((float) Math.PI * 2F);
    }

    public int getMoonPhase() {
        return this.blockAccess.dimensionType().moonPhase(time);
    }

    public float getSkyDarken() {
        float dayTime = this.getTimeOfDay();
        float h = 1.0f - (Mth.cos(dayTime * ((float) Math.PI * 2)) * 2.0f + 0.2f);
        h = Mth.clamp(h, 0.0f, 1.0f);
        h = 1.0f - h;
        h *= 1.0f - this.getRainLevel() * 5.0f / 16.0f;
        h *= 1.0f - this.getThunderLevel() * 5.0f / 16.0f;
        return h * 0.8f + 0.2f;
    }

    public float getRainLevel() {
        return this.rainLevel;
    }

    public float getThunderLevel() {
        return this.thunderLevel * this.getRainLevel();
    }

    public float getStarBrightness() {
        float f = this.getTimeOfDay();
        float f1 = 1.0F - (Mth.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.25F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    public Vec3 getSkyColor(Vec3 position) {
        float dayTime = this.getTimeOfDay();

        Vec3 samplePosition = position.subtract(2.0, 2.0, 2.0).scale(0.25);

        Vec3 skyColor = CubicSampler.gaussianSampleVec3(samplePosition, (i, j, k) -> Vec3.fromRGB24(blockAccess.getBiomeManager().getNoiseBiomeAtQuart(i, j, k).value().getSkyColor()));

        float h = Mth.cos(dayTime * ((float) Math.PI * 2)) * 2.0f + 0.5f;
        h = Mth.clamp(h, 0.0f, 1.0f);
        float skyColorR = (float) skyColor.x * h;
        float skyColorG = (float) skyColor.y * h;
        float skyColorB = (float) skyColor.z * h;

        float rain = this.getRainLevel();
        float thunder;
        if (rain > 0.0f) {
            float luminance = (skyColorR * 0.3f + skyColorG * 0.59f + skyColorB * 0.11f) * 0.6f;
            float darkening = 1.0f - rain * 0.75f;
            skyColorR = skyColorR * darkening + luminance * (1.0f - darkening);
            skyColorG = skyColorG * darkening + luminance * (1.0f - darkening);
            skyColorB = skyColorB * darkening + luminance * (1.0f - darkening);
        }
        if ((thunder = this.getThunderLevel()) > 0.0f) {
            float luminance = (skyColorR * 0.3f + skyColorG * 0.59f + skyColorB * 0.11f) * 0.2f;
            float darkening = 1.0f - thunder * 0.75f;
            skyColorR = skyColorR * darkening + luminance * (1.0f - darkening);
            skyColorG = skyColorG * darkening + luminance * (1.0f - darkening);
            skyColorB = skyColorB * darkening + luminance * (1.0f - darkening);
        }
        if (!mc.options.hideLightningFlash().get() && this.skyFlashTime > 0) {
            float flash = (float) this.skyFlashTime - mc.getTimer().getGameTimeDeltaPartialTick(false);
            if (flash > 1.0f) {
                flash = 1.0f;
            }
            skyColorR = skyColorR * (1.0f - (flash *= 0.45f)) + 0.8f * flash;
            skyColorG = skyColorG * (1.0f - flash) + 0.8f * flash;
            skyColorB = skyColorB * (1.0f - flash) + flash;
        }
        return new Vec3(skyColorR, skyColorG, skyColorB);
    }

    public Vec3 getFogColor(Vec3 pos) {
        float f = Mth.clamp(Mth.cos(this.getTimeOfDay() * ((float) Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        Vec3 scaledPos = pos.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        return CubicSampler.gaussianSampleVec3(scaledPos, (x, y, z) -> this.dimensionInfo.getBrightnessDependentFogColor(Vec3.fromRGB24(this.blockAccess.getBiomeManager().getNoiseBiomeAtQuart(x, y, z).value().getFogColor()), f));
    }

    public Vec3 getCloudColour() {
        float dayTime = this.getTimeOfDay();
        float f1 = Mth.cos(dayTime * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float rain = this.getRainLevel();

        if (rain > 0.0F) {
            float luma = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
            float dark = 1.0F - rain * 0.95F;
            r = r * dark + luma * (1.0F - dark);
            g = g * dark + luma * (1.0F - dark);
            b = b * dark + luma * (1.0F - dark);
        }

        r = r * (f1 * 0.9F + 0.1F);
        g = g * (f1 * 0.9F + 0.1F);
        b = b * (f1 * 0.85F + 0.15F);
        float thunder = this.getThunderLevel();

        if (thunder > 0.0F) {
            float luma = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float dark = 1.0F - thunder * 0.95F;
            r = r * dark + luma * (1.0F - dark);
            g = g * dark + luma * (1.0F - dark);
            b = b * dark + luma * (1.0F - dark);
        }

        return new Vec3(r, g, b);
    }

    private void generateLightSky() {
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.skyBuffer.bind();
        this.skyBuffer.upload(buildSkyDisc(Tesselator.getInstance(), 16.0f));
        VertexBuffer.unbind();
    }

    private void generateDarkSky() {
        if (this.darkBuffer != null) {
            this.darkBuffer.close();
        }
        this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.darkBuffer.bind();
        this.darkBuffer.upload(buildSkyDisc(Tesselator.getInstance(), -16.0f));
        VertexBuffer.unbind();
    }

    private static MeshData buildSkyDisc(Tesselator tesselator, float posY) {
        float g = Math.signum(posY) * 512.0f;
        float h = 512.0f;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(0.0F, posY, 0.0F);
        for (int i = -180; i <= 180; i += 45) {
            bufferBuilder.addVertex(g * Mth.cos((float) i * ((float) Math.PI / 180)), posY, 512.0f * Mth.sin((float) i * ((float) Math.PI / 180)));
        }
        return bufferBuilder.buildOrThrow();
    }

    private void createStars() {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.starVBO != null) {
            this.starVBO.close();
        }
        this.starVBO = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.starVBO.bind();
        this.starVBO.upload(this.drawStars(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }

    private MeshData drawStars(Tesselator tesselator) {
        RandomSource randomSource = RandomSource.create(10842L);
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        int starCount = 1500;
        float starDistance = 100.0F;

        for (int i = 0; i < starCount; i++) {
            Vector3f starPoint = new Vector3f(randomSource.nextFloat(), randomSource.nextFloat(), randomSource.nextFloat()).mul(2.0F).sub(1.0F, 1.0F, 1.0F);
            float starSize = 0.15F + randomSource.nextFloat() * 0.1F;
            float distance = starPoint.lengthSquared();
            if (distance <= 0.010000001F || distance >= 1.0F) continue;

            starPoint = starPoint.normalize(starDistance);
            float starRotation = (float)(randomSource.nextDouble() * Math.PI * 2.0);

            Quaternionf quaternionf = new Quaternionf()
                .rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), starPoint)
                .rotateZ(starRotation);

            bufferBuilder.addVertex(starPoint.add(new Vector3f(starSize, -starSize, 0.0F).rotate(quaternionf)));
            bufferBuilder.addVertex(starPoint.add(new Vector3f(starSize, starSize, 0.0F).rotate(quaternionf)));
            bufferBuilder.addVertex(starPoint.add(new Vector3f(-starSize, starSize, 0.0F).rotate(quaternionf)));
            bufferBuilder.addVertex(starPoint.add(new Vector3f(-starSize, -starSize, 0.0F).rotate(quaternionf)));
        }
        return bufferBuilder.buildOrThrow();
    }

    public void turnOffLightLayer() {
        RenderSystem.setShaderTexture(2, 0);
    }

    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.lightTextureLocation);
        mc.getTextureManager().bindForSetup(this.lightTextureLocation);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public void updateTorchFlicker() {
        this.blockLightRedFlicker += (float) ((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        this.blockLightRedFlicker *= 0.9f;
        this.lightmapUpdateNeeded = true;
    }

    public void updateLightmap() {
        if (this.lightmapUpdateNeeded) {
            // not possible, needs a full world
			/*if (Config.isCustomColors())
			{
				boolean flag = this.client.player.isPotionActive(MobEffects.NIGHT_VISION) || this.client.player.isPotionActive(MobEffects.CONDUIT_POWER);

				if (CustomColors.updateLightmap(world, this.torchFlickerX, this.nativeImage, flag, partialTicks))
				{
					this.dynamicTexture.updateDynamicTexture();
					this.needsUpdate = false;
					this.client.profiler.endSection();
					return;
				}
			}*/

            float skyLight = getSkyDarken();
            float effectiveSkyLight = skyFlashTime > 0 ? 1.0f : skyLight * 0.95F + 0.05F;

			/* no darkness effect, we don't have an actual player
			float darknessScale = this.mc.options.darknessEffectScale().get().floatValue();
			float darknessGamma = this.getDarknessGamma(0) * darknessScale;
			float effectiveDarknessScale = this.calculateDarknessScale(this.mc.player, darknessGamma, 0) * darknessScale;
			*/

            float waterVision = getWaterVision();
			/* no night vision, we don't have a player
			float nightVision = this.mc.player.hasEffect(MobEffects.NIGHT_VISION)
			 ? GameRenderer.getNightVisionScale(this.mc.player, 0)
			 : (waterVision > 0.0f && this.mc.player.hasEffect(MobEffects.CONDUIT_POWER) ? waterVision : 0.0f);
			*/
            float nightVision = 0.0f;

            Vector3f skylightColor = new Vector3f(skyLight, skyLight, 1.0f).lerp(new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);

            Vector3f finalColor = new Vector3f();
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    float skyBrightness = LightTexture.getBrightness(this.blockAccess.dimensionType(), i) * effectiveSkyLight;
                    float blockBrightnessRed = LightTexture.getBrightness(this.blockAccess.dimensionType(), j) * (blockLightRedFlicker + 1.5f);
                    float blockBrightnessGreen = blockBrightnessRed * ((blockBrightnessRed * 0.6f + 0.4f) * 0.6f + 0.4f);
                    float blockBrightnessBlue = blockBrightnessRed * (blockBrightnessRed * blockBrightnessRed * 0.6f + 0.4f);

                    finalColor.set(blockBrightnessRed, blockBrightnessGreen, blockBrightnessBlue);

                    if (dimensionInfo.forceBrightLightmap()) {
                        finalColor.lerp(new Vector3f(0.99f, 1.12f, 1.0f), 0.25f);
                        finalColor.set(Mth.clamp(finalColor.x, 0.0f, 1.0f), Mth.clamp(finalColor.y, 0.0f, 1.0f), Mth.clamp(finalColor.z, 0.0f, 1.0f));
                    } else {
                        finalColor.add(new Vector3f(skylightColor).mul(skyBrightness));
                        finalColor.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                        // no darkening from bosses
//						if (getDarkenWorldAmount() > 0.0f) {
//							finalColor.lerp(new Vector3f(finalColor).mul(0.7f, 0.6f, 0.6f), getDarkenWorldAmount());
//						}
                    }

					/* no night vision, no player
					if (nightVision > 0.0f && (w = Math.max(finalColor.x(), Math.max(finalColor.y(), finalColor.z()))) < 1.0f) {
						v = 1.0f / w;
						vector3f4 = new Vector3f(finalColor).mul(v);
						finalColor.lerp(vector3f4, nightVision);
					}
					*/

                    if (!dimensionInfo.forceBrightLightmap()) {
						/* no darkness, no player
						if (effectiveDarknessScale > 0.0f) {
							finalColor.add(-effectiveDarknessScale, -effectiveDarknessScale, -effectiveDarknessScale);
						}
						 */
                        finalColor.set(Mth.clamp(finalColor.x, 0.0f, 1.0f), Mth.clamp(finalColor.y, 0.0f, 1.0f), Mth.clamp(finalColor.z, 0.0f, 1.0f));
                    }

                    float gamma = this.mc.options.gamma().get().floatValue();
                    Vector3f vector3f5 = new Vector3f(this.notGamma(finalColor.x), this.notGamma(finalColor.y), this.notGamma(finalColor.z));
                    finalColor.lerp(vector3f5, Math.max(0.0f, gamma /*- darknessGamma*/));
                    finalColor.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
                    finalColor.set(Mth.clamp(finalColor.x, 0.0f, 1.0f), Mth.clamp(finalColor.y, 0.0f, 1.0f), Mth.clamp(finalColor.z, 0.0f, 1.0f));
                    finalColor.mul(255.0f);

                    int r = (int) finalColor.x();
                    int g = (int) finalColor.y();
                    int b = (int) finalColor.z();
                    this.lightPixels.setPixelRGBA(j, i, 0xFF000000 | b << 16 | g << 8 | r);
                }
            }

            this.lightTexture.upload();
            this.lightmapUpdateNeeded = false;
        }
    }

    private float notGamma(float f) {
        float g = 1.0f - f;
        return 1.0f - g * g * g * g;
    }

    public float getWaterVision() {
        if (!this.areEyesInFluid(FluidTags.WATER)) {
            return 0.0F;
        } else {
            if ((float) this.waterVisionTime >= 600.0F) {
                return 1.0F;
            } else {
                float f2 = Mth.clamp((float) this.waterVisionTime / 100.0F, 0.0F, 1.0F);
                float f3 = (float) this.waterVisionTime < 100.0F ? 0.0F : Mth.clamp(((float) this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
                return f2 * 0.6F + f3 * 0.39999998F;
            }
        }
    }

    public boolean areEyesInFluid(TagKey<Fluid> tagIn) {
        if (blockAccess == null) {
            return false;
        }

        Vec3 pos = getEyePos();
        BlockPos blockpos = BlockPos.containing(pos);
        FluidState fluidstate = this.blockAccess.getFluidState(blockpos);
        return isFluidTagged(fluidstate, tagIn) && pos.y < (double) ((float) blockpos.getY() + fluidstate.getAmount() + 0.11111111F);
    }

    public Vec3 getEyePos() {
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.hmd.getPosition();
    }

    private boolean isFluidTagged(Fluid fluid, TagKey<Fluid> tag) {
        // Apparently fluid tags are server side, so we have to hard-code this shit.
        // Thanks Mojang.
        if (tag == FluidTags.WATER) {
            return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
        } else if (tag == FluidTags.LAVA) {
            return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
        }
        return false;
    }

    private boolean isFluidTagged(FluidState fluidState, TagKey<Fluid> tag) {
        return isFluidTagged(fluidState.getType(), tag);
    }

    public static class MenuFogRenderer {
        public float fogRed;
        public float fogGreen;
        public float fogBlue;
        private int targetBiomeFog;
        private int previousBiomeFog;
        private long biomeChangedTime;
        private final MenuWorldRenderer menuWorldRenderer;

        public MenuFogRenderer(MenuWorldRenderer menuWorldRenderer) {
            this.menuWorldRenderer = menuWorldRenderer;
        }

        public void setupFogColor() {
            Vec3 eyePos = this.menuWorldRenderer.getEyePos();

            FogType fogType = getEyeFogType();

            if (fogType == FogType.WATER) {
                this.updateWaterFog(this.menuWorldRenderer.getLevel());
            } else if (fogType == FogType.LAVA) {
                fogRed = 0.6F;
                fogGreen = 0.1F;
                fogBlue = 0.0F;
                this.biomeChangedTime = -1L;
            } else if (fogType == FogType.POWDER_SNOW) {
                fogRed = 0.623f;
                fogGreen = 0.734f;
                fogBlue = 0.785f;
                biomeChangedTime = -1L;
                // why is this here?
                RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
            } else {
                this.updateSurfaceFog();
                this.biomeChangedTime = -1L;
            }

            float d0 = (float) ((eyePos.y + this.menuWorldRenderer.getLevel().getGround()) * this.menuWorldRenderer.getLevel().getVoidFogYFactor());

			/* no entity available
			MobEffectFogFunction mobEffectFogFunction = FogRenderer.getPriorityFogFunction(entity, f);
			if (mobEffectFogFunction != null) {
				LivingEntity livingEntity = (LivingEntity)entity;
				d0 = mobEffectFogFunction.getModifiedVoidDarkness(livingEntity, livingEntity.getEffect(mobEffectFogFunction.getMobEffect()), d0, f);
			}*/

            if (d0 < 1.0D && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
                if (d0 < 0.0F) {
                    d0 = 0.0F;
                }

                d0 = d0 * d0;
                fogRed = fogRed * d0;
                fogGreen = fogGreen * d0;
                fogBlue = fogBlue * d0;
            }

            // no boss available
			/*if (this.gameRenderer.getDarkenWorldAmount(partialTicks) > 0.0F)
			{
				float f = this.gameRenderer.getDarkenWorldAmount(partialTicks);
				fogRed = fogRed * (1.0F - f) + fogRed * 0.7F * f;
				fogGreen = fogGreen * (1.0F - f) + fogGreen * 0.6F * f;
				fogBlue = fogBlue * (1.0F - f) + fogBlue * 0.6F * f;
			}*/

            if (fogType == FogType.WATER && fogRed != 0.0f && fogGreen != 0.0f && fogBlue != 0.0f) {
                float f1 = this.menuWorldRenderer.getWaterVision();
                float f3 = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));

                fogRed = fogRed * (1.0F - f1) + fogRed * f3 * f1;
                fogGreen = fogGreen * (1.0F - f1) + fogGreen * f3 * f1;
                fogBlue = fogBlue * (1.0F - f1) + fogBlue * f3 * f1;
            }

            if (OptifineHelper.isOptifineLoaded()) {
                // custom fog colors
                if (fogType == FogType.WATER) {
                    Vec3 colUnderwater = OptifineHelper.getCustomUnderwaterColor(menuWorldRenderer.blockAccess, eyePos.x, eyePos.y, eyePos.z);
                    if (colUnderwater != null) {
                        fogRed = (float) colUnderwater.x;
                        fogGreen = (float) colUnderwater.y;
                        fogBlue = (float) colUnderwater.z;
                    }
                } else if (fogType == FogType.LAVA) {
                    Vec3 colUnderlava = OptifineHelper.getCustomUnderlavaColor(menuWorldRenderer.blockAccess, eyePos.x, eyePos.y, eyePos.z);
                    if (colUnderlava != null) {
                        fogRed = (float) colUnderlava.x;
                        fogGreen = (float) colUnderlava.y;
                        fogBlue = (float) colUnderlava.z;
                    }
                }
            }

            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
        }

        private void updateSurfaceFog() {
            float f = 0.25F + 0.75F * (float) this.menuWorldRenderer.renderDistanceChunks / 32.0F;
            f = 1.0F - (float) Math.pow(f, 0.25);
            Vec3 eyePos = this.menuWorldRenderer.getEyePos();
            Vec3 skyColor = this.menuWorldRenderer.getSkyColor(eyePos);
            if (OptifineHelper.isOptifineLoaded()) {
                if (menuWorldRenderer.blockAccess.dimensionType().effectsLocation().equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS)) {
                    skyColor = OptifineHelper.getCustomSkyColor(skyColor, menuWorldRenderer.blockAccess, eyePos.x, eyePos.y, eyePos.z);
                } else if (menuWorldRenderer.blockAccess.dimensionType().effectsLocation().equals(BuiltinDimensionTypes.END_EFFECTS)) {
                    skyColor = OptifineHelper.getCustomSkyColorEnd(skyColor);
                }
            }
            float f1 = (float) skyColor.x;
            float f2 = (float) skyColor.y;
            float f3 = (float) skyColor.z;
            Vec3 fogColor = this.menuWorldRenderer.getFogColor(eyePos);
            if (OptifineHelper.isOptifineLoaded()) {
                if (menuWorldRenderer.blockAccess.dimensionType().effectsLocation().equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS)) {
                    fogColor = OptifineHelper.getCustomFogColor(fogColor, menuWorldRenderer.blockAccess, eyePos.x, eyePos.y, eyePos.z);
                } else if (menuWorldRenderer.blockAccess.dimensionType().effectsLocation().equals(BuiltinDimensionTypes.END_EFFECTS)) {
                    fogColor = OptifineHelper.getCustomFogColorEnd(fogColor);
                } else if (menuWorldRenderer.blockAccess.dimensionType().effectsLocation().equals(BuiltinDimensionTypes.NETHER_EFFECTS)) {
                    fogColor = OptifineHelper.getCustomFogColorNether(fogColor);
                }
            }
            fogRed = (float) fogColor.x;
            fogGreen = (float) fogColor.y;
            fogBlue = (float) fogColor.z;

            if (this.menuWorldRenderer.renderDistanceChunks >= 4) {
                float d0 = Mth.sin(this.menuWorldRenderer.getSunAngle()) > 0.0F ? -1.0F : 1.0F;
                Vec3 vec3d2 = new Vec3(d0, 0.0F, 0.0F).yRot(0);
                float f5 = (float) ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.hmd.getDirection().yRot(menuWorldRenderer.worldRotation).dot(vec3d2);

                if (f5 < 0.0F) {
                    f5 = 0.0F;
                }

                if (f5 > 0.0F) {
                    float[] afloat = this.menuWorldRenderer.dimensionInfo.getSunriseColor(this.menuWorldRenderer.getTimeOfDay(), 0);

                    if (afloat != null) {
                        f5 = f5 * afloat[3];
                        fogRed = fogRed * (1.0F - f5) + afloat[0] * f5;
                        fogGreen = fogGreen * (1.0F - f5) + afloat[1] * f5;
                        fogBlue = fogBlue * (1.0F - f5) + afloat[2] * f5;
                    }
                }
            }

            fogRed += (f1 - fogRed) * f;
            fogGreen += (f2 - fogGreen) * f;
            fogBlue += (f3 - fogBlue) * f;

            float f6 = menuWorldRenderer.getRainLevel();
            if (f6 > 0.0F) {
                float f4 = 1.0F - f6 * 0.5F;
                float f8 = 1.0F - f6 * 0.4F;
                fogRed *= f4;
                fogGreen *= f4;
                fogBlue *= f8;
            }

            float f7 = menuWorldRenderer.getThunderLevel();
            if (f7 > 0.0F) {
                float f9 = 1.0F - f7 * 0.5F;
                fogRed *= f9;
                fogGreen *= f9;
                fogBlue *= f9;
            }
            biomeChangedTime = -1L;
        }

        private void updateWaterFog(LevelReader levelIn) {
            long currentTime = Util.getMillis();
            int waterFogColor = levelIn.getBiome(BlockPos.containing(this.menuWorldRenderer.getEyePos())).value().getWaterFogColor();

            if (this.biomeChangedTime < 0L) {
                targetBiomeFog = waterFogColor;
                previousBiomeFog = waterFogColor;
                biomeChangedTime = currentTime;
            }

            int k = targetBiomeFog >> 16 & 255;
            int l = targetBiomeFog >> 8 & 255;
            int i1 = targetBiomeFog & 255;
            int j1 = previousBiomeFog >> 16 & 255;
            int k1 = previousBiomeFog >> 8 & 255;
            int l1 = previousBiomeFog & 255;
            float f = Mth.clamp((float) (currentTime - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);

            float f1 = Mth.lerp(f, j1, k);
            float f2 = Mth.lerp(f, k1, l);
            float f3 = Mth.lerp(f, l1, i1);
            fogRed = f1 / 255.0F;
            fogGreen = f2 / 255.0F;
            fogBlue = f3 / 255.0F;

            if (targetBiomeFog != waterFogColor) {
                targetBiomeFog = waterFogColor;
                previousBiomeFog = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
                this.biomeChangedTime = currentTime;
            }
        }

        public void setupFog(FogRenderer.FogMode fogMode) {
            FogType fogType = getEyeFogType();

            float fogStart, fogEnd;
            FogShape fogShape = FogShape.SPHERE;

            if (fogType == FogType.LAVA) {
                fogStart = 0.25f;
                fogEnd = 1.0f;
            } else if (fogType == FogType.POWDER_SNOW) {
                fogStart = 0.0f;
                fogEnd = 2.0f;
            } else if (fogType == FogType.WATER) {
                fogStart = -8.0f;
                fogEnd = 96.0f;

                Holder<Biome> holder = menuWorldRenderer.blockAccess.getBiome(BlockPos.containing(menuWorldRenderer.getEyePos()));
                if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    fogEnd *= 0.85f;
                }
                if (fogEnd > menuWorldRenderer.renderDistance) {
                    fogEnd = menuWorldRenderer.renderDistance;
                    fogShape = FogShape.CYLINDER;
                }
            } else if (menuWorldRenderer.blockAccess.getDimensionReaderInfo().isFoggyAt(0, 0)) {
                fogStart = menuWorldRenderer.renderDistance * 0.05f;
                fogEnd = Math.min(menuWorldRenderer.renderDistance, 192.0f) * 0.5f;
            } else if (fogMode == FogRenderer.FogMode.FOG_SKY) {
                fogStart = 0.0f;
                fogEnd = menuWorldRenderer.renderDistance;
                fogShape = FogShape.CYLINDER;
            } else {
                float h = Mth.clamp(menuWorldRenderer.renderDistance / 10.0f, 4.0f, 64.0f);
                fogStart = menuWorldRenderer.renderDistance - h;
                fogEnd = menuWorldRenderer.renderDistance;
                fogShape = FogShape.CYLINDER;
            }
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.setShaderFogEnd(fogEnd);
            RenderSystem.setShaderFogShape(fogShape);
        }

        private FogType getEyeFogType() {
            FogType fogType = FogType.NONE;
            if (menuWorldRenderer.areEyesInFluid(FluidTags.WATER)) {
                fogType = FogType.WATER;
            } else if (menuWorldRenderer.areEyesInFluid(FluidTags.LAVA)) {
                fogType = FogType.LAVA;
            } else if (menuWorldRenderer.blockAccess.getBlockState(BlockPos.containing(menuWorldRenderer.getEyePos())).is(Blocks.POWDER_SNOW)) {
                fogType = FogType.POWDER_SNOW;
            }
            return fogType;
        }

        public void setupNoFog() {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        }

        public void levelFogColor() {
            RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
        }
    }

    private static class FluidStateWrapper extends FluidState {
        private final FluidState fluidState;

        @SuppressWarnings("unchecked")
        public FluidStateWrapper(FluidState fluidState) {
            super(fluidState.getType(), (Reference2ObjectArrayMap<Property<?>, Comparable<?>>) fluidState.getValues(), fluidState.propertiesCodec);

            this.fluidState = fluidState;
        }

        @Override
        public boolean is(TagKey<Fluid> tagIn) {
            // Yeah I know this is super dirty, blame Mojang for making FluidTags server-side
            if (tagIn == FluidTags.WATER) {
                return this.getType() == Fluids.WATER || this.getType() == Fluids.FLOWING_WATER;
            } else if (tagIn == FluidTags.LAVA) {
                return this.getType() == Fluids.LAVA || this.getType() == Fluids.FLOWING_LAVA;
            }
            return fluidState.is(tagIn);
        }
    }
}
