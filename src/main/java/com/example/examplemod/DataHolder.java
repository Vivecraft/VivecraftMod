package com.example.examplemod;

import net.minecraft.client.Minecraft;

public class DataHolder {
	
	private static DataHolder INSTANCE = new DataHolder();
	private Minecraft mc = Minecraft.getInstance();
	public final String minecriftVerString = "Vivecraft 1.17.1  jrbudda-NONVR-1-b2";
	
	public static DataHolder getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DataHolder();
		}
		return INSTANCE;
	}
	

}
