package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRVulkanTextureData;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.GraphicsAPI;
import org.vivecraft.client_vr.render.helpers.vulkan.VulkanHelper;
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
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution == null) {
            // get texture size
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var renderSizeX = stack.callocInt(1);
                var renderSizeY = stack.callocInt(1);
                VRSystem_GetRecommendedRenderTargetSize(renderSizeX, renderSizeY);

                this.resolution = new Tuple<>(renderSizeX.get(0), renderSizeY.get(0));
                VRSettings.LOGGER.info("Vivecraft: OpenVR Render Res {}x{}", this.resolution.getA(),
                    this.resolution.getB());

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

                    for (int vertex = 0; vertex < this.hiddenMeshVertices[eye].length; vertex += 2) {
                        this.hiddenMeshVertices[eye][vertex] *= (float) this.resolution.getA();
                        this.hiddenMeshVertices[eye][vertex + 1] *= (float) this.resolution.getB();
                    }

                    VRSettings.LOGGER.info("Vivecraft: Stencil mesh loaded for eye '{}'", eye);
                }
            }
        }
        return this.resolution;
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip, boolean zZeroToOne) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer left = stack.callocFloat(1);
            FloatBuffer right = stack.callocFloat(1);
            FloatBuffer top = stack.callocFloat(1);
            FloatBuffer bottom = stack.callocFloat(1);
            VRSystem_GetProjectionRaw(eyeType == 0 ? VR.EVREye_Eye_Left : VR.EVREye_Eye_Right,
                left, right, top, bottom);

            return new Matrix4f().setFrustum(
                left.get() * nearClip, right.get() * nearClip,
                top.get() * nearClip, bottom.get() * nearClip,
                nearClip, farClip, zZeroToOne);
        }
    }

    @Override
    public void createRenderTexture(int width, int height) {
        if (this.framebufferEye0 == null || this.framebufferEye1 == null) {
            throw new RuntimeException("framebuffers need to be initialized first");
        }

        // map left eye texture
        mapTexture(0, this.framebufferEye0.getColorTexture());

        // map right eye texture
        mapTexture(1, this.framebufferEye1.getColorTexture());

        this.lastError = GraphicsAPI.getInstance().checkError("create VR textures");
    }

    private void mapTexture(int eye, GpuTexture framebuffer) {
        Texture texture = eye == 0 ? this.openvr.texType0 : this.openvr.texType1;
        texture.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        switch (GraphicsAPI.getInstance().type()) {
            case OPENGL -> {
                texture.eType(VR.ETextureType_TextureType_OpenGL);
                texture.handle(GraphicsAPI.getInstance().getImageHandle(framebuffer));
            }
            case VULKAN -> {
                VRVulkanTextureData data = eye == 0 ? this.openvr.texVulkan0 : this.openvr.texVulkan1;
                texture.eType(VR.ETextureType_TextureType_Vulkan);
                texture.handle(data.address());
                data.m_nImage(GraphicsAPI.getInstance().getImageHandle(framebuffer));
                // TODO figure out how to query that, in case a mod uses MSAA
                data.m_nSampleCount(1);
                data.m_nFormat(GraphicsAPI.getInstance().getFormat(framebuffer.getFormat()));
                data.m_nWidth(framebuffer.getWidth(0));
                data.m_nHeight(framebuffer.getHeight(0));

                VulkanHelper.VulkanDeviceData deviceData = ((VulkanHelper) GraphicsAPI.getInstance()).getDeviceData();

                // pipeline data
                data.m_pDevice(deviceData.device());
                data.m_pPhysicalDevice(deviceData.physicalDevice());
                data.m_pInstance(deviceData.instance());
                data.m_pQueue(deviceData.queue());
                data.m_nQueueFamilyIndex(deviceData.queueFamilyIndex());
            }
            default -> throw new RuntimeException("Unknown GraphicsAPI: " + GraphicsAPI.getInstance().type());
        }
    }

    @Override
    public void endFrame() throws RenderConfigException {
        GraphicsAPI.getInstance()
            .changeTexturePurpose(this.framebufferEye0.getColorTexture(), GraphicsAPI.TexturePurpose.TRANSFER);
        GraphicsAPI.getInstance()
            .changeTexturePurpose(this.framebufferEye1.getColorTexture(), GraphicsAPI.TexturePurpose.TRANSFER);

        // make sure everything rendered before submitting
        GraphicsAPI.getInstance().flushPreSubmit();

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

        // flush, recommended by the openvr docs for opengl
        // https://github.com/ValveSoftware/openvr/blob/91825305130f446f82054c1ec3d416321ace0072/headers/openvr.h#L3605-L3606
        GraphicsAPI.getInstance().flushPostSubmit();

        GraphicsAPI.getInstance()
            .changeTexturePurpose(this.framebufferEye0.getColorTexture(), GraphicsAPI.TexturePurpose.RENDER);
        GraphicsAPI.getInstance()
            .changeTexturePurpose(this.framebufferEye1.getColorTexture(), GraphicsAPI.TexturePurpose.RENDER);
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
