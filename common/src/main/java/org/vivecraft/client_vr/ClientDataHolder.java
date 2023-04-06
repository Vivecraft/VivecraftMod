package org.vivecraft.client_vr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.vivecraft.api.ErrorHelper;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.trackers.BackpackTracker;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.gameplay.trackers.CrawlTracker;
import org.vivecraft.client_vr.gameplay.trackers.EatingTracker;
import org.vivecraft.client_vr.gameplay.trackers.HorseTracker;
import org.vivecraft.client_vr.gameplay.trackers.InteractTracker;
import org.vivecraft.client_vr.gameplay.trackers.JumpTracker;
import org.vivecraft.client_vr.gameplay.trackers.RowTracker;
import org.vivecraft.client_vr.gameplay.trackers.RunTracker;
import org.vivecraft.client_vr.gameplay.trackers.SneakTracker;
import org.vivecraft.client_vr.gameplay.trackers.SwimTracker;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TeleportTracker;
import org.vivecraft.client_vr.gameplay.trackers.VehicleTracker;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client.render.RenderPass;
import org.vivecraft.client.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.sounds.SoundEngine;

public class ClientDataHolder {

    public static boolean kiosk;
    public static boolean ismainhand;
    public static boolean katvr;
    public static boolean infinadeck;
    public static boolean viewonly;
    public static ModelResourceLocation thirdPersonCameraModel = new ModelResourceLocation("vivecraft", "camcorder", "");
    public static ModelResourceLocation thirdPersonCameraDisplayModel = new ModelResourceLocation("vivecraft", "camcorder_display", "");
    public static List<String> hrtfList = new ArrayList<>();
    private static ClientDataHolder INSTANCE;
    public final float PIOVER180 = ((float) Math.PI / 180F);
    //public String minecriftVerString; Common
    public VRPlayer vrPlayer;
    public MCVR vr;
    public VRRenderer vrRenderer;
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
    //public PhysicalGuiManager physicalGuiManager = new PhysicalGuiManager( Minecraft.getInstance());
    public InteractTracker interactTracker = new InteractTracker(Minecraft.getInstance(), this);
    public CrawlTracker crawlTracker = new CrawlTracker(Minecraft.getInstance(), this);
    public CameraTracker cameraTracker = new CameraTracker(Minecraft.getInstance(), this);
    public ThreadGroup backgroundThreadGroup = new ThreadGroup("background");
    public int lastShaderIndex = -1;
    //	public Field fieldHwnd = null;
//	public Field fieldDisplay = null;
//	public Field fieldWindow = null;
//	public Field fieldResized = null;
//	public Method fieldResizedMethod = null;
    public VRSettings vrSettings;
    public long lastIntegratedServerLaunchCheck = 0L;
    public boolean integratedServerLaunchInProgress = false;
    public boolean grabScreenShot = false;
    public boolean lastShowMouseNative = true;
    public boolean enableWorldExport = false;
    public SoundEngine sndManager = null;
    //public MenuWorldRenderer menuWorldRenderer;
    //	private boolean firstInit = true;
    public boolean showSplashScreen = true;
    public long splashTimer1 = 0L;
    public long splashTimer2 = 0L;
    //	private RenderTarget splash;
//	private float splashFadeAlpha = 0.0F;
    public Deque<Long> runTickTimeNanos = new ArrayDeque<>();
    public long medianRunTickTimeNanos = 0L;
    public long frameIndex = 0L;
    public ErrorHelper errorHelper;
    public RenderPass currentPass;
    //	private boolean lastClick;
    //public boolean resourcePacksChanged; SERVER
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

    public static ClientDataHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientDataHolder();
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


}
