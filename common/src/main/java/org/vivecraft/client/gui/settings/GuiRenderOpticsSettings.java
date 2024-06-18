package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.ShadersHelper;

public class GuiRenderOpticsSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] monoDisplayOptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.MONO_FOV,
        VRSettings.VrOptions.DUMMY,
        VRSettings.VrOptions.FSAA
    };
    private static final VRSettings.VrOptions[] openVRDisplayOptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.RENDER_SCALEFACTOR,
        VRSettings.VrOptions.MIRROR_DISPLAY,
        VRSettings.VrOptions.FSAA,
        VRSettings.VrOptions.STENCIL_ON,
        VRSettings.VrOptions.HANDHELD_CAMERA_RENDER_SCALE,
        VRSettings.VrOptions.HANDHELD_CAMERA_FOV,
        VRSettings.VrOptions.RELOAD_EXTERNAL_CAMERA,
        VRSettings.VrOptions.MIRROR_EYE
    };
    private static final VRSettings.VrOptions[] MROptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.MIXED_REALITY_UNITY_LIKE,
        VRSettings.VrOptions.MIXED_REALITY_RENDER_HANDS,
        VRSettings.VrOptions.MIXED_REALITY_KEY_COLOR,
        VRSettings.VrOptions.MIXED_REALITY_FOV,
        VRSettings.VrOptions.MIXED_REALITY_UNDISTORTED,
        VRSettings.VrOptions.MONO_FOV,
        VRSettings.VrOptions.MIXED_REALITY_ALPHA_MASK,
        VRSettings.VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
    };
    private static final VRSettings.VrOptions[] UDOptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.MONO_FOV,
        VRSettings.VrOptions.MIRROR_CENTER_SMOOTH
    };
    private static final VRSettings.VrOptions[] TUDOptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.MIXED_REALITY_FOV,
        VRSettings.VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
    };
    private static final VRSettings.VrOptions[] CROPOptions = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.MIRROR_CROP
    };
    private float prevRenderScaleFactor = this.vrSettings.renderScaleFactor;
    private float prevHandCameraResScale = this.vrSettings.handCameraResScale;

    public GuiRenderOpticsSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.stereorendering";
        VRSettings.VrOptions[] buttons = new VRSettings.VrOptions[openVRDisplayOptions.length];
        System.arraycopy(openVRDisplayOptions, 0, buttons, 0, openVRDisplayOptions.length);

        for (int i = 0; i < buttons.length; i++) {
            VRSettings.VrOptions option = buttons[i];

            if (option == VRSettings.VrOptions.RELOAD_EXTERNAL_CAMERA && (!VRHotkeys.hasExternalCameraConfig() ||
                (this.dataHolder.vrSettings.displayMirrorMode != VRSettings.MirrorMode.MIXED_REALITY &&
                    this.dataHolder.vrSettings.displayMirrorMode != VRSettings.MirrorMode.THIRD_PERSON
                )
            ))
            {
                buttons[i] = VRSettings.VrOptions.DUMMY;
            }

            if (option == VRSettings.VrOptions.MIRROR_EYE &&
                this.dataHolder.vrSettings.displayMirrorMode != VRSettings.MirrorMode.CROPPED &&
                this.dataHolder.vrSettings.displayMirrorMode != VRSettings.MirrorMode.SINGLE &&
                !(this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY &&
                    this.dataHolder.vrSettings.mixedRealityUnityLike &&
                    !this.dataHolder.vrSettings.mixedRealityUndistorted
                ))
            {
                if (this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY
                    && this.dataHolder.vrSettings.mixedRealityUnityLike)
                {
                    buttons[i] = VRSettings.VrOptions.MIRROR_CENTER_SMOOTH;
                } else {
                    buttons[i] = VRSettings.VrOptions.DUMMY;
                }
            }
        }

        super.init(buttons, true);

        if (this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            buttons = new VRSettings.VrOptions[MROptions.length];
            System.arraycopy(MROptions, 0, buttons, 0, MROptions.length);

            for (int i = 0; i < buttons.length; i++) {
                VRSettings.VrOptions option = buttons[i];

                if (option == VRSettings.VrOptions.MONO_FOV && (!this.dataHolder.vrSettings.mixedRealityUndistorted || !this.dataHolder.vrSettings.mixedRealityUnityLike)) {
                    buttons[i] = VRSettings.VrOptions.DUMMY;
                }

                if (option == VRSettings.VrOptions.MIXED_REALITY_ALPHA_MASK && !this.dataHolder.vrSettings.mixedRealityUnityLike) {
                    buttons[i] = VRSettings.VrOptions.DUMMY;
                }

                if (option == VRSettings.VrOptions.MIXED_REALITY_UNDISTORTED && !this.dataHolder.vrSettings.mixedRealityUnityLike) {
                    buttons[i] = VRSettings.VrOptions.DUMMY;
                }

                if (option == VRSettings.VrOptions.MIXED_REALITY_KEY_COLOR && this.dataHolder.vrSettings.mixedRealityAlphaMask && this.dataHolder.vrSettings.mixedRealityUnityLike) {
                    buttons[i] = VRSettings.VrOptions.DUMMY;
                }
            }
            super.init(buttons, false);
        } else if (this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
            super.init(UDOptions, false);
        } else if (this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
            super.init(TUDOptions, false);
        } else if (this.dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.CROPPED) {
            super.init(CROPOptions, false);
        }

        super.addDefaultButtons();

        // disable the screenshot render scale, since that is not active, when shaders needs a fixed resolution for each pass
        this.children().stream().filter((w) -> w instanceof GuiVROption && w instanceof AbstractWidget).forEach((w) -> {
            if (((GuiVROption) w).getOption() == VRSettings.VrOptions.HANDHELD_CAMERA_RENDER_SCALE &&
                ShadersHelper.needsSameSizeBuffers())
            {
                ((AbstractWidget) w).active = false;
            }
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        this.minecraft.options.fov().set(70);
        if (VRState.vrInitialized) {
            this.dataHolder.vrRenderer.reinitFrameBuffers("Defaults Loaded");
        }
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getId() == VRSettings.VrOptions.MIRROR_DISPLAY.ordinal() || VRState.vrRunning && (guivroption.getId() == VRSettings.VrOptions.FSAA.ordinal() || guivroption.getId() == VRSettings.VrOptions.STENCIL_ON.ordinal())) {
                if (VRState.vrRunning) {
                    if (guivroption.getId() == VRSettings.VrOptions.STENCIL_ON.ordinal() || (guivroption.getId() == VRSettings.VrOptions.MIRROR_DISPLAY.ordinal() && ShadersHelper.isShaderActive())) {
                        this.dataHolder.vrRenderer.resizeFrameBuffers("Render Setting Changed");
                    } else {
                        this.dataHolder.vrRenderer.reinitFrameBuffers("Render Setting Changed");
                    }
                }
                this.reinit = true;
            }
            if (guivroption.getId() == VRSettings.VrOptions.RELOAD_EXTERNAL_CAMERA.ordinal()) {
                VRHotkeys.loadExternalCameraConfig();
            }
            if (guivroption.getId() == VRSettings.VrOptions.MIXED_REALITY_ALPHA_MASK.ordinal()
                || guivroption.getId() == VRSettings.VrOptions.MIXED_REALITY_UNITY_LIKE.ordinal()
                || guivroption.getId() == VRSettings.VrOptions.MIXED_REALITY_UNDISTORTED.ordinal()) {
                this.reinit = true;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Hacky way of making the render scale slider only reinit on mouse release
        if (this.vrSettings.renderScaleFactor != this.prevRenderScaleFactor || this.vrSettings.handCameraResScale != this.prevHandCameraResScale) {
            this.prevRenderScaleFactor = this.vrSettings.renderScaleFactor;
            this.prevHandCameraResScale = this.vrSettings.handCameraResScale;
            if (VRState.vrRunning) {
                this.dataHolder.vrRenderer.resizeFrameBuffers("Render Scale Changed: VR scale: %.1fx, Camera scale: %.1fx".formatted(this.vrSettings.renderScaleFactor, this.vrSettings.handCameraResScale));
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
