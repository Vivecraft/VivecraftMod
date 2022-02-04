package com.example.examplemod;

public interface NewMinecraftExtension {

	public void preRender(boolean tick);
	
	public void doRender(boolean tick, long frameStartTime);
	
	public void posRender(boolean tick);
}
