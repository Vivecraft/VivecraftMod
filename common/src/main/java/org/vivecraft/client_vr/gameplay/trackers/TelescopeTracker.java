package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.RenderPass;

public class TelescopeTracker extends Tracker {
    //public static final ResourceLocation scopeResource = new ResourceLocation("vivecraft:trashbin");
    public static final ModelResourceLocation scopeModel = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("vivecraft", "item/spyglass_in_hand"), "standalone");
    private static final double lensDistMax = 0.05D;
    private static final double lensDistMin = 0.185D;
    private static final double lensDotMax = 0.9D;
    private static final double lensDotMin = 0.75D;

    public TelescopeTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer player) {
        return false;
    }

    public void doProcess(LocalPlayer player) {
    }

    public static boolean isTelescope(ItemStack i) {
        return i != null && (i.getItem() == Items.SPYGLASS || isLegacyTelescope(i) || i.is(ItemTags.VIVECRAFT_TELESCOPE));
    }

    // TODO: old eye of the farseer, remove this eventually
    public static boolean isLegacyTelescope(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else if (!i.has(DataComponents.CUSTOM_NAME)) {
            return false;
        } else if (i.getItem() != Items.ENDER_EYE) {
            return false;
        } else if (!i.has(DataComponents.UNBREAKABLE)) {
            return false;
        } else {
            return i.getHoverName().getContents() instanceof TranslatableContents && ((TranslatableContents) i.getHoverName().getContents()).getKey().equals("vivecraft.item.telescope") || i.getHoverName().getString().equals("Eye of the Farseer");
        }
    }

    private static Vec3 getLensOrigin(int controller) {
        VRData.VRDevicePose vrdata$vrdevicepose = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getController(controller);
        return vrdata$vrdevicepose.getPosition().add(getViewVector(controller).scale(-0.2D).add(vrdata$vrdevicepose.getDirection().scale(0.05F)));
    }

    private static Vec3 getViewVector(int controller) {
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getController(controller).getCustomVector(new Vec3(0.0D, -1.0D, 0.0D));
    }

    public static boolean isViewing(int controller) {
        return viewPercent(controller) > 0.0F;
    }

    public static float viewPercent(int controller) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && ClientDataHolderVR.getInstance().vrSettings.seated) {
            if (isTelescope(p.getUseItem())) {
                return 1;
            } else {
                return 0;
            }
        }

        float out = 0.0F;

        for (int e = 0; e < 2; ++e) {
            float tmp = viewPercent(controller, e);

            if (tmp > out) {
                out = tmp;
            }
        }

        return out;
    }

    private static float viewPercent(int controller, int e) {
        if (e == -1 || ClientDataHolderVR.getInstance().vrPlayer == null) {
            return 0.0F;
        } else {
            VRData.VRDevicePose eye = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.values()[e]);
            double dist = eye.getPosition().subtract(getLensOrigin(controller)).length();
            Vec3 look = eye.getDirection();
            double dot = Math.abs(look.dot(getViewVector(controller)));

            double dfact = 0.0D;
            double distfact = 0.0D;

            if (dot > lensDotMin) {
                if (dot > lensDotMax) {
                    dfact = 1.0D;
                } else {
                    dfact = (dot - lensDotMin) / (lensDotMax - lensDotMin);
                }
            }

            if (dist < lensDistMin) {
                if (dist < lensDistMax) {
                    distfact = 1.0D;
                } else {
                    distfact = 1.0D - (dist - lensDistMax) / (lensDistMin - lensDistMax);
                }
            }

            return (float) Math.min(dfact, distfact);
        }
    }
}
