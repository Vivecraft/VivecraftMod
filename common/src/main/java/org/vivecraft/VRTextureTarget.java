package org.vivecraft;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.vivecraft.extensions.RenderTargetExtension;

public class VRTextureTarget extends RenderTarget{

	public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter, boolean useStencil) {
		super(usedepth);
		RenderSystem.assertOnGameThreadOrInit();
		((RenderTargetExtension)this).setName(name);
		((RenderTargetExtension)this).setTextid(texid);
		((RenderTargetExtension)this).isLinearFilter(linearFilter);
		((RenderTargetExtension)this).setUseStencil(useStencil);
		this.resize(width, height, onMac);
		if (useStencil) {
			Xplat.enableRenderTargetStencil(this);
		}
		this.setClearColor(0, 0, 0, 0);
	}	
}
