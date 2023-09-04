package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.trackers.*;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.List;

public class ClientDataHolderVR {

    public static boolean kiosk;
    public static boolean ismainhand;
    public static boolean katvr;
    public static boolean infinadeck;
    public static boolean viewonly;
    public static ModelResourceLocation thirdPersonCameraModel = new ModelResourceLocation("vivecraft", "camcorder", "");
    public static ModelResourceLocation thirdPersonCameraDisplayModel = new ModelResourceLocation("vivecraft", "camcorder_display", "");
    private static ClientDataHolderVR INSTANCE;

    public VRPlayer vrPlayer;
    public MCVR vr;
    public VRRenderer vrRenderer;
    public MenuWorldRenderer menuWorldRenderer;
    public BackpackTracker backpackTracker = new BackpackTracker(Minecraft.getInstance(), this);
    public BowTracker bowTracker = new BowTracker(Minecraft.getInstance(), this);
    public SwimTracker swimTracker = new SwimTracker(Minecraft.getInstance(), this);
    public EatingTracker autoFood = new EatingTracker(Minecraft.getInstance(), this);
    public JumpTracker jumpTracker = new JumpTracker(Minecraft.getInstance(), this);
    public SneakTracker sneakTracker = new SneakTracker(Minecraft.getInstance(), this);
    public ClimbTracker climbTracker = new ClimbTracker(Minecraft.getInstance(), this);
    public RunTracker runTracker = new RunTracker(Minecraft.getInstance(), this);
    public RowTracker rowTracker = new RowTracker(Minecraft.getInstance(), this);
    public TeleportTracker teleportTracker = new TeleportTracker(Minecraft.getInstance(), this);
    public SwingTracker swingTracker = new SwingTracker(Minecraft.getInstance(), this);
    public HorseTracker horseTracker = new HorseTracker(Minecraft.getInstance(), this);
    public VehicleTracker vehicleTracker = new VehicleTracker(Minecraft.getInstance(), this);
    public InteractTracker interactTracker = new InteractTracker(Minecraft.getInstance(), this);
    public CrawlTracker crawlTracker = new CrawlTracker(Minecraft.getInstance(), this);
    public CameraTracker cameraTracker = new CameraTracker(Minecraft.getInstance(), this);
    public VRSettings vrSettings;
    public boolean integratedServerLaunchInProgress = false;
    public boolean grabScreenShot = false;
    public long frameIndex = 0L;
    public RenderPass currentPass;
    public int tickCounter;
    public float watereffect;
    public float portaleffect;
    public float pumpkineffect;
    public static boolean isfphand;
    public boolean isFirstPass;
    long mirroNotifyStart;
    String mirrorNotifyText;
    boolean mirrorNotifyClear;
    long mirroNotifyLen;
    ArrayList<Tracker> trackers = new ArrayList<>();

    // showed chat notifications
    public boolean showedUpdateNotification;

    public boolean skipStupidGoddamnChunkBoundaryClipping;

    private ClientDataHolderVR() {
        addTracker(backpackTracker);
        addTracker(bowTracker);
        addTracker(swingTracker);
        addTracker(autoFood);
        addTracker(jumpTracker);
        addTracker(sneakTracker);
        addTracker(climbTracker);
        addTracker(runTracker);
        addTracker(rowTracker);
        addTracker(teleportTracker);
        addTracker(horseTracker);
        addTracker(vehicleTracker);
        addTracker(interactTracker);
        addTracker(crawlTracker);
        addTracker(cameraTracker);
    }

    public static ClientDataHolderVR getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientDataHolderVR();
        }
        return INSTANCE;
    }

    public void printChatMessage(String string) {
        // TODO Auto-generated method stub

    }

    public void print(String string) {
        string = string.replace("\n", "\n[Minecrift] ");
        System.out.println("[Minecrift] " + string);
    }

    public void addTracker(Tracker tracker) {
        if (trackers.contains(tracker)) {
            throw new IllegalArgumentException("Tracker is already added and should not be added again!");
        }
        trackers.add(tracker);
    }

    public List<Tracker> getTrackers() {
        return this.trackers;
    }
}
