package de.gazibaric.marvin.minesweeper;

// This file is part of Minesweeper, created on 17.12.2016 by (c) Marvin Gazibarić.
// You are generally not allowed to modify, distribute or use this code,
// when you do not have the explicit permission to do so.
// For questions about this, refer to the LICENSE.md file probably provided with the project files.

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class Menu implements RenderCanvas {

    public static final int STATE_DEF = 0;
    public static final int STATE_START_GAME = 1;
    public static final int STATE_OPEN_PREFERENCES = -1;
    public static final int STATE_QUIT_GAME = -2;
    public static final int STATE_START_HEXAGON = 6;

    private static final int ITEM_NORMAL = 0;
    private static final int ITEM_NORMAL_SELECT_DIFFICULTY = 1;
    private static final int ITEM_NORMAL_START = 2;
    private static final int ITEM_NORMAL_BACK = 3;
    private static final int ITEM_NORMAL_NEXT = 4;
    private static final int ITEM_NORMAL_PREV = 5;
    private static final int ITEM_PREFERENCES = -1;
    private static final int ITEM_QUIT_GAME = -2;
    private static final int ITEM_HEXAGON = 60;

    private int state;
    private final int INT_STATE_MAIN = 0;
    private final int INT_STATE_NORMAL_SELECTION = 1;

    private volatile Rectangle2D itemNormal;
    private volatile Rectangle2D itemHexagon;
    private volatile Rectangle2D itemQuitGame;
    private volatile Rectangle2D itemChooseDifficulty;
    private volatile Rectangle2D itemStart;
    private volatile Rectangle2D itemBack;
    private volatile Rectangle2D itemNext;
    private volatile Rectangle2D itemPrev;

    private volatile boolean startFocused = false;
    private volatile boolean backFocused = false;
    private volatile boolean nextFocused = false;
    private volatile boolean prevFocused = false;
    private volatile int presetNr = 0;

    private volatile boolean mousePressed = false;

    public Menu() {
        itemNormal = new Rectangle2D.Double();
        itemHexagon = new Rectangle2D.Double();
        //itemPref = new Rectangle2D.Double();
        itemQuitGame = new Rectangle2D.Double();
        itemChooseDifficulty = new Rectangle2D.Double();
        itemStart = new Rectangle2D.Double();
        itemBack = new Rectangle2D.Double();
        itemNext = new Rectangle2D.Double();
        itemPrev = new Rectangle2D.Double();

        state = INT_STATE_MAIN;

    }

    @Override
    public synchronized int update(Point mouse) {
        if (!mousePressed)
            return STATE_DEF;

        mousePressed = false;
        if (state == INT_STATE_MAIN) {
            if (itemNormal.contains(mouse)) {
                state = INT_STATE_NORMAL_SELECTION;
                return STATE_DEF;
            }
            if (itemQuitGame.contains(mouse))
                return STATE_QUIT_GAME;
            //if (itemPref.contains(mouse))
            //    return STATE_OPEN_PREFERENCES;
            if (itemHexagon.contains(mouse))
                return STATE_DEF;//STATE_START_HEXAGON;
        }
        else if (state == INT_STATE_NORMAL_SELECTION) {
            if (itemStart.contains(mouse))
                return STATE_START_GAME;
            if (itemBack.contains(mouse)) {
                state = INT_STATE_MAIN;
                return STATE_DEF;
            }
            if (itemPrev.contains(mouse) && presetNr > 0)
                presetNr--;
            if (itemNext.contains(mouse) && presetNr < MinefieldDefault.getPresets().length - 1)
                presetNr++;
        }
        return STATE_DEF;
    }

    @Override
    public synchronized void render(Graphics2D gx, int renderWidth, int renderHeight, Point mouse) {
        gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_BG));
        gx.fillRect(0, 0, renderWidth, renderHeight);

        if (state == INT_STATE_MAIN) {
            final int itemAspectRatio = 3;
            final int itemHeight = renderHeight / 6, itemWidth = itemHeight * itemAspectRatio;
            final int itemMargin = renderHeight / 12;
            final int mouseScale = itemWidth / 25;

            final int itemOffsetX = (renderWidth - itemWidth) / 2, itemOffsetY = (renderHeight - itemHeight * 3 - itemMargin * 2) / 2;

            itemNormal.setRect(itemOffsetX, itemOffsetY,
                    itemWidth, itemHeight);
            itemHexagon.setRect(itemOffsetX, itemOffsetY + itemHeight + itemMargin,
                    itemWidth, itemHeight);
            //itemPref.setRect(itemOffsetX, itemOffsetY + itemHeight * 2 + itemMargin * 2,
            //        itemWidth, itemHeight);
            itemQuitGame.setRect(itemOffsetX, itemOffsetY + itemHeight * 2 + itemMargin * 2,
                    itemWidth, itemHeight);

            if (itemNormal.contains(mouse))
                this.drawItem(ITEM_NORMAL, gx, new Rectangle2D.Double(
                        itemNormal.getX() - mouseScale, itemNormal.getY() - mouseScale,
                        itemNormal.getWidth() + 2 * mouseScale, itemNormal.getHeight() + 2 * mouseScale));
            else this.drawItem(ITEM_NORMAL, gx, itemNormal);

            //if (itemHexagon.contains(mouse))
            //    this.drawItem(ITEM_HEXAGON, gx, new Rectangle2D.Double(
            //            itemHexagon.getX() - mouseScale, itemHexagon.getY() - mouseScale,
            //            itemHexagon.getWidth() + 2 * mouseScale, itemHexagon.getHeight() + 2 * mouseScale));
            /*else*/ this.drawItem(ITEM_HEXAGON, gx, itemHexagon);

            /*if (itemPref.contains(mouse))
                this.drawItem(ITEM_PREFERENCES, gx, new Rectangle2D.Double(
                        itemPref.getX() - mouseScale, itemPref.getY() - mouseScale,
                        itemPref.getWidth() + 2 * mouseScale, itemPref.getHeight() + 2 * mouseScale));
            else
                this.drawItem(ITEM_PREFERENCES, gx, itemPref);*/

            if (itemQuitGame.contains(mouse))
                this.drawItem(ITEM_QUIT_GAME, gx, new Rectangle2D.Double(
                        itemQuitGame.getX() - mouseScale, itemQuitGame.getY() - mouseScale,
                        itemQuitGame.getWidth() + 2 * mouseScale, itemQuitGame.getHeight() + 2 * mouseScale));
            else
                this.drawItem(ITEM_QUIT_GAME, gx, itemQuitGame);
        }
        if (state == INT_STATE_NORMAL_SELECTION) {
            final float aspectRatio = 1.5f;
            final int chooserWidth = renderWidth / 2, chooserHeight = (int) (chooserWidth / aspectRatio);
            final int buttonWidth = renderWidth / 5, buttonHeight = buttonWidth / 2;
            final int buttonMargin = renderWidth / 8;
            final int arrowSize = renderWidth / 10;
            final int mouseScale = arrowSize / 25;

            itemChooseDifficulty.setRect(renderWidth / 2 - chooserWidth / 2,
                    renderHeight / 3 - chooserHeight / 2, chooserWidth, chooserHeight);

            itemBack.setRect(renderWidth / 2 - buttonMargin - buttonWidth,
                    renderHeight / 2  + buttonMargin, buttonWidth, buttonHeight);
            itemStart.setRect(renderWidth / 2 + buttonMargin,
                    renderHeight / 2 + buttonMargin, buttonWidth, buttonHeight);
            itemNext.setRect(renderWidth / 2 + chooserWidth / 2,
                    renderHeight / 3 - chooserHeight / 4, arrowSize, arrowSize);
            itemPrev.setRect(renderWidth / 2 - chooserWidth / 2 - arrowSize,
                    renderHeight / 3 - chooserHeight / 4, arrowSize, arrowSize);

            drawItem(ITEM_NORMAL_SELECT_DIFFICULTY, gx, itemChooseDifficulty);
            drawItem(ITEM_NORMAL_START, gx, itemStart);
            drawItem(ITEM_NORMAL_BACK, gx, itemBack);
            if (presetNr < MinefieldDefault.getPresets().length - 1) {
                if (itemNext.contains(mouse))
                    drawItem(ITEM_NORMAL_NEXT, gx, new Rectangle2D.Double(itemNext.getX() - mouseScale,
                            itemNext.getY() - mouseScale, itemNext.getWidth() + 2 * mouseScale, itemNext.getHeight() + 2 * mouseScale));
                else
                    drawItem(ITEM_NORMAL_NEXT, gx, itemNext);
            }
            if (presetNr > 0) {
                if (itemPrev.contains(mouse))
                    drawItem(ITEM_NORMAL_PREV, gx, new Rectangle2D.Double(itemPrev.getX() - mouseScale,
                            itemPrev.getY() - mouseScale, itemPrev.getWidth() + 2 * mouseScale, itemPrev.getHeight() + 2 * mouseScale));
                else
                    drawItem(ITEM_NORMAL_PREV, gx, itemPrev);
            }
        }
    }

    private void drawItem(final int itemNr, final Graphics2D gx, final Rectangle2D rect) {
        gx.setColor(Color.RED);
        //gx.draw(rect);

        if (itemNr == ITEM_NORMAL) {
            final int rectW = 0;//height / 8;
            final int margin = 0;//(int) (rectW * 0.25);
            final int offset = 0;//(height - 2 * rectW - margin) / 2;

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_MENU_NORMAL);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = (rect.getWidth() - 2 * rectW - 2 * margin) /
                    gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() +  2 * rectW + 2 * margin),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }

        else if (itemNr == ITEM_HEXAGON) {
            final int rectW = 0;//height / 5;
            final int margin = 0;//(int) (rectW * 0.25);
            final int offset = 0;//(height - 2 * rectW - margin) / 2;

            gx.setColor(Color.GRAY);
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_MENU_HEXAGON);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = (rect.getWidth() - 2 * rectW - 2 * margin) /
                    gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() +  2 * rectW + 2 * margin),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }

        else if (itemNr == ITEM_PREFERENCES) {
            final int rectW = 0;//height / 5;
            final int margin = 0;//(int) (rectW * 0.25);
            final int offset = 0;//(height - 2 * rectW - margin) / 2;

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_MENU_PREFERENCES);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = (rect.getWidth() - 2 * rectW - 2 * margin) /
                    gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() +  2 * rectW + 2 * margin),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }

        else if (itemNr == ITEM_QUIT_GAME) {
            final int rectW = 0;//height / 5;
            final int margin = 0;//(int) (rectW * 0.25);
            final int offset = 0;//(height - 2 * rectW - margin) / 2;

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_QUIT_GAME);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = (rect.getWidth() - 2 * rectW - 2 * margin) /
                    gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() +  2 * rectW + 2 * margin),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }

        else if (itemNr == ITEM_NORMAL_START) {
            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            String s = GameResources.getResources().getStringResource(GameResources.STRING_START_GAME);
            final String sRef = GameResources.getResources().getStringResource(GameResources.STRING_START_GAME);
            if (startFocused) s = GameResources.getResources().getClickedStringDecoration(s);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = rect.getWidth() / gx.getFontMetrics().getStringBounds(sRef, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterX()),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }
        else if (itemNr == ITEM_NORMAL_BACK) {
            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            String s = GameResources.getResources().getStringResource(GameResources.STRING_QUIT);
            final String sRef = GameResources.getResources().getStringResource(GameResources.STRING_START_GAME);
            if (backFocused) s = GameResources.getResources().getClickedStringDecoration(s);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = rect.getWidth() / gx.getFontMetrics().getStringBounds(sRef, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterX()),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(sRef, gx).getCenterY()));
        }
        else if (itemNr == ITEM_NORMAL_SELECT_DIFFICULTY) {
            presetNr %= MinefieldDefault.getPresets().length;
            MinefieldDefault.Preset preset = MinefieldDefault.getPresets()[presetNr];

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            String s = preset.name;
            final String sRef = "12345678910";
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = rect.getWidth() / gx.getFontMetrics().getStringBounds(sRef, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterX()),
                    (float) (rect.getY() + (float) rect.getHeight() / 2.5f - gx.getFontMetrics().getStringBounds(sRef, gx).getCenterY()));
            final String sInfo = GameResources.getResources().getStringResource(GameResources.STRING_FIELD_SIZE)
                    + preset.width + "x" + preset.height + "  " + GameResources.getResources().getStringResource(GameResources.STRING_MINE_COUNT) + preset.mineCount;
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio2 = rect.getWidth() / gx.getFontMetrics().getStringBounds(sInfo, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio2));
            gx.drawString(sInfo, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(sInfo, gx).getCenterX()),
                    (float) (rect.getY() + (float) rect.getHeight() / 1.35f - gx.getFontMetrics().getStringBounds(sInfo, gx).getCenterY()));
        }

        else if (itemNr == ITEM_NORMAL_NEXT) {
            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_NEXT_ARROW);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = rect.getWidth() / gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterX()),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }
        else if (itemNr == ITEM_NORMAL_PREV) {
            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
            final String s = GameResources.getResources().getStringResource(GameResources.STRING_PREV_ARROW);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            final double ratio = rect.getWidth() / gx.getFontMetrics().getStringBounds(s, gx).getWidth();
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(s, (float) (rect.getX() + rect.getWidth() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterX()),
                    (float) (rect.getY() + rect.getHeight() / 2 - gx.getFontMetrics().getStringBounds(s, gx).getCenterY()));
        }
    }

    public synchronized int getPresetNr() {
        return presetNr;
    }

    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            mousePressed = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (state == INT_STATE_NORMAL_SELECTION) {
            startFocused = itemStart.contains(e.getPoint());
            backFocused = itemBack.contains(e.getPoint());
        }
    }
}
