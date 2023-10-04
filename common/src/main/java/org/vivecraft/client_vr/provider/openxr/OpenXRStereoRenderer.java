package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;

import java.nio.IntBuffer;

public class OpenXRStereoRenderer extends VRRenderer {
    private final MCOpenXR openxr;
    private int swapIndex;
    private VRTextureTarget[] leftFramebuffers;
    private VRTextureTarget[] rightFramebuffers;
    private PointerBuffer layers;
    private XrFrameState frameState;

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
                leftFramebuffers[i] = new VRTextureTarget("L Eye " + i, width, height, openxrImage.image(), i);
                rightFramebuffers[i] = new VRTextureTarget("R Eye " + i, width, height, openxrImage.image(), i);
            }
        }
    }

    @Override
    public void setupRenderConfiguration() throws Exception {
        super.setupRenderConfiguration();

        try (MemoryStack stack = MemoryStack.stackPush()){
            this.frameState = XrFrameState.calloc(stack).type(XR10.XR_TYPE_FRAME_STATE);

            GLFW.glfwSwapBuffers(Minecraft.getInstance().getWindow().getWindow());
            //TODO tick game and poll input during xrWaitFrame (this might not work due to the gl context belonging to the xrWaitFrame thread)
            XR10.xrWaitFrame(
                    openxr.session,
                    XrFrameWaitInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_WAIT_INFO),
                    frameState);

            XR10.xrBeginFrame(
                openxr.session,
                XrFrameBeginInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_BEGIN_INFO));

            XrViewState viewState = XrViewState.calloc(stack).type(XR10.XR_TYPE_VIEW_STATE);
            IntBuffer intBuf = stack.callocInt(1);

            XrViewLocateInfo viewLocateInfo = XrViewLocateInfo.calloc(stack);
            viewLocateInfo.set(XR10.XR_TYPE_VIEW_LOCATE_INFO,
                0,
                XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                frameState.predictedDisplayTime(),
                openxr.xrAppSpace
            );

            XR10.xrLocateViews(openxr.session, viewLocateInfo, viewState, intBuf, openxr.viewBuffer);

            var projectionLayerViews = XrCompositionLayerProjectionView.calloc(2, stack);

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
                subImage.imageRect().extent().set(openxr.viewConfig.recommendedImageRectWidth(), openxr.viewConfig.recommendedImageRectHeight());
                subImage.imageArrayIndex(viewIndex);

            }

            XR10.xrReleaseSwapchainImage(
                openxr.swapchain,
                XrSwapchainImageReleaseInfo.calloc(stack)
                    .type(XR10.XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO));

            this.layers = stack.callocPointer(1);

            XrCompositionLayerProjection compositionLayerProjection = XrCompositionLayerProjection.calloc(stack)
                .type(XR10.XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                .space(openxr.xrAppSpace)
                .views(projectionLayerViews);

            layers.put(compositionLayerProjection);
            layers.flip();

        }
    }

    @Override
    public Matrix4f getProjectionMatrix(int var1, float var2, float var3) {
        return new Matrix4f();
    }

    @Override
    public void endFrame() throws RenderConfigException {
        try (MemoryStack stack = MemoryStack.stackPush()){
            XR10.xrEndFrame(
                openxr.session,
                XrFrameEndInfo.calloc(stack)
                    .type(XR10.XR_TYPE_FRAME_END_INFO)
                    .displayTime(frameState.predictedDisplayTime())
                    .environmentBlendMode(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                    .layers(layers));
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
        return new Tuple<>(openxr.viewConfig.recommendedImageRectHeight(), openxr.viewConfig.recommendedImageRectWidth());
    }
}
