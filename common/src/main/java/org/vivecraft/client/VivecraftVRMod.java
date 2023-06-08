package org.vivecraft.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.HandedKeyBinding;

import java.util.*;

public class VivecraftVRMod {

    public static final boolean compiledWithForge = false;

    private static final Minecraft mc = Minecraft.getInstance();

    public static VivecraftVRMod INSTANCE = new VivecraftVRMod();

    Set<KeyMapping> keyBindingSet;

    protected Set<KeyMapping> vanillaBindingSet;

    public final HandedKeyBinding keyClimbeyGrab = new HandedKeyBinding("vivecraft.key.climbeyGrab", -1, "vivecraft.key.category.climbey");
    public final HandedKeyBinding keyClimbeyJump = new HandedKeyBinding("vivecraft.key.climbeyJump", -1, "vivecraft.key.category.climbey");
    public final KeyMapping keyExportWorld = new KeyMapping("vivecraft.key.exportWorld", -1, "key.categories.misc");
    public final KeyMapping keyFreeMoveRotate = new KeyMapping("vivecraft.key.freeMoveRotate", -1, "key.categories.movement");
    public final KeyMapping keyFreeMoveStrafe = new KeyMapping("vivecraft.key.freeMoveStrafe", -1, "key.categories.movement");
    public final KeyMapping keyHotbarNext = new KeyMapping("vivecraft.key.hotbarNext", 266, "key.categories.inventory");
    public final KeyMapping keyHotbarPrev = new KeyMapping("vivecraft.key.hotbarPrev", 267, "key.categories.inventory");
    public final KeyMapping keyHotbarScroll = new KeyMapping("vivecraft.key.hotbarScroll", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarSwipeX = new KeyMapping("vivecraft.key.hotbarSwipeX", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarSwipeY = new KeyMapping("vivecraft.key.hotbarSwipeY", -1, "key.categories.inventory");
    public final KeyMapping keyMenuButton = new KeyMapping("vivecraft.key.ingameMenuButton", -1, "key.categories.ui");
    public final KeyMapping keyMoveThirdPersonCam = new KeyMapping("vivecraft.key.moveThirdPersonCam", -1, "key.categories.misc");
    public final KeyMapping keyQuickHandheldCam = new KeyMapping("vivecraft.key.quickHandheldCam", -1, "key.categories.misc");
    public final KeyMapping keyQuickTorch = new KeyMapping("vivecraft.key.quickTorch", 260, "key.categories.gameplay");
    public final KeyMapping keyRadialMenu = new KeyMapping("vivecraft.key.radialMenu", -1, "key.categories.ui");
    public final KeyMapping keyRotateAxis = new KeyMapping("vivecraft.key.rotateAxis", -1, "key.categories.movement");
    public final KeyMapping keyRotateFree = new KeyMapping("vivecraft.key.rotateFree", 268, "key.categories.movement");
    public final KeyMapping keyRotateLeft = new KeyMapping("vivecraft.key.rotateLeft", 263, "key.categories.movement");
    public final KeyMapping keyRotateRight = new KeyMapping("vivecraft.key.rotateRight", 262, "key.categories.movement");
    public final KeyMapping keySwapMirrorView = new KeyMapping("vivecraft.key.swapMirrorView", -1, "key.categories.misc");
    public final KeyMapping keyTeleport = new KeyMapping("vivecraft.key.teleport", -1, "key.categories.movement");
    public final KeyMapping keyTeleportFallback = new KeyMapping("vivecraft.key.teleportFallback", -1, "key.categories.movement");
    public final KeyMapping keyToggleHandheldCam = new KeyMapping("vivecraft.key.toggleHandheldCam", -1, "key.categories.misc");
    public final KeyMapping keyToggleKeyboard = new KeyMapping("vivecraft.key.toggleKeyboard", -1, "key.categories.ui");
    public final KeyMapping keyToggleMovement = new KeyMapping("vivecraft.key.toggleMovement", -1, "key.categories.movement");
    public final KeyMapping keyTogglePlayerList = new KeyMapping("vivecraft.key.togglePlayerList", -1, "key.categories.multiplayer");
    public final HandedKeyBinding keyTrackpadTouch = new HandedKeyBinding("vivecraft.key.trackpadTouch", -1, "key.categories.misc");
    public final HandedKeyBinding keyVRInteract = new HandedKeyBinding("vivecraft.key.vrInteract", -1, "key.categories.gameplay");
    public final KeyMapping keyWalkabout = new KeyMapping("vivecraft.key.walkabout", 269, "key.categories.movement");

    public Set<KeyMapping> getKeyBindings()
    {
        if (this.keyBindingSet == null)
        {
            this.keyBindingSet = new LinkedHashSet<>();
            this.keyBindingSet.add(this.keyRotateLeft);
            this.keyBindingSet.add(this.keyRotateRight);
            this.keyBindingSet.add(this.keyRotateAxis);
            this.keyBindingSet.add(this.keyRotateFree);
            this.keyBindingSet.add(this.keyWalkabout);
            this.keyBindingSet.add(this.keyTeleport);
            this.keyBindingSet.add(this.keyTeleportFallback);
            this.keyBindingSet.add(this.keyFreeMoveRotate);
            this.keyBindingSet.add(this.keyFreeMoveStrafe);
            this.keyBindingSet.add(this.keyToggleMovement);
            this.keyBindingSet.add(this.keyQuickTorch);
            this.keyBindingSet.add(this.keyHotbarNext);
            this.keyBindingSet.add(this.keyHotbarPrev);
            this.keyBindingSet.add(this.keyHotbarScroll);
            this.keyBindingSet.add(this.keyHotbarSwipeX);
            this.keyBindingSet.add(this.keyHotbarSwipeY);
            this.keyBindingSet.add(this.keyMenuButton);
            this.keyBindingSet.add(this.keyRadialMenu);
            this.keyBindingSet.add(this.keyVRInteract);
            this.keyBindingSet.add(this.keySwapMirrorView);
            this.keyBindingSet.add(this.keyExportWorld);
            this.keyBindingSet.add(this.keyToggleKeyboard);
            this.keyBindingSet.add(this.keyMoveThirdPersonCam);
            this.keyBindingSet.add(this.keyTogglePlayerList);
            this.keyBindingSet.add(this.keyToggleHandheldCam);
            this.keyBindingSet.add(this.keyQuickHandheldCam);
            this.keyBindingSet.add(this.keyTrackpadTouch);
            this.keyBindingSet.add(GuiHandler.keyLeftClick);
            this.keyBindingSet.add(GuiHandler.keyRightClick);
            this.keyBindingSet.add(GuiHandler.keyMiddleClick);
            this.keyBindingSet.add(GuiHandler.keyShift);
            this.keyBindingSet.add(GuiHandler.keyCtrl);
            this.keyBindingSet.add(GuiHandler.keyAlt);
            this.keyBindingSet.add(GuiHandler.keyScrollUp);
            this.keyBindingSet.add(GuiHandler.keyScrollDown);
            this.keyBindingSet.add(GuiHandler.keyScrollAxis);
            this.keyBindingSet.add(GuiHandler.keyKeyboardClick);
            this.keyBindingSet.add(GuiHandler.keyKeyboardShift);
            this.keyBindingSet.add(this.keyClimbeyGrab);
            this.keyBindingSet.add(this.keyClimbeyJump);
        }

        return this.keyBindingSet;
    }

    public KeyMapping[] initializeBindings(KeyMapping[] keyBindings)
    {
        for (KeyMapping keymapping : this.getKeyBindings())
        {
            keyBindings = ArrayUtils.add(keyBindings, keymapping);
        }

        this.setVanillaBindings(keyBindings);
        Map<String, Integer> map = KeyMapping.CATEGORY_SORT_ORDER;
        map.put("vivecraft.key.category.gui", 8);
        map.put("vivecraft.key.category.climbey", 9);
        map.put("vivecraft.key.category.keyboard", 10);
        return keyBindings;
    }

    public void setVanillaBindings(KeyMapping[] bindings)
    {
        this.vanillaBindingSet = new HashSet<>(Arrays.asList(bindings));
    }

    public boolean isSafeBinding(KeyMapping kb)
    {
        return this.getKeyBindings().contains(kb) || kb == this.mc.options.keyChat || kb == this.mc.options.keyInventory;
    }

    public boolean isModBinding(KeyMapping kb)
    {
        return !this.vanillaBindingSet.contains(kb);
    }

}
