package org.vivecraft.client_vr.render;

import net.minecraft.network.chat.Component;

public class RenderConfigException extends Exception {
    public Component title;
    public Component error;

    public RenderConfigException(Component title, Component error) {
        this.title = title;
        this.error = error;
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.error.getString();
    }
}
