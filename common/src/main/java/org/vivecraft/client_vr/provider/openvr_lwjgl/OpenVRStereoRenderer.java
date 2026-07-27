package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vulkan.VulkanConst;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import static org.lwjgl.openvr.VRCompositor.VRCompositor_PostPresentHandoff;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_Submit;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private final HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;

    private final VRVulkanTextureData[] vkEyeData = new VRVulkanTextureData[2];

    public OpenVRStereoRenderer(MCOpenVR vr) {
        super(vr);
        this.openvr = vr;

        // allocate meshes, they are freed in destroy()
        this.hiddenMeshes[0] = HiddenAreaMesh.calloc();
        this.hiddenMeshes[1] = HiddenAreaMesh.calloc();

        if (GraphicsHelper.INSTANCE.getType() == GraphicsHelper.Type.VULKAN) {
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
                vulkanHelper.getPhysicalDevice().address(), null);
            String deviceExtensions = "";
            if (length > 0) {
                deviceExtensions = VRCompositor.VRCompositor_GetVulkanDeviceExtensionsRequired(
                    vulkanHelper.getPhysicalDevice().address(), length);
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer deviceHandle = stack.callocLong(1);
                VRSystem.VRSystem_GetOutputDevice(deviceHandle, VR.ETextureType_TextureType_Vulkan,
                    vulkanHelper.getInstance().address());

                // check that all extensions are supported and loaded
                vulkanHelper.checkCompatibility(
                    Arrays.stream(instanceExtensions.split(" ")).toList(),
                    Arrays.stream(deviceExtensions.split(" ")).toList(),
                    null,
                    deviceHandle.get(0));
            }
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
        super.createRenderTexture(width, height);

        if (!this.lastError.isEmpty()) return;

        switch (GraphicsHelper.INSTANCE.getType()) {
            case OPENGL -> this.setupOpenGL();
            case VULKAN -> this.setupVulkan();
        }
    }

    private void setupOpenGL() {
        this.openvr.texType0.handle(GraphicsHelper.INSTANCE.getTextureHandle(this.framebufferEye[0].getColorTexture()));
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);

        this.openvr.texType1.handle(GraphicsHelper.INSTANCE.getTextureHandle(this.framebufferEye[1].getColorTexture()));
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
                this.vkEyeData[i].m_pDevice(vkHelper.getDevice().address());
                this.vkEyeData[i].m_pPhysicalDevice(vkHelper.getPhysicalDevice().address());
                this.vkEyeData[i].m_pInstance(vkHelper.getInstance().address());
                this.vkEyeData[i].m_pQueue(vkHelper.getQueuePointer());
                this.vkEyeData[i].m_nQueueFamilyIndex(vkHelper.getQueueFamilyIndex());
                this.vkEyeData[i].m_nWidth(this.framebufferEye[i].width);
                this.vkEyeData[i].m_nHeight(this.framebufferEye[i].height);
                this.vkEyeData[i].m_nFormat(VulkanConst.toVk(this.framebufferEye[i].gpuFormat));
                // hardcoded, maybe mixin to store per target?
                this.vkEyeData[i].m_nSampleCount(1);
            }
        } else {
            throw new IllegalStateException("Vivecraft: Vulkan on non vulkan device");
        }
    }


    @Override
    public void endFrame() throws RenderConfigException {
        // technically we are supposed to transition Vulkan images to VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL
        // vanilla has them in VK_IMAGE_LAYOUT_GENERAL by default which should also work though
        int leftError = VRCompositor_Submit(VR.EVREye_Eye_Left, this.openvr.texType0, null,
            VR.EVRSubmitFlags_Submit_Default);
        int rightError = VRCompositor_Submit(VR.EVREye_Eye_Right, this.openvr.texType1, null,
            VR.EVRSubmitFlags_Submit_Default);

        VRCompositor_PostPresentHandoff();

        if (leftError + rightError > VR.EVRCompositorError_VRCompositorError_None) {
            if (leftError == rightError && leftError == VR.EVRCompositorError_VRCompositorError_AlreadySubmitted) {
                VRSettings.LOGGER.error("Vivecraft: error ending frame: AlreadySubmitted");
            } else {
                throw new RenderConfigException(Component.literal("Compositor Error"),
                    Component.literal("Texture submission error: Left/Right " +
                        getCompositorError(leftError) + "/" + getCompositorError(rightError)));
            }
        }

        // flush, recommended by the openvr docs
        GraphicsHelper.INSTANCE.flush();
    }

    public static String getCompositorError(int code) {
        return switch (code) {
            case VR.EVRCompositorError_VRCompositorError_None -> "None";
            case VR.EVRCompositorError_VRCompositorError_RequestFailed -> "RequestFailed";
            case VR.EVRCompositorError_VRCompositorError_IncompatibleVersion -> "IncompatibleVersion";
            case VR.EVRCompositorError_VRCompositorError_DoNotHaveFocus -> "DoesNotHaveFocus";
            case VR.EVRCompositorError_VRCompositorError_InvalidTexture -> "InvalidTexture";
            case VR.EVRCompositorError_VRCompositorError_IsNotSceneApplication -> "IsNotSceneApplication";
            case VR.EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice -> "TextureIsOnWrongDevice";
            case VR.EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat ->
                "TextureUsesUnsupportedFormat:";
            case VR.EVRCompositorError_VRCompositorError_SharedTexturesNotSupported -> "SharedTexturesNotSupported";
            case VR.EVRCompositorError_VRCompositorError_IndexOutOfRange -> "IndexOutOfRange";
            case VR.EVRCompositorError_VRCompositorError_AlreadySubmitted -> "AlreadySubmitted";
            case VR.EVRCompositorError_VRCompositorError_InvalidBounds -> "InvalidBounds";
            case VR.EVRCompositorError_VRCompositorError_AlreadySet -> "AlreadySet";
            default -> "Unknown";
        };
    }

    @Override
    public boolean providesStencilMask() {
        return true;
    }

    public String getName() {
        return "OpenVR";
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
