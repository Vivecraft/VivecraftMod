package com.example.examplemod;

import java.util.ArrayDeque;
import java.util.Deque;

import org.vivecraft.api.ErrorHelper;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.trackers.BackpackTracker;
import org.vivecraft.gameplay.trackers.BowTracker;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.gameplay.trackers.ClimbTracker;
import org.vivecraft.gameplay.trackers.CrawlTracker;
import org.vivecraft.gameplay.trackers.EatingTracker;
import org.vivecraft.gameplay.trackers.HorseTracker;
import org.vivecraft.gameplay.trackers.InteractTracker;
import org.vivecraft.gameplay.trackers.JumpTracker;
import org.vivecraft.gameplay.trackers.RowTracker;
import org.vivecraft.gameplay.trackers.RunTracker;
import org.vivecraft.gameplay.trackers.SneakTracker;
import org.vivecraft.gameplay.trackers.SwimTracker;
import org.vivecraft.gameplay.trackers.SwingTracker;
import org.vivecraft.gameplay.trackers.TeleportTracker;
import org.vivecraft.gameplay.trackers.VehicleTracker;
import org.vivecraft.menuworlds.MenuWorldRenderer;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.VRRenderer;
import org.vivecraft.render.RenderPass;
import org.vivecraft.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;

public class DataHolder {
	
	private static DataHolder INSTANCE = new DataHolder();
	public static boolean kiosk;
	public static boolean ismainhand;
	public static boolean katvr;
	public static boolean infinadeck;
	public static boolean viewonly;
	
	public VRPlayer vrPlayer;
	public MCVR vr;
	public VRRenderer vrRenderer;
	public BackpackTracker backpackTracker = new BackpackTracker( Minecraft.getInstance());
	public BowTracker bowTracker = new BowTracker( Minecraft.getInstance());
	public SwimTracker swimTracker = new SwimTracker( Minecraft.getInstance());
	public EatingTracker autoFood = new EatingTracker( Minecraft.getInstance());
	public JumpTracker jumpTracker = new JumpTracker( Minecraft.getInstance());
	public SneakTracker sneakTracker = new SneakTracker( Minecraft.getInstance());
	public ClimbTracker climbTracker = new ClimbTracker( Minecraft.getInstance());
	public RunTracker runTracker = new RunTracker( Minecraft.getInstance());
	public RowTracker rowTracker = new RowTracker( Minecraft.getInstance());
	public TeleportTracker teleportTracker = new TeleportTracker( Minecraft.getInstance());
	public SwingTracker swingTracker = new SwingTracker( Minecraft.getInstance());
	public HorseTracker horseTracker = new HorseTracker( Minecraft.getInstance());
	public VehicleTracker vehicleTracker = new VehicleTracker( Minecraft.getInstance());
	//public PhysicalGuiManager physicalGuiManager = new PhysicalGuiManager( Minecraft.getInstance());
	public InteractTracker interactTracker = new InteractTracker( Minecraft.getInstance());
	public CrawlTracker crawlTracker = new CrawlTracker( Minecraft.getInstance());
	public CameraTracker cameraTracker = new CameraTracker( Minecraft.getInstance());
	public ThreadGroup backgroundThreadGroup = new ThreadGroup("background");
	public final float PIOVER180 = ((float)Math.PI / 180F);
//	private boolean oculus = false;
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
	public MenuWorldRenderer menuWorldRenderer;
//	private FloatBuffer matrixBuffer = MemoryTracker.create(16).asFloatBuffer();
//	private FloatBuffer matrixBuffer2 = MemoryTracker.create(16).asFloatBuffer();
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
	public boolean resourcePacksChanged;
	public int tickCounter;
	public final String minecriftVerString = "Vivecraft 1.17.1  jrbudda-NONVR-1-b2";
	
	public static DataHolder getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DataHolder();
		}
		return INSTANCE;
	}

	public void printChatMessage(String string) {
		// TODO Auto-generated method stub
		
	}

	public void print(String string) {
		// TODO Auto-generated method stub
		
	}
	
	
}
