package org.vivecraft.client.gui;

import net.minecraft.network.chat.ClickEvent;

/**
 * Custom ClickEvent to do stuff that vanilla doesn't have an option for
 */
public class VivecraftClickEvent extends ClickEvent {

    private final VivecraftAction vivecraftAction;
    private final Object value;

    public VivecraftClickEvent(VivecraftAction action, Object value) {
        // dummy action, in case our check fails
        super(Action.RUN_COMMAND, "");
        this.vivecraftAction = action;
        this.value = value;
    }


    public VivecraftAction getVivecraftAction() {
        return this.vivecraftAction;
    }

    public Object getVivecraftValue() {
        return this.value;
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
