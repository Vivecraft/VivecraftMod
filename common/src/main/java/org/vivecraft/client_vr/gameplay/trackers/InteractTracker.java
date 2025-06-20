package org.vivecraft.client_vr.gameplay.trackers;

import com.google.common.collect.Streams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

import java.util.*;

public class InteractTracker implements Tracker {

    // list of registered interact modules
    private final Queue<InteractModule> apiModules = new PriorityQueue<>(
        (a, b) -> a.getPriority() == b.getPriority() ? a.getId().compareTo(b.getId()) :
            Integer.compare(a.getPriority(), b.getPriority()));

    // list of registered priority interact modules
    private final Queue<InteractModule> priorityModules = new PriorityQueue<>(
        (a, b) -> a.getPriority() == b.getPriority() ? a.getId().compareTo(b.getId()) :
            Integer.compare(a.getPriority(), b.getPriority()));

    private final List<InteractModule> modules = new ArrayList<>();
    private final InteractModule[] activeModules = new InteractModule[2];
    private final boolean[] pressed = new boolean[2];

    protected Minecraft mc;
    protected ClientDataHolderVR dh;

    public InteractTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    /**
     * registers interact modules, and ads them sorted based on their priority
     *
     * @param modules modules to register
     * @throws IllegalArgumentException if a module is already registered
     */
    public void registerModules(InteractModule... modules) {
        registerModules(this.apiModules, modules);
    }

    /**
     * registers interact modules, and ads them to the priority list sorted based on their priority
     *
     * @param modules modules to register
     * @throws IllegalArgumentException if a module is already registered
     */
    public void registerPriorityModules(InteractModule... modules) {
        registerModules(this.priorityModules, modules);
    }

    // synchronized, since this could be called from multiple threads during startup
    private synchronized void registerModules(Collection<InteractModule> target, InteractModule... modules) {
        for (InteractModule module : modules) {
            if (Streams.concat(this.modules.stream(), target.stream())
                .anyMatch(m -> m.equals(module) || m.getId().equals(module.getId())))
            {
                throw new IllegalArgumentException(
                    "InteractModule '" + module.getId() + "' is already added and should not be added again!");
            }
            target.add(module);
        }
        this.modules.clear();
        this.modules.addAll(this.priorityModules);
        this.modules.addAll(this.apiModules);
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
            return !player.isBlocking() || this.dh.hotbarModule.hotbar >= 0;
        }
    }

    @Override
    public void reset(LocalPlayer player) {
        for (int c = 0; c < 2; c++) {
            this.reset(player, c);
        }
    }

    private void reset(LocalPlayer player, int c) {
        if (this.pressed[c] && this.activeModules[c] instanceof HeldInteractModule heldModule) {
            heldModule.onRelease(player, InteractionHand.values()[c]);
        }
        this.pressed[c] = false;
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
                this.pressed[c] = true;
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
