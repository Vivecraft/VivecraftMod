package org.vivecraft.client_vr.menuworlds;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MenuWorldRenderer {
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");

	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");

	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");

	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");

	private Minecraft mc;
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
	private HashMap<RenderType, VertexBuffer> vertexBuffers;
	private VertexBuffer starVBO;
	private VertexBuffer skyVBO;
	private VertexBuffer sky2VBO;
	private VertexBuffer cloudVBO;
	private int renderDistance;
	private int renderDistanceChunks;
	public MenuFogRenderer fogRenderer;
	public Set<TextureAtlasSprite> visibleTextures = new HashSet<>();
	private Random rand;
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

	private Set<TextureAtlasSprite> animatedSprites = null;

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
			InputStream inputStream = MenuWorldDownloader.getRandomWorld();
			if (inputStream != null) {
				VRSettings.logger.info("MenuWorlds: Initializing main menu world renderer...");
				loadRenderers();
				VRSettings.logger.info("MenuWorlds: Loading world data...");
				setWorld(MenuWorldExporter.loadWorld(inputStream));
				prepare();
				fastTime = new Random().nextInt(10) == 0;
			} else {
				VRSettings.logger.warn("Failed to load any main menu world, falling back to old menu room");
			}
		} catch (Throwable e) { // Only effective way of preventing crash on poop computers with low heap size
			if (e instanceof OutOfMemoryError || e.getCause() instanceof OutOfMemoryError) {
				VRSettings.logger.error("OutOfMemoryError while loading main menu world. Low heap size or 32-bit Java?");
			} else {
				VRSettings.logger.error("Exception thrown when loading main menu world, falling back to old menu room. \n {}", e.getMessage());
			}
			e.printStackTrace();
			destroy();
			setWorld(null);
		}
	}


	public void render(PoseStack poseStack) {

		// temporarily disable fabulous to render the menu world
		GraphicsStatus current = mc.options.graphicsMode().get();
		if (current == GraphicsStatus.FABULOUS) {
			mc.options.graphicsMode().set(GraphicsStatus.FANCY);
		}

		turnOnLightLayer();

		poseStack.pushPose();

		//rotate World
		poseStack.mulPose(Vector3f.YP.rotationDegrees(worldRotation));

		// small offset to center on source block, and add the partial block offset, this shouldn't be too noticable on the fog
		poseStack.translate(-0.5,-blockAccess.getGround()+(int)blockAccess.getGround(),-0.5);

		// not sure why this needs to be rotated twice, but it works
		Vec3 offset = new Vec3(0.5,-blockAccess.getGround()+(int)blockAccess.getGround(),0.5).yRot(worldRotation*0.0174533f);
		Vec3 eyePosition = getEyePos().add(offset).yRot(-worldRotation*0.0174533f);

		fogRenderer.levelFogColor();

		renderSky(poseStack, eyePosition);

		fogRenderer.setupFog(FogRenderer.FogMode.FOG_TERRAIN);

		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.enableBlend();

		Matrix4f modelView = poseStack.last().pose();
		Matrix4f projection = RenderSystem.getProjectionMatrix();

		RenderSystem.disableBlend();

		renderChunkLayer(RenderType.solid(), modelView, projection);
		renderChunkLayer(RenderType.cutoutMipped(), modelView, projection);
		renderChunkLayer(RenderType.cutout(), modelView, projection);

		RenderSystem.enableBlend();

		float cloudHeight = this.dimensionInfo.getCloudHeight();
		if (OptifineHelper.isOptifineLoaded()) {
			cloudHeight += OptifineHelper.getCloudHeight() * 128.0;
		}

		if (eyePosition.y+blockAccess.getGround()+blockAccess.getMinBuildHeight() < cloudHeight) {
			renderClouds(poseStack, eyePosition.x, eyePosition.y+blockAccess.getGround()+blockAccess.getMinBuildHeight(), eyePosition.z);
		}

		renderChunkLayer(RenderType.translucent(), modelView, projection);
		renderChunkLayer(RenderType.tripwire(), modelView, projection);

		if (eyePosition.y+blockAccess.getGround()+blockAccess.getMinBuildHeight() >= cloudHeight) {
			renderClouds(poseStack, eyePosition.x, eyePosition.y+blockAccess.getGround()+blockAccess.getMinBuildHeight(), eyePosition.z);
		}

		RenderSystem.depthMask(false);
		renderSnowAndRain(poseStack, eyePosition.x, 0, eyePosition.z);
		RenderSystem.depthMask(true);

		poseStack.popPose();
		turnOffLightLayer();
		mc.options.graphicsMode().set(current);
	}

	private void renderChunkLayer(RenderType layer, Matrix4f modelView, Matrix4f Projection) {
		layer.setupRenderState();
		VertexBuffer vertexBuffer = vertexBuffers.get(layer);
		vertexBuffer.bind();
		ShaderInstance shaderInstance = RenderSystem.getShader();
		shaderInstance.apply();
		turnOnLightLayer();
		vertexBuffer.drawWithShader(modelView, Projection, shaderInstance);
		turnOffLightLayer();
	}

	public void prepare() {
		if (vertexBuffers == null) {
			VRSettings.logger.info("MenuWorlds: Building geometry...");
			AmbientOcclusionStatus ao = mc.options.ambientOcclusion().get();
			mc.options.ambientOcclusion().set(AmbientOcclusionStatus.MAX);

			// disable redner regions during building, they mess with liquids
			boolean optifineRenderRegions = false;
			if (OptifineHelper.isOptifineLoaded()) {
				optifineRenderRegions = OptifineHelper.isRenderRegions();
				OptifineHelper.setRenderRegions(false);
			}

			ItemBlockRenderTypes.setFancy(true);
			visibleTextures.clear();

			// random offset to make the player fly
			if (rand.nextInt(1000) == 0) {
				blockAccess.setGroundOffset(100);
			}

			try {
				List<RenderType> layers = RenderType.chunkBufferLayers();
				vertexBuffers = new HashMap<>();
				animatedSprites = new HashSet<>();

				// disable liquid chunk wrapping
				ClientDataHolderVR.getInstance().skipStupidGoddamnChunkBoundaryClipping = true;

				if (!SodiumHelper.isLoaded() || !SodiumHelper.hasIssuesWithParallelBlockBuilding()) {
					// generate the data in parallel
					List<CompletableFuture<Pair<RenderType, BufferBuilder.RenderedBuffer>>> futures = new ArrayList<>();
					for (RenderType layer : layers) {
						futures.add(CompletableFuture.supplyAsync(() -> buildGeometryLayer(layer), Util.backgroundExecutor()));
					}
					for (Future<Pair<RenderType, BufferBuilder.RenderedBuffer>> future : futures) {
						try {
							Pair<RenderType, BufferBuilder.RenderedBuffer> pair = future.get();
							uploadGeometry(pair.getLeft(), pair.getRight());
						} catch (ExecutionException | InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				} else {
					// generate the data in series
					for (RenderType layer : layers) {
						Pair<RenderType, BufferBuilder.RenderedBuffer> pair = buildGeometryLayer(layer);
						uploadGeometry(pair.getLeft(), pair.getRight());
					}
				}

				copyVisibleTextures();
				ready = true;
			} finally {
				mc.options.ambientOcclusion().set(ao);
				if (OptifineHelper.isOptifineLoaded()) {
					OptifineHelper.setRenderRegions(optifineRenderRegions);
				}
				ClientDataHolderVR.getInstance().skipStupidGoddamnChunkBoundaryClipping = false;
			}
		}
	}

	private Pair<RenderType, BufferBuilder.RenderedBuffer> buildGeometryLayer(RenderType layer) {
		PoseStack thisPose = new PoseStack();
		int renderDistSquare = (renderDistance + 1) * (renderDistance + 1);
		BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

		BufferBuilder vertBuffer = new BufferBuilder(20 * 2097152);
		vertBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		RandomSource randomSource = RandomSource.create();
		int c = 0;
		for (int x = -blockAccess.getXSize() / 2; x < blockAccess.getXSize() / 2; x++) {
			for (int y = (int) -blockAccess.getGround(); y < blockAccess.getYSize() - (int) blockAccess.getGround(); y++) {
				// don't build unnecessary blocks in tall worlds
				if (Mth.abs(y) > renderDistance + 1)
					continue;
				for (int z = -blockAccess.getZSize() / 2; z < blockAccess.getZSize() / 2; z++) {
					// don't build unnecessary blocks in fog
					if (Mth.lengthSquared(x, z) > renderDistSquare)
						continue;

					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = blockAccess.getBlockState(pos);
					if (state != null) {
						FluidState fluidState = state.getFluidState();
						if (!fluidState.isEmpty() && ItemBlockRenderTypes.getRenderLayer(fluidState) == layer) {
							for (var sprite : Xplat.getFluidTextures(blockAccess, pos, fluidState)) {
								if (sprite != null && sprite.getUniqueFrames().sum() > 1) {
									animatedSprites.add(sprite);
								}
							}
							blockRenderer.renderLiquid(pos, blockAccess, vertBuffer, state, new FluidStateWrapper(fluidState));
							c++;
						}
						if (state.getRenderShape() != RenderShape.INVISIBLE && ItemBlockRenderTypes.getChunkRenderType(state) == layer) {
							for (var quad : mc.getModelManager().getBlockModelShaper().getBlockModel(state).getQuads(state, null, randomSource)) {
								if (quad.getSprite().getUniqueFrames().sum() > 1) {
									animatedSprites.add(quad.getSprite());
								}
							}
							thisPose.pushPose();
							thisPose.translate(pos.getX(), pos.getY(), pos.getZ());
							blockRenderer.renderBatched(state, pos, blockAccess, thisPose, vertBuffer, true, randomSource);
							c++;
							thisPose.popPose();
						}
					}
				}
			}
		}
		VRSettings.logger.info("Built " + c + " blocks.");
		if (layer == RenderType.translucent()) {
			vertBuffer.setQuadSortOrigin(0, Mth.frac(blockAccess.getGround()), 0);
		}
		return Pair.of(layer, vertBuffer.end());
	}

	private void uploadGeometry(RenderType layer, BufferBuilder.RenderedBuffer renderedBuffer) {
		VertexBuffer buffer = new VertexBuffer();
		buffer.bind();
		buffer.upload(renderedBuffer);
		VertexBuffer.unbind();
		vertexBuffers.put(layer, buffer);
	}


	public void destroy() {
		if (vertexBuffers != null) {
			for (VertexBuffer vertexBuffer : vertexBuffers.values()) {
				if (vertexBuffer != null) vertexBuffer.close();
			}
			vertexBuffers = null;
		}
		animatedSprites = null;
		ready = false;
	}

	public void completeDestroy() {
		destroy();
		if (starVBO != null) starVBO.close();
		if (skyVBO != null) skyVBO.close();
		if (sky2VBO != null) sky2VBO.close();
		if (cloudVBO != null) cloudVBO.close();
		ready = false;
	}

	public void tick() {
		ticks++;
		this.updateTorchFlicker();

		if (this.areEyesInFluid(FluidTags.WATER))
		{
			int i = 1; //this.isSpectator() ? 10 : 1;
			this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
		}
		else if (this.waterVisionTime > 0)
		{
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

		this.generateSky();
		this.generateSky2();
		this.generateStars();
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

	public void renderSky(PoseStack poseStack, Vec3 position)
	{
		if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.END)
		{
			this.renderEndSky(poseStack);
		}
		else if (this.dimensionInfo.skyType() == DimensionSpecialEffects.SkyType.NORMAL)
		{
			RenderSystem.setShader(GameRenderer::getPositionShader);
			fogRenderer.setupFog(FogRenderer.FogMode.FOG_SKY);
			ShaderInstance skyShader = RenderSystem.getShader();
			RenderSystem.disableTexture();

			Vec3 skyColor = this.getSkyColor(position);

			if (OptifineHelper.isOptifineLoaded()) {
				skyColor = OptifineHelper.getCustomSkyColor(skyColor, blockAccess, position.x, position.y, position.z);
			}

			fogRenderer.levelFogColor();

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor((float)skyColor.x, (float)skyColor.y, (float)skyColor.z, 1.0f);


			if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled())
			{
				this.skyVBO.bind();
				this.skyVBO.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), skyShader);
				VertexBuffer.unbind();
			}

			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			float[] sunriseColor = this.dimensionInfo.getSunriseColor(this.getTimeOfDay(), 0); // calcSunriseSunsetColors

			if (sunriseColor != null && (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled()))
			{
				RenderSystem.disableTexture();
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
				poseStack.pushPose();

				poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(this.getSunAngle()) < 0.0f ? 180.0f : 0.0f));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));

				Matrix4f modelView = poseStack.last().pose();
				bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder
					.vertex(modelView, 0.0f, 100.0f, 0.0f)
					.color(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3])
					.endVertex();

				for (int j = 0; j <= 16; ++j)
				{
					float f6 = (float)j * ((float)Math.PI * 2F) / 16.0F;
					float f7 = Mth.sin(f6);
					float f8 = Mth.cos(f6);
					bufferBuilder
						.vertex(modelView, f7 * 120.0F, f8 * 120.0F, -f8 * 40.0F * sunriseColor[3])
						.color(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0F)
						.endVertex();
				}

				BufferUploader.drawWithShader(bufferBuilder.end());
				poseStack.popPose();
			}

			RenderSystem.enableTexture();

			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			poseStack.pushPose();

			float f10 = 1.0F - getRainLevel();
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f10);
			poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
			Matrix4f modelView = poseStack.last().pose();

			//if (OptifineHelper.isOptifineLoaded()) {
				// needs a full Level
				//CustomSky.renderSky(this.world, poseStack, Minecraft.getInstance().getFrameTime());
			//}

			poseStack.mulPose(Vector3f.XP.rotationDegrees(this.getTimeOfDay() * 360.0f));

			float size = 30.0F;
			if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled())
			{
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, SUN_LOCATION);
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				bufferBuilder.vertex(modelView, -size, 100.0F, -size).uv(0.0F, 0.0F).endVertex();
				bufferBuilder.vertex(modelView,  size, 100.0F, -size).uv(1.0F, 0.0F).endVertex();
				bufferBuilder.vertex(modelView,  size, 100.0F,  size).uv(1.0F, 1.0F).endVertex();
				bufferBuilder.vertex(modelView, -size, 100.0F,  size).uv(0.0F, 1.0F).endVertex();
				BufferUploader.drawWithShader(bufferBuilder.end());
			}

			size = 20.0F;
			if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSunMoonEnabled())
			{
				RenderSystem.setShaderTexture(0, MOON_LOCATION);
				int moonPhase = this.getMoonPhase();
				int l = moonPhase % 4;
				int i1 = moonPhase / 4 % 2;
				float u0 = (float)(l + 0) / 4.0F;
				float v0 = (float)(i1 + 0) / 2.0F;
				float u1 = (float)(l + 1) / 4.0F;
				float v1 = (float)(i1 + 1) / 2.0F;
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				bufferBuilder.vertex(modelView, -size, -100.0f,  size).uv(u0, v1).endVertex();
				bufferBuilder.vertex(modelView,  size, -100.0f,  size).uv(u1, v1).endVertex();
				bufferBuilder.vertex(modelView,  size, -100.0f, -size).uv(u1, v0).endVertex();
				bufferBuilder.vertex(modelView, -size, -100.0f, -size).uv(u0, v0).endVertex();
				BufferUploader.drawWithShader(bufferBuilder.end());
			}

			RenderSystem.disableTexture();

			float starBrightness = this.getStarBrightness() * f10;

			if (starBrightness > 0.0F && (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isStarsEnabled()) /*&& !CustomSky.hasSkyLayers(this.world)*/)
			{
				RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
				fogRenderer.setupNoFog();
				this.starVBO.bind();
				this.starVBO.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
				VertexBuffer.unbind();
				fogRenderer.setupFog(FogRenderer.FogMode.FOG_SKY);
			}

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();

			poseStack.popPose();
			RenderSystem.disableTexture();

			double horizonDistance = position.y - this.blockAccess.getHorizon();

			if (horizonDistance < 0.0D)
			{
				RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
				poseStack.pushPose();
				poseStack.translate(0.0f, 12.0f, 0.0f);
				this.sky2VBO.bind();
				this.sky2VBO.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), skyShader);
				VertexBuffer.unbind();
				poseStack.popPose();
			}

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.depthMask(true);
		}
	}

	private void renderEndSky(PoseStack poseStack)
	{
		if (!OptifineHelper.isOptifineLoaded() || OptifineHelper.isSkyEnabled())
		{
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.depthMask(false);
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();

			for (int i = 0; i < 6; ++i)
			{
				poseStack.pushPose();
				switch (i) {
					case 1 -> poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
					case 2 -> poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0f));
					case 3 -> poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
					case 4 -> poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
					case 5 -> poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0f));
				}

				Matrix4f modelView = poseStack.last().pose();
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

				int r = 40;
				int g = 40;
				int b = 40;

				if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isCustomColors())
				{
					Vec3 newSkyColor = new Vec3((double)r / 255.0D, (double)g / 255.0D, (double)b / 255.0D);
					newSkyColor = OptifineHelper.getCustomSkyColorEnd(newSkyColor);
					r = (int)(newSkyColor.x * 255.0D);
					g = (int)(newSkyColor.y * 255.0D);
					b = (int)(newSkyColor.z * 255.0D);
				}
				bufferBuilder.vertex(modelView, -100.0f, -100.0f, -100.0f).uv( 0.0f,  0.0f).color(r, g, b, 255).endVertex();
				bufferBuilder.vertex(modelView, -100.0f, -100.0f,  100.0f).uv( 0.0f, 16.0f).color(r, g, b, 255).endVertex();
				bufferBuilder.vertex(modelView,  100.0f, -100.0f,  100.0f).uv(16.0f, 16.0f).color(r, g, b, 255).endVertex();
				bufferBuilder.vertex(modelView,  100.0f, -100.0f, -100.0f).uv(16.0f,  0.0f).color(r, g, b, 255).endVertex();
				tesselator.end();
				poseStack.popPose();
			}

			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
		}
	}

	public void renderClouds(PoseStack poseStack, double x, double y, double z)
	{
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
			double cloudOffset = ((float) ticks + mc.getFrameTime()) * 0.03f;
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
				this.prevCloudColor.distanceToSqr(cloudColor) > 2.0E-4)
			{
				this.prevCloudX = cloudXfloor;
				this.prevCloudY = cloudYfloor;
				this.prevCloudZ = cloudZfloor;
				this.prevCloudColor = cloudColor;
				this.prevCloudsType = this.mc.options.getCloudsType();
				this.generateClouds = true;
			}
			if (this.generateClouds) {
				this.generateClouds = false;
				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
				if (this.cloudVBO != null) {
					this.cloudVBO.close();
				}
				this.cloudVBO = new VertexBuffer();
				BufferBuilder.RenderedBuffer renderedBuffer = this.buildClouds(bufferBuilder, cloudX, cloudY, cloudZ, cloudColor);
				this.cloudVBO.bind();
				this.cloudVBO.upload(renderedBuffer);
				VertexBuffer.unbind();
			}

			// render
			RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
			RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
			fogRenderer.levelFogColor();
			poseStack.pushPose();
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
					this.cloudVBO.drawWithShader(poseStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
				}
				VertexBuffer.unbind();
			}
			poseStack.popPose();
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder bufferBuilder, double cloudX, double cloudY, double cloudZ, Vec3 cloudColor) {
		float l = (float)Mth.floor(cloudX) * 0.00390625f;
		float m = (float)Mth.floor(cloudZ) * 0.00390625f;
		float redTop = (float)cloudColor.x;
		float greenTop = (float)cloudColor.y;
		float blueTop = (float)cloudColor.z;
		float redX = redTop * 0.9f;
		float greenX = greenTop * 0.9f;
		float blueX = blueTop * 0.9f;
		float redBottom = redTop * 0.7f;
		float greenBottom = greenTop * 0.7f;
		float blueBottom = blueTop * 0.7f;
		float redZ = redTop * 0.8f;
		float greenZ = greenTop * 0.8f;
		float blueZ = blueTop * 0.8f;
		RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
		float z = (float)Math.floor(cloudY / 4.0) * 4.0f;
		if (this.prevCloudsType == CloudStatus.FANCY) {
			for (int aa = -3; aa <= 4; ++aa) {
				for (int ab = -3; ab <= 4; ++ab) {
					int ae;
					float ac = aa * 8;
					float ad = ab * 8;
					if (z > -5.0f) {
						bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + 8.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + 8.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redBottom, greenBottom, blueBottom, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
					}
					if (z <= 5.0f) {
						bufferBuilder.vertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 8.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
						bufferBuilder.vertex(ac + 0.0f, z + 4.0f - 9.765625E-4f, ad + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, 1.0f, 0.0f).endVertex();
					}
					if (aa > -1) {
						for (ae = 0; ae < 8; ++ae) {
							bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 0.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 4.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 4.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 0.0f, z + 0.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(-1.0f, 0.0f, 0.0f).endVertex();
						}
					}
					if (aa <= 1) {
						for (ae = 0; ae < 8; ++ae) {
							bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 8.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 8.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 4.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
							bufferBuilder.vertex(ac + (float)ae + 1.0f - 9.765625E-4f, z + 0.0f, ad + 0.0f).uv((ac + (float)ae + 0.5f) * 0.00390625f + l, (ad + 0.0f) * 0.00390625f + m).color(redX, greenX, blueX, 0.8f).normal(1.0f, 0.0f, 0.0f).endVertex();
						}
					}
					if (ab > -1) {
						for (ae = 0; ae < 8; ++ae) {
							bufferBuilder.vertex(ac + 0.0f, z + 4.0f, ad + (float)ae + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
							bufferBuilder.vertex(ac + 8.0f, z + 4.0f, ad + (float)ae + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
							bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + (float)ae + 0.0f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
							bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + (float)ae + 0.0f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, -1.0f).endVertex();
						}
					}
					if (ab > 1) continue;
					for (ae = 0; ae < 8; ++ae) {
						bufferBuilder.vertex(ac + 0.0f, z + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 4.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
						bufferBuilder.vertex(ac + 8.0f, z + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 8.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
						bufferBuilder.vertex(ac + 0.0f, z + 0.0f, ad + (float)ae + 1.0f - 9.765625E-4f).uv((ac + 0.0f) * 0.00390625f + l, (ad + (float)ae + 0.5f) * 0.00390625f + m).color(redZ, greenZ, blueZ, 0.8f).normal(0.0f, 0.0f, 1.0f).endVertex();
					}
				}
			}
		} else {
			boolean aa = true;
			int ab = 32;
			for (int af = -32; af < 32; af += 32) {
				for (int ag = -32; ag < 32; ag += 32) {
					bufferBuilder.vertex(af + 0, z, ag + 32).uv((float)(af + 0) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
					bufferBuilder.vertex(af + 32, z, ag + 32).uv((float)(af + 32) * 0.00390625f + l, (float)(ag + 32) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
					bufferBuilder.vertex(af + 32, z, ag + 0).uv((float)(af + 32) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
					bufferBuilder.vertex(af + 0, z, ag + 0).uv((float)(af + 0) * 0.00390625f + l, (float)(ag + 0) * 0.00390625f + m).color(redTop, greenTop, blueTop, 0.8f).normal(0.0f, -1.0f, 0.0f).endVertex();
				}
			}
		}
		return bufferBuilder.end();
	}

	private void renderSnowAndRain(PoseStack poseStack, double inX, double inY, double inZ) {
		if (getRainLevel() <= 0.0f) {
			return;
		}

		RenderSystem.getModelViewStack().pushPose();
		RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
		RenderSystem.applyModelViewMatrix();

		int xFloor = Mth.floor(inX);
		int yFloor = Mth.floor(inY);
		int zFloor = Mth.floor(inZ);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		int rainDistance = 5;
		if (Minecraft.useFancyGraphics()) {
			rainDistance = 10;
		}
		RenderSystem.depthMask(true);
		int count = -1;
		float rainAnimationTime = this.ticks + mc.getFrameTime();
		RenderSystem.setShader(GameRenderer::getParticleShader);
		turnOnLightLayer();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		for (int rainZ = zFloor - rainDistance; rainZ <= zFloor + rainDistance; ++rainZ) {
			for (int rainX = xFloor - rainDistance; rainX <= xFloor + rainDistance; ++rainX) {
				int q = (rainZ - zFloor + 16) * 32 + rainX - xFloor + 16;
				double r = (double)this.rainSizeX[q] * 0.5;
				double s = (double)this.rainSizeZ[q] * 0.5;
				mutableBlockPos.set(rainX, inY, rainZ);
				Biome biome = blockAccess.getBiome(mutableBlockPos).value();
				if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;

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

				mutableBlockPos.setY(rainY);

				double localX = rainX + 0.5;
				double localZ = rainZ + 0.5;
				float distance = (float)Math.sqrt(localX * localX + localZ * localZ) / (float)rainDistance;
				float blend;
				float xOffset = 0;
				float yOffset = 0;

				int skyLight = blockAccess.getBrightness(LightLayer.SKY, mutableBlockPos) << 4;
				int blockLight = blockAccess.getBrightness(LightLayer.BLOCK, mutableBlockPos) << 4;

				if (biome.warmEnoughToRain(mutableBlockPos)) {
					if (count != 0) {
						if (count >= 0) {
							tesselator.end();
						}
						count = 0;
						RenderSystem.setShaderTexture(0, RAIN_LOCATION);
						bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
					}

					blend = ((1.0f - distance * distance) * 0.5f + 0.5f);
					int x = this.ticks + rainX * rainX * 3121 + rainX * 45238971 + rainZ * rainZ * 418711 + rainZ * 13761 & 0x1F;
					yOffset = -((float)x + mc.getFrameTime()) / 32.0f * (3.0f + randomSource.nextFloat());
				} else {
					if (count != 1) {
						if (count >= 0) {
							tesselator.end();
						}
						count = 1;
						RenderSystem.setShaderTexture(0, SNOW_LOCATION);
						bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
					}

					blend = ((1.0f - distance * distance) * 0.3f + 0.5f);
					xOffset = (float)(randomSource.nextDouble() + (double)rainAnimationTime * 0.01 * (double)((float)randomSource.nextGaussian()));
					float ae = -((float)(this.ticks & 0x1FF) + mc.getFrameTime()) / 512.0f;
					float af = (float)(randomSource.nextDouble() + (double)(rainAnimationTime * (float)randomSource.nextGaussian()) * 0.001);
					yOffset = ae + af;

					//snow is brighter
					skyLight = (skyLight * 3 + 240) / 4;
					blockLight = (blockLight * 3 + 240) / 4;
				}
				bufferBuilder
					.vertex(localX - r, (double)upper - inY, localZ - s)
					.uv(0.0f + xOffset, (float)lower * 0.25f + yOffset)
					.color(1.0f, 1.0f, 1.0f, blend).uv2(blockLight, skyLight).endVertex();
				bufferBuilder
					.vertex(localX + r, (double)upper - inY, localZ + s)
					.uv(1.0f + xOffset, (float)lower * 0.25f + yOffset)
					.color(1.0f, 1.0f, 1.0f, blend).uv2(blockLight, skyLight).endVertex();
				bufferBuilder
					.vertex(localX + r, (double)lower - inY, localZ + s)
					.uv(1.0f + xOffset, (float)upper * 0.25f + yOffset)
					.color(1.0f, 1.0f, 1.0f, blend).uv2(blockLight, skyLight).endVertex();
				bufferBuilder
					.vertex(localX - r, (double)lower - inY, localZ - s)
					.uv(0.0f + xOffset, (float)upper * 0.25f + yOffset)
					.color(1.0f, 1.0f, 1.0f, blend).uv2(blockLight, skyLight).endVertex();
			}
		}
		if (count >= 0) {
			tesselator.end();
		}
		RenderSystem.getModelViewStack().popPose();
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

	public float getTimeOfDay()
	{
		return this.blockAccess.dimensionType().timeOfDay(time);
	}

	public float getSunAngle()
	{
		float dayTime = this.getTimeOfDay();
		return dayTime * ((float)Math.PI * 2F);
	}

	public int getMoonPhase()
	{
		return this.blockAccess.dimensionType().moonPhase(time);
	}

	public float getSkyDarken() {
		float dayTime = this.getTimeOfDay();
		float h = 1.0f - (Mth.cos(dayTime * ((float)Math.PI * 2)) * 2.0f + 0.2f);
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

	public float getStarBrightness()
	{
		float f = this.getTimeOfDay();
		float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
		f1 = Mth.clamp(f1, 0.0F, 1.0F);
		return f1 * f1 * 0.5F;
	}

	public Vec3 getSkyColor(Vec3 position)
	{
		float dayTime = this.getTimeOfDay();

		Vec3 samplePosition = position.subtract(2.0, 2.0, 2.0).scale(0.25);

		Vec3 skyColor = CubicSampler.gaussianSampleVec3(samplePosition, (i, j, k) -> Vec3.fromRGB24(blockAccess.getBiomeManager().getNoiseBiomeAtQuart(i, j, k).value().getSkyColor()));

		float h = Mth.cos(dayTime * ((float)Math.PI * 2)) * 2.0f + 0.5f;
		h = Mth.clamp(h, 0.0f, 1.0f);
		float skyColorR = (float)skyColor.x * h;
		float skyColorG = (float)skyColor.y * h;
		float skyColorB = (float)skyColor.z * h;

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
			float flash = (float)this.skyFlashTime - mc.getFrameTime();
			if (flash > 1.0f) {
				flash = 1.0f;
			}
			skyColorR = skyColorR * (1.0f - (flash *= 0.45f)) + 0.8f * flash;
			skyColorG = skyColorG * (1.0f - flash) + 0.8f * flash;
			skyColorB = skyColorB * (1.0f - flash) + flash;
		}
		return new Vec3(skyColorR, skyColorG, skyColorB);
	}

	public Vec3 getFogColor(Vec3 pos)
	{
		float f = Mth.clamp(Mth.cos(this.getTimeOfDay() * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
		Vec3 scaledPos = pos.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
		return CubicSampler.gaussianSampleVec3(scaledPos, (x, y, z) -> this.dimensionInfo.getBrightnessDependentFogColor(Vec3.fromRGB24(this.blockAccess.getBiomeManager().getNoiseBiomeAtQuart(x, y, z).value().getFogColor()), f));
	}

	public Vec3 getCloudColour()
	{
		float dayTime = this.getTimeOfDay();
		float f1 = Mth.cos(dayTime * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
		f1 = Mth.clamp(f1, 0.0F, 1.0F);
		float r = 1.0F;
		float g = 1.0F;
		float b = 1.0F;
        float rain = this.getRainLevel();

        if (rain > 0.0F)
        {
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

        if (thunder > 0.0F)
        {
            float luma = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
            float dark = 1.0F - thunder * 0.95F;
            r = r * dark + luma * (1.0F - dark);
            g = g * dark + luma * (1.0F - dark);
            b = b * dark + luma * (1.0F - dark);
        }

		return new Vec3(r, g, b);
	}

	private void generateSky()
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.skyVBO != null) {
			this.skyVBO.close();
		}
		this.skyVBO = new VertexBuffer();
		BufferBuilder.RenderedBuffer renderedBuffer = buildSkyDisc(bufferBuilder, 16.0f);
		this.skyVBO.bind();
		this.skyVBO.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private void generateSky2()
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		if (this.sky2VBO != null) {
			this.sky2VBO.close();
		}
		this.sky2VBO = new VertexBuffer();
		BufferBuilder.RenderedBuffer renderedBuffer = buildSkyDisc(bufferBuilder, -16.0f);
		this.sky2VBO.bind();
		this.sky2VBO.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder bufferBuilder, float posY) {
		float g = Math.signum(posY) * 512.0f;
		float h = 512.0f;
		RenderSystem.setShader(GameRenderer::getPositionShader);
		bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		bufferBuilder.vertex(0.0, posY, 0.0).endVertex();
		for (int i = -180; i <= 180; i += 45) {
			bufferBuilder.vertex(g * Mth.cos((float)i * ((float)Math.PI / 180)), posY, 512.0f * Mth.sin((float)i * ((float)Math.PI / 180))).endVertex();
		}
		return bufferBuilder.end();
	}

	private void generateStars()
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		if (this.starVBO != null) {
			this.starVBO.close();
		}
		this.starVBO = new VertexBuffer();
		BufferBuilder.RenderedBuffer renderedBuffer = this.buildStars(bufferBuilder);
		this.starVBO.bind();
		this.starVBO.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	private BufferBuilder.RenderedBuffer buildStars(BufferBuilder bufferBuilderIn)
	{
		Random random = new Random(10842L);
		bufferBuilderIn.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (int i = 0; i < 1500; ++i)
		{
			double d0 = random.nextFloat() * 2.0F - 1.0F;
			double d1 = random.nextFloat() * 2.0F - 1.0F;
			double d2 = random.nextFloat() * 2.0F - 1.0F;
			double d3 = 0.15F + random.nextFloat() * 0.1F;
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
		return bufferBuilderIn.end();
	}

	public void turnOffLightLayer() {
		RenderSystem.setShaderTexture(2, 0);
	}

	public void turnOnLightLayer() {
		RenderSystem.setShaderTexture(2, this.lightTextureLocation);
		mc.getTextureManager().bindForSetup(this.lightTextureLocation);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D , GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	}

	public void updateTorchFlicker()
	{
		this.blockLightRedFlicker += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
		this.blockLightRedFlicker *= 0.9f;
		this.lightmapUpdateNeeded = true;
	}

	public void updateLightmap()
	{
		if (this.lightmapUpdateNeeded)
		{
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

			Vector3f skylightColor = new Vector3f(skyLight, skyLight, 1.0f);
			skylightColor.lerp(new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);

			Vector3f finalColor = new Vector3f();
			for (int i = 0; i < 16; ++i)
			{
				for (int j = 0; j < 16; ++j)
				{
					float skyBrightness = LightTexture.getBrightness(this.blockAccess.dimensionType(), i) * effectiveSkyLight;
					float blockBrightnessRed = LightTexture.getBrightness(this.blockAccess.dimensionType(), j) * (blockLightRedFlicker + 1.5f);
					float blockBrightnessGreen = blockBrightnessRed * ((blockBrightnessRed * 0.6f + 0.4f) * 0.6f + 0.4f);
					float blockBrightnessBlue = blockBrightnessRed * (blockBrightnessRed * blockBrightnessRed * 0.6f + 0.4f);

					finalColor.set(blockBrightnessRed, blockBrightnessGreen, blockBrightnessBlue);

					if (dimensionInfo.forceBrightLightmap()) {
						finalColor.lerp(new Vector3f(0.99f, 1.12f, 1.0f), 0.25f);
						finalColor.clamp(0.0f, 1.0f);
					} else {
						Vector3f skylightColorCopy = skylightColor.copy();
						skylightColorCopy.mul(skyBrightness);
						finalColor.add(skylightColorCopy);
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
						finalColor.clamp(0.0f, 1.0f);
					}

					float gamma = this.mc.options.gamma().get().floatValue();

					Vector3f vector3f5 = finalColor.copy();
					vector3f5.map(this::notGamma);
					finalColor.lerp(vector3f5, Math.max(0.0f, gamma /*- darknessGamma*/));
					finalColor.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04f);
					finalColor.clamp(0.0f, 1.0f);
					finalColor.mul(255.0f);

					int r = (int)finalColor.x();
					int g = (int)finalColor.y();
					int b = (int)finalColor.z();
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

	public float getWaterVision()
	{
		if (!this.areEyesInFluid(FluidTags.WATER))
		{
			return 0.0F;
		}
		else
		{
			if ((float)this.waterVisionTime >= 600.0F)
			{
				return 1.0F;
			}
			else
			{
				float f2 = Mth.clamp((float)this.waterVisionTime / 100.0F, 0.0F, 1.0F);
				float f3 = (float)this.waterVisionTime < 100.0F ? 0.0F : Mth.clamp(((float)this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
				return f2 * 0.6F + f3 * 0.39999998F;
			}
		}
	}

	public boolean areEyesInFluid(TagKey<Fluid> tagIn)
	{
		if (blockAccess == null)
			return false;

		Vec3 pos = getEyePos();
		BlockPos blockpos = new BlockPos(pos);
		FluidState fluidstate = this.blockAccess.getFluidState(blockpos);
		return isFluidTagged(fluidstate, tagIn) && pos.y < (double)((float)blockpos.getY() + fluidstate.getAmount() + 0.11111111F);
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

			float d0 = (float)((eyePos.y + this.menuWorldRenderer.getLevel().getGround()) * this.menuWorldRenderer.getLevel().getVoidFogYFactor());

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
				fogGreen =  fogGreen * d0;
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
						fogRed = (float)colUnderwater.x;
						fogGreen = (float)colUnderwater.y;
						fogBlue = (float)colUnderwater.z;
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
			if (f6 > 0.0F)
			{
				float f4 = 1.0F - f6 * 0.5F;
				float f8 = 1.0F - f6 * 0.4F;
				fogRed *= f4;
				fogGreen *= f4;
				fogBlue *= f8;
			}

			float f7 = menuWorldRenderer.getThunderLevel();
			if (f7 > 0.0F)
			{
				float f9 = 1.0F - f7 * 0.5F;
				fogRed *= f9;
				fogGreen *= f9;
				fogBlue *= f9;
			}
			biomeChangedTime = -1L;
		}

		private void updateWaterFog(LevelReader levelIn) {
			long currentTime = Util.getMillis();
			int waterFogColor = levelIn.getBiome(new BlockPos(this.menuWorldRenderer.getEyePos())).value().getWaterFogColor();

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

				Holder<Biome> holder = menuWorldRenderer.blockAccess.getBiome(new BlockPos(menuWorldRenderer.getEyePos()));
				if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
					fogEnd *= 0.85f;
				}
				if (fogEnd > menuWorldRenderer.renderDistance) {
					fogEnd = menuWorldRenderer.renderDistance;
					fogShape = FogShape.CYLINDER;
				}
			} else if (menuWorldRenderer.blockAccess.getDimensionReaderInfo().isFoggyAt(0,0)) {
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
			if(menuWorldRenderer.areEyesInFluid(FluidTags.WATER)) {
				fogType = FogType.WATER;
			} else if(menuWorldRenderer.areEyesInFluid(FluidTags.LAVA)){
				fogType = FogType.LAVA;
			} else if(menuWorldRenderer.blockAccess.getBlockState(new BlockPos(menuWorldRenderer.getEyePos())).is(Blocks.POWDER_SNOW)){
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
			super(fluidState.getType(), fluidState.getValues(), fluidState.propertiesCodec);

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
