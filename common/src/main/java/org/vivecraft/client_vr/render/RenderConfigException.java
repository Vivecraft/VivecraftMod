package org.vivecraft.client_vr.render;

import net.minecraft.network.chat.Component;

public class RenderConfigException extends Exception {
    public String title;
    public Component error;

    public RenderConfigException(String title, Component error) {
        this.title = title;
        this.error = error;
    }

    public String toString() {
        return this.error.getString();
    }
}
