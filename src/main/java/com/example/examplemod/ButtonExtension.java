package com.example.examplemod;

import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;

public interface ButtonExtension {

	List<AbstractWidget> getButtonList();

	AbstractWidget getSelectedButton(int pMouseX, int p_94738_, List<AbstractWidget> buttonList);

}
