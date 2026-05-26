package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.VR;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.FloatBuffer;

import static org.lwjgl.openvr.VRCompositor.VRCompositor_PostPresentHandoff;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_Submit;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private final HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;

    public OpenVRStereoRenderer(MCVR vr) {
        super(vr);
        this.openvr = (MCOpenVR) vr;

        // allocate meshes, they are freed in destroy()
        this.hiddenMeshes[0] = HiddenAreaMesh.calloc();
        this.hiddenMeshes[1] = HiddenAreaMesh.calloc();
    }

    @Override
    public Vector2ic getRenderTextureSizes() {
        if (this.resolution == null) {
            // get texture size
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var renderSizeX = stack.callocInt(1);
                var renderSizeY = stack.callocInt(1);
                VRSystem_GetRecommendedRenderTargetSize(renderSizeX, renderSizeY);

                this.resolution = new Vector2i(renderSizeX.get(0), renderSizeY.get(0));
                VRSettings.LOGGER.info("Vivecraft: OpenVR Render Res {}x{}", this.resolution.x(), this.resolution.y());

                this.ss = this.openvr.getSuperSampling();
                VRSettings.LOGGER.info("Vivecraft: OpenVR Supersampling: {}", this.ss);
            }

            // get stencil meshes
            for (int eye = 0; eye < 2; eye++) {
                VRSystem_GetHiddenAreaMesh(eye, VR.EHiddenAreaMeshType_k_eHiddenAreaMesh_Standard,
                    this.hiddenMeshes[eye]);
                int count = this.hiddenMeshes[eye].unTriangleCount();

                if (count <= 0) {
                    VRSettings.LOGGER.info("Vivecraft: No stencil mesh found for eye '{}'", eye);
                } else {
                    this.hiddenMeshVertices[eye] = new float[count * 3 * 2];
                    MemoryUtil.memFloatBuffer(MemoryUtil.memAddress(this.hiddenMeshes[eye].pVertexData()),
                        this.hiddenMeshVertices[eye].length).get(this.hiddenMeshVertices[eye]);

                    VRSettings.LOGGER.info("Vivecraft: Stencil mesh loaded for eye '{}'", eye);
                }
            }
        }
        return this.resolution;
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer left = stack.mallocFloat(1);
            FloatBuffer right = stack.mallocFloat(1);
            FloatBuffer top = stack.mallocFloat(1);
            FloatBuffer bottom = stack.mallocFloat(1);

            VRSystem_GetProjectionRaw(eyeType, left, right, top, bottom);
            return new Matrix4f().frustum(
                left.get() * nearClip, right.get() * nearClip,
                top.get() * nearClip, bottom.get() * nearClip,
                nearClip, farClip, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
        }
    }

    @Override
    public void createRenderTexture(int width, int height) {
        // generate left eye texture
        this.framebufferEye0 = VRTextureTarget.builder("L Eye")
            .withSize(width, height)
            .withFormat(GpuFormat.RGBA8_UNORM)
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEye0);
        GraphicsHelper.INSTANCE.checkError("Left Eye framebuffer setup");

        this.openvr.texType0.handle(GraphicsHelper.INSTANCE.getTextureHandle(this.framebufferEye0.getColorTexture()));
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        switch (GraphicsHelper.INSTANCE.getDeviceType()) {
            case OPENGL -> this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);
            case VULKAN -> this.openvr.texType0.eType(VR.ETextureType_TextureType_Vulkan);
            default ->
                throw new IllegalStateException("Unexpected device type: " + GraphicsHelper.INSTANCE.getDeviceType());
        }

        // generate right eye texture
        this.framebufferEye1 = VRTextureTarget.builder("R Eye")
            .withSize(width, height)
            .withFormat(GpuFormat.RGBA8_UNORM)
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEye1);
        GraphicsHelper.INSTANCE.checkError("Right Eye framebuffer setup");

        this.openvr.texType1.handle(GraphicsHelper.INSTANCE.getTextureHandle(this.framebufferEye1.getColorTexture()));
        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        switch (GraphicsHelper.INSTANCE.getDeviceType()) {
            case OPENGL -> this.openvr.texType1.eType(VR.ETextureType_TextureType_OpenGL);
            case VULKAN -> this.openvr.texType1.eType(VR.ETextureType_TextureType_Vulkan);
            default ->
                throw new IllegalStateException("Unexpected device type: " + GraphicsHelper.INSTANCE.getDeviceType());
        }

        this.lastError = GraphicsHelper.INSTANCE.checkError("create VR textures");
    }

    @Override
    public void endFrame() throws RenderConfigException {
        int leftError = VRCompositor_Submit(VR.EVREye_Eye_Left, this.openvr.texType0, null,
            VR.EVRSubmitFlags_Submit_Default);
        int rightError = VRCompositor_Submit(VR.EVREye_Eye_Right, this.openvr.texType1, null,
            VR.EVRSubmitFlags_Submit_Default);

        VRCompositor_PostPresentHandoff();

        if (leftError + rightError > VR.EVRCompositorError_VRCompositorError_None) {
            throw new RenderConfigException(Component.literal("Compositor Error"),
                Component.literal("Texture submission error: Left/Right " +
                    getCompositorError(leftError) + "/" + getCompositorError(rightError)));
        }

        // flush, recommended by the openvr docs
        GraphicsHelper.INSTANCE.flush();
    }

    public static String getCompositorError(int code) {
        return switch (code) {
            case 0 -> "None";
            case 1 -> "RequestFailed";
            case 100 -> "IncompatibleVersion";
            case 101 -> "DoesNotHaveFocus";
            case 102 -> "InvalidTexture";
            case 103 -> "IsNotSceneApplication";
            case 104 -> "TextureIsOnWrongDevice";
            case 105 -> "TextureUsesUnsupportedFormat:";
            case 106 -> "SharedTexturesNotSupported";
            case 107 -> "IndexOutOfRange";
            case 108 -> "AlreadySubmitted";
            case 109 -> "InvalidBounds";
            case 110 -> "AlreadySet";
            default -> "Unknown";
        };
    }

    @Override
    public boolean providesStencilMask() {
        return true;
    }

    @Override
    public String getName() {
        return "OpenVR";
    }

    @Override
    public void destroy() {
        super.destroy();
        this.hiddenMeshes[0].free();
        this.hiddenMeshes[1].free();
    }
}
