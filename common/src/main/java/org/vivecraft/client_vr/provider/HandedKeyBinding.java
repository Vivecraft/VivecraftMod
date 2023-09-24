package org.vivecraft.client_vr.provider;

import net.minecraft.client.KeyMapping;

import java.util.Arrays;

public class HandedKeyBinding extends KeyMapping
{
    private boolean[] pressed = new boolean[ControllerType.values().length];
    private int[] pressTime = new int[ControllerType.values().length];

    public HandedKeyBinding(String string, int i, String string2)
    {
        super(string, i, string2);
    }

    @Override
    public boolean consumeClick()
    {
        return Arrays.stream(ControllerType.values()).map(this::consumeClick).reduce(false, (a, b) -> a || b);
    }

    @Override
    public boolean isDown()
    {
        return Arrays.stream(ControllerType.values()).map(this::isDown).reduce(false, (a, b) -> a || b);
    }

    public boolean consumeClick(ControllerType hand)
    {
        if (this.pressTime[hand.ordinal()] > 0)
        {
            this.pressTime[hand.ordinal()]--;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isDown(ControllerType hand)
    {
        return this.pressed[hand.ordinal()];
    }

    public void pressKey(ControllerType hand)
    {
        this.pressed[hand.ordinal()] = true;
        this.pressTime[hand.ordinal()]++;
    }

    public void unpressKey(ControllerType hand)
    {
        this.pressTime[hand.ordinal()] = 0;
        this.pressed[hand.ordinal()] = false;
    }

    public boolean isPriorityOnController(ControllerType type)
    {
        return true;
    }
}
