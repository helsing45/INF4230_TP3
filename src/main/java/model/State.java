package main.java.model;

import main.java.players.Criminal;
import main.java.players.Player;
import main.java.players.Detective;
import main.java.utilities.clone.DeepCopy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class State implements Serializable{

    private static final int MAX_NUMBER_OF_ROUNDS = 24;
    public static final List<Integer> CRIMINAL_REVEAL_ROUNDS = new ArrayList<>(Arrays.asList(3, 8, 13, 18, 24));

    private final PlayersOnBoard playersOnBoard;
    private final int numberOfPlayers;
    private int currentRound;
    private int currentPlayerIndex;
    private int previousPlayerIndex;
    private Action.Transportation lastCriminalTransportation;
    private boolean inSearch;
    private boolean searchInvokingPlayerIsCriminal;
    private boolean searchInvokingPlayerUsesCoalitionReduction;
    private boolean searchInvokingPlayerUsesMoveFiltering;


    public State(Player[] players) {
        this(new PlayersOnBoard(players),players.length);
    }

    private State(PlayersOnBoard playersOnBoard, int numberOfPlayers) {
        this.playersOnBoard = playersOnBoard;
        this.numberOfPlayers = numberOfPlayers;
        this.currentRound = 1;
        this.currentPlayerIndex = 0;
        this.previousPlayerIndex = numberOfPlayers - 1;
        this.lastCriminalTransportation = null;
        this.inSearch = false;
        this.searchInvokingPlayerIsCriminal = false;
    }

    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }

    public PlayersOnBoard getPlayersOnBoard() {
        return playersOnBoard;
    }

    public Player getCurrentAgent() {
        return playersOnBoard.getPlayerAtIndex(currentPlayerIndex);
    }

    public Player getPreviousAgent() {
        return playersOnBoard.getPlayerAtIndex(previousPlayerIndex);
    }

    public void setSearchModeOn() {
        inSearch = true;
        searchInvokingPlayerIsCriminal = playersOnBoard.playerIsCriminal(currentPlayerIndex);
        searchInvokingPlayerUsesCoalitionReduction = playersOnBoard.playerUsesCoalitionReduction(currentPlayerIndex);
        searchInvokingPlayerUsesMoveFiltering = playersOnBoard.playerUsesMoveFiltering(currentPlayerIndex);
    }

    public boolean searchInvokingPlayerUsesCoalitionReduction() {
        return searchInvokingPlayerUsesCoalitionReduction;
    }

    public void setSearchModeOff() {
        inSearch = false;
    }

    public boolean currentPlayerIsCriminal() {
        return playersOnBoard.playerIsCriminal(currentPlayerIndex);
    }

    public boolean previousPlayerIsCriminal() {
        return playersOnBoard.playerIsCriminal(previousPlayerIndex);
    }

    public boolean currentPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(currentPlayerIndex);
    }

    public boolean previousPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(previousPlayerIndex);
    }

    /**
     * Determine si on est entrait de faire des estimations.
     */
    private boolean inSearchFromDetectivesPov() {
        return inSearch && !searchInvokingPlayerIsCriminal;
    }

    public int getCurrentRound(){
        return currentRound;
    }

    public boolean isCriminalSurfacesRound() {
        return CRIMINAL_REVEAL_ROUNDS.contains(currentRound);
    }

    public boolean isTerminal() {
        return detectiveWon() || criminalWon();
    }

    public boolean detectiveWon() {
        if (inSearchFromDetectivesPov())
            return playersOnBoard.anyDetectiveOnCriminalsMostProbablePosition();
        else
            return playersOnBoard.anyDetectiveOnCriminalsActualPosition();
    }

    public boolean criminalWon() {
        return currentRound == MAX_NUMBER_OF_ROUNDS;
    }

    public boolean detectiveWon(Detective detective) {
        if (inSearchFromDetectivesPov())
            return playersOnBoard.seekerOnCriminalsMostProbablePosition(detective);
        else
            return playersOnBoard.seekerOnCriminalsActualPosition(detective);
    }

    public State performActionForCurrentAgent(Action action) {
        if (inSearchFromDetectivesPov())
            playersOnBoard.movePlayerFromDetectivesPov(currentPlayerIndex, action);
        else
            playersOnBoard.movePlayerFromActualPosition(currentPlayerIndex, action);
        if (currentPlayerIsCriminal())
            lastCriminalTransportation = action.getTransportation();
        setCriminalMostProbablePosition(lastCriminalTransportation);
        prepareStateForNextPlayer();
        performDoubleMoveIfShould();
        return this;
    }

    private void setCriminalMostProbablePosition(Action.Transportation transportation) {
        if (currentPlayerIsCriminal())
            setCriminalsMostProbablePositionAfterCriminal(transportation);
        else
            playersOnBoard.removeCurrentDetectivePositionFromPossibleCriminalPositions(currentPlayerIndex);
    }

    private void setCriminalsMostProbablePositionAfterCriminal(Action.Transportation transportation) {
        if (isCriminalSurfacesRound())
            playersOnBoard.setCriminalsActualAsMostProbablePosition();
        else
            playersOnBoard.recalculateCriminalsMostProbablePosition(transportation);
    }

    private void  performDoubleMoveIfShould() {
        if (shouldCheckForCriminalDoubleMoveAutomatically()) {
            Criminal criminal = (Criminal)getPreviousAgent();
            if (criminal.shouldUseDoubleMove(playersOnBoard, searchInvokingPlayerUsesMoveFiltering)) {
                skipAllDetective();
                criminal.removeDoubleMoveCard();
            }
        }
    }

    private boolean shouldCheckForCriminalDoubleMoveAutomatically() {
        return previousPlayerIsCriminal() && (previousPlayerIsHuman() && inSearch || !previousPlayerIsHuman());
    }

    public void skipAllDetective() {
        currentPlayerIndex--;
        currentRound++;
    }

    public State skipCurrentAgent() {
        prepareStateForNextPlayer();
        return this;
    }

    public int getNumberOfAvailableActionsForCurrentAgent() {
        return getAvailableActionsForCurrentAgent().size();
    }

    public List<Action> getAvailableActionsForCurrentAgent() {
        List<Action> availableActions;
        if (inSearchFromDetectivesPov())
            availableActions = playersOnBoard.getAvailableActionsFromDetectivesPov(currentPlayerIndex);
        else
            availableActions = playersOnBoard.getAvailableActionsForActualPosition(currentPlayerIndex);
        availableActions = addCriminalPasseDroitActions(availableActions);
        return availableActions;
    }

    private List<Action> addCriminalPasseDroitActions(List<Action> actions) {
        if (currentPlayerIsCriminal()) {
            if (notHumanInSearch())
                return addPasseDroitActionsIfAvailableTickets(
                        (Criminal) playersOnBoard.getPlayerAtIndex(currentPlayerIndex), actions);
            else
                return addPasseDroitForCriminalIfOptimal(
                        (Criminal) playersOnBoard.getPlayerAtIndex(currentPlayerIndex), actions);
        }
        return actions;
    }

    private boolean notHumanInSearch() {
        return currentPlayerIsHuman() && !inSearch;
    }

    List<Action> addPasseDroitActionsIfAvailableTickets(Criminal criminal, List<Action> actions) {
        if (criminal.hasPasseDroit())
            return addPasseDroitActions(actions);
        return actions;
    }

    List<Action> addPasseDroitForCriminalIfOptimal(Criminal criminal, List<Action> actions) {
        if (criminal.shouldUsePasseDroit(currentRound, actions, searchInvokingPlayerUsesMoveFiltering))
            return addPasseDroitActions(actions);
        return actions;
    }

    private List<Action> addPasseDroitActions(List<Action> actions) {
        List<Action> blackFareActions = generatePasseDroitActions(actions);
        actions.addAll(blackFareActions);
        return removeDuplicates(actions);
    }

    private List<Action> generatePasseDroitActions(List<Action> actions) {
        return actions.stream()
                .map(Action::generatePasseDroitAction).collect(Collectors.toList());
    }

    private List<Action> removeDuplicates(List<Action> actions) {
        return new ArrayList<>(new LinkedHashSet<>(actions));
    }

    private void prepareStateForNextPlayer() {
        if (isLastPlayerOfRound())
            currentRound++;
        previousPlayerIndex = currentPlayerIndex;
        currentPlayerIndex = ++currentPlayerIndex % playersOnBoard.getNumberOfPlayers();
    }

    private boolean isLastPlayerOfRound() {
        return currentPlayerIndex == numberOfPlayers - 1;
    }

    public int getCurrentPlayerIndex(){
        return currentPlayerIndex;
    }

    public void updateCriminalProbablePosition() {
        playersOnBoard.fixCriminalProbablePosition();
    }

    public State copy(){
        return DeepCopy.copy(this);
    }
}