package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.data.ViveItems;

public class InteractiveHotbarModule implements DebugRenderModule, InteractModule {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("vivecraft", "interactive_hotbar");

    private final ClientDataHolderVR dh;
    private final Minecraft mc;

    // indicates the pointed at hotbar slot
    public int hotbar = -1;
    private int previousHotbar = -1;

    public InteractiveHotbarModule(Minecraft mc, ClientDataHolderVR dh) {
        this.dh = dh;
        this.mc = mc;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // hotbar first
        return 0;
    }

    @Override
    public boolean swingsArm() {
        return false;
    }

    @Override
    public void reset(LocalPlayer player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.previousHotbar = this.hotbar;
            this.hotbar = -1;
        }
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        // hotbar selection is only for the main hand
        if (hand != InteractionHand.MAIN_HAND) return false;

        if (this.dh.vrSettings.seated) return false;
        if (this.mc.screen != null || !this.dh.vrSettings.vrTouchHotbar) return false;
        if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HEAD || !GuiHandler.HUD_POPUP) return false;

        // this shouldn't happen, the inventory is supposed to be final
        if (player == null || player.getInventory() == null) return false;
        if (this.dh.climbTracker.isGrabbingLadder() && ViveItems.isClimbingClaws(player.getMainHandItem())) {
            return false;
        }
        if (!this.dh.interactTracker.isActive(player)) return false;
        if (GuiHandler.GUI_RENDER_POS_ROOM == null) return false;

        float scale =
            GuiHandler.GUI_SCALE_APPLIED * (float) this.mc.getWindow().getGuiScale() / GuiHandler.GUI_SCALE_FACTOR_MAX;

        // get room pos
        Vector3f barStart = getHotbarStart(scale);
        Vector3f barEnd = getHotbarEnd(barStart, scale);

        Vector3fc main = this.dh.vrPlayer.vrdata_room_pre.getController(0).getPositionF();

        Vector3fc barLine = barStart.sub(barEnd, new Vector3f());
        Vector3fc handToBar = barStart.sub(main, new Vector3f());

        // check if the hand is close enough
        float dist = handToBar.cross(barLine, new Vector3f()).length() / barLine.length();
        if (dist > 0.06F) return false;

        // check that the controller is to the right of the offhand slot, and how far it's to the right
        float fact = handToBar.dot(barLine) / barLine.lengthSquared();
        if (fact < -1F) return false;

        // get the closest point from the hand to the hotbar
        Vector3f point = barLine.mul(fact, new Vector3f()).sub(handToBar);
        // subtract and store in point
        main.sub(point, point);

        float barSize = barLine.length();
        float ilen = barStart.distance(point);
        if (fact < 0F) {
            ilen *= -1F;
        }
        float pos = ilen / barSize * 9F;

        // actual slot that is selected
        int box = (int) Math.floor(pos);

        if (box > 8) {
            if (this.dh.vrSettings.reverseHands && pos >= 9.5 && pos <= 10.5) {
                box = 9;
            } else {
                return false;
            }
        } else if (box < 0) {
            if (!this.dh.vrSettings.reverseHands && pos <= -0.5 && pos >= -1.5) {
                box = 9;
            } else {
                return false;
            }
        }

        // all that maths for this.
        this.hotbar = box;
        if (this.previousHotbar != this.hotbar) {
            this.dh.vr.triggerHapticPulse(0, 750);
        }

        // active if any slot is selected
        return this.hotbar >= 0;
    }

    private Vector3f getHotbarStart(float scale) {
        // offset from center to the left of the hotbar
        Vector3f offset = GuiHandler.GUI_OFFSET_LOCAL.add(-0.32F * scale, -0.38F * scale, 0,
            new Vector3f());

        // transform local offset to room offset
        GuiHandler.GUI_RENDER_ROTATION_ROOM.transformDirection(offset);

        return offset.add(GuiHandler.GUI_RENDER_POS_ROOM);
    }

    private Vector3f getHotbarEnd(Vector3fc start, float scale) {
        Vector3f endDir = GuiHandler.GUI_RENDER_ROTATION_ROOM.transformDirection(MathUtils.LEFT, new Vector3f())
            .mul(0.64F * scale);
        return endDir.add(start);
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        if (this.hotbar >= 0 && this.hotbar < 9 && player.getInventory().getSelectedSlot() != this.hotbar &&
            hand == InteractionHand.MAIN_HAND)
        {
            player.getInventory().setSelectedSlot(this.hotbar);
            return true;
        } else if (this.hotbar == 9 && hand == InteractionHand.MAIN_HAND) {
            player.connection.send(
                new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ZERO, Direction.DOWN));
            return true;
        }
        return false;
    }

    @Override
    public void renderDebug(boolean isActive) {
        if (this.dh.vrSettings.seated) return;
        if (this.mc.screen != null || !this.dh.vrSettings.vrTouchHotbar) return;
        if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HEAD || !GuiHandler.HUD_POPUP) return;

        float scale =
            GuiHandler.GUI_SCALE_APPLIED * (float) this.mc.getWindow().getGuiScale() / GuiHandler.GUI_SCALE_FACTOR_MAX;

        VRData world = this.dh.vrPlayer.getVRDataWorld();

        // room pos
        Vector3f barStartRoom = getHotbarStart(scale);
        Vector3f barEndRoom = getHotbarEnd(barStartRoom, scale);

        // convert to world pos
        Vec3 barStart = VRPlayer.roomToWorldPos(barStartRoom, world);
        Vec3 barEnd = VRPlayer.roomToWorldPos(barEndRoom, world);

        Vector3f line = MathUtils.subtractToVector3f(barEnd, barStart).div(9F);

        // origin offset since the camera is room relative
        Vec3 slotPos = barStart;

        float size = 0.06F * world.worldScale;

        if (!this.dh.vrSettings.reverseHands) {
            DebugRenderHelper.renderCylinder(slotPos.subtract(line.x * 1.5F, line.y * 1.5F, line.z * 1.5F), line, size,
                this.hotbar == 9 ? MathUtils.GREEN_INT : MathUtils.RED_INT);
        }

        for (int i = 0; i < 9; i++) {
            DebugRenderHelper.renderCylinder(slotPos, line, size,
                this.hotbar == i ? MathUtils.GREEN_INT : MathUtils.RED_INT);
            slotPos = slotPos.add(line.x, line.y, line.z);
        }

        if (this.dh.vrSettings.reverseHands) {
            DebugRenderHelper.renderCylinder(slotPos.add(line.x * 0.5F, line.y * 0.5F, line.z * 0.5F), line, size,
                this.hotbar == 9 ? MathUtils.GREEN_INT : MathUtils.RED_INT);
        }
    }
}
