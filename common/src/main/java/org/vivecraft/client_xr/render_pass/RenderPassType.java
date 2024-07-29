package org.vivecraft.client_xr.render_pass;

public enum RenderPassType {
    VANILLA,
    GUI_ONLY,
    WORLD_ONLY;

    /**
     * @return if the active pass is the vanilla pass
     */
    public static boolean isVanilla() {
        return RenderPassManager.renderPassType == VANILLA;
    }

    /**
     * @return if the active pass is the GUI pass
     */
    public static boolean isGuiOnly() {
        return RenderPassManager.renderPassType == GUI_ONLY;
    }

    /**
     * @return if the active pass is a custom world rendering pass
     */
    public static boolean isWorldOnly() {
        return RenderPassManager.renderPassType == WORLD_ONLY;
    }
}
