package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.openvr.HiddenAreaMesh;
import org.lwjgl.openvr.VR;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.FloatBuffer;

import static org.lwjgl.openvr.VRCompositor.VRCompositor_PostPresentHandoff;
import static org.lwjgl.openvr.VRCompositor.VRCompositor_Submit;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private final HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;
    protected int leftEyeTextureId = -1;
    protected int rightEyeTextureId = -1;
    public RenderTarget framebufferEyeLeft;
    public RenderTarget framebufferEyeRight;

    public OpenVRStereoRenderer(MCOpenVR vr) {
        super(vr);
        this.openvr = vr;

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
                nearClip, farClip, RenderSystem.getDevice().isZZeroToOne());
        }
    }

    @Override
    public void createRenderTexture(int width, int height) {
        int boundTextureId = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);

        // generate left eye texture
        this.leftEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.leftEyeTextureId);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA,
            GL11C.GL_INT, null);
        this.openvr.texType0.handle(this.leftEyeTextureId);
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);

        // generate right eye texture
        this.rightEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.rightEyeTextureId);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA,
            GL11C.GL_INT, null);
        this.openvr.texType1.handle(this.rightEyeTextureId);
        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(VR.ETextureType_TextureType_OpenGL);

        VRSettings.LOGGER.info("Vivecraft: VR Provider supplied render texture IDs: {}, {}", this.leftEyeTextureId,
            this.rightEyeTextureId);

        this.lastError = RenderHelper.checkGLError("create VR textures");

        this.framebufferEyeLeft = VRTextureTarget.builder("L Eye")
            .withSize(width, height)
            .withTexId(this.leftEyeTextureId)
            .withLinearFilter()
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEyeLeft);
        String leftError = RenderHelper.checkGLError("Left Eye framebuffer setup");

        this.framebufferEyeRight = VRTextureTarget.builder("R Eye")
            .withSize(width, height)
            .withTexId(this.rightEyeTextureId)
            .withLinearFilter()
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEyeRight);
        String rightError = RenderHelper.checkGLError("Right Eye framebuffer setup");

        if (this.lastError.isEmpty()) {
            this.lastError = !leftError.isEmpty() ? leftError : rightError;
        }

        GlStateManager._bindTexture(boundTextureId);
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
    public RenderTarget getLeftEyeTarget() {
        return this.framebufferEyeLeft;
    }

    @Override
    public RenderTarget getRightEyeTarget() {
        return this.framebufferEyeRight;
    }

    public float[] getStencilMask(RenderPass eye) {
        if (this.hiddenMeshVertices != null && (eye == RenderPass.LEFT || eye == RenderPass.RIGHT)) {
            return eye == RenderPass.LEFT ? this.hiddenMeshVertices[0] : this.hiddenMeshVertices[1];
        } else {
            return null;
        }
    }

    public String getName() {
        return "OpenVR";
    }

    @Override
    public void destroy() {
        super.destroy();

        this.hiddenMeshes[0].free();
        this.hiddenMeshes[1].free();

        if (this.framebufferEyeLeft != null) {
            this.framebufferEyeLeft.destroyBuffers();
            this.framebufferEyeLeft = null;
        }

        if (this.framebufferEyeRight != null) {
            this.framebufferEyeRight.destroyBuffers();
            this.framebufferEyeRight = null;
        }
        if (this.leftEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.leftEyeTextureId);
            this.leftEyeTextureId = -1;
        }

        if (this.rightEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.rightEyeTextureId);
            this.rightEyeTextureId = -1;
        }
    }
}
