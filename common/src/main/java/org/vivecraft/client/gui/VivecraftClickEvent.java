package org.vivecraft.client.gui;

import net.minecraft.network.chat.ClickEvent;

import java.util.function.Supplier;

/**
 * Custom ClickEvent to do stuff that vanilla doesn't have an option for
 */
public class VivecraftClickEvent implements ClickEvent {

    private final VivecraftAction vivecraftAction;
    private final Supplier<?> value;

    public VivecraftClickEvent(VivecraftAction action, Supplier<?> value) {
        // dummy action, in case our check fails
        this.vivecraftAction = action;
        this.value = value;
    }

    public VivecraftAction getVivecraftAction() {
        return this.vivecraftAction;
    }

    public Supplier<?> getVivecraftValue() {
        return this.value;
    }

    @Override
    public Action action() {
        return Action.RUN_COMMAND;
    }

    public enum VivecraftAction {
        OPEN_SCREEN("open_screen");

        private final String name;

        VivecraftAction(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
