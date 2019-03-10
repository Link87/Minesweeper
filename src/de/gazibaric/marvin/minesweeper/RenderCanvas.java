package de.gazibaric.marvin.minesweeper;

// This file is part of Minesweeper, created on 18.12.2016 by (c) Marvin Gazibarić.
// You are generally not allowed to modify, distribute or use this code,
// when you do not have the explicit permission to do so.
// For questions about this, refer to the LICENSE.md file probably provided with the project files.

import java.awt.*;
import java.awt.event.*;

public interface RenderCanvas extends MouseListener, MouseMotionListener, KeyListener {

    default void render(Graphics2D gx, int renderWidth, int renderHeight, Point mouse) {
        gx.setColor(Color.CYAN);
        gx.fillRect(0, 0, renderWidth, renderHeight);
    }

    int update(Point mouse);

    @Override
    default void mouseClicked(MouseEvent e) {}

    @Override
    default void mouseEntered(MouseEvent e) {}

    @Override
    default void mouseExited(MouseEvent e) {}

    @Override
    default void mousePressed(MouseEvent e) {}

    @Override
    default void mouseReleased(MouseEvent e) {}

    @Override
    default void mouseDragged(MouseEvent e) {}

    @Override
    default void mouseMoved(MouseEvent e) {}

    @Override
    default void keyPressed(KeyEvent e) {}

    @Override
    default void keyReleased(KeyEvent e) {}

    @Override
    default void keyTyped(KeyEvent e) {}
}
