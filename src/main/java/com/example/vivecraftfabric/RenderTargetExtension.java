package com.example.vivecraftfabric;

import org.lwjgl.opengl.GL30;

public interface RenderTargetExtension {

	String getName();

	void setBlitLegacy(boolean b);

	void blitToScreen(int i, int viewWidth, int viewHeight, int j, boolean b, float f, float g, boolean c);

	int getDepthBufferId();

	default void genMipMaps() {
		GL30.glGenerateMipmap(3553);
	}

	void setName(String name);

	void setTextid(int texid);


	void isLinearFilter(boolean linearFilter);

}
