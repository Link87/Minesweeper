package de.gazibaric.marvin.minesweeper;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class GameResources {

    public static final String STRING_MENU_NORMAL = "menu_normal";
    public static final String STRING_MENU_HEXAGON = "menu_hexagon";
    public static final String STRING_MENU_PREFERENCES = "menu_preferences";
    public static final String STRING_GAME_OVER = "game_over";
    public static final String STRING_GAME_OVER_2ND = "game_over_2nd";
    public static final String STRING_YOU_WON = "you_won";
    public static final String STRING_YOU_WON_2ND = "you_won_2nd";
    public static final String STRING_QUIT = "quit";
    public static final String STRING_QUIT_GAME = "quit_game";
    public static final String STRING_RETRY = "retry";
    public static final String STRING_START_GAME = "start_game";
    public static final String STRING_FIELD_SIZE = "field_size";
    public static final String STRING_MINE_COUNT = "mine_count";
    public static final String STRING_NEXT_ARROW = "next_arrow";
    public static final String STRING_PREV_ARROW = "prev_arrow";
    public static final String STRING_PAUSED = "paused";
    public static final String STRING_ESC_TO_RESUME = "esc_to_resume";
    public static final String STRING_RESTART = "restart";

    private static final String STRING_CLICKED_DECORATION_LEFT = "clicked_decoration_left";
    private static final String STRING_CLICKED_DECORATION_RIGHT = "clicked_decoration_right";

    private static final String MAGIC_WORD = "4422";

    public static final String COLOR_BG = "color_bg";
    public static final String COLOR_ACC = "color_acc";
    public static final String COLOR_FG = "color_fg";

    public static final String COLOR_STH_NEARBY = "color_level_1";
    public static final String COLOR_BE_CAREFUL = "color_level_2";
    public static final String COLOR_BE_VERY_CAREFUL = "color_level_3";
    public static final String COLOR_DANGER = "color_level_4";
    public static final String COLOR_GREAT_DANGER = "color_level_5";
    public static final String COLOR_SURROUNDED_BY_MINES = "color_level_6";
    public static final String COLOR_THERE_IS_NO_ESCAPE = "color_level_7";
    public static final String COLOR_YOU_WILL_DIE_NOW = "color_level_8";

    private volatile Properties strings;
    private volatile Properties colors;

    private static GameResources resources;

    public static GameResources getResources() {
        if (resources == null) resources = new GameResources();
        return resources;
    }

    private GameResources() {
        strings = new Properties();
        colors = new Properties();
    }

    public void load() throws IOException {
        strings.loadFromXML(new BufferedInputStream(GameResources.class.getResourceAsStream("strings.xml")));
        colors.loadFromXML(new BufferedInputStream(GameResources.class.getResourceAsStream("color.xml")));
    }

    public String getStringResource(String key) {
        String s = strings.getProperty(key, "");
        try {
            if (s.split("-")[0].equals(MAGIC_WORD)) {
                Random rng = new Random();
                int i = rng.nextInt(Integer.parseInt(s.split("-")[1])) + 1;
                s = strings.getProperty(key + '-' + i);
            }
        }
        catch (Exception e) {
            return "";
        }
        return s;
    }

    public String getClickedStringDecoration(String s) {
        String left = strings.getProperty(STRING_CLICKED_DECORATION_LEFT, "");
        String right = strings.getProperty(STRING_CLICKED_DECORATION_RIGHT, "");
        return left + s + right;
    }

    public Color getColorResource(String key) {
        String s = colors.getProperty(key, "");
        return new Color(Integer.parseInt(s, 16));
    }

}
