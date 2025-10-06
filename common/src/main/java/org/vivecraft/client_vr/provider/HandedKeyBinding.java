package org.vivecraft.client_vr.provider;

import net.minecraft.client.KeyMapping;

import java.util.Arrays;

/**
 * extension of a regular KeyMapping, that keeps track of which hand pressed it
 */
public class HandedKeyBinding extends KeyMapping {
    private final boolean[] pressed = new boolean[ControllerType.values().length];
    private final int[] pressTime = new int[ControllerType.values().length];

    public HandedKeyBinding(String name, int keyCode, String category) {
        super(name, keyCode, category);
    }

    @Override
    public boolean consumeClick() {
        return Arrays.stream(ControllerType.values()).map(this::consumeClick).reduce(false, (a, b) -> a || b);
    }

    @Override
    public boolean isDown() {
        return Arrays.stream(ControllerType.values()).map(this::isDown).reduce(false, (a, b) -> a || b);
    }

    public boolean consumeClick(ControllerType hand) {
        if (this.pressTime[hand.ordinal()] > 0) {
            this.pressTime[hand.ordinal()]--;
            return true;
        } else {
            return false;
        }
    }

    public boolean isDown(ControllerType hand) {
        return this.pressed[hand.ordinal()];
    }

    public void pressKey(ControllerType hand) {
        this.pressed[hand.ordinal()] = true;
        this.pressTime[hand.ordinal()]++;
    }

    public void unpressKey(ControllerType hand) {
        this.pressTime[hand.ordinal()] = 0;
        this.pressed[hand.ordinal()] = false;
    }

    /**
     * if the binding has priority, by default always true, some override it to have it based on a condition
     *
     * @param type controller to check
     */
    public boolean isPriorityOnController(ControllerType type) {
        return true;
    }
}
