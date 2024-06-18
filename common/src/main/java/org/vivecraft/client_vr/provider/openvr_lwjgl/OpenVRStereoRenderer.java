package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VR;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

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
                VRSettings.logger.info("OpenVR Render Res {}x{}", this.resolution.getA(), this.resolution.getB());

                this.ss = this.openvr.getSuperSampling();
                VRSettings.logger.info("OpenVR Supersampling: {}", this.ss);
            }

            // get stencil meshes
            for (int eye = 0; eye < 2; eye++) {
                VRSystem_GetHiddenAreaMesh(eye, VR.EHiddenAreaMeshType_k_eHiddenAreaMesh_Standard,
                    this.hiddenMeshes[eye]);
                int count = this.hiddenMeshes[eye].unTriangleCount();

                if (count <= 0) {
                    VRSettings.logger.info("No stencil mesh found for eye '{}'", eye);
                } else {
                    this.hiddenMeshVertices[eye] = new float[count * 3 * 2];
                    MemoryUtil.memFloatBuffer(MemoryUtil.memAddress(this.hiddenMeshes[eye].pVertexData()),
                        this.hiddenMeshVertices[eye].length).get(this.hiddenMeshVertices[eye]);

                    for (int vertex = 0; vertex < this.hiddenMeshVertices[eye].length; vertex += 2) {
                        this.hiddenMeshVertices[eye][vertex] *= (float) this.resolution.getA();
                        this.hiddenMeshVertices[eye][vertex + 1] *= (float) this.resolution.getB();
                    }

                    VRSettings.logger.info("Stencil mesh loaded for eye '{}'", eye);
                }
            }
        }
        return this.resolution;
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (eyeType == VR.EVREye_Eye_Left) {
                return OpenVRUtil.Matrix4fFromOpenVR(VRSystem_GetProjectionMatrix(VR.EVREye_Eye_Left, nearClip, farClip, HmdMatrix44.calloc(stack)));
            } else {
                return OpenVRUtil.Matrix4fFromOpenVR(VRSystem_GetProjectionMatrix(VR.EVREye_Eye_Right, nearClip, farClip, HmdMatrix44.calloc(stack)));
            }
        }
    }

    @Override
    public void createRenderTexture(int width, int height) {
        int boundTextureId = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);
        // generate left eye texture
        this.LeftEyeTextureId = GlStateManager._genTexture();
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);
        this.openvr.texType0.handle(this.LeftEyeTextureId);
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);

        // generate right eye texture
        this.RightEyeTextureId = GlStateManager._genTexture();
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);
        this.openvr.texType1.handle(this.RightEyeTextureId);
        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(VR.ETextureType_TextureType_OpenGL);

        RenderSystem.bindTexture(boundTextureId);
        lastError = RenderHelper.checkGLError("create VR textures");
    }

    @Override
    public void endFrame() throws RenderConfigException {
        int leftError = VRCompositor_Submit(VR.EVREye_Eye_Left, this.openvr.texType0, null, VR.EVRSubmitFlags_Submit_Default);
        int rightError = VRCompositor_Submit(VR.EVREye_Eye_Right, this.openvr.texType1, null, VR.EVRSubmitFlags_Submit_Default);

        VRCompositor_PostPresentHandoff();

        if (leftError + rightError > VR.EVRCompositorError_VRCompositorError_None) {
            throw new RenderConfigException("Compositor Error", Component.literal(
                "Texture submission error: Left/Right " + getCompositorError(leftError) + "/" +
                    getCompositorError(rightError)));
        }

        // flush, recommended by the openvr docs
        GL11C.glFlush();
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
        if (this.LeftEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.LeftEyeTextureId);
            this.LeftEyeTextureId = -1;
        }

        if (this.RightEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.RightEyeTextureId);
            this.RightEyeTextureId = -1;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.hiddenMeshes[0].free();
        this.hiddenMeshes[1].free();
    }
}
