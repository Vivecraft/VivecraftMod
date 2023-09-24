package org.vivecraft.client_vr;

import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.trackers.*;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.resources.model.ModelResourceLocation;

import javax.annotation.Nonnull;

public class ClientDataHolderVR {

    public static boolean kiosk;
    public static boolean ismainhand;
    public static boolean katvr;
    public static boolean infinadeck;
    public static boolean viewonly;
    public static final ModelResourceLocation thirdPersonCameraModel = new ModelResourceLocation("vivecraft", "camcorder", "");
    public static final ModelResourceLocation thirdPersonCameraDisplayModel = new ModelResourceLocation("vivecraft", "camcorder_display", "");

    public VRPlayer vrPlayer;
    public MCVR vr;
    public VRRenderer vrRenderer;
    public VRSettings vrSettings;
    public MenuWorldRenderer menuWorldRenderer;
    public final BackpackTracker backpackTracker = new BackpackTracker();
    public final BowTracker bowTracker = new BowTracker();
    public final CameraTracker cameraTracker = new CameraTracker();
    public final ClimbTracker climbTracker = new ClimbTracker();
    public final CrawlTracker crawlTracker = new CrawlTracker();
    public final EatingTracker eatingTracker = new EatingTracker();
    public final HorseTracker horseTracker = new HorseTracker();
    public final InteractTracker interactTracker = new InteractTracker();
    public final JumpTracker jumpTracker = new JumpTracker();
    public final RowTracker rowTracker = new RowTracker();
    public final RunTracker runTracker = new RunTracker();
    public final SneakTracker sneakTracker = new SneakTracker();
    public final SwimTracker swimTracker = new SwimTracker();
    public final SwingTracker swingTracker = new SwingTracker();
    public final TeleportTracker teleportTracker = new TeleportTracker();
    public final TelescopeTracker telescopeTracker = new TelescopeTracker();
    public final VehicleTracker vehicleTracker = new VehicleTracker();
    public boolean integratedServerLaunchInProgress = false;
    public boolean grabScreenShot = false;
    public long frameIndex = 0L;
    @Nonnull public RenderPass currentPass = RenderPass.VANILLA;
    public int tickCounter;
    public float watereffect;
    public float portaleffect;
    public float pumpkineffect;
    public static boolean isfphand;
    public boolean isFirstPass;

    // showed chat notifications
    public boolean showedUpdateNotification;

    public boolean skipStupidGoddamnChunkBoundaryClipping;
}
