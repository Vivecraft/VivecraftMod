package org.vivecraft.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.HandedKeyBinding;

import java.util.*;

public class VivecraftVRMod {

    private static final Minecraft mc = Minecraft.getInstance();

    public static VivecraftVRMod INSTANCE = new VivecraftVRMod();

    Set<KeyMapping> allKeyBindingSet;

    // key binds that are settable by the user
    Set<KeyMapping> userKeyBindingSet;

    // key binds that are needed internally, but are not required to be set by the user
    Set<KeyMapping> hiddenKeyBindingSet;

    protected Set<KeyMapping> vanillaBindingSet;

    public final HandedKeyBinding keyClimbeyGrab = new HandedKeyBinding("vivecraft.key.climbeyGrab", -1, "vivecraft.key.category.climbey");
    public final HandedKeyBinding keyClimbeyJump = new HandedKeyBinding("vivecraft.key.climbeyJump", -1, "vivecraft.key.category.climbey");
    public final KeyMapping keyExportWorld = new KeyMapping("vivecraft.key.exportWorld", -1, "key.categories.misc");
    public final KeyMapping keyFreeMoveRotate = new KeyMapping("vivecraft.key.freeMoveRotate", -1, "key.categories.movement");
    public final KeyMapping keyFreeMoveStrafe = new KeyMapping("vivecraft.key.freeMoveStrafe", -1, "key.categories.movement");
    public final KeyMapping keyHotbarNext = new KeyMapping("vivecraft.key.hotbarNext", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarPrev = new KeyMapping("vivecraft.key.hotbarPrev", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarScroll = new KeyMapping("vivecraft.key.hotbarScroll", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarSwipeX = new KeyMapping("vivecraft.key.hotbarSwipeX", -1, "key.categories.inventory");
    public final KeyMapping keyHotbarSwipeY = new KeyMapping("vivecraft.key.hotbarSwipeY", -1, "key.categories.inventory");
    public final KeyMapping keyMenuButton = new KeyMapping("vivecraft.key.ingameMenuButton", -1, "key.categories.ui");
    public final KeyMapping keyMoveThirdPersonCam = new KeyMapping("vivecraft.key.moveThirdPersonCam", -1, "key.categories.misc");
    public final KeyMapping keyQuickHandheldCam = new KeyMapping("vivecraft.key.quickHandheldCam", -1, "key.categories.misc");
    public final KeyMapping keyQuickTorch = new KeyMapping("vivecraft.key.quickTorch", -1, "key.categories.gameplay");
    public final KeyMapping keyRadialMenu = new KeyMapping("vivecraft.key.radialMenu", -1, "key.categories.ui");
    public final KeyMapping keyRotateAxis = new KeyMapping("vivecraft.key.rotateAxis", -1, "key.categories.movement");
    public final KeyMapping keyRotateFree = new KeyMapping("vivecraft.key.rotateFree", -1, "key.categories.movement");
    public final KeyMapping keyRotateLeft = new KeyMapping("vivecraft.key.rotateLeft", -1, "key.categories.movement");
    public final KeyMapping keyRotateRight = new KeyMapping("vivecraft.key.rotateRight", -1, "key.categories.movement");
    public final KeyMapping keySwapMirrorView = new KeyMapping("vivecraft.key.swapMirrorView", -1, "key.categories.misc");
    public final KeyMapping keyTeleport = new KeyMapping("vivecraft.key.teleport", -1, "key.categories.movement");
    public final KeyMapping keyTeleportFallback = new KeyMapping("vivecraft.key.teleportFallback", -1, "key.categories.movement");
    public final KeyMapping keyToggleHandheldCam = new KeyMapping("vivecraft.key.toggleHandheldCam", -1, "key.categories.misc");
    public final KeyMapping keyToggleKeyboard = new KeyMapping("vivecraft.key.toggleKeyboard", -1, "key.categories.ui");
    public final KeyMapping keyToggleMovement = new KeyMapping("vivecraft.key.toggleMovement", -1, "key.categories.movement");
    public final KeyMapping keyTogglePlayerList = new KeyMapping("vivecraft.key.togglePlayerList", -1, "key.categories.multiplayer");
    public final HandedKeyBinding keyTrackpadTouch = new HandedKeyBinding("vivecraft.key.trackpadTouch", -1, "key.categories.misc");
    public final HandedKeyBinding keyVRInteract = new HandedKeyBinding("vivecraft.key.vrInteract", -1, "key.categories.gameplay");
    public final KeyMapping keyWalkabout = new KeyMapping("vivecraft.key.walkabout", -1, "key.categories.movement");

    private void setupKeybindingSets() {
        if (this.userKeyBindingSet == null || hiddenKeyBindingSet == null)
        {
            this.userKeyBindingSet = new LinkedHashSet<>();
            this.hiddenKeyBindingSet = new LinkedHashSet<>();
            this.allKeyBindingSet = new LinkedHashSet<>();

            this.userKeyBindingSet.add(this.keyRotateLeft);
            this.userKeyBindingSet.add(this.keyRotateRight);
            this.userKeyBindingSet.add(this.keyTeleport);
            this.userKeyBindingSet.add(this.keyTeleportFallback);
            this.userKeyBindingSet.add(this.keyToggleMovement);
            this.userKeyBindingSet.add(this.keyQuickTorch);
            this.userKeyBindingSet.add(this.keySwapMirrorView);
            this.userKeyBindingSet.add(this.keyExportWorld);
            this.userKeyBindingSet.add(this.keyMoveThirdPersonCam);
            this.userKeyBindingSet.add(this.keyTogglePlayerList);
            this.userKeyBindingSet.add(this.keyToggleHandheldCam);
            this.userKeyBindingSet.add(this.keyQuickHandheldCam);

            this.hiddenKeyBindingSet.add(GuiHandler.keyLeftClick);
            this.hiddenKeyBindingSet.add(GuiHandler.keyRightClick);
            this.hiddenKeyBindingSet.add(GuiHandler.keyMiddleClick);
            this.hiddenKeyBindingSet.add(GuiHandler.keyShift);
            this.hiddenKeyBindingSet.add(GuiHandler.keyCtrl);
            this.hiddenKeyBindingSet.add(GuiHandler.keyAlt);
            this.hiddenKeyBindingSet.add(GuiHandler.keyScrollUp);
            this.hiddenKeyBindingSet.add(GuiHandler.keyScrollDown);
            this.hiddenKeyBindingSet.add(GuiHandler.keyScrollAxis);
            this.hiddenKeyBindingSet.add(GuiHandler.keyKeyboardClick);
            this.hiddenKeyBindingSet.add(GuiHandler.keyKeyboardShift);
            this.hiddenKeyBindingSet.add(this.keyClimbeyGrab);
            this.hiddenKeyBindingSet.add(this.keyClimbeyJump);
            this.hiddenKeyBindingSet.add(this.keyMenuButton);
            this.hiddenKeyBindingSet.add(this.keyRadialMenu);
            this.hiddenKeyBindingSet.add(this.keyToggleKeyboard);
            this.hiddenKeyBindingSet.add(this.keyHotbarSwipeX);
            this.hiddenKeyBindingSet.add(this.keyHotbarSwipeY);
            this.hiddenKeyBindingSet.add(this.keyTrackpadTouch);

            this.hiddenKeyBindingSet.add(this.keyRotateAxis);
            this.hiddenKeyBindingSet.add(this.keyRotateFree);
            this.hiddenKeyBindingSet.add(this.keyFreeMoveRotate);
            this.hiddenKeyBindingSet.add(this.keyFreeMoveStrafe);
            this.hiddenKeyBindingSet.add(this.keyHotbarNext);
            this.hiddenKeyBindingSet.add(this.keyHotbarPrev);
            this.hiddenKeyBindingSet.add(this.keyHotbarScroll);
            this.hiddenKeyBindingSet.add(this.keyVRInteract);
            this.hiddenKeyBindingSet.add(this.keyWalkabout);

            allKeyBindingSet.addAll(userKeyBindingSet);
            allKeyBindingSet.addAll(hiddenKeyBindingSet);
        }
    }

    public Set<KeyMapping> getUserKeyBindings()
    {
        setupKeybindingSets();
        return this.userKeyBindingSet;
    }

    public Set<KeyMapping> getHiddenKeyBindings() {
        setupKeybindingSets();
        return hiddenKeyBindingSet;
    }

    public Set<KeyMapping> getAllKeyBindings() {
        setupKeybindingSets();
        return allKeyBindingSet;
    }

    public KeyMapping[] initializeBindings(KeyMapping[] keyBindings)
    {
        for (KeyMapping keymapping : this.getUserKeyBindings())
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
        // add hidden keys, since those are not in there
        vanillaBindingSet.addAll(hiddenKeyBindingSet);
    }

    public boolean isSafeBinding(KeyMapping kb)
    {
        return this.getAllKeyBindings().contains(kb) || kb == mc.options.keyChat || kb == mc.options.keyInventory;
    }

    public boolean isModBinding(KeyMapping kb)
    {
        return !this.vanillaBindingSet.contains(kb);
    }

}
