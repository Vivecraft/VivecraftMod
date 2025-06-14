package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.interact_modules.*;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InteractTracker implements Tracker {

    public final InteractiveHotbarModule hotbarModule;
    public final RoomscaleBowModule bowModule;
    public final ThirdPersonCameraModule thirdCamModule;
    public final ScreenshotCameraModule screenCamModule;

    public final EntityInteractionModule entityModule;
    public final BlockInteractionModule blockModule;

    private final List<InteractModule> preAPIModules;
    private final List<InteractModule> postAPIModules;

    private List<InteractModule> modules;

    public final InteractModule[] activeModules = new InteractModule[2];

    protected Minecraft mc;
    protected ClientDataHolderVR dh;

    public InteractTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;

        this.hotbarModule = new InteractiveHotbarModule();
        this.bowModule = new RoomscaleBowModule(dh);
        this.thirdCamModule = new ThirdPersonCameraModule(dh);
        this.screenCamModule = new ScreenshotCameraModule(dh);

        this.entityModule = new EntityInteractionModule(mc, dh);
        this.blockModule = new BlockInteractionModule(mc, dh);

        this.preAPIModules = List.of(this.hotbarModule, this.bowModule, this.thirdCamModule, this.screenCamModule);
        this.postAPIModules = List.of(this.entityModule, this.blockModule);

        setModules(Collections.emptyList());
    }

    public void setModules(Collection<InteractModule> modules) {
        this.modules = new ArrayList<>(this.preAPIModules.size() + modules.size() + this.postAPIModules.size());
        this.modules.addAll(this.preAPIModules);
        this.modules.addAll(modules);
        this.modules.addAll(this.postAPIModules);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.mc.gameMode == null) {
            return false;
        } else if (player == null) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else if (this.dh.vrSettings.seated) {
            return false;
        } else {
            return !player.isBlocking() || this.hotbarModule.hotbar >= 0;
        }
    }

    @Override
    public void reset(LocalPlayer player) {
        for (int c = 0; c < 2; c++) {
            this.reset(player, c);
        }
    }

    private void reset(LocalPlayer player, int c) {
        if (this.activeModules[c] instanceof HeldInteractModule heldModule) {
            heldModule.onRelease(player, InteractionHand.values()[c]);
        }
        this.activeModules[c] = null;
        this.modules.forEach(module -> module.reset(player, InteractionHand.values()[c]));

        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).setEnabled(ControllerType.values()[c], false);
    }

    @Override
    public TrackerTickType tickType() {
        return TrackerTickType.PER_TICK;
    }

    @Override
    public void doProcess(LocalPlayer player) {
        for (int c = 0; c < 2; c++) {
            if (VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.values()[c]) &&
                this.activeModules[c] instanceof HeldInteractModule heldModule &&
                heldModule.onHoldTick(player, InteractionHand.values()[c]))
            {
                // don't reevaluate, if the interact is still active
                continue;
            }

            boolean wasActive = this.activeModules[c] != null;

            this.reset(player, c);

            Vec3 handPos = this.dh.vrPlayer.vrdata_world_pre.getController(c).getPosition();

            for (InteractModule module : this.modules) {
                if (module.isActive(player, InteractionHand.values()[c], handPos)) {
                    this.activeModules[c] = module;
                    break;
                }
            }

            // haptic if something activated
            if (!wasActive && this.activeModules[c] != null) {
                this.dh.vr.triggerHapticPulse(c, 250);
            }

            this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract)
                .setEnabled(ControllerType.values()[c], this.activeModules[c] != null);
        }
    }

    /**
     * check if the given {@code module} is active on any controller
     *
     * @param module InteractModule to check
     * @return if the module is active on any controller
     */
    public boolean isActiveModule(InteractModule module) {
        return isActiveModule(module, 0) || isActiveModule(module, 1);
    }

    /**
     * check if the given {@code module} is active on the given controller
     *
     * @param module     InteractModule to check
     * @param controller controller to check
     * @return if the module is active on the given controller
     */
    public boolean isActiveModule(InteractModule module, int controller) {
        return this.activeModules[controller] == module;
    }

    public void processBindings() {
        for (int c = 0; c < 2; c++) {
            if (VivecraftVRMod.INSTANCE.keyVRInteract.consumeClick(ControllerType.values()[c]) &&
                this.activeModules[c] != null)
            {
                InteractionHand hand = InteractionHand.values()[c];
                if (this.activeModules[c].onPress(this.mc.player, hand)) {
                    if (this.activeModules[c].swingsArm()) {
                        // swing arm on success
                        this.dh.swingType = VRFirstPersonArmSwing.Interact;
                        this.mc.player.swing(hand);
                    }
                    this.dh.vr.triggerHapticPulse(c, 750);
                }
            }
        }
    }
}
