package com.example.examplemod;

public interface RenderTargetExtension {

	String getName();

	void setBlitLegacy(boolean b);

	void blitToScreen(int i, int viewWidth, int viewHeight, int j, boolean b, float f, float g, boolean c);

	int getDepthBufferId();

}
