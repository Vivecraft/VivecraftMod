package org.vivecraft.client_vr.provider.openvr_lwjgl;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HandedKeyBinding;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.client.KeyMapping;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;
import static org.lwjgl.openvr.VR.k_ulInvalidInputValueHandle;

import static com.mojang.blaze3d.platform.InputConstants.Key;
import static com.mojang.blaze3d.platform.InputConstants.Type;

public class VRInputAction
{
    public final KeyMapping keyBinding;
    public final String name;
    public final String requirement;
    public final String type;
    public final VRInputActionSet actionSet;
    private int priority = 0;
    private final boolean[] enabled = new boolean[ControllerType.values().length];
    private final List<KeyListener> listeners = new ArrayList<>();
    private ControllerType currentHand = ControllerType.RIGHT;
    private boolean currentlyInUse;
    public long handle;
    private final boolean[] pressed = new boolean[ControllerType.values().length];
    protected int[] unpressInTicks = new int[ControllerType.values().length];
    public DigitalData[] digitalData = new DigitalData[ControllerType.values().length];
    public AnalogData[] analogData = new AnalogData[ControllerType.values().length];

    public VRInputAction(KeyMapping keyBinding, String requirement, String type, VRInputActionSet actionSetOverride)
    {
        this.keyBinding = keyBinding;
        this.requirement = requirement;
        this.type = type;
        this.actionSet = actionSetOverride != null ? actionSetOverride : VRInputActionSet.fromKeyBinding(keyBinding);
        this.name = this.actionSet.name + "/in/" + keyBinding.getName().replace('/', '_');

        for (int i = 0; i < ControllerType.values().length; ++i)
        {
            this.enabled[i] = true;
            this.analogData[i] = new AnalogData();
            this.digitalData[i] = new DigitalData();
        }
    }

    public boolean isButtonPressed()
    {
        if ("boolean".equals(this.type))
        {
            return this.digitalData().state;
        }
        else
        {
            Vector3f vector3 = this.getAxis3D(false);
            return abs(vector3.x()) > 0.5F || abs(vector3.y()) > 0.5F || abs(vector3.z()) > 0.5F;
        }
    }

    public boolean isButtonChanged()
    {
        if ("boolean".equals(this.type))
        {
            return this.digitalData().isChanged;
        }
        else
        {
            Vector3f vector3 = this.getAxis3D(false);
            Vector3f vector31 = this.getAxis3D(true);
            return abs(vector3.x() - vector31.x()) > 0.5F != abs(vector3.x()) > 0.5F || abs(vector3.y() - vector31.y()) > 0.5F != abs(vector3.y()) > 0.5F || abs(vector3.z() - vector31.z()) > 0.5F != abs(vector3.z()) > 0.5F;
        }
    }

    public float getAxis1D(boolean delta)
    {

        return switch (this.type) {
            case "boolean" -> this.digitalToAnalog(delta);
            case "vector1", "vector2", "vector3" -> delta ? this.analogData().deltaX : this.analogData().x;
            default -> 0.0F;
        };
    }

    public Vector2f getAxis2D(boolean delta)
    {

        return switch (this.type) {
            case "boolean" -> new Vector2f(this.digitalToAnalog(delta), 0.0F);
            case "vector1" ->
                delta ? new Vector2f(this.analogData().deltaX, 0.0F) : new Vector2f(this.analogData().x, 0.0F);
            case "vector2", "vector3" ->
                delta ? new Vector2f(this.analogData().deltaX, this.analogData().deltaY) : new Vector2f(this.analogData().x, this.analogData().y);
            default -> new Vector2f();
        };
    }

    public Vector3f getAxis3D(boolean delta)
    {

        return switch (this.type) {
            case "boolean" -> new Vector3f(this.digitalToAnalog(delta), 0.0F, 0.0F);
            case "vector1" ->
                delta ? new Vector3f(this.analogData().deltaX, 0.0F, 0.0F) : new Vector3f(this.analogData().x, 0.0F, 0.0F);
            case "vector2" ->
                delta ? new Vector3f(this.analogData().deltaX, this.analogData().deltaY, 0.0F) : new Vector3f(this.analogData().x, this.analogData().y, 0.0F);
            case "vector3" ->
                delta ? new Vector3f(this.analogData().deltaX, this.analogData().deltaY, this.analogData().deltaZ) : new Vector3f(this.analogData().x, this.analogData().y, this.analogData().z);
            default -> new Vector3f();
        };
    }

    public float getAxis1DUseTracked()
    {
        if (!this.currentlyInUse && !this.isEnabled())
        {
            return 0.0F;
        }
        else
        {
            float f = this.getAxis1D(false);
            this.currentlyInUse = f != 0.0F;
            return f;
        }
    }

    public Vector2f getAxis2DUseTracked()
    {
        if (!this.currentlyInUse && !this.isEnabled())
        {
            return new Vector2f();
        }
        else
        {
            Vector2f vector2 = this.getAxis2D(false);
            this.currentlyInUse = vector2.x() != 0.0F || vector2.y() != 0.0F;
            return vector2;
        }
    }

    Vector3f getAxis3DUseTracked()
    {
        if (!this.currentlyInUse && !this.isEnabled())
        {
            return new Vector3f();
        }
        else
        {
            Vector3f vector3 = this.getAxis3D(false);
            this.currentlyInUse = vector3.x() != 0.0F || vector3.y() != 0.0F || vector3.z() != 0.0F;
            return vector3;
        }
    }

    private float digitalToAnalog(boolean delta)
    {
        if (delta)
        {
            if (this.digitalData().isChanged)
            {
                return this.digitalData().state ? 1.0F : -1.0F;
            }
            else
            {
                return 0.0F;
            }
        }
        else
        {
            return this.digitalData().state ? 1.0F : 0.0F;
        }
    }

    public long getLastOrigin()
    {

        return switch (this.type) {
            case "boolean" -> this.digitalData().activeOrigin;
            case "vector1", "vector2", "vector3" -> this.analogData().activeOrigin;
            default -> k_ulInvalidInputValueHandle;
        };
    }

    public ControllerType getCurrentHand()
    {
        return this.currentHand;
    }

    public void setCurrentHand(ControllerType currentHand)
    {
        this.currentHand = currentHand;
    }

    private DigitalData digitalData()
    {
        return this.isHanded() ? this.digitalData[this.currentHand.ordinal()] : this.digitalData[0];
    }

    private AnalogData analogData()
    {
        return this.isHanded() ? this.analogData[this.currentHand.ordinal()] : this.analogData[0];
    }

    public void setHandle(long handle)
    {
        if (this.handle != 0L)
        {
            throw new IllegalStateException("Handle already assigned!");
        }
        else
        {
            this.handle = handle;
        }
    }

    public int getPriority()
    {
        return this.priority;
    }

    public VRInputAction setPriority(int priority)
    {
        this.priority = priority;
        return this;
    }

    public boolean isEnabled()
    {
        if (!this.isEnabledRaw(this.currentHand))
        {
            return false;
        }
        else if (dh.vr == null)
        {
            return false;
        }
        else
        {
            long i = this.getLastOrigin();
            ControllerType controllertype = dh.vr.getOriginControllerType(i);

            if (controllertype == null && this.isHanded())
            {
                return false;
            }
            else
            {
                for (VRInputAction vrinputaction : dh.vr.getInputActions())
                {
                    if (vrinputaction != this && vrinputaction.isEnabledRaw(controllertype) && vrinputaction.isActive() && vrinputaction.getPriority() > this.getPriority() && dh.vr.getOrigins(vrinputaction).contains(i))
                    {
                        return vrinputaction.isHanded() && !((HandedKeyBinding) vrinputaction.keyBinding).isPriorityOnController(controllertype);

                    }
                }

                return true;
            }
        }
    }

    public boolean isEnabledRaw(ControllerType hand)
    {
        if (!this.isHanded())
        {
            return this.enabled[0];
        }
        else
        {
            return hand != null && this.enabled[hand.ordinal()];
        }
    }

    public boolean isEnabledRaw()
    {
        return Arrays.stream(ControllerType.values()).anyMatch(this::isEnabledRaw);
    }

    public VRInputAction setEnabled(ControllerType hand, boolean enabled)
    {
        if (!this.isHanded())
        {
            throw new IllegalStateException("Not a handed key binding!");
        }
        else
        {
            this.enabled[hand.ordinal()] = enabled;
            return this;
        }
    }

    public VRInputAction setEnabled(boolean enabled)
    {
        if (this.isHanded())
        {
            for (ControllerType controllertype : ControllerType.values())
            {
                this.enabled[controllertype.ordinal()] = enabled;
            }
        }
        else
        {
            this.enabled[0] = enabled;
        }

        return this;
    }

    public boolean isActive()
    {

        return switch (this.type) {
            case "boolean" -> this.digitalData().isActive;
            case "vector1", "vector2", "vector3" -> this.analogData().isActive;
            default -> false;
        };
    }

    public boolean isHanded()
    {
        return this.keyBinding instanceof HandedKeyBinding;
    }

    public void registerListener(KeyListener listener)
    {
        this.listeners.add(listener);
        this.listeners.sort(Comparator.comparingInt(KeyListener::getPriority).reversed());
    }

    public void unregisterListener(KeyListener listener)
    {
        this.listeners.remove(listener);
    }

    public boolean notifyListeners(boolean pressed, ControllerType hand)
    {
        for (KeyListener vrinputaction$keylistener : this.listeners)
        {
            if (pressed)
            {
                if (vrinputaction$keylistener.onPressed(hand))
                {
                    return true;
                }
            }
            else if (vrinputaction$keylistener.onUnpressed(hand))
            {
                return true;
            }
        }

        return false;
    }

    public void tick()
    {
        if (this.isHanded())
        {
            for (int i = 0; i < ControllerType.values().length; ++i)
            {
                if (this.unpressInTicks[i] > 0 && --this.unpressInTicks[i] == 0)
                {
                    this.unpressBindingImmediately(ControllerType.values()[i]);
                }
            }
        }
        else if (this.unpressInTicks[0] > 0 && --this.unpressInTicks[0] == 0)
        {
            this.unpressBindingImmediately(null);
        }
    }

    private void pressBinding(ControllerType hand)
    {
        if (this.isHanded())
        {
            if (hand == null || this.pressed[hand.ordinal()])
            {
                return;
            }

            this.pressed[hand.ordinal()] = true;

            if (this.notifyListeners(true, hand))
            {
                return;
            }

            ((HandedKeyBinding)this.keyBinding).pressKey(hand);
        }
        else
        {
            if (this.pressed[0])
            {
                return;
            }

            this.pressed[0] = true;

            if (this.notifyListeners(true, null))
            {
                return;
            }

            this.pressKey();
        }
    }

    public void pressBinding()
    {
        this.pressBinding(this.currentHand);
    }

    public void unpressBinding(int unpressInTicks, ControllerType hand)
    {
        if (this.isHanded())
        {
            if (hand == null || !this.pressed[hand.ordinal()])
            {
                return;
            }

            this.unpressInTicks[hand.ordinal()] = unpressInTicks;
        }
        else
        {
            if (!this.pressed[0])
            {
                return;
            }

            this.unpressInTicks[0] = unpressInTicks;
        }
    }

    public void unpressBinding(int unpressInTicks)
    {
        this.unpressBinding(unpressInTicks, this.currentHand);
    }

    public void unpressBinding()
    {
        this.unpressBinding(1);
    }

    public void unpressBindingImmediately(ControllerType hand)
    {
        if (this.isHanded())
        {
            if (hand == null || !this.pressed[hand.ordinal()])
            {
                return;
            }

            this.pressed[hand.ordinal()] = false;

            if (this.notifyListeners(false, hand))
            {
                return;
            }

            ((HandedKeyBinding)this.keyBinding).unpressKey(hand);
        }
        else
        {
            if (!this.pressed[0])
            {
                return;
            }

            this.pressed[0] = false;

            if (this.notifyListeners(false, null))
            {
                return;
            }

            this.unpressKey();
        }
    }

    public static void setKeyBindState(KeyMapping kb, boolean pressed)
    {
        if (kb != null)
        {
            kb.setDown(pressed);
            kb.clickCount += 1;
        }
    }

    private void pressKey()
    {
        Key inputconstants$key = this.keyBinding.key;

        if (inputconstants$key.getValue() != -1 && !VivecraftVRMod.isSafeBinding(this.keyBinding)) //&& (!Reflector.ForgeKeyBinding_getKeyModifier.exists() || Reflector.call(this.keyBinding, Reflector.ForgeKeyBinding_getKeyModifier) == Reflector.getFieldValue(Reflector.KeyModifier_NONE)))
        {
            if (inputconstants$key.getType() == Type.KEYSYM)
            {
                InputSimulator.pressKey(inputconstants$key.getValue());
                return;
            }

            if (inputconstants$key.getType() == Type.MOUSE)
            {
                InputSimulator.pressMouse(inputconstants$key.getValue());
                return;
            }
        }

        setKeyBindState(this.keyBinding, true);
    }

    public void unpressKey()
    {
        Key inputconstants$key = this.keyBinding.key;

        if (inputconstants$key.getValue() != -1 && !VivecraftVRMod.isSafeBinding(this.keyBinding)) // && (!Reflector.ForgeKeyBinding_getKeyModifier.exists() || Reflector.call(this.keyBinding, Reflector.ForgeKeyBinding_getKeyModifier) == Reflector.getFieldValue(Reflector.KeyModifier_NONE)))
        {
            if (inputconstants$key.getType() == Type.KEYSYM)
            {
                InputSimulator.releaseKey(inputconstants$key.getValue());
                return;
            }

            if (inputconstants$key.getType() == Type.MOUSE)
            {
                InputSimulator.releaseMouse(inputconstants$key.getValue());
                return;
            }
        }

        this.keyBinding.release();
    }

    public static class AnalogData
    {
        public float x;
        public float y;
        public float z;
        public float deltaX;
        public float deltaY;
        public float deltaZ;
        public boolean isChanged;
        public boolean isActive;
        public long activeOrigin;
    }

    public static class DigitalData
    {
        public boolean state;
        public boolean isChanged;
        public boolean isActive;
        public long activeOrigin;
    }

    public interface KeyListener
    {
        boolean onPressed(@Nullable ControllerType var1);

        boolean onUnpressed(@Nullable ControllerType var1);

        int getPriority();
    }
}
