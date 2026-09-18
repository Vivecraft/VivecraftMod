package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.world.item.ItemDisplayContext;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.render.VivecraftItemRendering;

public class FirstPersonHandsAdditions {

    public VivecraftItemRendering.VivecraftItemTransformType mainHandItemTransformType;
    public VivecraftItemRendering.VivecraftItemTransformType offHandItemTransformType;

    public ItemDisplayContext mainHandItemDisplayContext;
    public ItemDisplayContext offHandItemDisplayContext;

    public boolean bowActive;
    public boolean isBlocking;
    public boolean isInWaterOrRain;
    public int useItemRemainingTicks;
    public float ticksSinceLastKineticHitFeedback;

    public boolean handsVisibleInFirstPerson;
    public boolean handsVisibleInThirdPerson;

    public float mainHandFade;
    public float offHandFade;

    public boolean mainHandCooldown;
    public boolean offHandCooldown;

    public int mainHandItemUseDuration;
    public int offHandItemUseDuration;

    public float mainHandRiptideLevel;
    public float offHandRiptideLevel;

    public RenderPass currentPass;

    public float getHandFade(boolean mainHand) {
        return mainHand ? this.mainHandFade : this.offHandFade;
    }

    public boolean isCooldown(boolean mainHand) {
        return mainHand ? this.mainHandCooldown : this.offHandCooldown;
    }

    public int getItemUseDuration(boolean mainHand) {
        return mainHand ? this.mainHandItemUseDuration : this.offHandItemUseDuration;
    }

    public float getRiptideLevel(boolean mainHand) {
        return mainHand ? this.mainHandRiptideLevel : this.offHandRiptideLevel;
    }
}
