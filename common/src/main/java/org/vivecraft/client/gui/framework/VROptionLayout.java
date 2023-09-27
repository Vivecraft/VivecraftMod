package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.function.BiFunction;

public class VROptionLayout {
    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;
    private VRSettings.VrOptions _e;
    Position _pos;
    float _row;
    public boolean _enabled;
    String _title = "";
    int _ordinal;
    boolean _defaultb;
    float _defaultf;
    float _maxf;
    float _minf;
    float _incrementf;
    int _defaulti;
    int _maxi;
    int _mini;
    int _incrementi;
    Class<? extends Screen> screen;
    BiFunction<GuiVROption, Vec2, Boolean> customHandler;

    public VROptionLayout(VRSettings.VrOptions e, BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        this._e = e;
        this._pos = pos;
        this._row = row;

        if (title != null) {
            this._title = title;
        }

        this._enabled = enabled;
        this.customHandler = handler;
    }

    public VROptionLayout(VRSettings.VrOptions e, Position pos, float row, boolean enabled, String title) {
        this._e = e;
        this._pos = pos;
        this._row = row;

        if (title != null) {
            this._title = title;
        }

        this._enabled = enabled;
    }

    public VROptionLayout(Class<? extends Screen> screen, BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        this._pos = pos;
        this._row = row;

        if (title != null) {
            this._title = title;
        }

        this._enabled = enabled;
        this.screen = screen;
        this.customHandler = handler;
    }

    public VROptionLayout(Class<? extends Screen> screen, Position pos, float row, boolean enabled, String title) {
        this._pos = pos;
        this._row = row;

        if (title != null) {
            this._title = title;
        }

        this._enabled = enabled;
        this.screen = screen;
    }

    public VROptionLayout(BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        this._pos = pos;
        this._row = row;

        if (title != null) {
            this._title = title;
        }

        this._enabled = enabled;
        this.customHandler = handler;
    }

    public VROptionLayout(int ordinal, Position pos, float row, boolean enabled, String title) {
        this._ordinal = ordinal;
        this._pos = pos;
        this._row = row;
        this._title = title;
        this._enabled = enabled;
    }

    public int getX(int screenWidth) {
        if (this._pos == Position.POS_LEFT) {
            return screenWidth / 2 - 155;
        } else {
            return this._pos == Position.POS_RIGHT ? screenWidth / 2 - 155 + 160 : screenWidth / 2 - 155 + 80;
        }
    }

    public int getY(int screenHeight) {
        return (int) Math.ceil((float) (screenHeight / 6) + 21.0F * this._row - 10.0F);
    }

    public String getButtonText() {
        return this._title.isEmpty() && this._e != null ? ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(this._e) : this._title;
    }

    public VRSettings.VrOptions getOption() {
        return this._e;
    }

    public Class<? extends Screen> getScreen() {
        return this.screen;
    }

    public BiFunction<GuiVROption, Vec2, Boolean> getCustomHandler() {
        return this.customHandler;
    }

    public int getOrdinal() {
        return this._e == null ? this._ordinal : this._e.returnEnumOrdinal();
    }

    public enum Position {
        POS_LEFT,
        POS_CENTER,
        POS_RIGHT
    }
}
