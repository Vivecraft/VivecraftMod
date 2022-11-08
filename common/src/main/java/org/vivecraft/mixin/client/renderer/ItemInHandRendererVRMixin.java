package org.vivecraft.mixin.client.renderer;

import java.util.Optional;
import java.util.stream.Stream;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.EntityRenderDispatcherExtension;
import org.vivecraft.extensions.GameRendererExtension;
import org.vivecraft.extensions.ItemInHandRendererExtension;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRArmRenderer;
import org.vivecraft.render.VRFirstPersonArmSwing;
import org.vivecraft.render.VivecraftItemRendering;

@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererVRMixin implements ItemInHandRendererExtension {

	@Unique
	private float xdist = 0F;

	@Unique
	private VRFirstPersonArmSwing swingType = VRFirstPersonArmSwing.Attack;

	@Final
	@Shadow
	private Minecraft minecraft;
	ClientDataHolder dh = ClientDataHolder.getInstance();
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
		boolean mainHand = pHand == InteractionHand.MAIN_HAND;
		HumanoidArm humanoidarm = mainHand ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
		pEquippedProgress = this.getEquipProgress(pHand, pPartialTicks);
		pMatrixStack.pushPose();
		boolean renderArm = true;
		
		if (dh.currentPass == RenderPass.THIRD && !dh.vrSettings.mixedRealityRenderHands)
		{
			renderArm = false;
		}

		if (dh.currentPass == RenderPass.CAMERA)
		{
			renderArm = false;
		}

		if (BowTracker.isBow(pStack) && dh.bowTracker.isActive((LocalPlayer)pPlayer))
		{
			renderArm = false;
		}

		if (TelescopeTracker.isTelescope(pStack) && (pHand == InteractionHand.OFF_HAND && dh.currentPass == RenderPass.SCOPEL || pHand == InteractionHand.MAIN_HAND && dh.currentPass == RenderPass.SCOPER))
		{
			renderArm = false;
		}

		if (renderArm && !pPlayer.isInvisible())
		{
			this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
		}

		if (!pStack.isEmpty())
		{   			
			pMatrixStack.pushPose();        

			if (pPlayer.swingingArm == pHand)
				this.transformFirstPersonVR(pMatrixStack, humanoidarm, pSwingProgress);

			VivecraftItemRendering.VivecraftItemTransformType rendertype = VivecraftItemRendering.getTransformType(pStack, pPlayer, itemRenderer);

			boolean useLeftHandModelinLeftHand = false;

			ItemTransforms.TransformType transformType;
			if(dh.vrSettings.thirdPersonItems) {
				useLeftHandModelinLeftHand = true; //test
				VivecraftItemRendering.applyThirdPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
				transformType = mainHand ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
			}
			else {
				VivecraftItemRendering.applyFirstPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
				transformType = mainHand ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);                
			}

			dh.isfphand = true;

			if (rendertype == VivecraftItemRendering.VivecraftItemTransformType.Map)
			{
				RenderSystem.disableCull();
				this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
			}
			else if (rendertype == VivecraftItemRendering.VivecraftItemTransformType.Telescope)
			{
				if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER)
				{
					pMatrixStack.pushPose();
					pMatrixStack.scale(0.625F, 0.625F, 0.625F);
					pMatrixStack.translate(mainHand ? -0.53D : -0.47D, -0.5D, -0.6D);
					//pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
					this.minecraft.getBlockRenderer().getModelRenderer().renderModel(pMatrixStack.last(), pBuffer.getBuffer(Sheets.solidBlockSheet()), (BlockState)null, this.minecraft.getModelManager().getModel(TelescopeTracker.scopeModel), 0.5F, 0.5F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);
					pMatrixStack.popPose();
				}

				pMatrixStack.pushPose();
				pMatrixStack.translate(mainHand ? -0.01875D : 0.01875D, 0.215D, -0.0626D);
				pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
				pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
				((GameRendererExtension)this.minecraft.gameRenderer).DrawScopeFB(pMatrixStack, pHand == InteractionHand.MAIN_HAND ? 0 : 1);
				pMatrixStack.popPose();
			}
			else
			{
				this.renderItem(pPlayer, pStack, transformType, !mainHand && useLeftHandModelinLeftHand, pMatrixStack, pBuffer, pCombinedLight);
			}

			dh.isfphand = false;
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
		boolean slim = abstractclientplayer.getModelName().equals("slim");

            /*
             x offset: (arm x origin + arm x offset + arm x dimension * 0.5) / 16
             z offset: (arm y origin + arm y offset + arm y dimension) / 16
             slim
             x offset: (5 + -1 + 3*0.5) / 16 = 0.34375
             z offset: (-2 + 2.5 + 12) / 16 = 0.78125
             regular
             x offset: (5 - 1 + 4*0.5) / 16 = 0.375
             z offset: (-2 + 2 + 12) / 16 = 0.75
            */

		poseStack.translate((slim ? -0.34375F : -0.375F) * h, 0.0F, slim ? 0.78125F : 0.75F);
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
