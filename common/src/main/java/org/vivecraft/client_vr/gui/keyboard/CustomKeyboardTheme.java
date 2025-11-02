package org.vivecraft.client_vr.gui.keyboard;

import net.minecraft.client.Minecraft;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.RGBAColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CustomKeyboardTheme implements KeyboardTheme.IdTheme {

    private final Map<Integer, RGBAColor> keys = new HashMap<>();

    private final static RGBAColor DEFAULT = new RGBAColor(1F, 1F, 1F, 1F);

    @Override
    public void updateColor(RGBAColor color, int keyId) {
        color.setRGB(getColor(keyId));
    }

    @Override
    public void reload() {
        this.keys.clear();
        File themeFile = new File(Minecraft.getInstance().gameDirectory, "keyboardtheme.txt");
        if (!themeFile.exists()) {
            save();
        } else {
            // Load theme file
            try (Stream<String> lines = Files.lines(Paths.get(themeFile.toURI()), StandardCharsets.UTF_8)) {
                lines.forEach(line -> {
                    if (line.isEmpty() || line.charAt(0) == '#') {
                        return;
                    }
                    try {
                        String[] split = line.split("=", 2);
                        int id = Integer.parseInt(split[0]);
                        String[] colorSplit = split[1].split(",");
                        RGBAColor color = new RGBAColor(Integer.parseInt(colorSplit[0]),
                            Integer.parseInt(colorSplit[1]), Integer.parseInt(colorSplit[2]), 255);
                        this.keys.put(id, color);
                    } catch (Exception ex) {
                        VRSettings.LOGGER.error("Vivecraft: error reading keyboard theme line: {}:", line, ex);
                    }
                });
            } catch (IOException ex) {
                VRSettings.LOGGER.error("Vivecraft: error reading keyboard theme:", ex);
            }
        }
    }

    public void load(KeyboardTheme theme) {
        this.keys.clear();
        RGBAColor color = new RGBAColor(1F, 1F, 1F, 1F);
        int keyCount = KeyboardKeys.COLUMNS * KeyboardKeys.MAX_ROWS;
        VRSettings.KeyboardLayout dummy = new VRSettings.KeyboardLayout("", "", " ".repeat(keyCount),
            " ".repeat(keyCount));
        for (KeyboardKeys.Key key : KeyboardKeys.getRegularKeys(dummy, false, () -> {}).keys()) {
            theme.theme.updateColor(color, key.id(), key.x(), key.y());
            this.keys.put(key.id(), color.copy());
        }
        for (KeyboardKeys.Key key : KeyboardKeys.getRegularKeys(dummy, true, () -> {}).keys()) {
            theme.theme.updateColor(color, key.id(), key.x(), key.y());
            this.keys.put(key.id(), color.copy());
        }
        for (KeyboardKeys.Key key : KeyboardKeys.getSpecialKeys()) {
            theme.theme.updateColor(color, key.id(), key.x(), key.y());
            this.keys.put(key.id(), color.copy());
        }
    }

    public void save() {
        File themeFile = new File(Minecraft.getInstance().gameDirectory, "keyboardtheme.txt");
        // Write template theme file
        try (PrintWriter pw = new PrintWriter(new FileWriter(themeFile, StandardCharsets.UTF_8))) {
            VRSettings.KeyboardLayout current = ClientDataHolderVR.getInstance().vrSettings.getKeyboardLayout();
            char[] normalChars = current.regular().get().toCharArray();
            for (int i = 0; i < KeyboardKeys.COLUMNS * KeyboardKeys.MAX_ROWS; i++) {
                RGBAColor color = getColor(i);
                pw.println("# " + (i < normalChars.length ? normalChars[i] : " ") + " (Normal)");
                pw.println(i +
                    "=%d,%d,%d".formatted((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255)));
            }
            char[] shiftChars = current.shift().get().toCharArray();
            for (int i = 0; i < KeyboardKeys.COLUMNS * KeyboardKeys.MAX_ROWS; i++) {
                RGBAColor color = getColor(i + 500);
                pw.println("# " + (i < shiftChars.length ? shiftChars[i] : " ") + " (Shifted)");
                pw.println((i + 500) +
                    "=%d,%d,%d".formatted((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255)));
            }
            KeyboardKeys.getSpecialKeys().forEach(key -> {
                RGBAColor color = getColor(key.id());
                pw.println("# " + key.label());
                pw.println(key.id() +
                    "=%d,%d,%d".formatted((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255)));
            });
        } catch (IOException ex) {
            VRSettings.LOGGER.error("Vivecraft: error saving keyboard theme: ", ex);
        }
    }

    private RGBAColor getColor(int keyId) {
        return this.keys.getOrDefault(keyId, DEFAULT);
    }

    public void setColor(int keyId, RGBAColor color) {
        this.keys.put(keyId, color.copy());
    }
}
