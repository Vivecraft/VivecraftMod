package org.vivecraft.client_vr.provider.openvr_lwjgl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HandedKeyBinding;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.common.utils.math.Vector2;
import org.vivecraft.common.utils.math.Vector3;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public class VRInputAction
{
    public final KeyMapping keyBinding;
    public final String name;
    public final String requirement;
    public final String type;
    public final VRInputActionSet actionSet;
    private int priority = 0;
    private boolean[] enabled = new boolean[ControllerType.values().length];
    private List<KeyListener> listeners = new ArrayList<>();
    private ControllerType currentHand = ControllerType.RIGHT;
    private boolean currentlyInUse;
    public long handle;
    private boolean[] pressed = new boolean[ControllerType.values().length];
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
        if (this.type.equals("boolean"))
        {
            return this.digitalData().state;
        }
        else
        {
            Vector3 vector3 = this.getAxis3D(false);
            return Math.abs(vector3.getX()) > 0.5F || Math.abs(vector3.getY()) > 0.5F || Math.abs(vector3.getZ()) > 0.5F;
        }
    }

    public boolean isButtonChanged()
    {
        if (this.type.equals("boolean"))
        {
            return this.digitalData().isChanged;
        }
        else
        {
            Vector3 vector3 = this.getAxis3D(false);
            Vector3 vector31 = this.getAxis3D(true);
            return Math.abs(vector3.getX() - vector31.getX()) > 0.5F != Math.abs(vector3.getX()) > 0.5F || Math.abs(vector3.getY() - vector31.getY()) > 0.5F != Math.abs(vector3.getY()) > 0.5F || Math.abs(vector3.getZ() - vector31.getZ()) > 0.5F != Math.abs(vector3.getZ()) > 0.5F;
        }
    }

    public float getAxis1D(boolean delta)
    {
        String s = this.type;

        switch (s)
        {
            case "boolean":
                return this.digitalToAnalog(delta);

            case "vector1":
            case "vector2":
            case "vector3":
                return delta ? this.analogData().deltaX : this.analogData().x;

            default:
                return 0.0F;
        }
    }

    public Vector2 getAxis2D(boolean delta)
    {
        String s = this.type;

        switch (s)
        {
            case "boolean":
                return new Vector2(this.digitalToAnalog(delta), 0.0F);

            case "vector1":
                return delta ? new Vector2(this.analogData().deltaX, 0.0F) : new Vector2(this.analogData().x, 0.0F);

            case "vector2":
            case "vector3":
                return delta ? new Vector2(this.analogData().deltaX, this.analogData().deltaY) : new Vector2(this.analogData().x, this.analogData().y);

            default:
                return new Vector2();
        }
    }

    public Vector3 getAxis3D(boolean delta)
    {
        String s = this.type;

        switch (s)
        {
            case "boolean":
                return new Vector3(this.digitalToAnalog(delta), 0.0F, 0.0F);

            case "vector1":
                return delta ? new Vector3(this.analogData().deltaX, 0.0F, 0.0F) : new Vector3(this.analogData().x, 0.0F, 0.0F);

            case "vector2":
                return delta ? new Vector3(this.analogData().deltaX, this.analogData().deltaY, 0.0F) : new Vector3(this.analogData().x, this.analogData().y, 0.0F);

            case "vector3":
                return delta ? new Vector3(this.analogData().deltaX, this.analogData().deltaY, this.analogData().deltaZ) : new Vector3(this.analogData().x, this.analogData().y, this.analogData().z);

            default:
                return new Vector3();
        }
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

    public Vector2 getAxis2DUseTracked()
    {
        if (!this.currentlyInUse && !this.isEnabled())
        {
            return new Vector2();
        }
        else
        {
            Vector2 vector2 = this.getAxis2D(false);
            this.currentlyInUse = vector2.getX() != 0.0F || vector2.getY() != 0.0F;
            return vector2;
        }
    }

    Vector3 getAxis3DUseTracked()
    {
        if (!this.currentlyInUse && !this.isEnabled())
        {
            return new Vector3();
        }
        else
        {
            Vector3 vector3 = this.getAxis3D(false);
            this.currentlyInUse = vector3.getX() != 0.0F || vector3.getY() != 0.0F || vector3.getZ() != 0.0F;
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
        String s = this.type;

        switch (s)
        {
            case "boolean":
                return this.digitalData().activeOrigin;

            case "vector1":
            case "vector2":
            case "vector3":
                return this.analogData().activeOrigin;

            default:
                return 0L;
        }
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
        else if (MCOpenVR.get() == null)
        {
            return false;
        }
        else
        {
            long i = this.getLastOrigin();
            ControllerType controllertype = MCOpenVR.get().getOriginControllerType(i);

            if (controllertype == null && this.isHanded())
            {
                return false;
            }
            else
            {
                for (VRInputAction vrinputaction : MCOpenVR.get().getInputActions())
                {
                    if (vrinputaction != this && vrinputaction.isEnabledRaw(controllertype) && vrinputaction.isActive() && vrinputaction.getPriority() > this.getPriority() && MCVR.get().getOrigins(vrinputaction).contains(i))
                    {
                        if (vrinputaction.isHanded())
                        {
                            return !((HandedKeyBinding)vrinputaction.keyBinding).isPriorityOnController(controllertype);
                        }

                        return false;
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
        String s = this.type;

        switch (s)
        {
            case "boolean":
                return this.digitalData().isActive;

            case "vector1":
            case "vector2":
            case "vector3":
                return this.analogData().isActive;

            default:
                return false;
        }
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
            this.unpressBindingImmediately((ControllerType)null);
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

            if (this.notifyListeners(true, (ControllerType)null))
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

            if (this.notifyListeners(false, (ControllerType)null))
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
            kb.isDown = pressed;
            kb.clickCount += 1;
        }
    }

    private void pressKey()
    {
        InputConstants.Key inputconstants$key = this.keyBinding.key;

        if (inputconstants$key.getValue() != -1 && !VivecraftVRMod.INSTANCE.isSafeBinding(this.keyBinding)) //&& (!Reflector.ForgeKeyBinding_getKeyModifier.exists() || Reflector.call(this.keyBinding, Reflector.ForgeKeyBinding_getKeyModifier) == Reflector.getFieldValue(Reflector.KeyModifier_NONE)))
        {
            if (inputconstants$key.getType() == InputConstants.Type.KEYSYM)
            {
                InputSimulator.pressKey(inputconstants$key.getValue());
                return;
            }

            if (inputconstants$key.getType() == InputConstants.Type.MOUSE)
            {
                InputSimulator.pressMouse(inputconstants$key.getValue());
                return;
            }
        }

        setKeyBindState(this.keyBinding, true);
    }

    public void unpressKey()
    {
        InputConstants.Key inputconstants$key = this.keyBinding.key;

        if (inputconstants$key.getValue() != -1 && !VivecraftVRMod.INSTANCE.isSafeBinding(this.keyBinding)) // && (!Reflector.ForgeKeyBinding_getKeyModifier.exists() || Reflector.call(this.keyBinding, Reflector.ForgeKeyBinding_getKeyModifier) == Reflector.getFieldValue(Reflector.KeyModifier_NONE)))
        {
            if (inputconstants$key.getType() == InputConstants.Type.KEYSYM)
            {
                InputSimulator.releaseKey(inputconstants$key.getValue());
                return;
            }

            if (inputconstants$key.getType() == InputConstants.Type.MOUSE)
            {
                InputSimulator.releaseMouse(inputconstants$key.getValue());
                return;
            }
        }

        this.keyBinding.release();
    }

    public class AnalogData
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

    public class DigitalData
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
