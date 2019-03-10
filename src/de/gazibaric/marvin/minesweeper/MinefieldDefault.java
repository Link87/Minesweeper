package de.gazibaric.marvin.minesweeper;

// This file is part of Minesweeper, created on 17.12.2016 by (c) Marvin Gazibarić.
// You are generally not allowed to modify, distribute or use this code,
// when you do not have the explicit permission to do so.
// For questions about this, refer to the LICENSE.md file probably provided with the project files.

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class MinefieldDefault implements Minefield {

    public static final int STATE_IN_GAME = 0;
    public static final int STATE_GONNA_QUIT = -1;
    public static final int STATE_GONNA_RETRY = 1;

    private static final Preset[] presets = new Preset[] {
            new Preset("Easy", 7, 7, 7),
            new Preset("Medium", 18, 12, 25),
            new Preset("Hard", 18, 18, 65),
            new Preset("Very Hard", 25, 25, 150),
            new Preset("Extreme", 32, 32, 270),
            new Preset("Insane", 50, 50, 750) };

    private Field[][] fields;
    private Rectangle2D[][] rects;

    private Rectangle2D retryRect;
    private Rectangle2D quitRect;

    private final int width, height, mineCount;
    private boolean mouseClicked = false;
    private boolean mouseRightClicked = false;
    private boolean escClicked = false;
    private boolean minesSpread = false;
    private boolean gameOver = false; // does NOT actually mean that you have lost. The game has simply ended.
    private boolean win = false;
    private boolean paused = false;

    private String sndLine;
    private boolean focused1st = false;
    private boolean focused2nd = false;

    public MinefieldDefault(MinefieldDefault old) {
        this(old.width, old.height, old.mineCount);
    }

    public MinefieldDefault(Preset preset) {
        this(preset.width, preset.height, preset.mineCount);
    }

    private MinefieldDefault(int width, int height, int mineCount) {
        this.width = width;
        this.height = height;
        this.mineCount = mineCount;
        this.fields = new Field[width][height];
        this.rects = new Rectangle2D.Double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                fields[x][y] = new Field();
                rects[x][y] = new Rectangle2D.Double();
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x > 0)
                    fields[x][y].addAdjacent(fields[x - 1][y]);
                if (x > 0 && y < height - 1)
                    fields[x][y].addAdjacent(fields[x - 1][y + 1]);
                if (y < height - 1)
                    fields[x][y].addAdjacent(fields[x][y + 1]);
                if (x < width - 1 && y < height - 1)
                    fields[x][y].addAdjacent(fields[x + 1][y + 1]);
                if (x < width - 1)
                    fields[x][y].addAdjacent(fields[x + 1][y]);
                if (x < width - 1 && y > 0)
                    fields[x][y].addAdjacent(fields[x + 1][y - 1]);
                if (y > 0)
                    fields[x][y].addAdjacent(fields[x][y - 1]);
                if (x > 0 && y > 0)
                    fields[x][y].addAdjacent(fields[x - 1][y - 1]);
            }
        }
        this.retryRect = new Rectangle2D.Double();
        this.quitRect = new Rectangle2D.Double();
    }

    @Override
    public synchronized int update(Point mouse) {
        if (!mouseClicked && !mouseRightClicked && !escClicked) return STATE_IN_GAME;
        if (escClicked && !gameOver) {
            escClicked = false;
            paused = !paused;
            return STATE_IN_GAME;
        }
        escClicked = false;
        if (mouseRightClicked && !gameOver && !paused) {
            mouseRightClicked = false;
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    if (rects[x][y].contains(mouse))
                        fields[x][y].toggleMarked();
            return STATE_IN_GAME;
        }
        mouseClicked = false;
        mouseRightClicked = false;

        if (gameOver || paused) {
            if (focused1st) return STATE_GONNA_RETRY;
            if (focused2nd) return STATE_GONNA_QUIT;
            else return STATE_IN_GAME;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (rects[x][y].contains(mouse)) {
                    if (fields[x][y].isMarked()) {
                        fields[x][y].toggleMarked();
                        continue;
                    }
                    if (!minesSpread)
                        spreadMines(x, y);
                    if (fields[x][y].reveal()) {
                        gameOver = true;
                        win = false;
                        for (int i = 0; i < width; i++)
                            for (int j = 0; j < height; j++)
                                if (fields[i][j].isMine())
                                    fields[i][j].reveal();
                    }
                    else {
                        boolean done = true;
                        for (int i = 0; i < width; i++)
                            for (int j = 0; j < height; j++)
                                if (!fields[i][j].isMine() && !fields[i][j].isRevealed())
                                    done = false;
                        if (done) {
                            win = true;
                            gameOver = true;
                        }
                    }
                }
            }
        }

        return STATE_IN_GAME;
    }

    @Override
    public synchronized void render(final Graphics2D gx, final int renderWidth, final int renderHeight, final Point mouse) {
        gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_BG));
        gx.fillRect(0, 0, renderWidth, renderHeight);

        //Unterteilung der Fläche in ein Raster zur gleichmäßigen Anordnung aller Quadrate
        final int p;
        final float s, xoffset, yoffset;

        if ((float) renderWidth / width > (float) renderHeight / height) {
            p = height * 6 + 3;
            s = (float) renderHeight / p;
            xoffset = (renderWidth - s * p) / 2;
            yoffset = 0;
        }
        else {
            p = width * 6 + 3;
            s = (float) renderWidth / p;
            xoffset = 0;
            yoffset = (renderHeight - s * p) / 2;
        }

        final float mouseScale = s / 25;

        gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                rects[x][y].setRect((int) (xoffset + x * s * 6 + s * 2),
                        (int) (yoffset + y * s * 6 + s * 2), (int) (s * 5), (int) (s * 5));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_ACC));
                if (!fields[x][y].isRevealed() && rects[x][y].contains(mouse) && !fields[x][y].isMarked() && !gameOver && !paused)
                    gx.fillRect((int) (rects[x][y].getX() - mouseScale), (int) (rects[x][y].getY() - mouseScale),
                            (int) (rects[x][y].getWidth() - mouseScale), (int) (rects[x][y].getHeight() - mouseScale));
                else if (!fields[x][y].isRevealed()) {
                    gx.fill(rects[x][y]);
                    if (fields[x][y].isMarked()) {
                        gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_BG));
                        gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
                        final double ratio = rects[x][y].getWidth() /
                                gx.getFontMetrics().getStringBounds("X", gx).getWidth() / 2;
                        gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
                        gx.drawString("X", (float) (rects[x][y].getX() + rects[x][y].getWidth() / 2 -
                                        gx.getFontMetrics().getStringBounds("X", gx).getCenterX()),
                                (float) (rects[x][y].getY() + rects[x][y].getHeight() / 2 -
                                        gx.getFontMetrics().getStringBounds("X", gx).getCenterY()));
                    }
                }
                else {
                    String num;
                    Color col = GameResources.getResources().getColorResource(GameResources.COLOR_FG);
                    if (fields[x][y].isMine()) num = "X";
                    else {
                        int n = fields[x][y].getAdajacentMineCount();
                        num = n > 0 ? Integer.toString(n) : "";
                        switch(n) {
                            case 1:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_STH_NEARBY);
                                break;
                            case 2:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_BE_CAREFUL);
                                break;
                            case 3:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_BE_VERY_CAREFUL);
                                break;
                            case 4:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_DANGER);
                                break;
                            case 5:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_GREAT_DANGER);
                                break;
                            case 6:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_SURROUNDED_BY_MINES);
                                break;
                            case 7:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_THERE_IS_NO_ESCAPE);
                                break;
                            case 8:
                                col = GameResources.getResources().getColorResource(GameResources.COLOR_YOU_WILL_DIE_NOW);
                                break;
                        }
                    }

                    gx.setColor(col);
                    gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
                    final double ratio = rects[x][y].getWidth() /
                            gx.getFontMetrics().getStringBounds("A", gx).getWidth() / 2;
                    gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
                    gx.drawString(num, (float) (rects[x][y].getX() + rects[x][y].getWidth() / 2 -
                                    gx.getFontMetrics().getStringBounds(num, gx).getCenterX()),
                            (float) (rects[x][y].getY() + rects[x][y].getHeight() / 2 -
                                    gx.getFontMetrics().getStringBounds(num, gx).getCenterY()));
                }

            }
        }

        if (paused) {
            gx.setColor(new Color(0xDD000000, true));
            gx.fillRect(0, 0, renderWidth, renderHeight);

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_FG));
            String fstLine = GameResources.getResources().getStringResource(GameResources.STRING_PAUSED);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            double ratio = renderWidth / gx.getFontMetrics().getStringBounds(fstLine, gx).getWidth() / 3;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(fstLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(fstLine, gx).getCenterX()),
                    (float) (renderHeight / 2 - gx.getFontMetrics().getStringBounds(fstLine, gx).getHeight()));

            if (sndLine == null)
                sndLine = GameResources.getResources().getStringResource(GameResources.STRING_ESC_TO_RESUME);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = renderWidth / gx.getFontMetrics().getStringBounds(sndLine, gx).getWidth() / 3;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(sndLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(sndLine, gx).getCenterX()),
                    (float) (renderHeight / 2 + gx.getFontMetrics().getStringBounds(sndLine, gx).getCenterY()));

            double buttonW = renderWidth / 4, buttonH = renderHeight / 5;
            retryRect.setRect(renderWidth / 2 - renderWidth / 8 - buttonW, renderHeight / 3 * 2, buttonW, buttonH);
            quitRect.setRect(renderWidth / 2 + renderWidth / 8, renderHeight / 3 * 2, buttonW, buttonH);

            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = retryRect.getWidth() / gx.getFontMetrics().
                    getStringBounds(GameResources.getResources().getStringResource(GameResources.STRING_RESTART), gx).getWidth() / 1.2;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            String retryString = focused1st ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_RESTART)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_RESTART);
            gx.drawString(retryString, (float) (retryRect.getX() + retryRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterX()),
                    (float) (retryRect.getY() + retryRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterY()));
            String quitString = focused2nd ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_QUIT)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_QUIT);
            gx.drawString(quitString, (float) (quitRect.getX() + quitRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterX()),
                    (float) (quitRect.getY() + quitRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterY()));
            return;
        }

        if (gameOver && !win) {
            gx.setColor(new Color(0xDD000000, true));
            gx.fillRect(0, 0, renderWidth, renderHeight);

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_FG));
            String fstLine = GameResources.getResources().getStringResource(GameResources.STRING_GAME_OVER);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            double ratio = renderWidth / gx.getFontMetrics().getStringBounds(fstLine, gx).getWidth() / 2;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(fstLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(fstLine, gx).getCenterX()),
                    (float) (renderHeight / 2 - gx.getFontMetrics().getStringBounds(fstLine, gx).getHeight()));

            if (sndLine == null)
                sndLine = GameResources.getResources().getStringResource(GameResources.STRING_GAME_OVER_2ND);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = renderWidth / gx.getFontMetrics().getStringBounds(sndLine, gx).getWidth() / 2;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(sndLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(sndLine, gx).getCenterX()),
                    (float) (renderHeight / 2 + gx.getFontMetrics().getStringBounds(sndLine, gx).getCenterY()));

            double buttonW = renderWidth / 4, buttonH = renderHeight / 5;
            retryRect.setRect(renderWidth / 2 - renderWidth / 8 - buttonW, renderHeight / 3 * 2, buttonW, buttonH);
            quitRect.setRect(renderWidth / 2 + renderWidth / 8, renderHeight / 3 * 2, buttonW, buttonH);

            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = retryRect.getWidth() / gx.getFontMetrics().
                    getStringBounds(GameResources.getResources().getStringResource(GameResources.STRING_RETRY), gx).getWidth() / 1.2;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            String retryString = focused1st ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_RETRY)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_RETRY);
            gx.drawString(retryString, (float) (retryRect.getX() + retryRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterX()),
                    (float) (retryRect.getY() + retryRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterY()));
            String quitString = focused2nd ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_QUIT)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_QUIT);
            gx.drawString(quitString, (float) (quitRect.getX() + quitRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterX()),
                    (float) (quitRect.getY() + quitRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterY()));

            return;
        }
        else if (gameOver) {
            gx.setColor(new Color(0xDD000000, true));
            gx.fillRect(0, 0, renderWidth, renderHeight);

            gx.setColor(GameResources.getResources().getColorResource(GameResources.COLOR_FG));
            String fstLine = GameResources.getResources().getStringResource(GameResources.STRING_YOU_WON);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            double ratio = renderWidth / gx.getFontMetrics().getStringBounds(fstLine, gx).getWidth() / 3;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(fstLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(fstLine, gx).getCenterX()),
                    (float) (renderHeight / 2 - gx.getFontMetrics().getStringBounds(fstLine, gx).getHeight()));

            if (sndLine == null)
                sndLine = GameResources.getResources().getStringResource(GameResources.STRING_YOU_WON_2ND);
            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = renderWidth / gx.getFontMetrics().getStringBounds(sndLine, gx).getWidth() / 3;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            gx.drawString(sndLine, (float) (renderWidth / 2 -
                            gx.getFontMetrics(). getStringBounds(sndLine, gx).getCenterX()),
                    (float) (renderHeight / 2 + gx.getFontMetrics().getStringBounds(sndLine, gx).getCenterY()));

            double buttonW = renderWidth / 4, buttonH = renderHeight / 5;
            retryRect.setRect(renderWidth / 2 - renderWidth / 8 - buttonW, renderHeight / 3 * 2, buttonW, buttonH);
            quitRect.setRect(renderWidth / 2 + renderWidth / 8, renderHeight / 3 * 2, buttonW, buttonH);

            gx.setFont(PreferencesManager.getPreferencesManager().getFont().deriveFont(Font.BOLD, 12));
            ratio = retryRect.getWidth() / gx.getFontMetrics().
                    getStringBounds(GameResources.getResources().getStringResource(GameResources.STRING_RETRY), gx).getWidth() / 1.2;
            gx.setFont(gx.getFont().deriveFont(12 * (float) ratio));
            String retryString = focused1st ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_RETRY)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_RETRY);
            gx.drawString(retryString, (float) (retryRect.getX() + retryRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterX()),
                    (float) (retryRect.getY() + retryRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(retryString, gx).getCenterY()));
            String quitString = focused2nd ? GameResources.getResources().getClickedStringDecoration(GameResources.getResources().getStringResource(GameResources.STRING_QUIT)) :
                    GameResources.getResources().getStringResource(GameResources.STRING_QUIT);
            gx.drawString(quitString, (float) (quitRect.getX() + quitRect.getWidth() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterX()),
                    (float) (quitRect.getY() + quitRect.getHeight() / 2 - gx.getFontMetrics().
                            getStringBounds(quitString, gx).getCenterY()));
        }
    }

    private void spreadMines (final int x, final int y) {
        minesSpread = true;
        List<Field> unminedFields = new Vector<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i >= x - 1 && i <= x + 1 && j >= y - 1 && j <= y + 1) continue;
                unminedFields.add(fields[i][j]);
            }
        }
        assert mineCount < unminedFields.size() : "Too many mines";
        Random rng = new Random();
        for (int m = mineCount; m > 0; m--) {
            int pos = rng.nextInt(unminedFields.size());
            unminedFields.get(pos).setMine(true);
            unminedFields.remove(pos);
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            mouseClicked = true;
        if (e.getButton() == MouseEvent.BUTTON3)
            mouseRightClicked = true;
    }

    @Override
    public synchronized void mouseMoved(MouseEvent e) {
        focused1st = retryRect.contains(e.getPoint());
        focused2nd = quitRect.contains(e.getPoint());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            escClicked = true;
    }

    public static Preset[] getPresets() {
        return presets;
    }

    public static final class Preset {
        public final String name;
        public final int width;
        public final int height;
        public final int mineCount;

        public Preset(String name, int width, int height, int mineCount) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.mineCount = mineCount;
        }
    }

}
