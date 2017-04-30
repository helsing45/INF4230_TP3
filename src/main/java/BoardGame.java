package main.java;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;
import main.java.players.Hider;
import main.java.players.Player;
import main.java.players.Seeker;
import main.java.search.Mcts;
import main.java.strategies.CoalitionReduction;
import main.java.strategies.MoveFiltering;
import main.java.strategies.Playouts;
import main.java.view.MapLabel;
import main.java.view.SettingsDialog;
import main.java.view.TableDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static main.java.players.Player.Operator.COMPUTER;
import static main.java.players.Player.Operator.HUMAN;

public class BoardGame extends JApplet implements ListSelectionListener, SettingsDialog.Listener {
    private static final int MCTS_ITERATIONS = 20;
    private static final double HIDERS_EXPLORATION = 0.2;
    private static final double SEEKERS_EXPLORATION = 2;

    boolean gameStarted;
    JFrame parentFrame;
    Container container;
    JButton done, start, btnDetectives, mx;
    JTextField txtInfo, detectiveStatus;
    MapLabel map;
    JComboBox cboAvailableMoves;
    SettingsDialog settingsDialog;

    boolean hiderIsComputer = true, seekerIsComputer = true;
    int detectiveCount = 5;


    private List<Action[]> history;
    private Action[] tempAction;

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

        GridBagConstraints mapConstraints = new GridBagConstraints();
        mapConstraints.fill = GridBagConstraints.BOTH;

        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
        int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

        JScrollPane map = new JScrollPane(this.map = new MapLabel(new ImageIcon(getClass().getResource("/images/map.jpg")), PlayersOnBoard.NUMBER_OF_PLAYERS), v, h);
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
        mx.setEnabled(false);
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

    private void showPreviousMoves() {
        TableDialog tb = new TableDialog(parentFrame, "Previous moves", history, this, false);
        tb.setVisible(true);
    }

    private void startGame() {
        playGame(initializeSearch());
    }

    public void playGame(Mcts mcts) {
        Player[] players = initializePlayersWithOperator(hiderIsComputer ? COMPUTER : HUMAN,
                seekerIsComputer ? COMPUTER : HUMAN);
        State state = State.initialize(players);

        history = new ArrayList<>();
        tempAction = new Action[state.getNumberOfPlayers()];

        while (!state.isTerminal()) {
            performOneAction(state, mcts);
        }

        boolean b = true;
    }

    private Player[] initializePlayersWithOperator(Player.Operator hider, Player.Operator seeker) {
        Player[] players = new Player[detectiveCount + 1];
        players[0] = new Hider(hider, Playouts.Uses.BASIC, CoalitionReduction.Uses.YES, MoveFiltering.Uses.YES);
        for (int i = 1; i < players.length; i++)
            players[i] = new Seeker(seeker, Seeker.Color.values()[i - 1], Playouts.Uses.BASIC,
                    CoalitionReduction.Uses.YES, MoveFiltering.Uses.YES);
        return players;
    }

    private void performOneAction(State state, Mcts mcts) {
        if (state.currentPlayerIsHider()) {
            state.printNewRound();
            state.printAllPositions();
            history.add(tempAction);
            tempAction = new Action[state.getNumberOfPlayers()];
        }

        if (currentPlayerCanMove(state)) {
            main.java.model.Action mostPromisingAction = getNextAction(state, mcts);
            tempAction[state.getCurrentPlayerIndex()] = mostPromisingAction;
            state.performActionForCurrentAgent(mostPromisingAction);
        } else {
            state.skipCurrentAgent();
            tempAction[state.getCurrentPlayerIndex()] = null;
        }
        askHumanForDoubleMove(state);
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

    private Action getNextAction(State state, Mcts mcts) {
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

    private Action getActionFromMctsSearch(State state, Mcts mcts) {
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

    private Action getActionFromSearch(State state, Mcts mcts) {
        if (state.currentPlayerIsRandom())
            return getRandomAction(state);
        else
            return getActionFromMctsSearch(state, mcts);
    }

    private boolean currentPlayerCanMove(State state) {
        return state.getAvailableActionsForCurrentAgent().size() > 0;
    }

    private static Mcts initializeSearch() {
        return Mcts.initializeIterations(MCTS_ITERATIONS);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (gameStarted) return;

    }

    @Override
    public void onSettingsChange(boolean hiderIsComputer, boolean seekerIsComputer, int detectiveCount) {
        this.hiderIsComputer = hiderIsComputer;
        this.seekerIsComputer = seekerIsComputer;
        this.detectiveCount = detectiveCount;
    }
}
