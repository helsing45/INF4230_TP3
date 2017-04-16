package main.java;

import main.java.controller.Controller;
import main.java.model.Move;
import main.java.view.MapLabel;
import main.java.view.TableDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Created by j-c9 on 2017-04-15.
 */
public class BoardGame extends JApplet implements ListSelectionListener {
    boolean gameStarted;
    JFrame parentFrame;
    Container container;
    JButton done, start, btnDetectives, mx;
    JTextField txtInfo, detectiveStatus;
    MapLabel map;
    JComboBox cboAvailableMoves;
    int currentDetectiveIndex = 0;
    Controller controller;
    boolean canMove = true;

    void buildUI(JFrame f) {
        parentFrame = f;
        container = parentFrame.getContentPane();
        container.setLayout(new GridBagLayout());

        GridBagConstraints mapConstraints = new GridBagConstraints();
        mapConstraints.fill = GridBagConstraints.BOTH;

        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
        int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        JScrollPane map = new JScrollPane(this.map = new MapLabel(new ImageIcon(getClass().getResource("/images/map.jpg")), Controller.getNumberOfDetectives()), v, h);
        mapConstraints.gridx = 0;
        mapConstraints.gridy = 0;
        mapConstraints.gridwidth = 7;
        mapConstraints.ipady = 1018; // Display long map
        mapConstraints.ipady = 809; // Display wide map
        mapConstraints.weightx = 1.0; // should occupy all available horizontal space on
        // resizing
        mapConstraints.weighty = 1.0; // should occupy all available vertical space on
        // resizing
        mapConstraints.insets = new Insets(10, 10, 10, 10);
        container.add(map, mapConstraints);

        btnDetectives = new JButton("DetectivesButton");
        btnDetectives.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDetectivesStats();
            }
        });
        btnDetectives.setActionCommand("detectives");
        btnDetectives.setVisible(true);
        btnDetectives.setEnabled(gameStarted);

        GridBagConstraints detectivesBtnConstraints = new GridBagConstraints();
        detectivesBtnConstraints.gridx = 0;
        detectivesBtnConstraints.gridy = 1;
        detectivesBtnConstraints.insets = new Insets(0, 10, 5, 5);
        container.add(btnDetectives, detectivesBtnConstraints);

        mx = new JButton("Previous moves");
        mx.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreviousMoves();
            }
        });
        mx.setVisible(true);
        //mx.setEnabled(gameStarted);
        GridBagConstraints mxC = new GridBagConstraints();
        mxC.gridx = 1;
        mxC.gridy = 1;
        mxC.insets = new Insets(0, 5, 5, 5);
        container.add(mx, mxC);

        start = new JButton("Start Game Button");
        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        start.setEnabled(!gameStarted);
        GridBagConstraints startC = new GridBagConstraints();
        startC.gridx = 2;
        startC.gridy = 1;
        startC.insets = new Insets(0, 5, 5, 5);
        container.add(start, startC);

        done = new JButton("Play Button");
        done.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manualPlay();
            }
        });
        if (!gameStarted)
            done.setEnabled(false);
        GridBagConstraints doneC = new GridBagConstraints();
        doneC.gridx = 6;
        doneC.gridy = 1;
        doneC.insets = new Insets(0, 5, 5, 5);
        doneC.fill = GridBagConstraints.HORIZONTAL; // fill the remaining space
        // at the end of row
        doneC.anchor = GridBagConstraints.EAST;
        container.add(done, doneC);

        txtInfo = new JTextField(50);
        GridBagConstraints msgC = new GridBagConstraints();
        msgC.gridx = 3;
        msgC.gridy = 1;
        msgC.ipadx = 150; // make this one a little longer
        msgC.insets = new Insets(0, 5, 5, 5);
        container.add(txtInfo, msgC);
        txtInfo.setEditable(false);
        txtInfo.setText("Appuyer sur commencer");

        detectiveStatus = new JTextField(120);
        detectiveStatus.setEditable(false);
        GridBagConstraints detectiveStatusC = new GridBagConstraints();
        detectiveStatusC.gridx = 4;
        detectiveStatusC.gridy = 1;
        detectiveStatusC.ipadx = 320;
        detectiveStatusC.insets = new Insets(0, 5, 5, 5);

        GridBagConstraints getMoveC = new GridBagConstraints();
        getMoveC.gridx = 5;
        getMoveC.gridy = 1;
        getMoveC.insets = new Insets(0, 5, 5, 5);

        parentFrame.setVisible(true);
        addMenu();
    }

    private void addMenu() {
        JMenuBar mbar = new JMenuBar();
        parentFrame.setJMenuBar(mbar);
        JMenu fileMenu = new JMenu("Fichier");
        mbar.add(fileMenu);

        JMenuItem newGame = new JMenuItem("Nouveau");
        fileMenu.add(newGame);
        newGame.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        JMenuItem exitGame = new JMenuItem("Quitter");
        fileMenu.add(exitGame);
        exitGame.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void reset() {
        try {
            btnDetectives.setEnabled(false);
            mx.setEnabled(false);
            start.setEnabled(true);
            container.remove(cboAvailableMoves);
            done.setEnabled(false);
            done.setVisible(false);
            detectiveStatus.setVisible(false);
            map.setCurrentPlayer(-1);
            cboAvailableMoves.repaint();
        } catch (NullPointerException e) {
        }
        currentDetectiveIndex = 0;
        parentFrame.setVisible(true);
        done.repaint();
        txtInfo.setText("Appuyer sur commencer");
        repaint();
        gameStarted = false;
    }

    private void manualPlay() {
        //TODO do something usefull
    }

    private void showDetectivesStats() {
        String detectivesStats = "";
        for (int i = 0; i < Controller.getNumberOfDetectives(); i++) {
            detectivesStats += String.format("Detective %$1d %$2s \n", i + 1, controller.getDetectives()[i].toString());
        }
        JOptionPane.showMessageDialog(parentFrame, detectivesStats, "DetectiveInfoTitle", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPreviousMoves() {
        TableDialog tb = new TableDialog(parentFrame, "Previous moves", controller, this, false);
        tb.setVisible(true);
    }

    private void startGame() {
        gameStarted = true;
        controller = new Controller();
        for (int i = 1; i <= Controller.getNumberOfDetectives(); i++) {
            map.setPlayerPos(i, controller.getPoint(controller.getDetectives()[i - 1].getPosition().getPosition()));
        }
        map.setCurrentPlayer(1);
        parentFrame.setVisible(true);
        fullyAutomaticMode();
    }

    /**
     * Tout les joueurs sont des ordinateurs
     **/
    private void fullyAutomaticMode() {
        int round = 0;
        while (gameStarted) {
            System.out.println("Round: " + round);
            moveMrX();
            for (int i = 0; i < Controller.getNumberOfDetectives(); i++) {
                moveDetective(i);
            }
            round++;
        }
    }

    private void gameDone(boolean seekerAsWin) {
        gameStarted = false;
        System.out.println((seekerAsWin ? "Les detectives ont" : "Monsieurs X a") + " gagner");
    }

    private void moveMrX() {
        if (controller.isSeekerAsWin())
            gameDone(true);
        Move move = controller.moveHider();
        System.out.println("Mr X move to " + move.getNode());
        map.mrXVisible = controller.isNeedToBeReveal();
        map.setPlayerPos(0, controller.getPoint(move.getNode()).getLocation());

        if (controller.isHiderAsWin())
            gameDone(false);
    }

    private void moveDetective(int detectiveId) {
        Move move = controller.moveDetectives(detectiveId);

        if(move == null){
            return;
        }

        System.out.println("Detective# " + detectiveId + " move to " + move.getNode());
        if (canMove) {
            controller.changeDetectivePosition(detectiveId, move);
        }

        int nodePos = controller.getDetectives()[detectiveId].getPosition().getPosition();
        Point currentDetectivePos = controller.getPoint(nodePos);
        map.setPlayerPos(detectiveId, currentDetectivePos.x, currentDetectivePos.y);

        parentFrame.setVisible(true);
        //currentDetectiveIndex = (detectiveId + 1) % NO_OF_DETECTIVES;
        map.setCurrentPlayer((detectiveId + 1) % Controller.getNumberOfDetectives());
        if (controller.isSeekerAsWin())
            gameDone(true);
    }

    public static void main(String args[]) {
        JFrame f = new JFrame("Scotland yard");
        BoardGame game = new BoardGame();
        game.buildUI(f);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        f.setSize(new Dimension(1057, 942));

        f.setLocation(dim.width / 2 - f.getSize().width / 2, dim.height / 2 - f.getSize().height / 2);
        f.setVisible(true);
        f.setResizable(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (gameStarted) return;

        DefaultListSelectionModel table = (DefaultListSelectionModel) evt.getSource();
        int index = table.getMaxSelectionIndex();
        for (int i = 0; i <= Controller.getNumberOfDetectives(); i++) {
            LinkedList<Move> prevPos;
            if (i == 0) {
                prevPos = controller.getMrX().getPrevPos();
            } else {
                prevPos = controller.getDetectives()[i - 1].getPrevPos();
            }

            int pos = (prevPos.size() <= index + 1 ? prevPos.getLast().getNode() : prevPos.get(
                    index + 1).getNode());
            map.setPlayerPos(i, controller.getPoint(pos));
            map.setCurrentPlayer(-1);
        }
    }
}
