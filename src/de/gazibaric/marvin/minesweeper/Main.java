package de.gazibaric.marvin.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;

public class Main extends JFrame implements Runnable {

    private final BufferStrategy bs;
    private final Object renderMutex = new Object();

    private boolean inGame = false;
    private Minefield minefield;
    private Menu menu;

    public static void main(String... args) {
        Main prog = new Main();
        prog.menu = new Menu();
        prog.registerListeners(prog.menu);

        new Thread(prog).run();

    }

    public Main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}
        SwingUtilities.updateComponentTreeUI(this);

        //Load stored properties and save them when the user has enough of this
        try {
            PreferencesManager.getPreferencesManager().load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Not able to load your preferences. " +
                    "Gonna use the default ones", "Ups, an error occured", JOptionPane.ERROR_MESSAGE);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                PreferencesManager.getPreferencesManager().store();
            } catch (IOException ignored) {}
        }));

        try {
            GameResources.getResources().load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Not able to load resources. " +
                    "Not able to proceed.", "A fatal error occured", JOptionPane.ERROR_MESSAGE);
            System.exit(30404);
        }

        this.setIconImage(null);//TODO - Add an icon

        this.setResizable(false);
        switch (PreferencesManager.getPreferencesManager().getWindowMode()) {
            case WINDOWED:
                this.setSize(PreferencesManager.getPreferencesManager().getResolution());
                this.setLocationRelativeTo(null);
                this.setAlwaysOnTop(true);
                System.out.println("WindowMode set to: windowed");
                break;
            case FULLSCREEN:
                if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
                    this.setUndecorated(true);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
                    System.out.println("WindowMode set to: fullscreen");
                    break;
                }
            default:
                this.setUndecorated(true);
                this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                System.out.println("WindowMode set to: borderless");
        }

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setIgnoreRepaint(true);
        this.setVisible(true);

        this.createBufferStrategy(2);
        this.bs = this.getBufferStrategy();
    }

    public void run() {
        while (true) {
            synchronized (this.renderMutex) {
                do {
                    Graphics2D gx = null;
                    try {
                        Point mouse = MouseInfo.getPointerInfo().getLocation();
                        SwingUtilities.convertPointFromScreen(mouse, this);
                        gx = (Graphics2D) this.bs.getDrawGraphics();
                        gx.setRenderingHints(PreferencesManager.getPreferencesManager().getRenderingHints());
                        if (inGame && minefield != null) {
                            int res = this.minefield.update(mouse);
                            if (minefield instanceof MinefieldDefault) {
                                if (res == MinefieldDefault.STATE_GONNA_QUIT) {
                                    inGame = false;
                                    menu = new Menu();
                                    registerListeners(menu);
                                } else if (res == MinefieldDefault.STATE_GONNA_RETRY) {
                                    inGame = true;
                                    unregisterListeners(minefield);
                                    minefield = new MinefieldDefault((MinefieldDefault) minefield);
                                    registerListeners(minefield);
                                }
                            }
                            this.minefield.render(gx, this.getWidth(), this.getHeight(), mouse);
                        }
                        else if (!inGame && menu != null) {
                            int res = this.menu.update(mouse);
                            if (res == Menu.STATE_START_GAME) {
                                inGame = true;
                                minefield = new MinefieldDefault(MinefieldDefault.getPresets()[menu.getPresetNr()]);
                                unregisterListeners(menu);
                                registerListeners(minefield);
                            }
                            if (res == Menu.STATE_START_HEXAGON) {
                                inGame = true;
                                minefield = new MinefieldHexagonal();
                                unregisterListeners(menu);
                                registerListeners(minefield);
                            }
                            else if (res == Menu.STATE_QUIT_GAME) Runtime.getRuntime().exit(0);
                            this.menu.render(gx, this.getWidth(), this.getHeight(), mouse);
                        }
                        else {
                            gx.setColor(Color.BLUE);
                            gx.drawRect(0, 0, this.getWidth(), this.getHeight());
                        }
                    }
                    finally {
                        if (gx != null)
                            gx.dispose();
                    }
                    bs.show();
                } while (this.bs.contentsLost());
            }
            try {
                Thread.sleep(10);
            }
            catch(InterruptedException ignored) {}
        }
    }

    private void unregisterListeners(RenderCanvas can) {
        removeMouseListener(can);
        removeMouseMotionListener(can);
        removeKeyListener(can);
    }
    private void registerListeners(RenderCanvas can) {
        addMouseListener(can);
        addMouseMotionListener(can);
        addKeyListener(can);
    }
}
