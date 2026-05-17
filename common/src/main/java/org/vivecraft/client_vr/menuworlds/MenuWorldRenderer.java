package org.vivecraft.client_vr.menuworlds;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
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
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.Xplat;
import org.vivecraft.client.extensions.BufferBuilderExtension;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.OptionInstanceExtension;
import org.vivecraft.client_vr.extensions.StateHolderExtension;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

import java.io.InputStream;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MenuWorldRenderer {
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/environment/sun.png");
    private static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/environment/end_sky.png");

    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/misc/forcefield.png");

    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/environment/rain.png");

    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/environment/snow.png");

    private final Minecraft mc;
    private DimensionSpecialEffects dimensionInfo;
    private FakeBlockAccess blockAccess;
    private final GpuTexture lightMap;
    private final GpuTextureView lightMapView;
    private final MappableRingBuffer lightMapUbo;
    private boolean lightmapUpdateNeeded;
    private float blockLightRedFlicker;
    private int waterVisionTime;

    public int ticks = 0;
    public long time = 1000;
    public boolean fastTime;
    private HashMap<ChunkSectionLayer, List<Pair<Integer, GpuBuffer>>> vertexBuffers;
    private GpuBuffer starVBO;
    private final RenderSystem.AutoStorageIndexBuffer starIndices = RenderSystem.getSequentialBuffer(
        VertexFormat.Mode.QUADS);
    private int starIndexCount;
    private GpuBuffer skyVBO;
    private GpuBuffer sky2VBO;
    private GpuBuffer endSkyVBO;
    private int renderDistance;
    private int renderDistanceChunks;
    public final MenuFogRenderer fogRenderer;
    public Set<TextureAtlasSprite> animatedSprites;
    private final Random rand;
    private boolean ready;
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
    private boolean reenableShaders = false;
    private long buildStartTime;
    private Map<BlockPos, Map<ChunkSectionLayer, BufferBuilder>> bufferBuilders;
    private Map<BlockPos, BlockPos.MutableBlockPos> currentPositions;
    private Map<BlockPos, Integer> blockCounts;
    private Map<BlockPos, Long> renderTimes;
    private final List<CompletableFuture<Void>> builderFutures = new ArrayList<>();
    private final Queue<Thread> builderThreads = new ConcurrentLinkedQueue<>();
    private Throwable builderError;

    private static boolean FIRST_RENDER_DONE;

    private boolean rendering = false;

    public MenuWorldRenderer() {
        this.mc = Minecraft.getInstance();

        this.lightMap = RenderSystem.getDevice().createTexture("MenuWOrld Light Texture",
            GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT, TextureFormat.RGBA8,
            16, 16, 1, 1);
        this.lightMap.setTextureFilter(FilterMode.LINEAR, false);
        this.lightMapView = RenderSystem.getDevice().createTextureView(this.lightMap);
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.lightMap, 0xFFFFFFFF);
        this.lightMapUbo = new MappableRingBuffer(() -> "Menuworld Lightmap UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator().putFloat().putFloat().putFloat().putInt().putFloat().putFloat().putFloat()
                .putFloat().putVec3().get());

        this.fogRenderer = new MenuFogRenderer(this);
        this.rand = new Random();
        this.rand.nextInt(); // toss some bits in the bin
    }

    public void init() {
        if (ClientDataHolderVR.getInstance().vrSettings.menuWorldSelection == VRSettings.MenuWorld.NONE) {
            VRSettings.LOGGER.info("Vivecraft: Main menu worlds disabled.");
            return;
        }

        try {
            VRSettings.LOGGER.info("Vivecraft: MenuWorlds: Initializing main menu world renderer...");
            loadRenderers();
            this.getWorldTask = CompletableFuture.supplyAsync(() -> {
                try (InputStream inputStream = MenuWorldDownloader.getRandomWorld()) {
                    VRSettings.LOGGER.info("Vivecraft: MenuWorlds: Loading world data...");
                    return inputStream != null ? MenuWorldExporter.loadWorld(inputStream) : null;
                } catch (Exception e) {
                    VRSettings.LOGGER.error(
                        "Vivecraft: Exception thrown when loading main menu world, falling back to old menu room.", e);
                    return null;
                }
            }, Util.backgroundExecutor());
        } catch (Exception e) {
            VRSettings.LOGGER.error(
                "Vivecraft: Exception thrown when initializing main menu world renderer, falling back to old menu room.",
                e);
        }
    }

    public void checkTask() {
        if (this.getWorldTask == null || !this.getWorldTask.isDone()) {
            return;
        }

        try {
            FakeBlockAccess world = this.getWorldTask.get();
            if (world != null) {
                setWorld(world);
                prepare();
            } else {
                VRSettings.LOGGER.warn("Vivecraft: Failed to load any main menu world, falling back to old menu room");
            }
        } catch (Exception e) {
            VRSettings.LOGGER.error("Vivecraft: error starting menuworld building:", e);
        } finally {
            this.getWorldTask = null;
        }
    }

    public void render(Matrix4fStack poseStack) {
        this.rendering = true;

        // temporarily disable fabulous to render the menu world
        GraphicsStatus currentGraphics = this.mc.options.graphicsMode().get();
        if (currentGraphics == GraphicsStatus.FABULOUS) {
            ((OptionInstanceExtension<GraphicsStatus>) (Object) this.mc.options.graphicsMode()).vivecraft$setWithoutUpdate(
                GraphicsStatus.FANCY);
        }

        poseStack.pushMatrix();

        // rotate World
        poseStack.rotate(Axis.YP.rotationDegrees(this.worldRotation));

        // small offset to center on source block, and add the partial block offset, this shouldn't be too noticeable on the fog
        poseStack.translate(-0.5F, -Mth.frac(this.blockAccess.getGround()), -0.5F);

        // not sure why this needs to be rotated twice, but it works
        Vec3 offset = new Vec3(0.5, -Mth.frac(this.blockAccess.getGround()), 0.5)
            .yRot(this.worldRotation * Mth.DEG_TO_RAD);
        Vec3 eyePosition = getEyePos().add(offset).yRot(-this.worldRotation * Mth.DEG_TO_RAD);

        this.fogRenderer.setFog(FogRenderer.FogMode.WORLD);

        renderSky(poseStack, eyePosition);

        renderChunkLayer(ChunkSectionLayerGroup.OPAQUE);

        float cloudHeight = this.blockAccess.dimensionType().cloudHeight().orElse(0);
        if (OptifineHelper.isOptifineLoaded()) {
            cloudHeight += (float) (OptifineHelper.getCloudHeight() * 128.0);
        }

        if (eyePosition.y + this.blockAccess.getGround() + this.blockAccess.getMinY() < cloudHeight) {
            renderClouds(eyePosition.x,
                eyePosition.y + this.blockAccess.getGround() + this.blockAccess.getMinY(),
                eyePosition.z);
        }

        renderChunkLayer(ChunkSectionLayerGroup.TRANSLUCENT);
        renderChunkLayer(ChunkSectionLayerGroup.TRIPWIRE);

        if (eyePosition.y + this.blockAccess.getGround() + this.blockAccess.getMinY() >= cloudHeight) {
            renderClouds(eyePosition.x,
                eyePosition.y + this.blockAccess.getGround() + this.blockAccess.getMinY(),
                eyePosition.z);
        }

        renderSnowAndRain(eyePosition.x, 0, eyePosition.z);

        poseStack.popMatrix();
        turnOffLightLayer();
        ((OptionInstanceExtension<GraphicsStatus>) (Object) this.mc.options.graphicsMode()).vivecraft$setWithoutUpdate(
            currentGraphics);
        this.fogRenderer.setFog(FogRenderer.FogMode.NONE);
        this.rendering = false;
    }

    private void renderChunkLayer(ChunkSectionLayerGroup group) {
        for (ChunkSectionLayer layer : group.layers()) {
            List<Pair<Integer, GpuBuffer>> buffers = this.vertexBuffers.get(layer);
            if (buffers.isEmpty()) {
                continue;
            }
            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(),
                    new Matrix4f(), 1.0f);

            for (Pair<Integer, GpuBuffer> buffer : buffers) {
                RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(
                    VertexFormat.Mode.QUADS);
                GpuBuffer indexBuffer = autoStorageIndexBuffer.getBuffer(buffer.getLeft());

                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Menuworld: " + layer.label(),
                    this.mc.mainRenderTarget.getColorTextureView(), OptionalInt.empty(),
                    this.mc.mainRenderTarget.getDepthTextureView(), OptionalDouble.empty()))
                {
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setPipeline(layer.pipeline());
                    renderPass.bindSampler("Sampler2", this.lightMapView);
                    renderPass.bindSampler("Sampler0", layer.textureView());
                    // TODO 1.21.6 maybe use drawMultipleIndexed
                    renderPass.setVertexBuffer(0, buffer.getRight());
                    renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type());
                    renderPass.drawIndexed(0, 0, buffer.getLeft(), 1);
                }
            }
        }
    }

    public boolean isRendering() {
        return this.rendering;
    }

    public void prepare() {
        if (this.vertexBuffers == null && !this.building) {
            VRSettings.LOGGER.info("Vivecraft: MenuWorlds: Building geometry...");

            // random offset to make the player fly
            if (this.rand.nextInt(1000) == 0) {
                this.blockAccess.setGroundOffset(100);
            }
            this.fastTime = new Random().nextInt(10) == 0;

            this.animatedSprites = ConcurrentHashMap.newKeySet();
            this.blockCounts = new ConcurrentHashMap<>();
            this.renderTimes = new ConcurrentHashMap<>();
            if (IrisHelper.isLoaded() && IrisHelper.isShaderActive() && IrisHelper.hasIssuesWithMenuWorld()) {
                VRSettings.LOGGER.info("Vivecraft: Temporarily disabling shaders to build Menuworld.");
                this.reenableShaders = true;
                ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.menuworldshaderdisable"));
                IrisHelper.setShadersActive(false);
            }

            try {
                this.vertexBuffers = new HashMap<>();
                this.bufferBuilders = new HashMap<>();
                this.currentPositions = new HashMap<>();
                for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
                    this.vertexBuffers.put(layer, new LinkedList<>());
                }

                for (int x = -this.blockAccess.getXSize() / 2;
                     x < this.blockAccess.getXSize() / 2; x += this.segmentSize.getX()) {
                    for (int y = (int) -this.blockAccess.getGround(); y < this.blockAccess.getYSize() -
                        (int) this.blockAccess.getGround(); y += this.segmentSize.getY()) {
                        for (int z = -this.blockAccess.getZSize() / 2;
                             z < this.blockAccess.getZSize() / 2; z += this.segmentSize.getZ()) {
                            BlockPos pos = new BlockPos(x, y, z);
                            Map<ChunkSectionLayer, BufferBuilder> bufferMap = new EnumMap<>(ChunkSectionLayer.class);

                            this.bufferBuilders.put(pos, bufferMap);
                            this.currentPositions.put(pos, pos.mutable());
                        }
                    }
                }
            } catch (OutOfMemoryError e) {
                VRSettings.LOGGER.error(
                    "Vivecraft: OutOfMemoryError while building main menu world. Low system memory or 32-bit Java?", e);
                destroy();
                return;
            } catch (NullPointerException e) {
                VRSettings.LOGGER.error("Vivecraft: Something canceled menu world building while preparing", e);
                destroy();
                return;
            }

            this.buildStartTime = ClientUtils.milliTime();
            this.building = true;
        }
    }

    private BufferBuilder getOrBeginLayer(
        Map<ChunkSectionLayer, BufferBuilder> startedLayers, ChunkSectionLayer layer)
    {
        BufferBuilder builder = startedLayers.get(layer);
        if (builder == null) {

            boolean wasSkipping = false;
            if (IrisHelper.isLoaded()) {
                wasSkipping = IrisHelper.getSkipBufferExtension();
                IrisHelper.setSkipBufferExtension(true);
            }

            // 32768 yields most efficient memory use for some reason
            builder = new BufferBuilder(new ByteBufferBuilder(32768),
                VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

            if (!wasSkipping && IrisHelper.isLoaded()) {
                IrisHelper.setSkipBufferExtension(false);
            }
            startedLayers.put(layer, builder);
        }
        return builder;
    }


    public boolean isBuilding() {
        return this.building;
    }

    public void buildNext() {
        if (!this.builderFutures.stream().allMatch(CompletableFuture::isDone) || this.builderError != null) {
            return;
        }
        this.builderFutures.clear();

        if (this.currentPositions.entrySet().stream().allMatch(entry -> entry.getValue().getY() >=
            Math.min(this.segmentSize.getY() + entry.getKey().getY(),
                this.blockAccess.getYSize() - (int) this.blockAccess.getGround())))
        {
            finishBuilding();
            return;
        }

        long startTime = ClientUtils.milliTime();
        for (var entry : this.bufferBuilders.entrySet()) {
            if (this.currentPositions.get(entry.getKey()).getY() <
                Math.min(this.segmentSize.getY() + entry.getKey().getY(),
                    this.blockAccess.getYSize() - (int) this.blockAccess.getGround()))
            {
                if (FIRST_RENDER_DONE || !SodiumHelper.isLoaded() ||
                    !SodiumHelper.hasIssuesWithParallelBlockBuilding())
                {
                    // generate the data in parallel
                    this.builderFutures.add(
                        CompletableFuture.runAsync(() -> buildGeometry(entry.getKey(), startTime, this.renderMaxTime),
                            Util.backgroundExecutor()));
                } else {
                    // generate first data in series to avoid weird class loading error
                    buildGeometry(entry.getKey(), startTime, this.renderMaxTime);
                    if (this.blockCounts.getOrDefault(entry.getKey(), 0) > 0) {
                        FIRST_RENDER_DONE = true;
                    }
                }
            }
        }

        CompletableFuture.allOf(this.builderFutures.toArray(new CompletableFuture[0]))
            .thenRunAsync(this::handleError, Util.backgroundExecutor());
    }

    private void buildGeometry(BlockPos offset, long startTime, int maxTime) {
        if (ClientUtils.milliTime() - startTime >= maxTime) {
            return;
        }

        this.builderThreads.add(Thread.currentThread());
        long realStartTime = ClientUtils.milliTime();

        try {
            PoseStack thisPose = new PoseStack();
            int renderDistSquare = (this.renderDistance + 1) * (this.renderDistance + 1);
            BlockRenderDispatcher blockRenderer = this.mc.getBlockRenderer();
            BlockPos.MutableBlockPos pos = this.currentPositions.get(offset);
            RandomSource randomSource = RandomSource.create();

            int count = 0;
            while (
                ClientUtils.milliTime() - startTime < maxTime && pos.getY() <
                    Math.min(this.segmentSize.getY() + offset.getY(),
                        this.blockAccess.getYSize() - (int) this.blockAccess.getGround()) && this.building) {
                // only build blocks not obscured by fog
                if (Mth.abs(pos.getY()) <= this.renderDistance + 1 &&
                    Mth.lengthSquared(pos.getX(), pos.getZ()) <= renderDistSquare)
                {
                    BlockState state = this.blockAccess.getBlockState(pos);
                    if (state != null) {
                        FluidState fluidState = state.getFluidState();
                        if (!fluidState.isEmpty()) {
                            for (var sprite : Xplat.getFluidTextures(this.blockAccess, pos, fluidState)) {
                                if (sprite != null && sprite.contents().getUniqueFrames().sum() > 1) {
                                    this.animatedSprites.add(sprite);
                                }
                            }
                            blockRenderer.renderLiquid(pos, this.blockAccess,
                                this.getOrBeginLayer(this.bufferBuilders.get(offset),
                                    ItemBlockRenderTypes.getRenderLayer(fluidState)), state,
                                new FluidStateWrapper(fluidState));
                            count++;
                        }

                        if (state.getRenderShape() != RenderShape.INVISIBLE) {
                            List<BlockModelPart> parts = this.mc.getModelManager().getBlockModelShaper()
                                .getBlockModel(state)
                                .collectParts(randomSource);
                            for (var modelPart : parts) {
                                for (var quad : modelPart.getQuads(null)) {
                                    if (quad.sprite().contents().getUniqueFrames().sum() > 1) {
                                        this.animatedSprites.add(quad.sprite());
                                    }
                                }
                            }
                            thisPose.pushPose();
                            thisPose.translate(pos.getX(), pos.getY(), pos.getZ());
                            blockRenderer.renderBatched(state, pos, this.blockAccess, thisPose,
                                this.getOrBeginLayer(this.bufferBuilders.get(offset),
                                    ItemBlockRenderTypes.getChunkRenderType(state)), true, parts);
                            count++;
                            thisPose.popPose();
                        }
                    }
                }

                // iterate the position
                pos.setX(pos.getX() + 1);
                if (pos.getX() >= Math.min(this.segmentSize.getX() + offset.getX(), this.blockAccess.getXSize() / 2)) {
                    pos.setX(offset.getX());
                    pos.setZ(pos.getZ() + 1);
                    if (pos.getZ() >=
                        Math.min(this.segmentSize.getZ() + offset.getZ(), this.blockAccess.getZSize() / 2))
                    {
                        pos.setZ(offset.getZ());
                        pos.setY(pos.getY() + 1);
                    }
                }
            }

            // VRSettings.LOGGER.info("Vivecraft: MenuWorlds: Built segment of {} blocks in {} layer.", count, ((RenderStateShardAccessor) layer).getName());
            this.blockCounts.put(pos, this.blockCounts.getOrDefault(pos, 0) + count);
            this.renderTimes.put(pos,
                this.renderTimes.getOrDefault(pos, 0L) + (ClientUtils.milliTime() - realStartTime));

            if (pos.getY() >= Math.min(this.segmentSize.getY() + offset.getY(),
                this.blockAccess.getYSize() - (int) this.blockAccess.getGround()))
            {
                VRSettings.LOGGER.debug("Vivecraft: MenuWorlds: Built {} blocks at {},{},{} in {} ms",
                    this.blockCounts.get(pos),
                    offset.getX(), offset.getY(), offset.getZ(),
                    this.renderTimes.get(pos));
            }
        } catch (Throwable e) { // Only effective way of preventing crash on poop computers with low heap size
            this.builderError = e;
        } finally {
            this.builderThreads.remove(Thread.currentThread());
        }
    }

    private void finishBuilding() {
        this.building = false;

        // Sort buffers from nearest to furthest
        var entryList = new ArrayList<>(this.bufferBuilders.entrySet());
        entryList.sort(Comparator.comparing(Map.Entry::getKey, (posA, posB) -> {
            Vec3i center = new Vec3i(this.segmentSize.getX() / 2, this.segmentSize.getY() / 2,
                this.segmentSize.getZ() / 2);
            double distA = posA.offset(center).distSqr(BlockPos.ZERO);
            double distB = posB.offset(center).distSqr(BlockPos.ZERO);
            return Double.compare(distA, distB);
        }));

        long totalMemory = 0;
        int count = 0;
        int total = this.bufferBuilders.values().stream().mapToInt(Map::size).sum();
        try (ByteBufferBuilder builder = new ByteBufferBuilder(32768)) {
            for (var entry : entryList) {
                for (var layerBuffer : entry.getValue().entrySet()) {
                    ChunkSectionLayer layer = layerBuffer.getKey();
                    BufferBuilder bufferBuilder = layerBuffer.getValue();
                    MeshData meshData = bufferBuilder.build();
                    if (meshData != null) {
                        if (layer.pipeline() == RenderPipelines.TRANSLUCENT) {
                            meshData.sortQuads(builder,
                                VertexSorting.byDistance(0, Mth.frac(this.blockAccess.getGround()), 0));
                        }
                        uploadGeometry(layer, meshData);
                        count++;
                    }
                    totalMemory += ((BufferBuilderExtension) bufferBuilder).vivecraft$getBufferSize();
                    ((BufferBuilderExtension) bufferBuilder).vivecraft$freeBuffer();
                }
            }
        }

        this.bufferBuilders = null;
        this.currentPositions = null;
        this.ready = true;
        VRSettings.LOGGER.info("Vivecraft: MenuWorlds: Built {} blocks in {} ms ({} ms CPU time)",
            this.blockCounts.values().stream().reduce(Integer::sum).orElse(0),
            ClientUtils.milliTime() - this.buildStartTime,
            this.renderTimes.values().stream().reduce(Long::sum).orElse(0L));
        VRSettings.LOGGER.info(
            "Vivecraft: MenuWorlds: Used {} temporary buffers ({} MiB), uploaded {} non-empty buffers",
            total,
            totalMemory / 1048576L,
            count);
        if (this.reenableShaders) {
            this.reenableShaders = false;
            IrisHelper.setShadersActive(true);
        }
    }

    public boolean isOnBuilderThread() {
        return this.builderThreads.contains(Thread.currentThread());
    }

    private void handleError() {
        if (this.builderError == null) {
            return;
        }
        if (this.builderError instanceof OutOfMemoryError || this.builderError.getCause() instanceof OutOfMemoryError) {
            VRSettings.LOGGER.error(
                "Vivecraft: OutOfMemoryError while building main menu world. Low system memory or 32-bit Java?",
                this.builderError);
        } else {
            VRSettings.LOGGER.error(
                "Vivecraft: Exception thrown when building main menu world, falling back to old menu room.:",
                this.builderError);
        }
        destroy();
        setWorld(null);
        this.builderError = null;
    }

    private void uploadGeometry(ChunkSectionLayer layer, MeshData meshData) {
        try (meshData) {
            this.vertexBuffers.get(layer).add(Pair.of(meshData.drawState().indexCount(), RenderSystem.getDevice()
                .createBuffer(null, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, meshData.vertexBuffer())));
        }
    }

    public void cancelBuilding() {
        this.building = false;
        this.builderFutures.forEach(CompletableFuture::join);
        this.builderFutures.clear();
        if (this.bufferBuilders != null) {
            this.bufferBuilders.values().stream().map(Map::values).forEach(buffers -> {
                for (BufferBuilder vertBuffer : buffers) {
                    ((BufferBuilderExtension) vertBuffer).vivecraft$freeBuffer();
                }
            });
            this.bufferBuilders = null;
        }
        this.currentPositions = null;
    }

    public void destroy() {
        cancelBuilding();
        if (this.vertexBuffers != null) {
            for (List<Pair<Integer, GpuBuffer>> buffers : this.vertexBuffers.values()) {
                for (Pair<Integer, GpuBuffer> buffer : buffers) {
                    if (buffer.getRight() != null) {
                        buffer.getRight().close();
                    }
                }
            }
            this.vertexBuffers = null;
        }
        this.animatedSprites = null;
        this.ready = false;
    }

    public void completeDestroy() {
        destroy();
        if (this.starVBO != null) {
            this.starVBO.close();
        }
        if (this.skyVBO != null) {
            this.skyVBO.close();
        }
        if (this.sky2VBO != null) {
            this.sky2VBO.close();
        }
        if (this.endSkyVBO != null) {
            this.endSkyVBO.close();
        }
        this.fogRenderer.close();
        this.lightMap.close();
        this.lightMapView.close();
        this.lightMapUbo.close();
        this.ready = false;
    }

    public void tick() {
        this.ticks++;
        this.updateTorchFlicker();

        if (this.areEyesInFluid(FluidTags.WATER)) {
            int i = 1; // this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.areEyesInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }
        if (SodiumHelper.isLoaded() && this.animatedSprites != null) {
            for (TextureAtlasSprite sprite : this.animatedSprites) {
                SodiumHelper.markTextureAsActive(sprite);
            }
        }
        if (OptifineHelper.isOptifineLoaded()) {
            for (TextureAtlasSprite sprite : this.animatedSprites) {
                OptifineHelper.markTextureAsActive(sprite);
            }
        }
    }

    public FakeBlockAccess getLevel() {
        return this.blockAccess;
    }

    public void setWorld(FakeBlockAccess blockAccess) {
        this.blockAccess = blockAccess;
        if (blockAccess != null) {
            this.dimensionInfo = blockAccess.getDimensionReaderInfo();
            this.lightmapUpdateNeeded = true;
            this.renderDistance = blockAccess.getXSize() / 2;
            this.renderDistanceChunks = this.renderDistance / 16;
            this.rainLevel = blockAccess.getRain() ? 1.0F : 0.0F;
            this.thunderLevel = blockAccess.getThunder() ? 1.0F : 0.0F;

            this.worldRotation = blockAccess.getRotation();
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

        this.generateSky();
        this.generateEndSky();
        this.generateStars();
    }

    public boolean isReady() {
        return this.ready;
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
            this.renderEndSky();
        } else if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.OVERWORLD) {

            Vec3 skyColor = this.getSkyColor(position);

            if (OptifineHelper.isOptifineLoaded()) {
                skyColor = OptifineHelper.getCustomSkyColor(skyColor, this.blockAccess, position.x, position.y,
                    position.z);
            }

            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled()) {
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .writeTransform(RenderSystem.getModelViewMatrix(),
                        new Vector4f((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1F), new Vector3f(),
                        new Matrix4f(), 0F);
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(() -> "Menuworld sky", this.mc.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        this.mc.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty()))
                {
                    renderPass.setPipeline(RenderPipelines.SKY);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, this.skyVBO);
                    renderPass.draw(0, 10);
                }
            }

            int sunriseColor = 0;
            try {
                sunriseColor = this.dimensionInfo.getSunriseOrSunsetColor(
                    this.getTimeOfDay()); // calcSunriseSunsetColors
            } catch (Exception ignore) {}

            if (sunriseColor != 0 && this.dimensionInfo.isSunriseOrSunset(this.getTimeOfDay()) &&
                (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()))
            {
                Matrix4f rotation = new Matrix4f();
                rotation.rotate(Axis.XP.rotationDegrees(90.0f));
                rotation.rotate(Axis.ZP.rotationDegrees(Mth.sin(this.getSunAngle()) < 0.0f ? 180.0f : 0.0f));
                rotation.rotate(Axis.ZP.rotationDegrees(90.0f));

                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.sunriseSunset());
                vertexConsumer
                    .addVertex(rotation, 0.0f, 100.0f, 0.0f)
                    .setColor(sunriseColor);

                for (int j = 0; j <= 16; ++j) {
                    float f6 = (float) j * Mth.TWO_PI / 16.0F;
                    float f7 = Mth.sin(f6);
                    float f8 = Mth.cos(f6);
                    vertexConsumer
                        .addVertex(rotation, f7 * 120.0F, f8 * 120.0F, -f8 * 40.0F * ARGB.alphaFloat(sunriseColor))
                        .setColor(ARGB.transparent(sunriseColor));
                }
                bufferSource.endLastBatch();
            }
            poseStack.pushMatrix();

            float skyVisibility = 1.0F - getRainLevel();
            poseStack.rotate(Axis.YP.rotationDegrees(-90.0f));

            // if (OptifineHelper.isOptifineLoaded()) {
            // needs a full Level
            // CustomSky.renderSky(this.world, poseStack, ClientUtils.getCurrentPartialTick());
            // }

            poseStack.rotate(Axis.XP.rotationDegrees(this.getTimeOfDay() * 360.0f));

            float size = 30.0F;
            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()) {
                int color = ARGB.white(skyVisibility);
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.celestial(SUN_LOCATION));
                vertexConsumer.addVertex(-size, 100.0F, -size).setUv(0.0F, 0.0F).setColor(color);
                vertexConsumer.addVertex(size, 100.0F, -size).setUv(1.0F, 0.0F).setColor(color);
                vertexConsumer.addVertex(size, 100.0F, size).setUv(1.0F, 1.0F).setColor(color);
                vertexConsumer.addVertex(-size, 100.0F, size).setUv(0.0F, 1.0F).setColor(color);
            }

            size = 20.0F;
            if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()) {
                int moonPhase = this.getMoonPhase();
                int l = moonPhase % 4;
                int i1 = moonPhase / 4 % 2;
                float u0 = (float) (l) / 4.0F;
                float v0 = (float) (i1) / 2.0F;
                float u1 = (float) (l + 1) / 4.0F;
                float v1 = (float) (i1 + 1) / 2.0F;
                int color = ARGB.white(skyVisibility);
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.celestial(MOON_LOCATION));
                vertexConsumer.addVertex(-size, -100.0f, size).setUv(u0, v1).setColor(color);
                vertexConsumer.addVertex(size, -100.0f, size).setUv(u1, v1).setColor(color);
                vertexConsumer.addVertex(size, -100.0f, -size).setUv(u1, v0).setColor(color);
                vertexConsumer.addVertex(-size, -100.0f, -size).setUv(u0, v0).setColor(color);
            }

            float starBrightness = this.getStarBrightness() * skyVisibility;

            if (starBrightness > 0.0F && (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isStarsEnabled()
            ) /*&& !CustomSky.hasSkyLayers(this.world)*/)
            {
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .writeTransform(poseStack, new Vector4f(starBrightness), new Vector3f(), new Matrix4f(), 0.0f);
                GpuBuffer indexBuffer = this.starIndices.getBuffer(this.starIndexCount);
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(() -> "Menuworld Stars", this.mc.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(), this.mc.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty()))
                {
                    renderPass.setPipeline(RenderPipelines.STARS);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, this.starVBO);
                    renderPass.setIndexBuffer(indexBuffer, this.starIndices.type());
                    renderPass.drawIndexed(0, 0, this.starIndexCount, 1);
                }
            }

            poseStack.popMatrix();
            // RenderSystem.disableTexture();

            double horizonDistance = position.y - this.blockAccess.getHorizon();

            if (horizonDistance < 0.0D) {
                Matrix4fStack stack = RenderSystem.getModelViewStack();
                stack.pushMatrix();
                stack.translate(0.0f, 12.0f, 0.0f);
                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                    .writeTransform(stack, new Vector4f(0F, 0F, 0F, 1F), new Vector3f(), new Matrix4f(), 0.0f);
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(() -> "Menuworld Dark Kky", this.mc.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        this.mc.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty()))
                {
                    renderPass.setPipeline(RenderPipelines.SKY);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                    renderPass.setVertexBuffer(0, this.sky2VBO);
                    renderPass.draw(0, 10);
                }
                stack.popMatrix();
            }
        }
    }

    private void renderEndSky() {
        if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled()) {
            AbstractTexture endSkyTexture = this.mc.getTextureManager().getTexture(END_SKY_LOCATION);
            endSkyTexture.setUseMipmaps(false);

            RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(
                VertexFormat.Mode.QUADS);
            GpuBuffer indexBuffer = autoStorageIndexBuffer.getBuffer(36);

            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1F), new Vector3f(), new Matrix4f(),
                    0F);

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Menuworld Endsky", this.mc.getMainRenderTarget().getColorTextureView(),
                    OptionalInt.empty(), this.mc.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty()))
            {
                renderPass.setPipeline(RenderPipelines.END_SKY);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                renderPass.bindSampler("Sampler0", endSkyTexture.getTextureView());
                renderPass.setVertexBuffer(0, this.endSkyVBO);
                renderPass.setIndexBuffer(indexBuffer, autoStorageIndexBuffer.type());
                renderPass.drawIndexed(0, 0, 36, 1);
            }
        }
    }

    public void renderClouds(double x, double y, double z) {
        Optional<Integer> cloudHeight = this.blockAccess.dimensionType().cloudHeight();

        if (cloudHeight.isPresent() && this.mc.options.getCloudsType() != CloudStatus.OFF) {
            // use the LevelRenderer CloudRenderer for the clouds
            this.mc.levelRenderer.getCloudRenderer()
                .render(getCloudColour(), this.mc.options.getCloudsType(), cloudHeight.get() + 0.35F, new Vec3(x, y, z),
                    this.ticks + this.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
    }

    private void renderSnowAndRain(double inX, double inY, double inZ) {
        if (getRainLevel() <= 0.0f) {
            return;
        }

        int xFloor = Mth.floor(inX);
        int yFloor = Mth.floor(inY);
        int zFloor = Mth.floor(inZ);
        VertexConsumer vertexConsumer;
        int rainDistance = Minecraft.useFancyGraphics() ? 10 : 5;
        float rainAnimationTime = this.ticks + ClientUtils.getCurrentPartialTick();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        MultiBufferSource.BufferSource bufferSource = this.mc.renderBuffers().bufferSource();

        for (int rainZ = zFloor - rainDistance; rainZ <= zFloor + rainDistance; ++rainZ) {
            for (int rainX = xFloor - rainDistance; rainX <= xFloor + rainDistance; ++rainX) {
                int q = (rainZ - zFloor + 16) * 32 + rainX - xFloor + 16;
                double r = (double) this.rainSizeX[q] * 0.5;
                double s = (double) this.rainSizeZ[q] * 0.5;
                mutableBlockPos.set(rainX, inY, rainZ);
                Biome biome = this.blockAccess.getBiome(mutableBlockPos).value();
                if (!biome.hasPrecipitation()) {
                    continue;
                }

                int blockingHeight = this.blockAccess.getHeightBlocking(rainX, rainZ);
                int lower = Math.max(yFloor - rainDistance, blockingHeight);
                int upper = Math.max(yFloor + rainDistance, blockingHeight);

                if (lower == upper) {
                    // no rain
                    continue;
                }
                int rainY = Math.max(blockingHeight, yFloor);

                RandomSource randomSource = RandomSource.create(
                    rainX * rainX * 3121L + rainX * 45238971L ^ rainZ * rainZ * 418711L + rainZ * 13761L);
                mutableBlockPos.setY(lower);
                Biome.Precipitation precipitation = biome.getPrecipitationAt(mutableBlockPos,
                    this.blockAccess.getSeaLevel());
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

                int skyLight = this.blockAccess.getBrightness(LightLayer.SKY, mutableBlockPos) << 4;
                int blockLight = this.blockAccess.getBrightness(LightLayer.BLOCK, mutableBlockPos) << 4;

                if (precipitation == Biome.Precipitation.RAIN) {
                    vertexConsumer = bufferSource.getBuffer(VRRenderTypes.weatherNoLightmapChange(RAIN_LOCATION));

                    blend = ((1.0f - distance * distance) * 0.5f + 0.5f);
                    int x =
                        this.ticks + rainX * rainX * 3121 + rainX * 45238971 + rainZ * rainZ * 418711 +
                            rainZ * 13761 &
                            0x1F;
                    yOffset =
                        -((float) x + ClientUtils.getCurrentPartialTick()) / 32.0f *
                            (3.0f + randomSource.nextFloat());
                } else if (precipitation == Biome.Precipitation.SNOW) {
                    vertexConsumer = bufferSource.getBuffer(VRRenderTypes.weatherNoLightmapChange(SNOW_LOCATION));

                    blend = ((1.0f - distance * distance) * 0.3f + 0.5f);
                    xOffset = (float) (randomSource.nextDouble() +
                        (double) rainAnimationTime * 0.01 * (double) ((float) randomSource.nextGaussian())
                    );
                    float ae = -((float) (this.ticks & 0x1FF) + ClientUtils.getCurrentPartialTick()) / 512.0f;
                    float af = (float) (randomSource.nextDouble() +
                        (double) (rainAnimationTime * (float) randomSource.nextGaussian()) * 0.001
                    );
                    yOffset = ae + af;

                    // snow is brighter
                    skyLight = (skyLight * 3 + 240) / 4;
                    blockLight = (blockLight * 3 + 240) / 4;
                } else {
                    continue;
                }
                vertexConsumer
                    .addVertex((float) (localX - r), upper - (float) inY, (float) (localZ - s))
                    .setUv(0.0f + xOffset, (float) lower * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                vertexConsumer
                    .addVertex((float) (localX + r), upper - (float) inY, (float) (localZ + s))
                    .setUv(1.0f + xOffset, (float) lower * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                vertexConsumer
                    .addVertex((float) (localX + r), lower - (float) inY, (float) (localZ + s))
                    .setUv(1.0f + xOffset, (float) upper * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
                vertexConsumer
                    .addVertex((float) (localX - r), lower - (float) inY, (float) (localZ - s))
                    .setUv(0.0f + xOffset, (float) upper * 0.25f + yOffset)
                    .setColor(1.0f, 1.0f, 1.0f, blend).setUv2(blockLight, skyLight);
            }
        }
        // use our lightmap
        turnOnLightLayer();
        bufferSource.endBatch();
        turnOffLightLayer();
    }

    public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int i = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
        int j = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
        return i << 20 | j << 4;
    }

    public float getTimeOfDay() {
        return this.blockAccess.dimensionType().timeOfDay(this.time);
    }

    public float getSunAngle() {
        float dayTime = this.getTimeOfDay();
        return dayTime * Mth.TWO_PI;
    }

    public int getMoonPhase() {
        return this.blockAccess.dimensionType().moonPhase(this.time);
    }

    public float getSkyDarken() {
        float dayTime = this.getTimeOfDay();
        float h = 1.0f - (Mth.cos(dayTime * Mth.TWO_PI) * 2.0f + 0.2f);
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
        float f1 = 1.0F - (Mth.cos(f * Mth.TWO_PI) * 2.0F + 0.25F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    public Vec3 getSkyColor(Vec3 position) {
        float dayTime = this.getTimeOfDay();

        Vec3 samplePosition = position.subtract(2.0, 2.0, 2.0).scale(0.25);

        Vec3 skyColor = CubicSampler.gaussianSampleVec3(samplePosition, (i, j, k) -> Vec3.fromRGB24(
            this.blockAccess.getBiomeManager().getNoiseBiomeAtQuart(i, j, k).value().getSkyColor()));

        float h = Mth.cos(dayTime * Mth.TWO_PI) * 2.0f + 0.5f;
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
        if (!this.mc.options.hideLightningFlash().get() && this.skyFlashTime > 0) {
            float flash = (float) this.skyFlashTime - ClientUtils.getCurrentPartialTick();
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
        float f = Mth.clamp(Mth.cos(this.getTimeOfDay() * Mth.TWO_PI) * 2.0F + 0.5F, 0.0F, 1.0F);
        Vec3 scaledPos = pos.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        return CubicSampler.gaussianSampleVec3(scaledPos,
            (x, y, z) -> this.dimensionInfo.getBrightnessDependentFogColor(
                Vec3.fromRGB24(this.blockAccess.getBiomeManager().getNoiseBiomeAtQuart(x, y, z).value().getFogColor()),
                f));
    }

    public int getCloudColour() {
        int color = -1;
        float rain = this.getRainLevel();
        if (rain > 0.0F) {
            int j = ARGB.scaleRGB(ARGB.greyscale(color), 0.6F);
            color = ARGB.lerp(rain * 0.95F, color, j);
        }

        float dayTime = this.getTimeOfDay();
        float k = Mth.cos(dayTime * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
        k = Mth.clamp(k, 0.0F, 1.0F);
        color = ARGB.multiply(color, ARGB.colorFromFloat(1.0F, k * 0.9F + 0.1F, k * 0.9F + 0.1F, k * 0.85F + 0.15F));
        float thunder = this.getThunderLevel();
        if (thunder > 0.0F) {
            int greyColor = ARGB.scaleRGB(ARGB.greyscale(color), 0.2F);
            color = ARGB.lerp(thunder * 0.95F, color, greyColor);
        }

        return color;
    }

    private void generateSky() {
        if (this.skyVBO != null) {
            this.skyVBO.close();
        }
        if (this.sky2VBO != null) {
            this.sky2VBO.close();
        }
        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(
            10 * DefaultVertexFormat.POSITION.getVertexSize()))
        {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN,
                DefaultVertexFormat.POSITION);
            buildSkyDisc(bufferBuilder, 16.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                this.skyVBO = RenderSystem.getDevice()
                    .createBuffer(() -> "Top sky vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        meshData.vertexBuffer());
            }

            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN,
                DefaultVertexFormat.POSITION);
            buildSkyDisc(bufferBuilder, -16.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                this.sky2VBO = RenderSystem.getDevice()
                    .createBuffer(() -> "Bottom sky vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        meshData.vertexBuffer());
            }
        }
    }

    private void generateEndSky() {
        if (this.endSkyVBO != null) {
            this.endSkyVBO.close();
        }
        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(
            24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()))
        {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = 0; i < 6; ++i) {
                Matrix4f matrix = new Matrix4f();
                switch (i) {
                    case 1 -> matrix.rotationX(Mth.HALF_PI);
                    case 2 -> matrix.rotationX(-Mth.HALF_PI);
                    case 3 -> matrix.rotationX(Mth.PI);
                    case 4 -> matrix.rotationZ(Mth.HALF_PI);
                    case 5 -> matrix.rotationZ(-Mth.HALF_PI);
                }

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
                bufferBuilder.addVertex(matrix, -100.0f, -100.0f, -100.0f)
                    .setUv(0.0f, 0.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(matrix, -100.0f, -100.0f, 100.0f)
                    .setUv(0.0f, 16.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(matrix, 100.0f, -100.0f, 100.0f)
                    .setUv(16.0f, 16.0f).setColor(r, g, b, 255);
                bufferBuilder.addVertex(matrix, 100.0f, -100.0f, -100.0f)
                    .setUv(16.0f, 0.0f).setColor(r, g, b, 255);
            }

            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                this.endSkyVBO = RenderSystem.getDevice()
                    .createBuffer(() -> "End sky vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        meshData.vertexBuffer());
            }
        }
    }

    private static void buildSkyDisc(VertexConsumer vertexConsumer, float posY) {
        float g = Math.signum(posY) * 512.0f;
        vertexConsumer.addVertex(0.0F, posY, 0.0F);
        for (int i = -180; i <= 180; i += 45) {
            vertexConsumer.addVertex(g * Mth.cos((float) i * Mth.DEG_TO_RAD), posY,
                512.0f * Mth.sin((float) i * Mth.DEG_TO_RAD));
        }
    }

    private void generateStars() {
        if (this.starVBO != null) {
            this.starVBO.close();
        }
        this.starVBO = this.buildStars();
    }

    private GpuBuffer buildStars() {
        RandomSource randomSource = RandomSource.create(10842L);
        int starCount = 1500;
        float starDistance = 100.0F;

        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(
            DefaultVertexFormat.POSITION.getVertexSize() * starCount * 4))
        {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION);
            for (int i = 0; i < starCount; i++) {
                Vector3f starPoint = new Vector3f(randomSource.nextFloat(), randomSource.nextFloat(),
                    randomSource.nextFloat()).mul(2.0F).sub(1.0F, 1.0F, 1.0F);
                float starSize = 0.15F + randomSource.nextFloat() * 0.1F;
                float distance = starPoint.lengthSquared();
                if (distance <= 0.010000001F || distance >= 1.0F) continue;

                starPoint.normalize(starDistance);
                float starRotation = (float) (randomSource.nextDouble() * Math.PI * 2.0);

                Matrix3f rotation = new Matrix3f()
                    .rotateTowards(starPoint.negate(new Vector3f()), new Vector3f(0.0f, 1.0f, 0.0f))
                    .rotateZ(-starRotation);

                bufferBuilder.addVertex(new Vector3f(starSize, -starSize, 0.0f).mul(rotation).add(starPoint));
                bufferBuilder.addVertex(new Vector3f(starSize, starSize, 0.0f).mul(rotation).add(starPoint));
                bufferBuilder.addVertex(new Vector3f(-starSize, starSize, 0.0f).mul(rotation).add(starPoint));
                bufferBuilder.addVertex(new Vector3f(-starSize, -starSize, 0.0f).mul(rotation).add(starPoint));
            }
            try (MeshData meshData = bufferBuilder.buildOrThrow()) {
                this.starIndexCount = meshData.drawState().indexCount();
                return RenderSystem.getDevice()
                    .createBuffer(() -> "Stars vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        meshData.vertexBuffer());
            }
        }
    }

    public void turnOffLightLayer() {
        RenderSystem.setShaderTexture(2, null);
    }

    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.lightMapView);
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

				if (CustomColors.updateLightmap(world, this.torchFlickerX, this.nativeImage, flag, partialTick))
				{
					this.dynamicTexture.updateDynamicTexture();
					this.needsUpdate = false;
					this.client.profiler.endSection();
					return;
				}
			}*/

            float skyLight = getSkyDarken();
            float effectiveSkyLight = this.skyFlashTime > 0 ? 1.0f : skyLight * 0.95F + 0.05F;

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

            RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(
                VertexFormat.Mode.QUADS);

            try (GpuBuffer.MappedView buffer = RenderSystem.getDevice().createCommandEncoder()
                .mapBuffer(this.lightMapUbo.currentBuffer(), false, true))
            {
                Std140Builder.intoBuffer(buffer.data())
                    .putFloat(this.blockAccess.dimensionType().ambientLight())
                    .putFloat(effectiveSkyLight)
                    .putFloat(this.blockLightRedFlicker + 1.5f)
                    .putInt(this.dimensionInfo.forceBrightLightmap() ? 1 : 0)
                    .putFloat(nightVision)
                    .putFloat(0F) // darkness factor
                    .putFloat(0F) // boss darkenworld factor
                    .putFloat(Math.max(0.0F, this.mc.options.gamma().get().floatValue()))
                    .putVec3(skylightColor);
            }

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Menuworld Lightmap", this.lightMapView, OptionalInt.empty()))
            {
                renderPass.setPipeline(RenderPipelines.LIGHTMAP);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("LightmapInfo", this.lightMapUbo.currentBuffer());
                renderPass.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());
                renderPass.setIndexBuffer(autoStorageIndexBuffer.getBuffer(6), autoStorageIndexBuffer.type());
                renderPass.drawIndexed(0, 0, 6, 1);
            }
            this.lightMapUbo.rotate();
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
                float f3 = (float) this.waterVisionTime < 100.0F ? 0.0F :
                    Mth.clamp(((float) this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
                return f2 * 0.6F + f3 * 0.39999998F;
            }
        }
    }

    public boolean areEyesInFluid(TagKey<Fluid> tagIn) {
        if (this.blockAccess == null) {
            return false;
        }

        Vec3 pos = getEyePos();
        BlockPos blockpos = BlockPos.containing(pos);
        FluidState fluidstate = this.blockAccess.getFluidState(blockpos);
        return isFluidTagged(fluidstate, tagIn) &&
            pos.y < (double) ((float) blockpos.getY() + fluidstate.getAmount() + 0.11111111F);
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
        public Vector4f fogColor = new Vector4f(0F, 0F, 0F, 1.0F);

        private int targetBiomeFog;
        private int previousBiomeFog;
        private long biomeChangedTime;
        private final MenuWorldRenderer menuWorldRenderer;
        private final GpuBuffer emptyBuffer;
        private final MappableRingBuffer fogBuffer;

        public MenuFogRenderer(MenuWorldRenderer menuWorldRenderer) {
            this.menuWorldRenderer = menuWorldRenderer;
            this.fogBuffer = new MappableRingBuffer(() -> "Menuworld Fog UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, FogRenderer.FOG_UBO_SIZE);
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                ByteBuffer byteBuffer = memoryStack.malloc(FogRenderer.FOG_UBO_SIZE);
                Std140Builder.intoBuffer(byteBuffer).putVec4(new Vector4f(0.0f)).putFloat(Float.MAX_VALUE)
                    .putFloat(Float.MAX_VALUE).putFloat(Float.MAX_VALUE).putFloat(Float.MAX_VALUE)
                    .putFloat(Float.MAX_VALUE).putFloat(Float.MAX_VALUE);
                this.emptyBuffer = RenderSystem.getDevice()
                    .createBuffer(() -> "Menuworld Empty fog", GpuBuffer.USAGE_UNIFORM, byteBuffer.flip());
            }
        }

        public void close() {
            this.emptyBuffer.close();
            this.fogBuffer.close();
        }

        public void setupFogColor() {
            Vec3 eyePos = this.menuWorldRenderer.getEyePos();

            FogType fogType = getEyeFogType();

            if (fogType == FogType.WATER) {
                this.updateWaterFog(this.menuWorldRenderer.getLevel());
            } else if (fogType == FogType.LAVA) {
                this.fogColor.x = 0.6F;
                this.fogColor.y = 0.1F;
                this.fogColor.z = 0.0F;
                this.biomeChangedTime = -1L;
            } else if (fogType == FogType.POWDER_SNOW) {
                this.fogColor.x = 0.623f;
                this.fogColor.y = 0.734f;
                this.fogColor.z = 0.785f;
                this.biomeChangedTime = -1L;
            } else {
                this.updateSurfaceFog();
                this.biomeChangedTime = -1L;
            }

            float d0 = (float) ((eyePos.y + this.menuWorldRenderer.getLevel().getGround()) *
                this.menuWorldRenderer.getLevel().getVoidFogYFactor()
            );

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
                this.fogColor.x = this.fogColor.x * d0;
                this.fogColor.y = this.fogColor.y * d0;
                this.fogColor.z = this.fogColor.z * d0;
            }

            // no boss available
			/*if (this.gameRenderer.getDarkenWorldAmount(partialTick) > 0.0F)
			{
				float f = this.gameRenderer.getDarkenWorldAmount(partialTick);
				fogRed = fogRed * (1.0F - f) + fogRed * 0.7F * f;
				fogGreen = fogGreen * (1.0F - f) + fogGreen * 0.6F * f;
				fogBlue = fogBlue * (1.0F - f) + fogBlue * 0.6F * f;
			}*/

            if (fogType == FogType.WATER && this.fogColor.x != 0.0f && this.fogColor.y != 0.0f &&
                this.fogColor.z != 0.0f)
            {
                float f1 = this.menuWorldRenderer.getWaterVision();
                float f3 = Math.min(1.0f / this.fogColor.x, Math.min(1.0f / this.fogColor.y, 1.0f / this.fogColor.z));

                this.fogColor.x = this.fogColor.x * (1.0F - f1) + this.fogColor.x * f3 * f1;
                this.fogColor.y = this.fogColor.y * (1.0F - f1) + this.fogColor.y * f3 * f1;
                this.fogColor.z = this.fogColor.z * (1.0F - f1) + this.fogColor.z * f3 * f1;
            }

            if (OptifineHelper.isOptifineLoaded()) {
                // custom fog colors
                if (fogType == FogType.WATER) {
                    Vec3 colUnderwater = OptifineHelper.getCustomUnderwaterColor(this.menuWorldRenderer.blockAccess,
                        eyePos.x, eyePos.y, eyePos.z);
                    if (colUnderwater != null) {
                        this.fogColor.x = (float) colUnderwater.x;
                        this.fogColor.y = (float) colUnderwater.y;
                        this.fogColor.z = (float) colUnderwater.z;
                    }
                } else if (fogType == FogType.LAVA) {
                    Vec3 colUnderlava = OptifineHelper.getCustomUnderlavaColor(this.menuWorldRenderer.blockAccess,
                        eyePos.x, eyePos.y, eyePos.z);
                    if (colUnderlava != null) {
                        this.fogColor.x = (float) colUnderlava.x;
                        this.fogColor.y = (float) colUnderlava.y;
                        this.fogColor.z = (float) colUnderlava.z;
                    }
                }
            }
        }

        private void updateSurfaceFog() {
            float f = 0.25F + 0.75F * (float) this.menuWorldRenderer.renderDistanceChunks / 32.0F;
            f = 1.0F - (float) Math.pow(f, 0.25);
            Vec3 eyePos = this.menuWorldRenderer.getEyePos();
            Vec3 skyColor = this.menuWorldRenderer.getSkyColor(eyePos);
            if (OptifineHelper.isOptifineLoaded()) {
                if (this.menuWorldRenderer.blockAccess.dimensionType().effectsLocation()
                    .equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS))
                {
                    skyColor = OptifineHelper.getCustomSkyColor(skyColor, this.menuWorldRenderer.blockAccess, eyePos.x,
                        eyePos.y, eyePos.z);
                } else if (this.menuWorldRenderer.blockAccess.dimensionType().effectsLocation()
                    .equals(BuiltinDimensionTypes.END_EFFECTS))
                {
                    skyColor = OptifineHelper.getCustomSkyColorEnd(skyColor);
                }
            }
            float skyRed = (float) skyColor.x;
            float skyGreen = (float) skyColor.y;
            float skyBlue = (float) skyColor.z;
            Vec3 fogColor = this.menuWorldRenderer.getFogColor(eyePos);
            if (OptifineHelper.isOptifineLoaded()) {
                if (this.menuWorldRenderer.blockAccess.dimensionType().effectsLocation()
                    .equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS))
                {
                    fogColor = OptifineHelper.getCustomFogColor(fogColor, this.menuWorldRenderer.blockAccess, eyePos.x,
                        eyePos.y, eyePos.z);
                } else if (this.menuWorldRenderer.blockAccess.dimensionType().effectsLocation()
                    .equals(BuiltinDimensionTypes.END_EFFECTS))
                {
                    fogColor = OptifineHelper.getCustomFogColorEnd(fogColor);
                } else if (this.menuWorldRenderer.blockAccess.dimensionType().effectsLocation()
                    .equals(BuiltinDimensionTypes.NETHER_EFFECTS))
                {
                    fogColor = OptifineHelper.getCustomFogColorNether(fogColor);
                }
            }
            this.fogColor.x = (float) fogColor.x;
            this.fogColor.y = (float) fogColor.y;
            this.fogColor.z = (float) fogColor.z;

            if (this.menuWorldRenderer.renderDistanceChunks >= 4) {
                float d0 = Mth.sin(this.menuWorldRenderer.getSunAngle()) > 0.0F ? -1.0F : 1.0F;
                float f5 = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_post.hmd.getDirection()
                    .rotateY(-this.menuWorldRenderer.worldRotation * Mth.DEG_TO_RAD).dot(d0, 0, 0);

                if (f5 < 0.0F) {
                    f5 = 0.0F;
                }

                if (f5 > 0.0F &&
                    this.menuWorldRenderer.dimensionInfo.isSunriseOrSunset(this.menuWorldRenderer.getTimeOfDay()))
                {
                    int sunriseColor = 0;
                    try {
                        sunriseColor = this.menuWorldRenderer.dimensionInfo.getSunriseOrSunsetColor(
                            this.menuWorldRenderer.getTimeOfDay());
                    } catch (Exception ignore) {}

                    if (sunriseColor != 0) {
                        f5 = f5 * ARGB.alphaFloat(sunriseColor);
                        this.fogColor.x = this.fogColor.x * (1.0F - f5) + ARGB.redFloat(sunriseColor) * f5;
                        this.fogColor.y =
                            this.fogColor.y * (1.0F - f5) + ARGB.greenFloat(sunriseColor) * f5;
                        this.fogColor.z = this.fogColor.z * (1.0F - f5) + ARGB.blueFloat(sunriseColor) * f5;
                    }
                }
            }

            this.fogColor.x += (skyRed - this.fogColor.x) * f;
            this.fogColor.y += (skyGreen - this.fogColor.y) * f;
            this.fogColor.z += (skyBlue - this.fogColor.z) * f;

            float f6 = this.menuWorldRenderer.getRainLevel();
            if (f6 > 0.0F) {
                float f4 = 1.0F - f6 * 0.5F;
                float f8 = 1.0F - f6 * 0.4F;
                this.fogColor.x *= f4;
                this.fogColor.y *= f4;
                this.fogColor.z *= f8;
            }

            float f7 = this.menuWorldRenderer.getThunderLevel();
            if (f7 > 0.0F) {
                float f9 = 1.0F - f7 * 0.5F;
                this.fogColor.x *= f9;
                this.fogColor.y *= f9;
                this.fogColor.z *= f9;
            }
            this.biomeChangedTime = -1L;
        }

        private void updateWaterFog(LevelReader levelIn) {
            long currentTime = Util.getMillis();
            int waterFogColor = levelIn.getBiome(BlockPos.containing(this.menuWorldRenderer.getEyePos())).value()
                .getWaterFogColor();

            if (this.biomeChangedTime < 0L) {
                this.targetBiomeFog = waterFogColor;
                this.previousBiomeFog = waterFogColor;
                this.biomeChangedTime = currentTime;
            }

            int k = this.targetBiomeFog >> 16 & 255;
            int l = this.targetBiomeFog >> 8 & 255;
            int i1 = this.targetBiomeFog & 255;
            int j1 = this.previousBiomeFog >> 16 & 255;
            int k1 = this.previousBiomeFog >> 8 & 255;
            int l1 = this.previousBiomeFog & 255;
            float f = Mth.clamp((float) (currentTime - this.biomeChangedTime) / 5000.0F, 0.0F, 1.0F);

            float f1 = Mth.lerp(f, j1, k);
            float f2 = Mth.lerp(f, k1, l);
            float f3 = Mth.lerp(f, l1, i1);
            this.fogColor.x = f1 / 255.0F;
            this.fogColor.y = f2 / 255.0F;
            this.fogColor.z = f3 / 255.0F;

            if (this.targetBiomeFog != waterFogColor) {
                this.targetBiomeFog = waterFogColor;
                this.previousBiomeFog = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
                this.biomeChangedTime = currentTime;
            }
        }

        public void updateFog() {
            FogType fogType = getEyeFogType();

            FogData fogData = new FogData();

            if (fogType == FogType.LAVA) {
                fogData.environmentalStart = 0.25f;
                fogData.environmentalEnd = 1.0f;
            } else if (fogType == FogType.POWDER_SNOW) {
                fogData.environmentalStart = 0.0f;
                fogData.environmentalEnd = 2.0f;
            } else if (fogType == FogType.WATER) {
                fogData.environmentalStart = -8.0f;
                fogData.environmentalEnd = 96.0f;

                Holder<Biome> holder = this.menuWorldRenderer.blockAccess.getBiome(
                    BlockPos.containing(this.menuWorldRenderer.getEyePos()));
                if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    fogData.environmentalEnd *= 0.85f;
                }
            } else if (this.menuWorldRenderer.blockAccess.getDimensionReaderInfo().isFoggyAt(0, 0)) {
                fogData.environmentalStart = this.menuWorldRenderer.renderDistance * 0.05f;
                fogData.environmentalEnd = Math.min(this.menuWorldRenderer.renderDistance, 192.0f) * 0.5f;
            } else if (fogType == FogType.ATMOSPHERIC) {
                // rain
                BlockPos center = BlockPos.containing(0, 0, 0);
                Biome biome = this.menuWorldRenderer.blockAccess.getBiome(center).value();
                float lightAmount = Mth.clamp(
                    (this.menuWorldRenderer.blockAccess.getBrightness(LightLayer.SKY, center) - 8.0f
                    ) / 7.0f, 0.0f, 1.0f);
                float fog = this.menuWorldRenderer.rainLevel * lightAmount * (biome.hasPrecipitation() ? 1.0f : 0.5f);
                fogData.environmentalStart = fog * -160.0f;
                fogData.environmentalEnd = 1024.0f - 256.0f * fog;
                fogData.skyEnd = this.menuWorldRenderer.renderDistance;
                fogData.cloudEnd = Minecraft.getInstance().options.cloudRange().get() * 16;
            }

            if (fogType != FogType.ATMOSPHERIC) {
                fogData.skyEnd = fogData.environmentalEnd;
                fogData.cloudEnd = fogData.environmentalEnd;
            }

            float h = Mth.clamp(this.menuWorldRenderer.renderDistance / 10.0f, 4.0f, 64.0f);
            fogData.renderDistanceStart = this.menuWorldRenderer.renderDistance - h;
            fogData.renderDistanceEnd = this.menuWorldRenderer.renderDistance;

            try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
                .mapBuffer(this.fogBuffer.currentBuffer(), false, true))
            {
                mappedView.data().position(0);
                Std140Builder.intoBuffer(mappedView.data()).putVec4(this.fogColor).putFloat(fogData.environmentalStart)
                    .putFloat(fogData.environmentalEnd).putFloat(fogData.renderDistanceStart)
                    .putFloat(fogData.renderDistanceEnd).putFloat(fogData.skyEnd).putFloat(fogData.cloudEnd);
            }
        }

        private FogType getEyeFogType() {
            FogType fogType = FogType.ATMOSPHERIC;
            if (this.menuWorldRenderer.areEyesInFluid(FluidTags.WATER)) {
                fogType = FogType.WATER;
            } else if (this.menuWorldRenderer.areEyesInFluid(FluidTags.LAVA)) {
                fogType = FogType.LAVA;
            } else if (this.menuWorldRenderer.blockAccess.getBlockState(
                BlockPos.containing(this.menuWorldRenderer.getEyePos())).is(Blocks.POWDER_SNOW))
            {
                fogType = FogType.POWDER_SNOW;
            }
            return fogType;
        }

        public GpuBufferSlice getBuffer(FogRenderer.FogMode fogMode) {
            return switch (fogMode) {
                case NONE -> this.emptyBuffer.slice(0, FogRenderer.FOG_UBO_SIZE);
                case WORLD -> this.fogBuffer.currentBuffer().slice(0, FogRenderer.FOG_UBO_SIZE);
            };
        }

        public void setFog(FogRenderer.FogMode fogMode) {
            RenderSystem.setShaderFog(getBuffer(fogMode));
        }
    }

    private static class FluidStateWrapper extends FluidState {
        private final FluidState fluidState;

        @SuppressWarnings("unchecked")
        public FluidStateWrapper(FluidState fluidState) {
            // need to do it this way, because FerriteCore changes the field type, which would error on a cast
            super(fluidState.getType(), null, fluidState.propertiesCodec);
            ((StateHolderExtension) (this)).vivecraft$setValues(fluidState.getValues());

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
            return this.fluidState.is(tagIn);
        }
    }
}
