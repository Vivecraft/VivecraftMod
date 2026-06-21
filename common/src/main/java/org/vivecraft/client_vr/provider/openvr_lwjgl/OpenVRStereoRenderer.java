package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VRVulkanTextureData;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.OpenGLHelper;
import org.vivecraft.client_vr.render.helpers.graphics.VulkanHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.Arrays;

import static org.lwjgl.openvr.VRCompositor.VRCompositor_PostPresentHandoff;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_Submit;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private final HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;

    private final VRVulkanTextureData[] vkEyeData = new VRVulkanTextureData[2];

    public OpenVRStereoRenderer(MCVR vr) {
        super(vr);
        this.openvr = (MCOpenVR) vr;

        // allocate meshes, they are freed in destroy()
        this.hiddenMeshes[0] = HiddenAreaMesh.calloc();
        this.hiddenMeshes[1] = HiddenAreaMesh.calloc();

        if (GraphicsHelper.INSTANCE instanceof VulkanHelper) {
            this.vkEyeData[0] = VRVulkanTextureData.calloc();
            this.vkEyeData[1] = VRVulkanTextureData.calloc();
        }
    }

    @Override
    public void checkCapabilities() throws RenderConfigException {
        super.checkCapabilities();
        if (GraphicsHelper.INSTANCE instanceof VulkanHelper vulkanHelper) {
            // check that the needed extensions are loaded, and remember them for the next start
            int length = VRCompositor.VRCompositor_GetVulkanInstanceExtensionsRequired(null);
            String instanceExtensions = "";
            if (length > 0) {
                instanceExtensions = VRCompositor.VRCompositor_GetVulkanInstanceExtensionsRequired(length);
            }

            length = VRCompositor.VRCompositor_GetVulkanDeviceExtensionsRequired(
                vulkanHelper.getPhysicalDevicePointer(), null);
            String deviceExtensions = "";
            if (length > 0) {
                deviceExtensions = VRCompositor.VRCompositor_GetVulkanDeviceExtensionsRequired(
                    vulkanHelper.getPhysicalDevicePointer(), length);
            }
            // remember the extensions for the next launch
            ClientDataHolderVR.getInstance().vrSettings.requiredVulkanInstanceExtensions = instanceExtensions;
            ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceExtensions = deviceExtensions;

            // check that all extensions are supported and loaded
            vulkanHelper.checkExtensionSupport(Arrays.stream(instanceExtensions.split(" ")).toList(),
                Arrays.stream(deviceExtensions.split(" ")).toList());
        }
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
            if (eyeType == VR.EVREye_Eye_Left) {
                return OpenVRUtil.Matrix4fFromOpenVR(
                    VRSystem_GetProjectionMatrix(VR.EVREye_Eye_Left, nearClip, Math.min(farClip, Float.MAX_VALUE),
                        HmdMatrix44.calloc(stack)));
            } else {
                return OpenVRUtil.Matrix4fFromOpenVR(
                    VRSystem_GetProjectionMatrix(VR.EVREye_Eye_Right, nearClip, Math.min(farClip, Float.MAX_VALUE),
                        HmdMatrix44.calloc(stack)));
            }
        }
    }

    @Override
    public void createRenderTexture(int width, int height) {
        // generate eye textures
        for (int i = 0; i < 2; i++) {
            int prevTexture = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
            this.eyeTextureId[i] = GlStateManager._genTexture();
            GlStateManager._bindTexture(this.eyeTextureId[i]);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA,
                GL11.GL_INT,
                null);

            GlStateManager._bindTexture(prevTexture);
            GraphicsHelper.INSTANCE.checkError((i == 0 ? "Left" : "Right") + " Eye framebuffer setup");
        }

        if (GraphicsHelper.INSTANCE instanceof OpenGLHelper) {
            this.setupOpenGL();
        } else if (GraphicsHelper.INSTANCE instanceof VulkanHelper) {
            this.setupVulkan();
        } else {
            throw new IllegalStateException(
                "Vivecraft: Unexpected device type: " + GraphicsHelper.INSTANCE.getClass().getName());
        }

        this.lastError = GraphicsHelper.INSTANCE.checkError("create VR textures");
    }

    private void setupOpenGL() {
        this.openvr.texType0.handle(this.eyeTextureId[0]);
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);

        this.openvr.texType1.handle(this.eyeTextureId[1]);
        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(VR.ETextureType_TextureType_OpenGL);
    }

    private void setupVulkan() {
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_Vulkan);
        this.openvr.texType0.handle(this.vkEyeData[0].address());

        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(VR.ETextureType_TextureType_Vulkan);
        this.openvr.texType1.handle(this.vkEyeData[1].address());

        // populate vk objects
        if (GraphicsHelper.INSTANCE instanceof VulkanHelper vkHelper) {
            for (int i = 0; i < 2; i++) {
                this.vkEyeData[i].m_nImage(
                    GraphicsHelper.INSTANCE.getTextureHandle(this.framebufferEye[i].getColorTexture()));
                this.vkEyeData[i].m_pDevice(vkHelper.getDevicePointer());
                this.vkEyeData[i].m_pPhysicalDevice(vkHelper.getPhysicalDevicePointer());
                this.vkEyeData[i].m_pInstance(vkHelper.getInstancePointer());
                this.vkEyeData[i].m_pQueue(vkHelper.getQueuePointer());
                this.vkEyeData[i].m_nQueueFamilyIndex(vkHelper.getQueueFamilyIndex());
                this.vkEyeData[i].m_nWidth(this.framebufferEye[i].width);
                this.vkEyeData[i].m_nHeight(this.framebufferEye[i].height);
                this.vkEyeData[i].m_nFormat(VK10.VK_FORMAT_R8G8B8_UNORM);
                // hardcoded, maybe mixin to store per target?
                this.vkEyeData[i].m_nSampleCount(1);
            }
        } else {
            throw new IllegalStateException("Vivecraft: Vulkan on non vulkan device");
        }
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
    protected void destroyBuffers() {
        super.destroyBuffers();
        if (this.eyeTextureId[0] > -1) {
            GlStateManager._deleteTexture(this.eyeTextureId[0]);
            this.eyeTextureId[0] = -1;
        }

        if (this.eyeTextureId[1] > -1) {
            GlStateManager._deleteTexture(this.eyeTextureId[1]);
            this.eyeTextureId[1] = -1;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.hiddenMeshes[0].free();
        this.hiddenMeshes[1].free();

        for (int i = 0; i < 2; i++) {
            if (this.vkEyeData[i] != null) {
                this.vkEyeData[i].free();
                this.vkEyeData[i] = null;
            }
        }
    }
}
