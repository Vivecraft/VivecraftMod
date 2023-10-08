package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.render.RenderPass;

import static org.joml.Math.abs;
import static org.joml.Math.min;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class TelescopeTracker extends Tracker {
    //public static final ResourceLocation scopeResource = new ResourceLocation("vivecraft:trashbin");
    public static final ModelResourceLocation scopeModel = new ModelResourceLocation("vivecraft", "spyglass_in_hand", "inventory");
    private static final float lensDistMax = 0.05F;
    private static final float lensDistMin = 0.185F;
    private static final float lensDotMax = 0.9F;
    private static final float lensDotMin = 0.75F;

    public static boolean isTelescope(ItemStack i) {
        return i != null && (i.getItem() == Items.SPYGLASS || isLegacyTelescope(i) || i.is(ItemTags.VIVECRAFT_TELESCOPE));
    }

    // TODO: old eye of the farseer, remove this eventually
    public static boolean isLegacyTelescope(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else if (!i.hasCustomHoverName()) {
            return false;
        } else if (i.getItem() != Items.ENDER_EYE) {
            return false;
        } else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return i.getHoverName().getContents() instanceof TranslatableContents && "vivecraft.item.telescope".equals(((TranslatableContents) i.getHoverName().getContents()).getKey()) || "Eye of the Farseer".equals(i.getHoverName().getString());
        }
    }

    private static Vector3f getLensOrigin(int controller, Vector3f dest) {
        VRDevicePose con = dh.vrPlayer.vrdata_room_pre.getController(controller);
        return con.getPosition(dest).add(getViewVector(controller, new Vector3f()).mul(-0.2F).add(con.getDirection(new Vector3f()).mul(0.05F)));
    }

    private static Vector3f getViewVector(int controller, Vector3f dest) {
        return dh.vrPlayer.vrdata_room_pre.getController(controller).getCustomVector(dest.set(0.0F, -1.0F, 0.0F));
    }

    public static boolean isViewing(int controller) {
        return viewPercent(controller) > 0.0F;
    }

    public static float viewPercent(int controller) {
        if (mc.player != null && dh.vrSettings.seated) {
            if (isTelescope(mc.player.getUseItem())) {
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
        if (e == -1) {
            return 0.0F;
        } else {
            VRDevicePose eye = dh.vrPlayer.vrdata_room_pre.getEye(RenderPass.values()[e]);
            float dist = eye.getPosition(new Vector3f()).sub(getLensOrigin(controller, new Vector3f())).length();
            Vector3f look = eye.getDirection(new Vector3f());
            float dot = abs(look.dot(getViewVector(controller, new Vector3f())));

            float dfact = 0.0F;
            float distfact = 0.0F;

            if (dot > lensDotMin) {
                if (dot > lensDotMax) {
                    dfact = 1.0F;
                } else {
                    dfact = (dot - lensDotMin) / (lensDotMax - lensDotMin);
                }
            }

            if (dist < lensDistMin) {
                if (dist < lensDistMax) {
                    distfact = 1.0F;
                } else {
                    distfact = 1.0F - (dist - lensDistMax) / (lensDistMin - lensDistMax);
                }
            }

            return min(dfact, distfact);
        }
    }
}
