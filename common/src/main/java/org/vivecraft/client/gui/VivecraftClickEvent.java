package org.vivecraft.client.gui;

import net.minecraft.network.chat.ClickEvent;

public class VivecraftClickEvent extends ClickEvent {

    private final VivecraftAction vivecraftAction;
    private final Object value;

    public VivecraftClickEvent(VivecraftAction action, Object value) {
        // dummy action, in case our check fails
        super(Action.RUN_COMMAND, "");
        vivecraftAction = action;
        this.value = value;
    }


    public VivecraftAction getVivecraftAction() {
        return this.vivecraftAction;
    }

    public Object getVivecraftValue() {
        return value;
    }

    public enum VivecraftAction {
        OPEN_SCREEN("open_screen");

        private final String name;

        VivecraftAction(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }
    }


}
