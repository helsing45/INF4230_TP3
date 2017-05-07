package main.java;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;
import main.java.players.Criminal;
import main.java.players.Player;
import main.java.players.Detective;
import main.java.search.SearchTree;
import main.java.view.MapLabel;
import main.java.view.SettingsDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;


public class ScotlandYard extends JApplet implements ListSelectionListener, SettingsDialog.Listener {

    boolean gameStarted;
    JFrame parentFrame;
    Container container;
    JButton  start;
    MapLabel map;
    JTextArea messageArea;
    SettingsDialog settingsDialog;

    SettingsDialog.Setting setting;

    public static void main(String args[]) {
        JFrame f = new JFrame("Scotland yard");
        ScotlandYard game = new ScotlandYard();
        game.buildUI(f);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setUndecorated(true);
        f.setVisible(true);
        f.setResizable(false);
    }

    private void buildUI(JFrame f) {
        parentFrame = f;
        container = parentFrame.getContentPane();
        container.setLayout(new GridBagLayout());

        GridBagConstraints messageConstraints = new GridBagConstraints();
        messageConstraints.fill = GridBagConstraints.BOTH;
        messageConstraints.gridx = 1018;
        messageConstraints.gridy = 0;
        messageConstraints.gridwidth = 7;
        messageConstraints.weightx = 1.0;
        messageConstraints.weighty = 1.0;
        messageConstraints.insets = new Insets(10, 10, 10, 10);
        JScrollPane scroll = new JScrollPane();
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        scroll.setViewportView(messageArea);
        container.add(scroll, messageConstraints);

        GridBagConstraints mapConstraints = new GridBagConstraints();
        mapConstraints.fill = GridBagConstraints.BOTH;


        JScrollPane map = new JScrollPane(this.map = new MapLabel(new ImageIcon(getClass().getResource("/images/map.jpg")), PlayersOnBoard.NUMBER_OF_PLAYERS), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mapConstraints.gridx = 0;
        mapConstraints.gridy = 0;
        mapConstraints.gridwidth = 7;
        mapConstraints.ipady = 1018;
        mapConstraints.ipadx = 809;
        mapConstraints.weightx = 1.0;
        mapConstraints.weighty = 1.0;
        mapConstraints.insets = new Insets(10, 10, 10, 10);
        container.add(map, mapConstraints);

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

        JMenuItem settings = new JMenuItem("Settings");
        fileMenu.add(settings);
        settings.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettingDialog();
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

    private void showSettingDialog() {
        settingsDialog = new SettingsDialog(this);
        settingsDialog.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        settingsDialog.setLocation(dim.width / 2 - settingsDialog.getSize().width / 2, dim.height / 2 - settingsDialog.getSize().height / 2);
        settingsDialog.setVisible(true);
    }

    private void reset() {
        start.setEnabled(true);
        map.setCurrentPlayer(-1);
        parentFrame.setVisible(true);
        messageArea.setText("");
        repaint();
        gameStarted = false;
    }

    private void startGame() {
        if(setting == null) setting = new SettingsDialog.Setting();
        playGame(new SearchTree(setting.getIterationCount()), true);
    }

    private void playGame(SearchTree searchTree, boolean showWinner) {
        if (setting == null) setting = new SettingsDialog.Setting();
        messageArea.setText("");
        //Initialisation des joueurs
        Player[] players = new Player[setting.getDetectiveCount() + 1];
        players[0] = new Criminal(setting.isCriminalHuman(), true, true);
        for (int i = 1; i < players.length; i++) {
            players[i] = new Detective(setting.isDetectiveHuman(), Detective.Color.values()[i - 1], true, true);
        }

        State state = new State(players);
        printStartPosition(state);

        for (int i = 0; i < state.getNumberOfPlayers(); i++) {
            map.setPlayerPos(i, state.getPlayersOnBoard().getLocationOf(i));
        }
        map.setCurrentPlayer(0);

        while (!state.isTerminal()) {
            performOneAction(state, searchTree);
        }

        map.setMrXVisible(true);
        map.repaint();
        if (showWinner) {
            JOptionPane.showMessageDialog(parentFrame, state.criminalWon() ? "Le criminel c'est enfuit." : "Le detective ont gagnee");
        }

    }

    private void performOneAction(State state, SearchTree searchTree) {
        // Le criminel est toujours lui qui commence le tour
        map.setMrXVisible(State.CRIMINAL_REVEAL_ROUNDS.contains(state.getCurrentRound()));
        if (state.currentPlayerIsCriminal()) {
            printRound(state);
        }

        map.setCurrentPlayer(state.getCurrentPlayerIndex());
        if (currentPlayerCanMove(state)) {
            main.java.model.Action mostPromisingAction = getNextAction(state, searchTree);
            printMove(state);
            state.performActionForCurrentAgent(mostPromisingAction);
            map.setPlayerPos(state.getCurrentPlayerIndex(), state.getPlayersOnBoard().getLocationOf(state.getCurrentPlayerIndex()));
        } else {
            state.skipCurrentAgent();
            message(state.getCurrentAgent() + " n'a pas bouger");
        }
        askHumanForDoubleMove(state);
    }

    private void printStartPosition(State state){
        message("================================");
        message("========= BONNE PARTIE =========");
        message("================================");
        message("Les joueurs commence au position");
        for (int i = 0; i < state.getNumberOfPlayers(); i++) {
            message(state.getPlayersOnBoard().getPlayerInfo(i,setting.isDebugMode()));
        }
    }

    private void printRound(State state) {
        String string;
            string = ("\n ----------------------------\n");
            string += "         Tour # " + state.getCurrentRound();
            string += ("\n ----------------------------\n");

            message(string);
    }

    private void printMove(State state) {
        message(state.getPlayersOnBoard().getPlayerInfo(state.getCurrentPlayerIndex(), setting.isDebugMode()));
    }

    public void message(String msg) {
        messageArea.append(msg + "\n");
        messageArea.setCaretPosition(messageArea.getText().length());
        messageArea.update(messageArea.getGraphics());
    }

    private static boolean shouldAskForDoubleMove(State state) {
        return state.previousPlayerIsCriminal() && state.previousPlayerIsHuman();
    }

    private static void askHumanForDoubleMove(State state) {
        if (shouldAskForDoubleMove(state)) {
            Criminal criminal = (Criminal) state.getPreviousAgent();
            if (criminal.hasDoubleMoveCard())
                askHumanForDoubleMoveConfidently(state, criminal);
        }
    }

    private static void askHumanForDoubleMoveConfidently(State state, Criminal criminal) {
        //TODO ask for double
        /*if (doubleMove.equals("y")) {
            state.skipAllDetective();
            criminal.removeDoubleMoveCard();
        }*/
    }

    private Action getNextAction(State state, SearchTree searchTree) {
        Action mostPromisingAction;
        if (state.currentPlayerIsHuman()) {
            mostPromisingAction = getInputDialog(state);
        }
        else
            mostPromisingAction = getActionFromSearchTree(state, searchTree);
        return mostPromisingAction;
    }

    private Action getInputDialog(State state){
        ArrayList<Action> availableMoves = new ArrayList<>(state.getAvailableActionsForCurrentAgent());
        Action[] options = availableMoves.toArray(new Action[availableMoves.size()]);
        int index =  JOptionPane.showOptionDialog(null,
                state.getPlayersOnBoard().getPlayer(state.getCurrentPlayerIndex()) + " où va-t-il ?",
                "Jouer votre tour",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,//do not use a custom Icon
                options,//the titles of buttons
                options[0]);
        return options[index];
    }

    private Action getActionFromSearchTree(State state, SearchTree mcts) {
        Action mostPromisingAction;
        state.setSearchModeOn();
        updateCriminalsMostProbablePosition(state);
        double explorationParameter = getAppropriateExplorationParameter(state);
        mostPromisingAction = mcts.searchWithExploration(state, explorationParameter);
        state.setSearchModeOff();
        return mostPromisingAction;
    }

    private double getAppropriateExplorationParameter(State state) {
        return state.currentPlayerIsCriminal() ? 0.3 : 2;
    }

    private void updateCriminalsMostProbablePosition(State state) {
        if (state.isTerminal())
            state.updateCriminalProbablePosition();
    }

    private boolean currentPlayerCanMove(State state) {
        return state.getAvailableActionsForCurrentAgent().size() > 0;
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (gameStarted) return;

    }

    @Override
    public void onSettingsChange(SettingsDialog.Setting setting) {
        this.setting = setting;
    }
}
