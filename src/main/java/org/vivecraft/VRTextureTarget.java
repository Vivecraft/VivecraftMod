package org.vivecraft;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public class VRTextureTarget extends RenderTarget{

	public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter) {
		super(usedepth);
		RenderSystem.assertOnGameThreadOrInit();
		((RenderTargetExtension)this).setName(name);
		((RenderTargetExtension)this).setTextid(texid);
		((RenderTargetExtension)this).isLinearFilter(linearFilter);
		this.resize(width, height, onMac);
		this.setClearColor(0, 0, 0, 0);
	}	
}
