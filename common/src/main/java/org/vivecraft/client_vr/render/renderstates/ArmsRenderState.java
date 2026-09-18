package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.vivecraft.client.extensions.FirstPersonHandsAndItemsStateExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.data.ViveItems;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import javax.annotation.Nullable;

public class ArmsRenderState {
    public boolean renderHands;
    public boolean handsSecond;
    public boolean menuHandMain;
    public boolean menuHandOff;
    public Vec3 mainHandWorldPos;
    public Vec3 offHandWorldPos;
    public final Matrix4f mainHandWorldRot = new Matrix4f();
    public final Matrix4f offHandWorldRot = new Matrix4f();
    public boolean skipMainHandItemRendering;
    public boolean skipOffHandItemRendering;
    public int headLight;
    public int rawHeadLightCoords;

    public boolean reverseHands;

    public void extract(@Nullable LocalPlayer player, Vec3 headPos) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();

        // render hands in second pass when gui is open
        this.handsSecond = RadialHandler.isShowing() || KeyboardHandler.SHOWING || mc.gui.screen() != null;
        this.menuHandMain = dataHolder.menuHandMain;
        this.menuHandOff = dataHolder.menuHandOff;
        this.mainHandWorldPos = RenderHelper.setupRenderingAtController(0, this.mainHandWorldRot.identity(), false);
        this.offHandWorldPos = RenderHelper.setupRenderingAtController(1, this.offHandWorldRot.identity(), false);
        this.headLight = player == null ? 15 :
            Math.max(ShadersHelper.ShaderLight(),
                player.level().getMaxLocalRawBrightness(BlockPos.containing(headPos)));
        this.rawHeadLightCoords =
            player != null ? LightCoordsUtil.getLightCoords(player.level(), BlockPos.containing(headPos)) :
                LightCoordsUtil.FULL_BRIGHT;

        // don't render claws with model arms
        this.skipMainHandItemRendering = player != null && dataHolder.vrSettings.shouldRenderSelf &&
            dataHolder.vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE &&
            (dataHolder.climbTracker.isClimbeyClimb() || ViveItems.isClimbingClaws(player.getMainHandItem()));
        this.skipOffHandItemRendering = player != null && dataHolder.vrSettings.shouldRenderSelf &&
            dataHolder.vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE &&
            (dataHolder.climbTracker.isClimbeyClimb() || ViveItems.isClimbingClaws(player.getOffhandItem()));

        if (player != null) {

        }
    }

    public static FirstPersonHandsAdditions extractAdditions(
        FirstPersonHandsAndItemsRenderState state, LocalPlayer player, float partialTick)
    {
        // set up arm and item states
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        FirstPersonHandsAdditions additions = ((FirstPersonHandsAndItemsStateExtension) state).vivecraft$getAdditions();

        additions.currentPass = dataHolder.currentPass;

        ItemStack mainHandItem = VRArmHelper.extractHandRenderItem(player, InteractionHand.MAIN_HAND);
        ItemStack offHandItem = VRArmHelper.extractHandRenderItem(player, InteractionHand.OFF_HAND);

        additions.handsVisibleInFirstPerson = dataHolder.vrSettings.showPlayerHands &&
            !(dataHolder.vrSettings.shouldRenderSelf &&
                dataHolder.vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE
            );
        additions.handsVisibleInThirdPerson = dataHolder.vrSettings.mixedRealityRenderHands;
        additions.bowActive = dataHolder.bowTracker.isActive(player);

        additions.mainHandItemTransformType = VivecraftItemRendering.getTransformType(mainHandItem, player);
        additions.offHandItemTransformType = VivecraftItemRendering.getTransformType(offHandItem, player);

        additions.isBlocking = player.isBlocking();
        additions.isInWaterOrRain = player.isInWaterOrRain();
        additions.useItemRemainingTicks = player.getUseItemRemainingTicks();
        additions.ticksSinceLastKineticHitFeedback = player.getTicksSinceLastKineticHitFeedback(partialTick);

        additions.mainHandFade = SwingTracker.getItemFade(player, ItemStack.EMPTY, true);
        additions.offHandFade = SwingTracker.getItemFade(player, ItemStack.EMPTY, false);
        additions.mainHandCooldown = player.getCooldowns().isOnCooldown(mainHandItem);
        additions.offHandCooldown = player.getCooldowns().isOnCooldown(offHandItem);
        additions.mainHandRiptideLevel = EnchantmentHelper.getTridentSpinAttackStrength(mainHandItem, player);
        additions.offHandRiptideLevel = EnchantmentHelper.getTridentSpinAttackStrength(offHandItem, player);
        additions.mainHandItemUseDuration = mainHandItem.getUseDuration(player);
        additions.offHandItemUseDuration = offHandItem.getUseDuration(player);

        state.mainHandItem = mainHandItem;
        state.offHandItem = offHandItem;
        additions.mainHandItemDisplayContext = getDisplayContext(player, state.mainHandItem, true,
            additions.mainHandItemTransformType);
        additions.offHandItemDisplayContext = getDisplayContext(player, state.offHandItem, false,
            additions.offHandItemTransformType);

        return additions;
    }

    private static ItemDisplayContext getDisplayContext(
        @Nullable LocalPlayer player, ItemStack itemStack, boolean mainHand,
        VivecraftItemRendering.VivecraftItemTransformType transformType)
    {
        boolean useLeftHandModelinLeftHand = false;

        // third person transforms for custom model data items/item model overrides, but not spear, shield and crossbow
        boolean hasItemOverride = itemStack.getComponents() instanceof PatchedDataComponentMap patched &&
            patched.hasNonDefault(DataComponents.ITEM_MODEL);
        boolean hasCMD = (hasItemOverride || itemStack.has(DataComponents.CUSTOM_MODEL_DATA)) &&
            transformType != VivecraftItemRendering.VivecraftItemTransformType.CROSSBOW &&
            transformType != VivecraftItemRendering.VivecraftItemTransformType.SPEAR &&
            transformType != VivecraftItemRendering.VivecraftItemTransformType.SHIELD;

        boolean isBow = BowTracker.isBow(itemStack) && ClientDataHolderVR.getInstance().bowTracker.isActive(player);

        if (ViveItems.isClimbingClaws(itemStack) || (!isBow &&
            (ClientNetworking.isThirdPersonItems() || (hasCMD && ClientNetworking.isThirdPersonItemsCustom()))
        ))
        {
            // swap hand, since it's backwards else wise
            if (ClientDataHolderVR.getInstance().vrSettings.reverseHands) {
                mainHand = !mainHand;
            }
            useLeftHandModelinLeftHand = true; // test

            return mainHand || !useLeftHandModelinLeftHand ?
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        } else {
            return mainHand || !useLeftHandModelinLeftHand ?
                ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        }
    }
}
