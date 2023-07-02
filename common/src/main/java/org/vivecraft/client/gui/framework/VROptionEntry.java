package org.vivecraft.client.gui.framework;

import java.util.function.BiFunction;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.settings.VRSettings;

public class VROptionEntry
{
    public final VRSettings.VrOptions option;
    public final String title;
    public final BiFunction<GuiVROption, Vec2, Boolean> customHandler;
    public final boolean center;

    public VROptionEntry(String label, BiFunction<GuiVROption, Vec2, Boolean> customHandler, boolean center)
    {
        this.option = null;
        this.title = label;
        this.customHandler = customHandler;
        this.center = center;
    }

    public VROptionEntry(String label, BiFunction<GuiVROption, Vec2, Boolean> customHandler)
    {
        this.option = null;
        this.title = label;
        this.customHandler = customHandler;
        this.center = false;
    }

    public VROptionEntry(VRSettings.VrOptions option, BiFunction<GuiVROption, Vec2, Boolean> customHandler, boolean center)
    {
        this.option = option;
        this.title = null;
        this.customHandler = customHandler;
        this.center = center;
    }

    public VROptionEntry(VRSettings.VrOptions option, BiFunction<GuiVROption, Vec2, Boolean> customHandler)
    {
        this.option = option;
        this.title = null;
        this.customHandler = customHandler;
        this.center = false;
    }

    public VROptionEntry(VRSettings.VrOptions option, boolean center)
    {
        this.option = option;
        this.title = null;
        this.customHandler = null;
        this.center = center;
    }

    public VROptionEntry(VRSettings.VrOptions option)
    {
        this.option = option;
        this.title = null;
        this.customHandler = null;
        this.center = false;
    }
}
