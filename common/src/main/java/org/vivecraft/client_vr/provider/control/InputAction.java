package org.vivecraft.client_vr.provider.control;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.vivecraft.Xplat;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.InputSimulator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InputAction {
    public final KeyMapping keyBinding;
    public final String name;
    public final String requirement;
    public ActionType type;
    public final VRInputActionSet actionSet;

    protected int priority = 0;
    protected final List<InputAction.KeyListener> listeners = new ArrayList<>();
    protected ControllerType currentHand = ControllerType.RIGHT;
    // Only used for the UseTracked axis methods
    protected boolean currentlyInUse;

    protected final boolean[] enabled = new boolean[ControllerType.values().length];
    protected final boolean[] pressed = new boolean[ControllerType.values().length];
    protected final boolean[] held = new boolean[ControllerType.values().length];
    protected final int[] unpressInTicks = new int[ControllerType.values().length];

    public InputAction(KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride) {
        this.keyBinding = keyMapping;
        this.requirement = requirement;
        this.type = type;
        this.actionSet = actionSetOverride != null ? actionSetOverride : VRInputActionSet.fromKeyBinding(keyMapping);
        this.name = this.actionSet.name + "/in/" + keyMapping.getName().replace('/', '_');
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isHanded() {
        return this.keyBinding instanceof HandedKeyBinding;
    }

    public ControllerType getCurrentHand() {
        return this.currentHand;
    }

    public void setCurrentHand(ControllerType currentHand) {
        this.currentHand = currentHand;
    }

    /**
     * check if the InputAction is enabled, if it is handed, checks for {@link InputAction#currentHand} <br>
     * also checks if any other InputAction with higher priority is active, then this InputAction is treated as disabled
     */
    public boolean isEnabled() {
        if (!this.isEnabledRaw(this.currentHand)) return false;
        if (ClientDataHolderVR.getInstance().vr == null) return false;

        long lastOrigin = this.getLastOrigin();
        ControllerType hand = ClientDataHolderVR.getInstance().vr.getOriginControllerType(lastOrigin);

        if (hand == null && this.isHanded()) return false;

        // iterate over all actions, and check if another action has a higher priority
        for (InputAction action : ClientDataHolderVR.getInstance().vr.getInputActions()) {
            if (action != this && action.isEnabledRaw(hand) && action.isActive() &&
                action.getPriority() > this.getPriority() &&
                ClientDataHolderVR.getInstance().vr.getOrigins(action).contains(lastOrigin))
            {
                if (action.isHanded()) {
                    return !((HandedKeyBinding) action.keyBinding).isPriorityOnController(hand);
                }
                return false;
            }
        }

        return true;
    }

    public boolean isEnabledRaw(ControllerType hand) {
        if (this.isHanded()) {
            return hand != null && this.enabled[hand.ordinal()];
        } else {
            return this.enabled[0];
        }
    }

    public boolean isEnabledRaw() {
        return Arrays.stream(ControllerType.values()).anyMatch(this::isEnabledRaw);
    }

    public void setEnabled(ControllerType hand, boolean enabled) {
        if (!this.isHanded()) {
            throw new IllegalStateException("Not a handed key binding!");
        } else {
            this.enabled[hand.ordinal()] = enabled;
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.isHanded()) {
            for (ControllerType controllertype : ControllerType.values()) {
                this.enabled[controllertype.ordinal()] = enabled;
            }
        } else {
            this.enabled[0] = enabled;
        }
    }

    public boolean isActive() {
        return true;
    }

    public boolean isButtonPressed() {
        return false;
    }

    public boolean isButtonChanged() {
        return false;
    }

    public void setPressed(boolean pressed) {
        if (pressed) {
            this.pressBinding();
        } else {
            this.unpressBinding();
        }
    }

    private void pressBinding(ControllerType hand) {
        if (this.isHanded()) {
            if (hand == null || this.pressed[hand.ordinal()]) return;

            this.pressed[hand.ordinal()] = true;

            if (this.notifyListeners(true, hand)) return;

            ((HandedKeyBinding) this.keyBinding).pressKey(hand);
        } else {
            if (this.pressed[0]) return;

            this.pressed[0] = true;

            if (this.notifyListeners(true, null)) return;

            this.pressKey();
        }
    }

    public void pressBinding() {
        this.pressBinding(this.currentHand);
    }

    public void unpressBinding(int unpressInTicks, ControllerType hand) {
        if (this.isHanded()) {
            if (hand == null || !this.pressed[hand.ordinal()]) return;

            this.unpressInTicks[hand.ordinal()] = unpressInTicks;
        } else {
            if (!this.pressed[0]) return;

            this.unpressInTicks[0] = unpressInTicks;
        }
    }

    public void unpressBinding(int unpressInTicks) {
        this.unpressBinding(unpressInTicks, this.currentHand);
    }

    public void unpressBinding() {
        this.unpressBinding(1);
    }

    /**
     * presses the KeyMapping assigned to this InputAction <br>
     * if the KeyMapping has a modifier key also presses that
     */
    private void pressKey() {
        InputConstants.Key key = this.keyBinding.key;

        // need to simulate the modifier or the binding wouldn't be pressed
        if (key.getValue() != -1 &&
            (!VivecraftVRMod.INSTANCE.isSafeBinding(this.keyBinding) || Xplat.INSTANCE.hasKeyModifier(this.keyBinding)))
        {
            if (key.getType() == InputConstants.Type.KEYSYM) {
                if (Xplat.INSTANCE.hasKeyModifier(this.keyBinding)) {
                    InputSimulator.pressModifier(Xplat.INSTANCE.getKeyModifierKey(this.keyBinding));
                }
                InputSimulator.pressKey(key.getValue(), Xplat.INSTANCE.getKeyModifier(this.keyBinding));
                return;
            }

            if (key.getType() == InputConstants.Type.MOUSE) {
                InputSimulator.pressMouse(key.getValue());
                return;
            }
        }

        setKeyBindState(this.keyBinding, true);
    }

    /**
     * unpresses the KeyMapping assigned to this InputAction <br>
     * if the KeyMapping has a modifier key also unpresses that
     */
    public void unpressKey() {
        InputConstants.Key key = this.keyBinding.key;

        if (key.getValue() != -1 &&
            (!VivecraftVRMod.INSTANCE.isSafeBinding(this.keyBinding) || Xplat.INSTANCE.hasKeyModifier(this.keyBinding)))
        {
            if (key.getType() == InputConstants.Type.KEYSYM) {
                InputSimulator.releaseKey(key.getValue());
                if (Xplat.INSTANCE.hasKeyModifier(this.keyBinding)) {
                    InputSimulator.releaseModifier(Xplat.INSTANCE.getKeyModifierKey(this.keyBinding));
                }
                return;
            }

            if (key.getType() == InputConstants.Type.MOUSE) {
                InputSimulator.releaseMouse(key.getValue());
                return;
            }
        }

        this.keyBinding.release();
    }

    public void holdBinding() {
        this.holdBinding(this.currentHand);
    }

    public void holdBinding(ControllerType hand) {
        if (this.isHanded()) {
            this.held[hand.ordinal()] = true;
        } else {
            this.held[0] = true;
        }
        this.pressBinding(this.currentHand);
    }

    public void stopHoldingBinding(int unpressInTicks) {
        this.stopHoldingBinding(unpressInTicks, this.currentHand);
    }

    public void stopHoldingBinding(int unpressInTicks, ControllerType hand) {
        if (this.isHanded()) {
            this.held[hand.ordinal()] = false;
        } else {
            this.held[0] = false;
        }
        this.unpressBinding(unpressInTicks, this.currentHand);
    }

    public void unpressBindingImmediately() {
        if (this.isHanded()) {
            for (int c = 0; c < ControllerType.values().length; c++) {
                this.unpressBindingImmediately(ControllerType.values()[c]);
            }
        } else {

            this.unpressBindingImmediately(null);
        }
    }

    public void unpressBindingImmediately(ControllerType hand) {
        if (this.isHanded()) {
            if (hand == null || !this.pressed[hand.ordinal()]) return;

            this.pressed[hand.ordinal()] = false;
            this.held[hand.ordinal()] = false;

            if (this.notifyListeners(false, hand)) return;

            ((HandedKeyBinding) this.keyBinding).unpressKey(hand);
        } else {
            if (!this.pressed[0]) return;

            this.pressed[0] = false;
            this.held[0] = false;

            if (this.notifyListeners(false, null)) return;

            this.unpressKey();
        }
    }

    public static void setKeyBindState(KeyMapping keyMapping, boolean pressed) {
        if (keyMapping != null) {
            keyMapping.setDown(pressed);
            keyMapping.clickCount += 1;
        }
    }

    public void tick() {
        if (this.isHanded()) {
            for (int c = 0; c < ControllerType.values().length; c++) {
                ControllerType type = ControllerType.values()[c];
                HandedKeyBinding handedKeyBinding = (HandedKeyBinding) this.keyBinding;
                if ((!this.held[c] && this.unpressInTicks[c] > 0 && --this.unpressInTicks[c] == 0) ||
                    (this.held[c] && (!handedKeyBinding.isDown(type) || handedKeyBinding.presses(type) == 0)))
                {
                    this.unpressBindingImmediately(type);
                }
            }
        } else {
            if ((!this.held[0] && this.unpressInTicks[0] > 0 && --this.unpressInTicks[0] == 0) ||
                (this.held[0] && (!this.keyBinding.isDown() || this.keyBinding.clickCount == 0)))
            {
                this.unpressBindingImmediately(null);
            }
        }
    }

    /**
     * adds a KeyListener that gets notified for state changes
     *
     * @param listener KeyListener to register
     */
    public void registerListener(KeyListener listener) {
        this.listeners.add(listener);
        this.listeners.sort(Comparator.comparingInt(KeyListener::getPriority).reversed());
    }

    /**
     * removes the specified KeyListeners
     */
    public void unregisterListener(KeyListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * notifies all registered KeyListener in priority order
     *
     * @param pressed if presses or released
     * @param hand    controller this was triggered by
     * @return if any KeyListener triggered
     */
    public boolean notifyListeners(boolean pressed, ControllerType hand) {
        for (KeyListener listener : this.listeners) {
            if (pressed) {
                if (listener.onPressed(hand)) {
                    return true;
                }
            } else if (listener.onUnpressed(hand)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the last origin the vr runtime sent, if the InputAction is not currently in an active set this is likely 0
     */
    public long getLastOrigin() {
        return 0;
    }

    public float getAxis1D(boolean delta) {
        return 0;
    }

    /**
     * This special variant of getAxis1D internally handles the isEnabled check and will continue
     * to give an output even after disabled until the user lets go of the input.
     * Cannot provide delta values as it wouldn't make any sense.
     */
    public float getAxis1DUseTracked() {
        return 0;
    }

    public Vector2fc getAxis2D(boolean delta) {
        return new Vector2f();
    }

    /**
     * This special variant of getAxis2D internally handles the isEnabled check and will continue
     * to give an output even after disabled until the user lets go of the input.
     * Cannot provide delta values as it wouldn't make any sense.
     */
    public Vector2fc getAxis2DUseTracked() {
        return new Vector2f();
    }

    public interface KeyListener {
        boolean onPressed(@Nullable ControllerType controllerType);

        boolean onUnpressed(@Nullable ControllerType controllerType);

        int getPriority();
    }
}
