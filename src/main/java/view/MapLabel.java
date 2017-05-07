package main.java.view;

import main.java.players.Detective;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MapLabel extends JLabel implements ActionListener {

    private static final int CIRCLE_R = 30;
    private Point[] playerPositions = null;

    private int currentPlayer = -1;
    boolean blinkOn = false;
    public boolean mrXVisible = false; /* Is Mr. X' Position revealed? */

    Timer blinkTimer = new Timer(1000, this);
    private int[] playerPos;

    public MapLabel(ImageIcon imageIcon, int numDetectives) {
        super(imageIcon);
        playerPositions = new Point[numDetectives + 1];
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (this.playerPositions != null) {
            Graphics2D g2 = (Graphics2D) g;

            for (int i = 0; i < playerPositions.length; i++) {
                if (playerPositions[i] != null && !(blinkOn && i == currentPlayer)
                        && !(i == 0 && !mrXVisible)) {

                    Point playerPos = getPlayerPos(i);

                    drawCenteredCircle(g2,playerPos.x,playerPos.y,CIRCLE_R, Detective.colors[i]);
                }
            }
        }
    }
    public void drawCenteredCircle(Graphics2D g, int x, int y, int r, Color color) {
        x = x-(r/2);
        y = y-(r/2);
        g.setStroke(new BasicStroke(10));
        g.setColor(color);
        g.drawOval(x,y,r,r);
    }

    public void setPlayerPos(int player, int x, int y) {
        this.playerPositions[player] = new Point(x, y);
        this.repaintPlayerPos(player);
    }

    public void setPlayerPos(int player, Point pos) {
        this.setPlayerPos(player, pos.x, pos.y);
    }

    public Point getPlayerPos(int player) {
        if (player < 0 || player >= playerPositions.length) {
            return null;
        }

        int xOffset = (this.getWidth() - this.getIcon().getIconWidth() < 0) ? 0
                : (this.getWidth() - this.getIcon().getIconWidth()) / 2;
        int yOffset = (this.getHeight() - this.getIcon().getIconHeight() < 0) ? 0 : (this
                .getHeight() - this.getIcon().getIconHeight()) / 2;

        return new Point(xOffset + playerPositions[player].x, yOffset + playerPositions[player].y);
    }

    public void setCurrentPlayer(int player) {
        if (player != currentPlayer) {
            /*
			 * Stop the blink timer and repaint the current player's position if
			 * it was currently not visible due to blinking.
			 */
            if (blinkTimer.isRunning()) {
                blinkTimer.stop();
            }

            blinkOn = false;
            if (currentPlayer != -1) {
                repaintPlayerPos(currentPlayer);
            }

			/* Change the current player and start the blinking timer */
            currentPlayer = player;
            blinkTimer.start();
            repaintPlayerPos(currentPlayer);
        }
    }

    public void repaintPlayerPos(int player) {
        Point playerPos = getPlayerPos(player);

        if (player != -1 && playerPos != null) {
            this.repaint(playerPos.x - CIRCLE_R, playerPos.y - CIRCLE_R , CIRCLE_R * 2, CIRCLE_R * 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == blinkTimer) {
            this.blinkOn = !this.blinkOn;

            if (this.playerPositions != null) {
                this.repaintPlayerPos(currentPlayer);
            }
        }
    }

    public void setPlayerPos(int[] playerPos) {
        this.playerPos = playerPos;
    }
}
