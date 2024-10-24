//package org.vivecraft.client_vr.provider.ovr_lwjgl;
//
//import net.minecraft.util.Tuple;
//import org.joml.Matrix4f;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.ovr.*;
//import org.lwjgl.system.MemoryStack;
//import org.vivecraft.client_vr.provider.MCVR;
//import org.vivecraft.client_vr.provider.VRRenderer;
//import org.vivecraft.client_vr.render.RenderConfigException;
//
//import java.nio.IntBuffer;
//
//public class OVR_StereoRenderer extends VRRenderer {
//    private final OVRSizei bufferSize;
//    private final OVRMatrix4f projL;
//    private final OVRMatrix4f projR;
//    private final MC_OVR mcovr;
//
//    public OVR_StereoRenderer(MCVR vr) {
//        super(vr);
//        this.mcovr = (MC_OVR) vr;
//        this.projL = OVRMatrix4f.malloc();
//        this.projR = OVRMatrix4f.malloc();
//        this.bufferSize = OVRSizei.malloc();
//    }
//
//    private static OVRRecti createRecti(int x, int y, int w, int h) {
//        OVRVector2i pos = OVRVector2i.malloc();
//        pos.set(x, y);
//        OVRSizei size = OVRSizei.malloc();
//        size.set(w, h);
//
//        OVRRecti recti = OVRRecti.malloc();
//        recti.set(pos, size);
//        return recti;
//    }
//
//    private void checkret(int ret) {
//        this.checkret(ret, "unspecified");
//    }
//
//    private void checkret(int ret, String desc) {
//        if (ret != 0) {
//            OVRErrorInfo ovrerrorinfo = OVRErrorInfo.malloc();
//            OVR.ovr_GetLastErrorInfo(ovrerrorinfo);
//            System.out.println("Oculus error in " + desc + " " + ovrerrorinfo.ErrorStringString());
//        }
//    }
//
//    @Override
//    public void createRenderTexture(int lwidth, int lheight) {
//        this.mcovr.textureSwapChainL = PointerBuffer.allocateDirect(1);
//        this.mcovr.textureSwapChainR = PointerBuffer.allocateDirect(1);
//
//        try (MemoryStack stack = MemoryStack.stackPush()) {
//
//            OVRTextureSwapChainDesc desc = OVRTextureSwapChainDesc.calloc(stack);
//            desc.set(OVR.ovrTexture_2D, OVR.OVR_FORMAT_R8G8B8A8_UNORM_SRGB, 1, this.bufferSize.w(), this.bufferSize.h(),
//                1, 1, false, desc.MiscFlags(), desc.BindFlags());
//
//            this.checkret(
//                OVRGL.ovr_CreateTextureSwapChainGL(this.mcovr.session.get(0), desc, this.mcovr.textureSwapChainL),
//                "create l eye");
//            this.checkret(
//                OVRGL.ovr_CreateTextureSwapChainGL(this.mcovr.session.get(0), desc, this.mcovr.textureSwapChainR),
//                "create r eye");
//
//            IntBuffer chainTexId = stack.callocInt(1);
//
//            this.checkret(
//                OVRGL.ovr_GetTextureSwapChainBufferGL(this.mcovr.session.get(0), this.mcovr.textureSwapChainL.get(0), 0,
//                    chainTexId), "create l chain");
//            this.LeftEyeTextureId = chainTexId.get();
//
//            chainTexId.rewind();
//
//            this.checkret(
//                OVRGL.ovr_GetTextureSwapChainBufferGL(this.mcovr.session.get(0), this.mcovr.textureSwapChainR.get(0), 0,
//                    chainTexId), "create r chain");
//            this.RightEyeTextureId = chainTexId.get();
//        }
//
//        this.mcovr.layer.Header().Type(OVR.ovrLayerType_EyeFov);
//        this.mcovr.layer.Header().Flags(0);
//
//        this.mcovr.layer.ColorTexture(OVR.ovrEye_Left, this.mcovr.textureSwapChainL.get(0));
//        this.mcovr.layer.ColorTexture(OVR.ovrEye_Right, this.mcovr.textureSwapChainR.get(0));
//
//        this.mcovr.layer.Fov(OVR.ovrEye_Left, this.mcovr.eyeRenderDesc0.Fov());
//        this.mcovr.layer.Fov(OVR.ovrEye_Right, this.mcovr.eyeRenderDesc1.Fov());
//
//        this.mcovr.layer.Viewport(OVR.ovrEye_Left, createRecti(0, 0, this.bufferSize.w(), this.bufferSize.h()));
//        this.mcovr.layer.Viewport(OVR.ovrEye_Right, createRecti(0, 0, this.bufferSize.w(), this.bufferSize.h()));
//
//        this.mcovr.layer.RenderPose(OVR.ovrEye_Left, this.mcovr.hmdToEyeViewPose.get(0));
//        this.mcovr.layer.RenderPose(OVR.ovrEye_Right, this.mcovr.hmdToEyeViewPose.get(1));
//    }
//
//    @Override
//    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
//        if (eyeType == 0) {
//            OVRUtil.ovrMatrix4f_Projection(this.mcovr.hmdDesc.DefaultEyeFov(OVR.ovrEye_Left), nearClip, farClip,
//                OVRUtil.ovrProjection_None, this.projL);
//            return OVRUtils.ovrMatrix4ToMatrix4f(this.projL).toMCMatrix();
//        } else {
//            OVRUtil.ovrMatrix4f_Projection(this.mcovr.hmdDesc.DefaultEyeFov(OVR.ovrEye_Right), nearClip, farClip,
//                OVRUtil.ovrProjection_None, this.projR);
//            return OVRUtils.ovrMatrix4ToMatrix4f(this.projR).toMCMatrix();
//        }
//    }
//
//    @Override
//    public void endFrame() throws RenderConfigException {
//        OVR.ovr_CommitTextureSwapChain(this.mcovr.session.get(0), this.mcovr.textureSwapChainL.get(0));
//        OVR.ovr_CommitTextureSwapChain(this.mcovr.session.get(0), this.mcovr.textureSwapChainR.get(0));
//
//        PointerBuffer layerPtrList = BufferUtils.createPointerBuffer(1);
//        layerPtrList.put(this.mcovr.layer.address());
//        layerPtrList.flip();
//
//        OVR.ovr_EndFrame(this.mcovr.session.get(0), 0L, null, layerPtrList);
//    }
//
//    @Override
//    public boolean providesStencilMask() {
//        return false;
//    }
//
//    @Override
//    public Tuple<Integer, Integer> getRenderTextureSizes() {
//        try (MemoryStack stack = MemoryStack.stackPush()) {
//            OVRSizei recommendedTex0Size = OVRSizei.calloc(stack);
//            OVR.ovr_GetFovTextureSize(this.mcovr.session.get(0), OVR.ovrEye_Left,
//                this.mcovr.hmdDesc.DefaultEyeFov(OVR.ovrEye_Left), 1.0F, recommendedTex0Size);
//
//            OVRSizei recommendedTex1Size = OVRSizei.calloc(stack);
//            OVR.ovr_GetFovTextureSize(this.mcovr.session.get(0), OVR.ovrEye_Right,
//                this.mcovr.hmdDesc.DefaultEyeFov(OVR.ovrEye_Right), 1.0F, recommendedTex1Size);
//
//            int bufferSizeW = Math.max(recommendedTex0Size.w(), recommendedTex1Size.w());
//            int bufferSizeH = Math.max(recommendedTex0Size.h(), recommendedTex1Size.h());
//            this.bufferSize.w(bufferSizeW);
//            this.bufferSize.h(bufferSizeH);
//            return new Tuple<>(bufferSizeW, bufferSizeH);
//        }
//    }
//}
