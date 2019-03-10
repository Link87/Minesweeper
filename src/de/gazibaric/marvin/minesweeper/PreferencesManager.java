package de.gazibaric.marvin.minesweeper;

import java.awt.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

// This file is part of Minesweeper, created on 17.12.2016 by (c) Marvin Gazibarić.
// You are generally not allowed to modify, distribute or use this code,
// when you do not have the explicit permission to do so.
// For questions about this, refer to the LICENSE.md file probably provided with the project files.

public class PreferencesManager {

    public enum WindowMode {FULLSCREEN, BORDERLESS, WINDOWED}

    private volatile Properties graphics;
    private volatile Font font;

    private static PreferencesManager manager;

    public static PreferencesManager getPreferencesManager() {
        if (manager == null) manager = new PreferencesManager();
        return manager;
    }

    private PreferencesManager() {
        graphics = new Properties();
    }

    public void load() throws IOException {
        try {
            graphics.loadFromXML(new BufferedInputStream(PreferencesManager.
                    class.getResourceAsStream("graphics.xml")));
            font = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(PreferencesManager.class.getResourceAsStream("font-boycott.ttf")));
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to load some files");
        }
    }

    public void store() throws IOException {
        try {
            graphics.storeToXML(new BufferedOutputStream(new FileOutputStream(new File(PreferencesManager.
                    class.getResource("graphics.xml").toURI()))), null);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new IOException("Failed to store preferences");
        }
    }

    public WindowMode getWindowMode() {
        String mode = graphics.getProperty("mode", "borderless");
        switch (mode) {
            case "fullscreen":
                return WindowMode.FULLSCREEN;
            case "windowed":
                return WindowMode.WINDOWED;
            default:
                return WindowMode.BORDERLESS;
        }
    }

    public Dimension getResolution() {
        return new Dimension(Integer.parseInt(graphics.getProperty("width", "800")),
                Integer.parseInt(graphics.getProperty("height", "600")));
    }

    public synchronized Map<RenderingHints.Key, Object> getRenderingHints() {
        Map<RenderingHints.Key, Object> hints = new Hashtable<>();
        switch ((String) graphics.get("antialiasing")) {
            case "on":
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                break;
            case "off":
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                break;
            default:
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        }
        switch ((String) graphics.get("quality")) {
            case "high":
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                break;
            case "low":
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                break;
            default:
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        }
        switch ((String) graphics.get("interpolation")) {
            case "bicubic":
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                break;
            case "neighbour":
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                break;
            default:
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        switch ((String) graphics.get("dither")) {
            case "enabled":
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                break;
            case "disabled":
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                break;
            default:
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
        }
        return hints;
    }

    public Font getFont() {
        return font;
    }

}
