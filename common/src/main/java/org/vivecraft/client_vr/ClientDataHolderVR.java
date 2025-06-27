package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.vivecraft.api.client.ItemInUseTracker;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.interact_modules.*;
import org.vivecraft.client_vr.gameplay.trackers.*;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class ClientDataHolderVR {

    public static final ResourceLocation THIRD_PERSON_CAMERA_MODEL = ResourceLocation.fromNamespaceAndPath("vivecraft",
        "camcorder");
    public static final ResourceLocation THIRD_PERSON_CAMERA_DISPLAY_MODEL = ResourceLocation.fromNamespaceAndPath(
        "vivecraft", "camcorder_display");

    private static ClientDataHolderVR INSTANCE = new ClientDataHolderVR();

    public final boolean katVr;
    public final boolean infinadeck;

    public final boolean kiosk;
    public final boolean viewOnly;

    public boolean isMainHand;
    public boolean isFpHand;

    public VRPlayer vrPlayer;
    public MCVR vr;
    public VRRenderer vrRenderer;
    public MenuWorldRenderer menuWorldRenderer;

    // list of all registered trackers
    private final List<Tracker> trackers = new ArrayList<>();
    // list of all trackers that control holding item usage
    private final List<ItemInUseTracker> itemInUseTrackers = new ArrayList<>();

    // our trackers
    public final BackpackTracker backpackTracker;
    public final BowTracker bowTracker;
    public final CameraTracker cameraTracker;
    public final ClimbTracker climbTracker;
    public final CrawlTracker crawlTracker;
    public final EatingTracker eatingTracker;
    public final HorseTracker horseTracker;
    public final InteractTracker interactTracker;
    public final JumpTracker jumpTracker;
    public final RowTracker rowTracker;
    public final RunTracker runTracker;
    public final SneakTracker sneakTracker;
    public final SwimTracker swimTracker;
    public final SwingTracker swingTracker;
    public final TeleportTracker teleportTracker;
    public final TelescopeTracker telescopeTracker;
    public final VehicleTracker vehicleTracker;

    // our interact modules
    public final InteractiveHotbarModule hotbarModule;
    public final BowModule bowModule;
    public final ThirdPersonCameraModule thirdCamModule;
    public final ScreenshotCameraModule screenCamModule;
    public final EntityInteractionModule entityModule;
    public final BlockInteractionModule blockModule;

    public VRSettings vrSettings;
    public boolean grabScreenShot = false;
    public Screen cachedScreen = null;
    public long frameIndex = 0L;

    public RenderPass currentPass;
    public boolean isFirstPass;

    // if the main/offhand should be rendered as menu hands
    public boolean menuHandOff;
    public boolean menuHandMain;

    public boolean completelyDisabled;

    public int tickCounter;

    public VRFirstPersonArmSwing swingType = VRFirstPersonArmSwing.Attack;

    // showed chat notifications
    public boolean showedUpdateNotification;
    public boolean showedStencilMessage;
    public boolean showedFbtCalibrationNotification;

    private ClientDataHolderVR() {
        // need to set this at the beginning again, since the static initializer only sets it after creation
        INSTANCE = this;
        // read property settings
        this.kiosk = System.getProperty("kiosk", "false").equals("true");
        if (this.kiosk) {
            VRSettings.LOGGER.info("Vivecraft: Setting kiosk");
            this.viewOnly = System.getProperty("viewonly", "false").equals("true");
            if (this.viewOnly) {
                VRSettings.LOGGER.info("Vivecraft: Setting viewonly");
            }
        } else {
            this.viewOnly = false;
        }
        this.katVr = System.getProperty("katvr", "false").equals("true");
        this.infinadeck = System.getProperty("infinadeck", "false").equals("true");

        // create trackers
        this.backpackTracker = createTracker(BackpackTracker::new);
        this.bowTracker = createTracker(BowTracker::new);
        this.cameraTracker = createTracker(CameraTracker::new);
        this.climbTracker = createTracker(ClimbTracker::new);
        this.crawlTracker = createTracker(CrawlTracker::new);
        this.eatingTracker = createTracker(EatingTracker::new);
        this.horseTracker = createTracker(HorseTracker::new);
        this.interactTracker = createTracker(InteractTracker::new);
        this.jumpTracker = createTracker(JumpTracker::new);
        this.rowTracker = createTracker(RowTracker::new);
        this.runTracker = createTracker(RunTracker::new);
        this.sneakTracker = createTracker(SneakTracker::new);
        this.swimTracker = createTracker(SwimTracker::new);
        this.swingTracker = createTracker(SwingTracker::new);
        this.teleportTracker = createTracker(TeleportTracker::new);
        this.telescopeTracker = createTracker(TelescopeTracker::new);
        this.vehicleTracker = createTracker(VehicleTracker::new);

        // create interact modules
        this.hotbarModule = new InteractiveHotbarModule();
        this.bowModule = new BowModule(this);
        this.thirdCamModule = new ThirdPersonCameraModule(this);
        this.screenCamModule = new ScreenshotCameraModule(this);
        this.entityModule = new EntityInteractionModule(Minecraft.getInstance(), this);
        this.blockModule = new BlockInteractionModule(Minecraft.getInstance(), this);

        this.interactTracker.registerModules(this.hotbarModule, this.bowModule, this.thirdCamModule,
            this.screenCamModule, this.entityModule, this.blockModule);
    }

    public static ClientDataHolderVR getInstance() {
        return INSTANCE;
    }

    /**
     * checks if the given arm side is currently a menu hand
     *
     * @param arm LEFT/RIGHT arm to check
     * @return if the arm is a menu hnd
     */
    public boolean isMenuHand(HumanoidArm arm) {
        if (arm == HumanoidArm.LEFT) {
            return this.vrSettings.reverseHands ? this.menuHandMain : this.menuHandOff;
        } else {
            return this.vrSettings.reverseHands ? this.menuHandOff : this.menuHandMain;
        }
    }

    /**
     * Creates a tracker instance, adds it to the registered list and returns it
     *
     * @param constructor Constructor to use to create the tracker instance
     * @param <T>         Class of the tracker
     * @return created tracker instance
     */
    private <T extends Tracker> T createTracker(BiFunction<Minecraft, ClientDataHolderVR, T> constructor) {
        T tracker = constructor.apply(Minecraft.getInstance(), this);
        registerTracker(tracker);
        return tracker;
    }

    /**
     * registers trackers
     *
     * @param trackers trackers to register
     * @throws IllegalArgumentException if s tracker is already registered
     */
    public void registerTracker(Tracker... trackers) throws IllegalArgumentException {
        for (Tracker tracker : trackers) {
            if (this.trackers.contains(tracker)) {
                throw new IllegalArgumentException("Tracker is already added and should not be added again!");
            }
            this.trackers.add(tracker);
            if (tracker instanceof ItemInUseTracker itemInUseTracker) {
                this.itemInUseTrackers.add(itemInUseTracker);
            }
        }
    }

    /**
     * @return Unmodifiable list of the registered trackers
     */
    public List<Tracker> getTrackers() {
        return Collections.unmodifiableList(this.trackers);
    }

    /**
     * @param player Current local player.
     * @return Whether some tracker is currently using an item.
     */
    public boolean isTrackerUsingItem(LocalPlayer player) {
        return this.itemInUseTrackers.stream().anyMatch(tracker -> tracker.itemInUse(player));
    }
}
