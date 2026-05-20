package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.utils.MathUtils;

public class EntityInteractionModule implements InteractModule {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("vivecraft", "entity_interact");

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    private final EntityHitResult[] inEntityHit = new EntityHitResult[2];

    public EntityInteractionModule(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void reset(LocalPlayer player, InteractionHand hand) {
        this.inEntityHit[hand.ordinal()] = null;
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        if (this.dh.vrSettings.realisticEntityInteractEnabled) {
            Vec3 hmdPos = this.dh.vrPlayer.vrdata_world_pre.getHeadPivot();
            Vector3f handDirection = this.dh.vrPlayer.vrdata_world_pre.getHand(hand.ordinal())
                .getCustomVector(MathUtils.BACK);

            Vec3 extWeapon = new Vec3(handPosition.x + handDirection.x * -0.1F,
                handPosition.y + handDirection.y * -0.1F, handPosition.z + handDirection.z * -0.1F);

            AABB weaponBB = new AABB(handPosition, extWeapon);
            this.inEntityHit[hand.ordinal()] = ProjectileUtil.getEntityHitResult(this.mc.getCameraEntity(), hmdPos,
                handPosition, weaponBB,
                (e) -> !e.isSpectator() && e.isPickable() && e != this.mc.getCameraEntity().getVehicle(), 0.0D);

            return this.inEntityHit[hand.ordinal()] != null;
        }
        return false;
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        return this.mc.gameMode.interact(player, this.inEntityHit[hand.ordinal()].getEntity(),
            this.inEntityHit[hand.ordinal()], hand) instanceof InteractionResult.Success;
    }
}
