package org.vivecraft;

import javax.swing.JOptionPane;

public class LaunchPopup {
    public static void main(String[] args) {

        String text = "This version of Vivecraft is a mod, and doesn't have an installer.\nPlease place this file inside your mods folder.";

        JOptionPane.showMessageDialog(null, text);
    }
}
