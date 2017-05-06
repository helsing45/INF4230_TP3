package main.java;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;
import main.java.players.Hider;
import main.java.players.Player;
import main.java.players.Seeker;
import main.java.search.SearchTree;
import main.java.strategies.CoalitionReduction;
import main.java.strategies.MoveFiltering;
import main.java.strategies.Playouts;
import main.java.view.MapLabel;
import main.java.view.SettingsDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import static main.java.players.Player.Operator.COMPUTER;
import static main.java.players.Player.Operator.HUMAN;

public class BoardGame extends JApplet implements ListSelectionListener, SettingsDialog.Listener {
    private static final int ITERATIONS = 20;
    private static final double HIDERS_EXPLORATION = 0.2;
    private static final double SEEKERS_EXPLORATION = 2;

    boolean gameStarted;
    JFrame parentFrame;
    Container container;
    JButton done, start, btnDetectives;
    JTextField txtInfo, detectiveStatus;
    MapLabel map;
    JComboBox cboAvailableMoves;
    JTextArea messageArea;
    SettingsDialog settingsDialog;

    SettingsDialog.Setting setting;

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

    void buildUI(JFrame f) {
        parentFrame = f;
        container = parentFrame.getContentPane();
        container.setLayout(new GridBagLayout());
        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
        int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

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


        JScrollPane map = new JScrollPane(this.map = new MapLabel(new ImageIcon(getClass().getResource("/images/map.jpg")), PlayersOnBoard.NUMBER_OF_PLAYERS), v, h);
        mapConstraints.gridx = 0;
        mapConstraints.gridy = 0;
        mapConstraints.gridwidth = 7;
        mapConstraints.ipady = 1018;
        mapConstraints.ipadx = 809;
        mapConstraints.weightx = 1.0;
        mapConstraints.weighty = 1.0;
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
        btnDetectives.setEnabled(false);
        start.setEnabled(true);
        container.remove(cboAvailableMoves);
        done.setEnabled(false);
        done.setVisible(false);
        detectiveStatus.setVisible(false);
        map.setCurrentPlayer(-1);
        cboAvailableMoves.repaint();
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
    }

    private void startGame() {
        playGame(initializeSearch(), true);
    }

    private void playGame(SearchTree searchTree, boolean showWinner) {
        if (setting == null) setting = new SettingsDialog.Setting();

        Player[] players = initializePlayersWithOperator(setting.isCriminalIsComputer() ? COMPUTER : HUMAN,
                setting.isDetectiveIsComputer() ? COMPUTER : HUMAN);
        State state = State.initialize(players);

        for (int i = 0; i < state.getNumberOfPlayers(); i++) {
            map.setPlayerPos(i, state.getPlayersOnBoard().getLocationOf(i));
        }
        map.setCurrentPlayer(0);

        while (!state.isTerminal()) {
            performOneAction(state, searchTree);
        }
        if (showWinner) {
            JOptionPane.showMessageDialog(parentFrame, state.hiderWon() ? "Le criminel c'est enfuit." : "Le detective ont gagnee");
        }

    }

    private Player[] initializePlayersWithOperator(Player.Operator hider, Player.Operator seeker) {
        Player[] players = new Player[setting.getDetectiveCount() + 1];
        players[0] = new Hider(hider, Playouts.Uses.BASIC, CoalitionReduction.Uses.YES, MoveFiltering.Uses.YES);
        for (int i = 1; i < players.length; i++)
            players[i] = new Seeker(seeker, Seeker.Color.values()[i - 1], Playouts.Uses.BASIC,
                    CoalitionReduction.Uses.YES, MoveFiltering.Uses.YES);
        return players;
    }

    private void performOneAction(State state, SearchTree mcts) {
        // Le criminel et toujours lui qui commence le tour
        if (state.currentPlayerIsHider()) {
            printRound(state);
        }

        map.setCurrentPlayer(state.getCurrentPlayerIndex());
        if (currentPlayerCanMove(state)) {
            main.java.model.Action mostPromisingAction = getNextAction(state, mcts);
            state.performActionForCurrentAgent(mostPromisingAction);
            map.setPlayerPos(state.getCurrentPlayerIndex(), state.getPlayersOnBoard().getLocationOf(state.getCurrentPlayerIndex()));
            printMove(state);
        } else {
            state.skipCurrentAgent();
            message(state.getCurrentAgent() + " didn't move");
        }
        askHumanForDoubleMove(state);
    }

    private void printRound(State state) {
        String round = state.printNewRound();
        if (round != null)
            message(round);
    }

    private void printMove(State state) {
        message(state.getPlayersOnBoard().printPlayers(state.getCurrentPlayerIndex(), setting.isDebugMode()));
    }

    public void message(String msg) {
        messageArea.append(msg + "\n");
        messageArea.setCaretPosition(messageArea.getText().length());
        messageArea.update(messageArea.getGraphics());
    }

    private static boolean shouldAskForDoubleMove(State state) {
        return state.previousPlayerIsHider() && state.previousPlayerIsHuman();
    }

    private static void askHumanForDoubleMove(State state) {
        if (shouldAskForDoubleMove(state)) {
            Hider hider = (Hider) state.getPreviousAgent();
            if (hider.hasDoubleMoveCard())
                askHumanForDoubleMoveConfidently(state, hider);
        }
    }

    private static void askHumanForDoubleMoveConfidently(State state, Hider hider) {
        //TODO ask for double
        /*if (doubleMove.equals("y")) {
            state.skipAllSeekers();
            hider.removeDoubleMoveCard();
        }*/
    }

    private Action getNextAction(State state, SearchTree mcts) {
        Action mostPromisingAction;
        if (state.currentPlayerIsHuman())
            mostPromisingAction = getActionFromInput(state);
        else
            mostPromisingAction = getActionFromSearch(state, mcts);
        return mostPromisingAction;
    }

    private Action getActionFromInput(State state) {
        int actionIndex = 1; // TODO get real input
        return state.getAvailableActionsForCurrentAgent().get(actionIndex);
    }

    private Action getRandomAction(State state) {
        java.util.List<Action> actions = state.getAvailableActionsForCurrentAgent();
        Collections.shuffle(actions);
        return actions.get(0);
    }

    private Action getActionFromMctsSearch(State state, SearchTree mcts) {
        Action mostPromisingAction;
        state.setSearchModeOn();
        updateHidersMostProbablePosition(state);
        double explorationParameter = getAppropriateExplorationParameter(state);
        mostPromisingAction = mcts.uctSearchWithExploration(state, explorationParameter);
        state.setSearchModeOff();
        return mostPromisingAction;
    }

    private double getAppropriateExplorationParameter(State state) {
        if (state.currentPlayerIsHider())
            return HIDERS_EXPLORATION;
        else
            return SEEKERS_EXPLORATION;
    }

    private void updateHidersMostProbablePosition(State state) {
        if (state.isTerminal())
            state.updateHidersProbablePosition();
    }

    private Action getActionFromSearch(State state, SearchTree mcts) {
        if (state.currentPlayerIsRandom())
            return getRandomAction(state);
        else
            return getActionFromMctsSearch(state, mcts);
    }

    private boolean currentPlayerCanMove(State state) {
        return state.getAvailableActionsForCurrentAgent().size() > 0;
    }

    private static SearchTree initializeSearch() {
        return SearchTree.initializeIterations(ITERATIONS);
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
