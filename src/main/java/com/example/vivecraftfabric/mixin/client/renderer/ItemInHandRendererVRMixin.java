package com.example.vivecraftfabric.mixin.client.renderer;

import java.util.Optional;
import java.util.stream.Stream;

import com.example.vivecraftfabric.DataHolder;
import com.example.vivecraftfabric.EntityRenderDispatcherExtension;
import com.example.vivecraftfabric.GameRendererExtension;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.example.vivecraftfabric.ItemInHandRendererExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.gameplay.trackers.BowTracker;
import org.vivecraft.gameplay.trackers.SwingTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRArmRenderer;
import org.vivecraft.render.VRFirstPersonArmSwing;
import org.vivecraft.render.VivecraftItemRendering;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererVRMixin implements ItemInHandRendererExtension {

	@Unique
	private float xdist = 0F;

	@Unique
	private VRFirstPersonArmSwing swingType = VRFirstPersonArmSwing.Attack;

	@Final
	@Shadow
	private Minecraft minecraft;

	@Final
	@Shadow
	private EntityRenderDispatcher entityRenderDispatcher;
	@Final
	@Shadow
	private ItemRenderer itemRenderer;
	@Shadow
	private float oMainHandHeight;
	@Shadow
	private float mainHandHeight;
	@Shadow
	private float oOffHandHeight;
	@Shadow
	private float offHandHeight;

	@Override
	public Triple<Float, BlockState, BlockPos> getNearOpaqueBlock(Vec3 in, double dist) {
		if (this.minecraft.level == null) {
			return null;
		} else {
			AABB aabb = new AABB(in.subtract(dist, dist, dist), in.add(dist, dist, dist));
			Stream<BlockPos> stream = BlockPos.betweenClosedStream(aabb).filter((bp) -> {
				return this.minecraft.level.getBlockState(bp).isViewBlocking(this.minecraft.level, bp);
			});
			Optional<BlockPos> optional = stream.findFirst();
			return optional.isPresent()
					? Triple.of(1.0F, this.minecraft.level.getBlockState(optional.get()), optional.get())
					: null;
		}
	}

	@Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
	public void overrideArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm, CallbackInfo ci) {
		vrPlayerArm(poseStack, multiBufferSource, i, f, g, humanoidArm);
		ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
	public void overrideArmItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		this.vrRenderArmWithItem(abstractClientPlayer, f, g, interactionHand, h, itemStack, i, poseStack, multiBufferSource, j);
		ci.cancel();
	}

	@Override
	public boolean isInsideOpaqueBlock(Vec3 in) {
		if (this.minecraft.level == null) {
			return false;
		} else {
			BlockPos blockpos = new BlockPos(in);
			return this.minecraft.level.getBlockState(blockpos).isSolidRender(this.minecraft.level, blockpos);
		}
	}

	public void vrRenderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
		boolean flag = pHand == InteractionHand.MAIN_HAND;
		HumanoidArm humanoidarm = flag ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
		pEquippedProgress = this.getEquipProgress(pHand, pPartialTicks);
		pMatrixStack.pushPose();
		boolean flag1 = true;

		if (DataHolder.getInstance().currentPass == RenderPass.THIRD && !DataHolder.getInstance().vrSettings.mixedRealityRenderHands) {
			flag1 = false;
		}

		if (DataHolder.getInstance().currentPass == RenderPass.CAMERA) {
			flag1 = false;
		}

		if (BowTracker.isBow(pStack) && DataHolder.getInstance().bowTracker.isActive((LocalPlayer)pPlayer)) {
			flag1 = false;
		}

		if (TelescopeTracker.isTelescope(pStack) && (pHand == InteractionHand.OFF_HAND && DataHolder.getInstance().currentPass == RenderPass.SCOPEL || pHand == InteractionHand.MAIN_HAND && DataHolder.getInstance().currentPass == RenderPass.SCOPER)) {
			flag1 = false;
		}

		if (flag1 && !pPlayer.isInvisible()) {
			this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
		}
		if (!pStack.isEmpty()) {
			Item item = pStack.getItem();
			boolean flag2 = false;
			VivecraftItemRendering vivecraftitemrendering = VivecraftItemRendering.Item;

			if (pStack.getUseAnimation() != UseAnim.EAT && pStack.getUseAnimation() != UseAnim.DRINK) {
				if (item instanceof BlockItem) {
					Block block = ((BlockItem) item).getBlock();

					if (block instanceof TorchBlock) {
						vivecraftitemrendering = VivecraftItemRendering.Block_Stick;
					} else {
						BakedModel bakedmodel = this.itemRenderer.getModel(pStack, this.minecraft.level, this.minecraft.player, 0);

						if (bakedmodel.isGui3d()) {
							vivecraftitemrendering = VivecraftItemRendering.Block_3D;
						} else {
							vivecraftitemrendering = VivecraftItemRendering.Block_Item;
						}
					}
				} else if (item instanceof MapItem) {
					vivecraftitemrendering = VivecraftItemRendering.Map;
				}
				else if (pStack.getUseAnimation() == UseAnim.BOW) {
					vivecraftitemrendering = VivecraftItemRendering.Bow_Seated;

					if (DataHolder.getInstance().bowTracker.isActive((LocalPlayer)pPlayer)) {
						if (DataHolder.getInstance().bowTracker.isDrawing) {
							vivecraftitemrendering = VivecraftItemRendering.Bow_Roomscale_Drawing;
						}
						else {
							vivecraftitemrendering = VivecraftItemRendering.Bow_Roomscale;
						}
					}
				} else if (item instanceof SwordItem) {
					vivecraftitemrendering = VivecraftItemRendering.Sword;
				}
				else if (item instanceof ShieldItem) {
					vivecraftitemrendering = VivecraftItemRendering.Shield;
				}
				else if (item instanceof TridentItem) {
					vivecraftitemrendering = VivecraftItemRendering.Spear;
				}
				else if (item instanceof CrossbowItem) {
					vivecraftitemrendering = VivecraftItemRendering.Crossbow;
				}
				else if (!(item instanceof CompassItem) && item != Items.CLOCK) {
					if (SwingTracker.isTool(item)) {
						vivecraftitemrendering = VivecraftItemRendering.Tool;

						if (item instanceof FoodOnAStickItem || item instanceof FishingRodItem) {
							vivecraftitemrendering = VivecraftItemRendering.Tool_Rod;
						}
					} else if (TelescopeTracker.isTelescope(pStack)) {
						vivecraftitemrendering = VivecraftItemRendering.Telescope;
					}
				}
				else {
					vivecraftitemrendering = VivecraftItemRendering.Compass;
				}
			}
			else {
				vivecraftitemrendering = VivecraftItemRendering.Noms;
			}
			int k = flag ? 1 : -1;
			double d8 = 0.7D;
			double d0 = -0.05D;
			double d1 = 0.005D;
			double d2 = 0.0D;
			double d3 = DataHolder.getInstance().vr.getGunAngle();
			Quaternion quaternion = Vector3f.YP.rotationDegrees(0.0F);
			Quaternion quaternion1 = Vector3f.YP.rotationDegrees(0.0F);
			quaternion.mul(Vector3f.XP.rotationDegrees((float)(-110.0D + d3)));
			pMatrixStack.pushPose();
			boolean flag3 = false;

			if (vivecraftitemrendering == VivecraftItemRendering.Bow_Seated) {
				d1 += -0.1D;
				d2 += 0.1D;
				quaternion.mul(Vector3f.XP.rotationDegrees((float)(90.0D - d3)));
				d8 = (double)0.7F;
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Bow_Roomscale) {
				quaternion = Vector3f.XP.rotationDegrees(0.0F);
				pMatrixStack.mulPose(Vector3f.XP.rotationDegrees((float)(-110.0D + d3)));
				d1 -= 0.25D;
				d2 += (double)0.025F + 0.03D * d3 / 40.0D;
				d0 += -0.0225D;
				d8 = 1.0D;
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Bow_Roomscale_Drawing) {
				quaternion = Vector3f.YP.rotationDegrees(0.0F);
				d8 = 1.0D;
				int i = 0;

				if (DataHolder.getInstance().vrSettings.reverseShootingEye) {
					i = 1;
				}
				Vec3 vec3 = DataHolder.getInstance().bowTracker.getAimVector();
				Vec3 vec31 = new Vec3(vec3.x, vec3.y, vec3.z);
				Vec3 vec32 = DataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
				Vec3 vec33 = DataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D));
				vec31.cross(vec32);
				double d4 = (180D / Math.PI) * Math.acos(vec31.dot(vec32));
				float f = (float) Math.toDegrees(Math.asin(vec31.y / vec31.length()));
				float f1 = (float) Math.toDegrees(Math.atan2(vec31.x, vec31.z));
				Vec3 vec34 = new Vec3(0.0D, 1.0D, 0.0D);
				Vec3 vec35 = new Vec3(vec31.x, 0.0D, vec31.z);
				Vec3 vec36 = Vec3.ZERO;
				double d5 = vec33.dot(vec35);

				if (d5 != 0.0D) {
					vec36 = vec35.scale(d5);
				}

				double d6 = 0.0D;
				Vec3 vec37 = vec33.subtract(vec36).normalize();
				d6 = vec37.dot(vec34);
				double d7 = vec35.dot(vec37.cross(vec34));
				float f2;

				if (d7 < 0.0D) {
					f2 = -((float)Math.acos(d6));
				}
				else {
					f2 = (float) Math.acos(d6);
				}
				float f3 = (float)((180D / Math.PI) * (double)f2);

				if (DataHolder.getInstance().bowTracker.isCharged()) {
					long j = Util.getMillis() - DataHolder.getInstance().bowTracker.startDrawTime;
					d0 += 0.003D * Math.sin((double)j);
				}
				pMatrixStack.translate(0.0D, 0.0D, 0.1D);
				pMatrixStack.last().pose().multiply(DataHolder.getInstance().vrPlayer.vrdata_world_render.getController(1).getMatrix().transposed().toMCMatrix());
				quaternion.mul(Vector3f.YP.rotationDegrees(f1));
				quaternion.mul(Vector3f.XP.rotationDegrees(-f));
				quaternion.mul(Vector3f.ZP.rotationDegrees(-f3));
				quaternion.mul(Vector3f.ZP.rotationDegrees(180.0F));
				pMatrixStack.last().pose().multiply(quaternion);
				quaternion = Vector3f.YP.rotationDegrees(0.0F);
				quaternion.mul(Vector3f.YP.rotationDegrees(180.0F));
				quaternion.mul(Vector3f.XP.rotationDegrees(160.0F));
				d1 += 0.1225D;
				d0 += 0.125D;
				d2 += 0.16D;
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Crossbow) {
				d0 += (double)0.01F;
				d2 += (double) - 0.02F;
				d1 += (double) - 0.02F;
				d8 = 0.5D;
				quaternion = Vector3f.XP.rotationDegrees(0.0F);
				quaternion.mul(Vector3f.YP.rotationDegrees(10.0F));
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Map) {
				flag2 = true;
				quaternion = Vector3f.XP.rotationDegrees(-45.0F);
				d0 = 0.0D;
				d1 = 0.16D;
				d2 = -0.075D;
				d8 = 0.75D;
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Noms) {
				long l = (long)this.minecraft.player.getUseItemRemainingTicks();
				quaternion = Vector3f.ZP.rotationDegrees(180.0F);
				quaternion.mul(Vector3f.XP.rotationDegrees(-135.0F));
				d2 = d2 + 0.006D * Math.sin((double)l);
				d2 = d2 + (double)0.02F;
				d0 += (double)0.08F;
				d8 = (double)0.4F;
			}
			else if (vivecraftitemrendering != VivecraftItemRendering.Item && vivecraftitemrendering != VivecraftItemRendering.Block_Item) {
				if (vivecraftitemrendering == VivecraftItemRendering.Compass) {
					quaternion = Vector3f.YP.rotationDegrees(90.0F);
					quaternion.mul(Vector3f.XP.rotationDegrees(25.0F));
					d8 = (double)0.4F;
				}
				else if (vivecraftitemrendering == VivecraftItemRendering.Block_3D) {
					d8 = (double)0.3F;
					d2 += (double) - 0.1F;
					d0 += (double)0.05F;
				}
				else if (vivecraftitemrendering == VivecraftItemRendering.Block_Stick) {
					quaternion = Vector3f.XP.rotationDegrees(0.0F);
					d1 += -0.105D + 0.06D * d3 / 40.0D;
					d2 += (double) - 0.1F;
					quaternion.mul(Vector3f.XP.rotationDegrees(-45.0F));
					quaternion.mul(Vector3f.XP.rotationDegrees((float)d3));
				}
				else if (vivecraftitemrendering == VivecraftItemRendering.Shield) {
					flag3 = !flag;
					d8 = (double) 0.4F;
					d1 += (double) 0.21F;

					if (flag) {
						d0 += (double) 0.11F;
					} else {
						d0 += -0.015D;
					}

					d2 += (double) -0.01F;
					quaternion.mul(Vector3f.XP.rotationDegrees((float) (105.0D - d3)));

					if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
						quaternion.mul(Vector3f.ZP.rotationDegrees((float) (k * -5)));
						d1 += (double) -0.13F;
						d0 += (double) ((float) k * 0.05F);
						d2 += (double) -0.1F;

						if (pPlayer.isBlocking()) {
							quaternion.mul(Vector3f.YP.rotationDegrees((float) k * 90.0F));
						} else {
							quaternion.mul(Vector3f.YP.rotationDegrees((1.0F - pEquippedProgress) * (float) k * 90.0F));
						}
					}
					quaternion.mul(Vector3f.YP.rotationDegrees((float)k * -90.0F));
				}
				else if (vivecraftitemrendering == VivecraftItemRendering.Spear) {
					quaternion = Vector3f.XP.rotationDegrees(0.0F);
					d0 += (double) - 0.135F;
					d2 = d2 + (double)0.575F;
					d8 = (double)0.6F;
					float f4 = 0.0F;
					boolean flag5 = false;
					int i1 = 0;

					if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
						flag5 = true;
						i1 = EnchantmentHelper.getRiptide(pStack);

						if (i1 <= 0 || i1 > 0 && pPlayer.isInWaterOrRain()) {
							f4 = (float) pStack.getUseDuration() - ((float) this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);

							if (f4 > 10.0F) {

								f4 = 10.0F;

								if (i1 > 0 && pPlayer.isInWaterOrRain()) {
									pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)(-DataHolder.getInstance().tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float)i1));
								}

								if (DataHolder.getInstance().frameIndex % 4L == 0L)
								{
									DataHolder.getInstance().vr.triggerHapticPulse(flag ? 0 : 1, 200);
								}
								long j1 = Util.getMillis() - DataHolder.getInstance().bowTracker.startDrawTime;
								d0 += 0.003D * Math.sin((double)j1);
							}
						}
					}

					if (pPlayer.isAutoSpinAttack()) {
						i1 = 5;
						d2 += (double) - 0.15F;
						pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)(-DataHolder.getInstance().tickCounter * 10 * i1 % 360) - pPartialTicks * 10.0F * (float)i1));
						flag5 = true;
					}

					if (!flag5) {
						d1 += 0.0D + 0.2D * d3 / 40.0D;
						quaternion.mul(Vector3f.XP.rotationDegrees((float)d3));
					}

					quaternion.mul(Vector3f.XP.rotationDegrees(-65.0F));
					d2 = d2 + (double)(-0.75F + f4 / 10.0F * 0.25F);
				}
				else if (vivecraftitemrendering != VivecraftItemRendering.Sword) {
					if (vivecraftitemrendering == VivecraftItemRendering.Tool_Rod) {
						d2 += (double) - 0.15F;
						d1 += -0.02D + d3 / 40.0D * 0.1D;
						d0 += (double)0.05F;
						quaternion.mul(Vector3f.XP.rotationDegrees(40.0F));
						d8 = (double)0.8F;
					}
					else if (vivecraftitemrendering == VivecraftItemRendering.Tool) {
						boolean flag4 = DataHolder.getInstance().climbTracker.isClaws(pStack) && DataHolder.getInstance().climbTracker.isClimbeyClimb();

						if (flag4) {
							quaternion.mul(Vector3f.XP.rotationDegrees((float) (-d3)));
							d8 = (double) 0.3F;
							d2 += (double) 0.075F;
							d1 += (double) 0.02F;
							d0 += (double) 0.03F;

							if (DataHolder.getInstance().vr.keyClimbeyGrab.isDown(ControllerType.RIGHT) && flag || DataHolder.getInstance().vr.keyClimbeyGrab.isDown(ControllerType.LEFT) && !flag) {
								d2 += (double) - 0.2F;
							}
						}
						if (item instanceof ArrowItem) {
							quaternion1 = Vector3f.ZP.rotationDegrees(-180.0F);
							quaternion.mul(Vector3f.XP.rotationDegrees((float)(-d3)));
						}
					}
					else if (vivecraftitemrendering == VivecraftItemRendering.Telescope) {
						quaternion1 = Vector3f.XP.rotationDegrees(0.0F);
						quaternion = Vector3f.XP.rotationDegrees(0.0F);
						d2 = 0.0D;
						d1 = 0.0D;
						d0 = 0.0D;
					}
				}
			}
			else {
				quaternion = Vector3f.ZP.rotationDegrees(180.0F);
				quaternion.mul(Vector3f.XP.rotationDegrees(-135.0F));
				d8 = (double)0.4F;
				d0 += (double)0.08F;
				d2 += (double) - 0.08F;
			}

			if (pPlayer.swingingArm == pHand) {
				this.transformFirstPersonVR(pMatrixStack, humanoidarm, pSwingProgress);
			}

			ItemRenderer itemrenderer = this.itemRenderer;
			DataHolder.getInstance().isfphand = true;
			ItemTransforms.TransformType itemtransforms$transformtype = flag ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : (flag3 ? ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);

			if (pStack.hasTag() && pStack.getTag().getInt("CustomModelData") > 0) {
				itemtransforms$transformtype = ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
				pMatrixStack.scale(0.7F, 0.7F, 0.7F);
			}
			else {
				pMatrixStack.mulPose(quaternion1);
				pMatrixStack.translate(d0, d1, d2);
				pMatrixStack.mulPose(quaternion);
				pMatrixStack.scale((float)d8, (float)d8, (float)d8);
			}

			if (flag2)
			{
				RenderSystem.disableCull();
				this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
			}
			else if (vivecraftitemrendering == VivecraftItemRendering.Telescope) {
				if (DataHolder.getInstance().currentPass != RenderPass.SCOPEL && DataHolder.getInstance().currentPass != RenderPass.SCOPER) {
					pMatrixStack.pushPose();
					pMatrixStack.scale(0.625F, 0.625F, 0.625F);
					pMatrixStack.translate(flag ? -0.53D : -0.47D, -0.5D, -0.6D);
					this.minecraft.getBlockRenderer().getModelRenderer().renderModel(pMatrixStack.last(), pBuffer.getBuffer(Sheets.solidBlockSheet()), (BlockState)null, this.minecraft.getModelManager().getModel(TelescopeTracker.scopeModel), 0.5F, 0.5F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);
					pMatrixStack.popPose();
				}
				pMatrixStack.pushPose();
				pMatrixStack.translate(flag ? -0.01875D : 0.01875D, 0.215D, -0.0626D);
				pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
				pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
				((GameRendererExtension)this.minecraft.gameRenderer).DrawScopeFB(pMatrixStack, pHand == InteractionHand.MAIN_HAND ? 0 : 1);
				pMatrixStack.popPose();
			}
			else {
				this.renderItem(pPlayer, pStack, itemtransforms$transformtype, flag3, pMatrixStack, pBuffer, pCombinedLight);
			}

			itemrenderer = this.itemRenderer;
			DataHolder.getInstance().isfphand = false;
			pMatrixStack.popPose();
		}
		pMatrixStack.popPose();
	}

	@Shadow
	public abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);

	@Shadow
	protected abstract void renderMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack);

	@Shadow
	protected abstract void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm humanoidarm);


	public float getEquipProgress(InteractionHand hand, float partialTicks) {
		return hand == InteractionHand.MAIN_HAND ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks) : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
	}


	public void vrPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm) {
		boolean flag = humanoidArm != HumanoidArm.LEFT;
		float h = flag ? 1.0F : -1.0F;
		AbstractClientPlayer abstractclientplayer = this.minecraft.player;
		RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
		VRArmRenderer vrarmrenderer = ((EntityRenderDispatcherExtension)entityRenderDispatcher).getArmSkinMap().get(abstractclientplayer.getModelName());
		poseStack.pushPose();

		if (abstractclientplayer.swingingArm == InteractionHand.MAIN_HAND && flag) {
			this.transformFirstPersonVR(poseStack, humanoidArm, g);
		}

		if (abstractclientplayer.swingingArm == InteractionHand.OFF_HAND && !flag) {
			this.transformFirstPersonVR(poseStack, humanoidArm, g);
		}

		poseStack.scale(0.4f, 0.4F, 0.4F);
		boolean slim = abstractclientplayer.getSkinTextureLocation().getPath().equals("slim");
		poseStack.translate(slim ? 0.345F * -h : 0.375F * -h, 0, slim ? 0.785F : 0.75F);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
		if (flag) {
			vrarmrenderer.renderRightHand(poseStack, multiBufferSource, i, abstractclientplayer);
		}
		else {
			vrarmrenderer.renderLeftHand(poseStack, multiBufferSource, i, abstractclientplayer);
		}
		poseStack.popPose();
	}


	@Override
	public void setXdist(float v) {
		this.xdist = v;
	}

	@Override
	public void setSwingType(VRFirstPersonArmSwing interact) {
		this.swingType = interact;
	}

	private void transformFirstPersonVR(PoseStack matrixStackIn, HumanoidArm hand, float swingProgress) {
		if (swingProgress != 0.0F) {
			switch (this.swingType) {
				case Attack:
					float f2 = Mth.sin((float)((double)(swingProgress * 3.0F) * Math.PI));
					if ((double)swingProgress > 0.5D)
					{
						f2 = Mth.sin((float)((double)swingProgress * Math.PI + Math.PI));
					}

					matrixStackIn.translate(0.0D, 0.0D, (double)0.2F);
					matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(f2 * 30.0F));
					matrixStackIn.translate(0.0D, 0.0D, (double) - 0.2F);
					break;

				case Interact:
					float f1 = Mth.sin((float)((double)(swingProgress * 3.0F) * Math.PI));

					if ((double)swingProgress > 0.5D) {
						f1 = Mth.sin((float)((double)swingProgress * Math.PI + Math.PI));
					}

					matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees((float)(hand == HumanoidArm.RIGHT ? -1 : 1) * f1 * 45.0F));
					break;

				case Use:
					float f = Mth.sin((float)((double)(swingProgress * 2.0F) * Math.PI));

					if ((double)swingProgress > 0.25D) {
						f = Mth.sin((float) ((double) (swingProgress / 2.0F) * Math.PI + Math.PI));
					}
					matrixStackIn.translate(0.0D, 0.0D, (double)(-(1.0F + f) * 0.1F));
			}
		}
	}
}
