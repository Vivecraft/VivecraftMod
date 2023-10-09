package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.mod_compat_vr.ShadersHelper;

import javax.annotation.Nonnull;

import static org.vivecraft.client_vr.VRState.*;

public class GuiRenderOpticsSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.stereorendering";
    private float prevRenderScaleFactor = ClientDataHolderVR.getInstance().vrSettings.renderScaleFactor;
    private float prevHandCameraResScale = ClientDataHolderVR.getInstance().vrSettings.handCameraResScale;

    public GuiRenderOpticsSettings(Screen par1Screen) {
        super(par1Screen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(
            VrOptions.RENDER_SCALEFACTOR,
            VrOptions.MIRROR_DISPLAY,
            VrOptions.FSAA,
            VrOptions.STENCIL_ON,
            VrOptions.HANDHELD_CAMERA_RENDER_SCALE,
            VrOptions.HANDHELD_CAMERA_FOV,
            switch (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode) {
                case MIXED_REALITY, THIRD_PERSON -> {
                    yield VRHotkeys.hasExternalCameraConfig();
                }
                default -> {
                    yield false;
                }
            } ? VrOptions.RELOAD_EXTERNAL_CAMERA : VrOptions.DUMMY,
            switch (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode) {
                case CROPPED, SINGLE -> {
                    yield true;
                }
                default -> {
                    yield false;
                }
            } ? VrOptions.MIRROR_EYE : VrOptions.DUMMY
        );

        switch (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode) {
            case MIXED_REALITY -> {
                super.init(
                    VrOptions.MIXED_REALITY_UNITY_LIKE,
                    VrOptions.MIXED_REALITY_RENDER_HANDS,
                    ClientDataHolderVR.getInstance().vrSettings.mixedRealityAlphaMask && ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike ?
                    VrOptions.DUMMY : VrOptions.MIXED_REALITY_KEY_COLOR,
                    VrOptions.MIXED_REALITY_FOV,
                    !ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike ? VrOptions.DUMMY : VrOptions.MIXED_REALITY_UNDISTORTED,
                    !ClientDataHolderVR.getInstance().vrSettings.mixedRealityUndistorted || !ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike ?
                    VrOptions.DUMMY : VrOptions.MONO_FOV,
                    !ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike ? VrOptions.DUMMY : VrOptions.MIXED_REALITY_ALPHA_MASK,
                    VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
                );
            }
            case FIRST_PERSON -> {
                super.init(VrOptions.MONO_FOV);
            }
            case THIRD_PERSON -> {
                super.init(
                    VrOptions.MIXED_REALITY_FOV,
                    VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
                );
            }
        }

        super.addDefaultButtons();
//        this.children().stream().filter((w) -> w instanceof GuiVROption).forEach((w) ->
//        {
//            GuiVROption guivroption = (GuiVROption)w;

//            if (guivroption.getOption() == VrOptions.HANDHELD_CAMERA_RENDER_SCALE && Config.isShaders())  //Optifine
//            {
//                guivroption.active = false;
//            }
//        });
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        Minecraft.getInstance().options.fov().set(70);
        if (vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Defaults Loaded");
        }
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            VrOptions option = guivroption.getOption();
            switch (option) {
                case MIRROR_DISPLAY, FSAA, STENCIL_ON -> {
                    if (vrRunning) {
                        if (option == VrOptions.STENCIL_ON || option == VrOptions.MIRROR_DISPLAY && ShadersHelper.isShaderActive()) {
                            ClientDataHolderVR.getInstance().vrRenderer.resizeFrameBuffers("Render Setting Changed");
                        } else {
                            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Render Setting Changed");
                        }
                        this.reinit = true;
                    }
                }
                case RELOAD_EXTERNAL_CAMERA -> {
                    VRHotkeys.loadExternalCameraConfig();
                }
                case MIXED_REALITY_ALPHA_MASK, MIXED_REALITY_UNITY_LIKE, MIXED_REALITY_UNDISTORTED -> {
                    this.reinit = true;
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ClientDataHolderVR.getInstance().vrSettings.renderScaleFactor != this.prevRenderScaleFactor || ClientDataHolderVR.getInstance().vrSettings.handCameraResScale != this.prevHandCameraResScale) {
            this.prevRenderScaleFactor = ClientDataHolderVR.getInstance().vrSettings.renderScaleFactor;
            this.prevHandCameraResScale = ClientDataHolderVR.getInstance().vrSettings.handCameraResScale;
            if (vrRunning) {
                ClientDataHolderVR.getInstance().vrRenderer.resizeFrameBuffers("Render Setting Changed");
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
