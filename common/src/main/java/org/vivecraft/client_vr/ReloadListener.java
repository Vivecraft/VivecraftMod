package org.vivecraft.client_vr;

import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.server.config.ServerConfig;

import java.util.List;

/**
 * A ReloadListener, to rebuild the menuworld, when changing resource packs
 */
public class ReloadListener implements ResourceManagerReloadListener {

    // stores the list of resourcePacks that were loaded before a reload, to know if the menuworld should be rebuilt
    private List<String> resourcePacks;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        List<String> newPacks = resourceManager.listPacks().map(PackResources::packId).toList();
        if (this.resourcePacks == null) {
            // first load
            this.resourcePacks = resourceManager.listPacks().map(PackResources::packId).toList();

            if (OptifineHelper.isOptifineLoaded()) {
                // with optifine this texture somehow fails to load, so manually reload it
                try {
                    Minecraft.getInstance().getTextureManager().getTexture(Gui.CROSSHAIR_SPRITE);
                } catch (ReportedException e) {
                    // if there was an error, just reload everything
                    Minecraft.getInstance().reloadResourcePacks();
                }
            }
        } else if (!this.resourcePacks.equals(newPacks) &&
            ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isReady())
        {
            this.resourcePacks = newPacks;
            try {
                ClientDataHolderVR.getInstance().menuWorldRenderer.destroy();
                ClientDataHolderVR.getInstance().menuWorldRenderer.prepare();
            } catch (Exception e) {
                VRSettings.LOGGER.error("Vivecraft: error reloading Menuworld:", e);
            }
        }
        // reinit on reload to update the language
        ServerConfig.init(null);

        VRShaders.reload();
    }
}
