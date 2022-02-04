package com.example.examplemod;

import net.minecraft.client.gui.components.AbstractWidget;

public interface ButtonExtension {

	Object getButtonList();

	AbstractWidget getSelectedButton(int pMouseX, int p_94738_, Object buttonList);

}
