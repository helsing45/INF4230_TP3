package main.java.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapLabel extends JLabel implements ActionListener {

    private static final long serialVersionUID = 2747458994628364853L;
    private static final int NO_OF_DETECTIVES = 5;

    private BufferedImage flagImage[];

    private Point[] playerPositions = null;

    private int currentPlayer = -1;
    boolean blinkOn = false;
    public boolean mrXVisible = false; /* Is Mr. X' Position revealed? */

    Timer blinkTimer = new Timer(1000, this);

    public MapLabel(ImageIcon imageIcon, int numDetectives) {
        super(imageIcon);
        playerPositions = new Point[numDetectives + 1];
        flagImage = new BufferedImage[NO_OF_DETECTIVES + 1];
        for (int i = 0; i <= NO_OF_DETECTIVES; i++) {
            String fileName = "/images/flag" + Integer.toString(i) + ".gif";
            try {
                flagImage[i] = ImageIO.read(new File(getClass().getResource(fileName).getPath()));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, String.format("Error opening file %s. Exiting!", fileName), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (this.playerPositions != null) {
            Graphics2D g2 = (Graphics2D) g;

            for (int i = 0; i < playerPositions.length; i++) {
                if (playerPositions[i] != null && !(blinkOn && i == currentPlayer)
                        && !(i == 0 && !mrXVisible)) {

                    Point playerPos = this.getPlayerPos(i);

                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.drawImage(flagImage[i], playerPos.x, playerPos.y - flagImage[i].getHeight(),
                            null);
                }
            }
        }
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
            int width = flagImage[player].getWidth();
            int height = flagImage[player].getHeight();
            this.repaint(playerPos.x, playerPos.y - height, width, height);
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

}
