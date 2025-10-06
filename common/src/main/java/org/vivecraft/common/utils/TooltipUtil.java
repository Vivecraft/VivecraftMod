package org.vivecraft.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

public class TooltipUtil {

    /**
     * gets the tooltip for the client setting {@code option}, also adds text when a setting is limited/not changeable
     *
     * @param option VrOptions to get the tooltip for
     * @return multiline String with the tooltip
     */
    public static String getClientConfigTooltip(VRSettings.VrOptions option) {
        Language lang = Language.getInstance();
        String tooltip = "";
        String tooltipString = "vivecraft.options." + option.name() + ".tooltip";
        String tooltipDisabledString = "vivecraft.options." + option.name() + ".disabled.tooltip";
        // check if it has a tooltip
        if (lang.has(tooltipString)) {
            tooltip = lang.getOrDefault(tooltipString);
        }

        if (!option.isChangeable()) {
            tooltip = lang.getOrDefault(tooltipDisabledString) + "\n" + tooltip;
        }

        if (ClientDataHolderVR.getInstance().vrSettings.overrides.hasSetting(option)) {
            VRSettings.ServerOverrides.Setting setting = ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(
                option);
            if (setting.isValueOverridden()) {
                tooltip = lang.getOrDefault("vivecraft.message.overriddenbyserver") + tooltip;
            } else if (setting.isFloat() && (setting.isValueMinOverridden() || setting.isValueMaxOverridden())) {
                tooltip = lang.getOrDefault("vivecraft.message.limitedbyserver")
                    .formatted(setting.getValueMin(), setting.getValueMax()) + tooltip;
            }
        }

        return tooltip;
    }

    /**
     * gets the tooltip/comment for the server setting at the give path {@code serverConfigPath}, if called on the client also adds a line if the setting is currently changeable
     *
     * @param serverConfigPath path to the server config
     * @param addAllTooltip    if true, the '.tooltipall' lang string of the given path or its parent, is appended
     * @return multiline String with the tooltip
     */
    public static String getServerConfigTooltip(String serverConfigPath, boolean addAllTooltip) {
        Language lang = Language.getInstance();
        String tooltip = "";
        String configString = "vivecraft.serverSettings." + serverConfigPath;
        String tooltipString = configString + ".tooltip";
        // check if it has a tooltip
        if (lang.has(tooltipString)) {
            tooltip = lang.getOrDefault(tooltipString);
        } else if (lang.has(configString)) {
            // use config string as fallback to have names in the config file
            tooltip = lang.getOrDefault(configString);
        }
        if (addAllTooltip) {
            // check if there is a tooltip that should be applied to all settings in the group
            String selfAllTooltip = "vivecraft.serverSettings." + serverConfigPath + ".tooltipall";
            if (lang.has(selfAllTooltip)) {
                tooltip += "\n" + lang.getOrDefault(selfAllTooltip);
            } else {
                String parent = serverConfigPath.substring(0, Math.max(serverConfigPath.lastIndexOf('.'), 0));
                String parentAllTooltip = "vivecraft.serverSettings." + parent + ".tooltipall";
                if (lang.has(parentAllTooltip)) {
                    tooltip += "\n" + lang.getOrDefault(parentAllTooltip);
                }
            }
        }
        if (!Xloader.isDedicatedServer()) {
            tooltip += getClientOnlyTooltip();
        }

        return tooltip;
    }

    private static String getClientOnlyTooltip() {
        if (Minecraft.getInstance().level != null && !Minecraft.getInstance().isLocalServer()) {
            return "\n" + Language.getInstance().getOrDefault("vivecraft.messages.serversettingsnotavailablesingle");
        } else {
            return "";
        }
    }
}
