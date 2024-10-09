package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class VROptionLayout {
    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;
    private VRSettings.VrOptions _option;
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

    public VROptionLayout(
        VRSettings.VrOptions option, BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row,
        boolean enabled, @Nullable String title)
    {
        this(option, pos, row, enabled, title);
        this.customHandler = handler;
    }

    public VROptionLayout(
        VRSettings.VrOptions option, Position pos, float row, boolean enabled, @Nullable String title)
    {
        this(pos, row, enabled, title);
        this._option = option;
    }

    public VROptionLayout(
        Class<? extends Screen> screen, BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row,
        boolean enabled, @Nullable String title)
    {
        this(screen, pos, row, enabled, title);
        this.customHandler = handler;
    }

    public VROptionLayout(
        Class<? extends Screen> screen, Position pos, float row, boolean enabled, @Nullable String title)
    {
        this(pos, row, enabled, title);
        this.screen = screen;
    }

    public VROptionLayout(
        BiFunction<GuiVROption, Vec2, Boolean> handler, Position pos, float row, boolean enabled,
        @Nullable String title)
    {
        this(pos, row, enabled, title);
        this.customHandler = handler;
    }

    public VROptionLayout(int ordinal, Position pos, float row, boolean enabled, @Nullable String title) {
        this(pos, row, enabled, title);
        this._ordinal = ordinal;
    }

    public VROptionLayout(Position pos, float row, boolean enabled, @Nullable String title) {
        this._pos = pos;
        this._row = row;
        this._enabled = enabled;
        if (title != null) {
            this._title = title;
        }
    }

    public int getX(int screenWidth) {
        return switch (this._pos) {
            case POS_LEFT -> screenWidth / 2 - 155;
            case POS_RIGHT -> screenWidth / 2 - 155 + 160;
            case POS_CENTER -> screenWidth / 2 - 155 + 80;
        };
    }

    public int getY(int screenHeight) {
        return (int) Math.ceil((float) (screenHeight / 6) + 21.0F * this._row - 10.0F);
    }

    public String getButtonText() {
        return this._title.isEmpty() && this._option != null ?
            ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(this._option) : this._title;
    }

    public VRSettings.VrOptions getOption() {
        return this._option;
    }

    public Class<? extends Screen> getScreen() {
        return this.screen;
    }

    public BiFunction<GuiVROption, Vec2, Boolean> getCustomHandler() {
        return this.customHandler;
    }

    public int getOrdinal() {
        return this._option == null ? this._ordinal : this._option.returnEnumOrdinal();
    }

    public enum Position {
        POS_LEFT,
        POS_CENTER,
        POS_RIGHT
    }
}
