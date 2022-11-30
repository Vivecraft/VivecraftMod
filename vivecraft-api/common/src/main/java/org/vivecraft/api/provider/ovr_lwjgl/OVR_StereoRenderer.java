//package org.vivecraft.provider.ovr_lwjgl;
//
//import com.mojang.math.Matrix4f;
//import java.nio.Buffer;
//import java.nio.IntBuffer;
//import net.minecraft.util.Tuple;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.ovr.OVR;
//import org.lwjgl.ovr.OVRErrorInfo;
//import org.lwjgl.ovr.OVRGL;
//import org.lwjgl.ovr.OVRMatrix4f;
//import org.lwjgl.ovr.OVRRecti;
//import org.lwjgl.ovr.OVRSizei;
//import org.lwjgl.ovr.OVRTextureSwapChainDesc;
//import org.lwjgl.ovr.OVRUtil;
//import org.lwjgl.ovr.OVRVector2i;
//import org.lwjgl.ovr.OVRViewScaleDesc;
//import org.vivecraft.provider.MCVR;
//import org.vivecraft.provider.VRRenderer;
//import org.vivecraft.render.RenderConfigException;
//
//public class OVR_StereoRenderer extends VRRenderer
//{
//    private OVRSizei bufferSize;
//    private OVRMatrix4f projL;
//    private OVRMatrix4f projR;
//    private MC_OVR mcovr;
//    int ret;
//
//    public OVR_StereoRenderer(MCVR vr)
//    {
//        super(vr);
//        this.mcovr = (MC_OVR)vr;
//        this.projL = OVRMatrix4f.malloc();
//        this.projR = OVRMatrix4f.malloc();
//        this.bufferSize = OVRSizei.malloc();
//    }
//
//    private void checkret(int ret)
//    {
//        this.checkret(ret, "unspecified");
//    }
//
//    private void checkret(int ret, String desc)
//    {
//        if (ret != 0)
//        {
//            OVRErrorInfo ovrerrorinfo = OVRErrorInfo.malloc();
//            OVR.ovr_GetLastErrorInfo(ovrerrorinfo);
//            System.out.println("Oculus error in " + desc + " " + ovrerrorinfo.ErrorStringString());
//        }
//    }
//
//    public void createRenderTexture(int lwidth, int lheight)
//    {
//        this.mcovr.textureSwapChainL = PointerBuffer.allocateDirect(1);
//        this.mcovr.textureSwapChainR = PointerBuffer.allocateDirect(1);
//        OVRTextureSwapChainDesc ovrtextureswapchaindesc = OVRTextureSwapChainDesc.calloc();
//        ovrtextureswapchaindesc.set(0, 5, 1, this.bufferSize.w(), this.bufferSize.h(), 1, 1, false, ovrtextureswapchaindesc.MiscFlags(), ovrtextureswapchaindesc.BindFlags());
//        this.checkret(OVRGL.ovr_CreateTextureSwapChainGL(this.mcovr.session.get(0), ovrtextureswapchaindesc, this.mcovr.textureSwapChainL), "create l eye");
//        this.checkret(OVRGL.ovr_CreateTextureSwapChainGL(this.mcovr.session.get(0), ovrtextureswapchaindesc, this.mcovr.textureSwapChainR), "create r eye");
//        IntBuffer intbuffer = BufferUtils.createIntBuffer(1);
//        this.checkret(OVRGL.ovr_GetTextureSwapChainBufferGL(this.mcovr.session.get(0), this.mcovr.textureSwapChainL.get(0), 0, intbuffer), "create l chain");
//        this.LeftEyeTextureId = intbuffer.get();
//        ((Buffer)intbuffer).rewind();
//        this.checkret(OVRGL.ovr_GetTextureSwapChainBufferGL(this.mcovr.session.get(0), this.mcovr.textureSwapChainR.get(0), 0, intbuffer), "create r chain");
//        this.RightEyeTextureId = intbuffer.get();
//        this.mcovr.layer.Header().Type(1);
//        this.mcovr.layer.Header().Flags(0);
//        this.mcovr.layer.ColorTexture(0, this.mcovr.textureSwapChainL.get(0));
//        this.mcovr.layer.ColorTexture(1, this.mcovr.textureSwapChainR.get(0));
//        this.mcovr.layer.Fov(0, this.mcovr.eyeRenderDesc0.Fov());
//        this.mcovr.layer.Fov(1, this.mcovr.eyeRenderDesc1.Fov());
//        this.mcovr.layer.Viewport(0, createRecti(0, 0, this.bufferSize.w(), this.bufferSize.h()));
//        this.mcovr.layer.Viewport(1, createRecti(0, 0, this.bufferSize.w(), this.bufferSize.h()));
//        this.mcovr.layer.RenderPose(0, this.mcovr.hmdToEyeViewPose.get(0));
//        this.mcovr.layer.RenderPose(1, this.mcovr.hmdToEyeViewPose.get(1));
//    }
//
//    private static OVRRecti createRecti(int x, int y, int w, int h)
//    {
//        OVRVector2i ovrvector2i = OVRVector2i.malloc();
//        ovrvector2i.set(x, y);
//        OVRSizei ovrsizei = OVRSizei.malloc();
//        ovrsizei.set(w, h);
//        OVRRecti ovrrecti = OVRRecti.malloc();
//        ovrrecti.set(ovrvector2i, ovrsizei);
//        return ovrrecti;
//    }
//
//    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip)
//    {
//        if (eyeType == 0)
//        {
//            OVRUtil.ovrMatrix4f_Projection(this.mcovr.hmdDesc.DefaultEyeFov(0), nearClip, farClip, 0, this.projL);
//            return OVRUtils.ovrMatrix4ToMatrix4f(this.projL).toMCMatrix();
//        }
//        else
//        {
//            OVRUtil.ovrMatrix4f_Projection(this.mcovr.hmdDesc.DefaultEyeFov(1), nearClip, farClip, 0, this.projR);
//            return OVRUtils.ovrMatrix4ToMatrix4f(this.projR).toMCMatrix();
//        }
//    }
//
//    public void endFrame() throws RenderConfigException
//    {
//        OVR.ovr_CommitTextureSwapChain(this.mcovr.session.get(0), this.mcovr.textureSwapChainL.get(0));
//        OVR.ovr_CommitTextureSwapChain(this.mcovr.session.get(0), this.mcovr.textureSwapChainR.get(0));
//        PointerBuffer pointerbuffer = BufferUtils.createPointerBuffer(1);
//        pointerbuffer.put(this.mcovr.layer.address());
//        pointerbuffer.flip();
//        OVR.ovr_EndFrame(this.mcovr.session.get(0), 0L, (OVRViewScaleDesc)null, pointerbuffer);
//    }
//
//    public boolean providesStencilMask()
//    {
//        return false;
//    }
//
//    public Tuple<Integer, Integer> getRenderTextureSizes()
//    {
//        OVRSizei ovrsizei = OVRSizei.malloc();
//        OVR.ovr_GetFovTextureSize(this.mcovr.session.get(0), 0, this.mcovr.hmdDesc.DefaultEyeFov(0), 1.0F, ovrsizei);
//        OVRSizei ovrsizei1 = OVRSizei.malloc();
//        OVR.ovr_GetFovTextureSize(this.mcovr.session.get(0), 1, this.mcovr.hmdDesc.DefaultEyeFov(1), 1.0F, ovrsizei1);
//        int i = Math.max(ovrsizei.w(), ovrsizei1.w());
//        int j = Math.max(ovrsizei.h(), ovrsizei1.h());
//        this.bufferSize.w(i);
//        this.bufferSize.h(j);
//        return new Tuple<>(i, j);
//    }
//}
