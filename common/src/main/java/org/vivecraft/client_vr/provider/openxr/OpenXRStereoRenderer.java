package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class OpenXRStereoRenderer extends VRRenderer {
    private final MCOpenXR openxr;
    private int swapIndex;
    private VRTextureTarget[] leftFramebuffers;
    private VRTextureTarget[] rightFramebuffers;
    private PointerBuffer layers;
    private XrCompositionLayerProjectionView.Buffer projectionLayerViews;
    private long time;
    private boolean render;

    public OpenXRStereoRenderer(MCOpenXR vr) {
        super(vr);
        this.openxr = vr;
    }

    @Override
    public void createRenderTexture(int width, int height) throws RenderConfigException{
        try (MemoryStack stack = MemoryStack.stackPush()) {

            //Get amount of views in the swapchain
            IntBuffer intBuffer = stack.ints(0); //Set value to 0
            XR10.xrEnumerateSwapchainImages(openxr.swapchain, intBuffer, null);

            //Now we know the amount, create the image buffer
            int imageCount = intBuffer.get(0);
            XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = XrSwapchainImageOpenGLKHR.calloc(imageCount, stack);
            for (XrSwapchainImageOpenGLKHR image : swapchainImageBuffer) {
                image.type(KHROpenGLEnable.XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_KHR);
            }

            XR10.xrEnumerateSwapchainImages(openxr.swapchain, intBuffer, XrSwapchainImageBaseHeader.create(swapchainImageBuffer.address(), swapchainImageBuffer.capacity()));

            this.leftFramebuffers = new VRTextureTarget[imageCount];
            this.rightFramebuffers = new VRTextureTarget[imageCount];

            for (int i = 0; i < imageCount; i++) {
                XrSwapchainImageOpenGLKHR openxrImage = swapchainImageBuffer.get(i);
                leftFramebuffers[i] = new VRTextureTarget("L Eye " + i, width, height, openxrImage.image(), 0);
                this.checkGLError("Left Eye framebuffer setup");
                rightFramebuffers[i] = new VRTextureTarget("R Eye " + i, width, height, openxrImage.image(), 1);
                this.checkGLError("Right Eye framebuffer setup");
            }
        }
    }

    @Override
    public void setupRenderConfiguration(boolean render) throws Exception {
        super.setupRenderConfiguration(render);
        
        if (!render) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()){
            this.layers = stack.callocPointer(1);
            XrFrameState frameState = XrFrameState.calloc(stack).type(XR10.XR_TYPE_FRAME_STATE);

            //TODO tick game and poll input during xrWaitFrame (this might not work due to the gl context belonging to the xrWaitFrame thread)
            int i = XR10.xrWaitFrame(
                    openxr.session,
                    XrFrameWaitInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_WAIT_INFO),
                    frameState);

            this.time = frameState.predictedDisplayTime();
            this.render = frameState.shouldRender();

            if (i != 0) {
                System.out.println("error " + i);
            }

            i = XR10.xrBeginFrame(
                openxr.session,
                XrFrameBeginInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_BEGIN_INFO));

            if (i != 0) {
                System.out.println("error2 " + i);
            }

            if (!frameState.shouldRender()) {
                return;
            }

            XrViewState viewState = XrViewState.calloc(stack).type(XR10.XR_TYPE_VIEW_STATE);
            IntBuffer intBuf = stack.callocInt(1);

            XrViewLocateInfo viewLocateInfo = XrViewLocateInfo.calloc(stack);
            viewLocateInfo.set(XR10.XR_TYPE_VIEW_LOCATE_INFO,
                0,
                XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                frameState.predictedDisplayTime(),
                openxr.xrAppSpace
            );

            i = XR10.xrLocateViews(openxr.session, viewLocateInfo, viewState, intBuf, openxr.viewBuffer);

            if (i != 0) {
                System.out.println("error3 " + i);
            }

            this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2, stack);

            IntBuffer intBuf2 = stack.callocInt(1);

            XR10.xrAcquireSwapchainImage(
                openxr.swapchain,
                XrSwapchainImageAcquireInfo.calloc(stack).type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO),
                intBuf2);

            XR10.xrWaitSwapchainImage(openxr.swapchain,
                XrSwapchainImageWaitInfo.calloc(stack)
                    .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
                    .timeout(XR10.XR_INFINITE_DURATION));

            this.swapIndex = intBuf2.get(0);

            // Render view to the appropriate part of the swapchain image.
            for (int viewIndex = 0; viewIndex < 2; viewIndex++) {

                var subImage = projectionLayerViews.get(viewIndex)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW)
                    .pose(openxr.viewBuffer.get(viewIndex).pose())
                    .fov(openxr.viewBuffer.get(viewIndex).fov())
                    .subImage();
                subImage.swapchain(openxr.swapchain);
                subImage.imageRect().offset().set(0, 0);
                subImage.imageRect().extent().set(openxr.width, openxr.height);
                subImage.imageArrayIndex(viewIndex);

            }
        }
    }

    @Override
    public Matrix4f getProjectionMatrix(int var1, float var2, float var3) {
        return new Matrix4f();
    }

    @Override
    public void endFrame() throws RenderConfigException {
        try (MemoryStack stack = MemoryStack.stackPush()){

            if (render) {
                XR10.xrReleaseSwapchainImage(
                    openxr.swapchain,
                    XrSwapchainImageReleaseInfo.calloc(stack)
                        .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO));

                XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                    .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                    .space(openxr.xrAppSpace)
                    .views(projectionLayerViews);

                layers.put(compositionLayerProjection);
            }
            layers.flip();

            int i = XR10.xrEndFrame(
                openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(time)
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));

            if (i != XR10.XR_SUCCESS) {
                ByteBuffer str = stack.calloc(XR10.XR_MAX_RESULT_STRING_SIZE);

                if (XR10.xrResultToString(openxr.instance, i, str) >= 0) {
                    System.out.println(memUTF8(memAddress(str)));
                }
            }
        }
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public RenderTarget getLeftEyeTarget() {
        return leftFramebuffers[swapIndex];
    }

    @Override
    public RenderTarget getRightEyeTarget() {
        return rightFramebuffers[swapIndex];
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        return new Tuple<>(openxr.width, openxr.height);
    }
}
