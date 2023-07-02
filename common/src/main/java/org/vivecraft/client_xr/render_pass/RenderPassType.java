package org.vivecraft.client_xr.render_pass;

public enum RenderPassType {
    VANILLA,
    GUI_ONLY,
    WORLD_ONLY;

    public static boolean isVanilla() {
        return RenderPassManager.renderPassType == VANILLA;
    }

    public static boolean isGuiOnly() {
        return RenderPassManager.renderPassType == GUI_ONLY;
    }

    public static boolean isWorldOnly() {
        return RenderPassManager.renderPassType == WORLD_ONLY;
    }
}
