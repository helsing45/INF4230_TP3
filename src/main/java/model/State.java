package main.java.model;

import main.java.players.Hider;
import main.java.players.Player;
import main.java.players.Seeker;
import main.java.utilities.clone.DeepCopy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class State implements Serializable{

    private static final int MAX_NUMBER_OF_ROUNDS = 24;
    public static final List<Integer> HIDER_SURFACES_ROUNDS = new ArrayList<>(Arrays.asList(3, 8, 13, 18, 24));

    private final PlayersOnBoard playersOnBoard;
    private final int numberOfPlayers;
    private int currentRound;
    private int currentPlayerIndex;
    private int previousPlayerIndex;
    private Action.Transportation lastHidersTransportation;
    private boolean inSearch;
    private boolean searchInvokingPlayerIsHider;
    private boolean searchInvokingPlayerUsesCoalitionReduction;
    private boolean searchInvokingPlayerUsesMoveFiltering;

    public static State initialize(Player[] players) {
        PlayersOnBoard playersOnBoard = PlayersOnBoard.initialize(players);
        return new State(playersOnBoard, players.length);
    }

    private State(PlayersOnBoard playersOnBoard, int numberOfPlayers) {
        this.playersOnBoard = playersOnBoard;
        this.numberOfPlayers = numberOfPlayers;
        this.currentRound = 1;
        this.currentPlayerIndex = 0;
        this.previousPlayerIndex = numberOfPlayers - 1;
        this.lastHidersTransportation = null;
        this.inSearch = false;
        this.searchInvokingPlayerIsHider = false;
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
        searchInvokingPlayerIsHider = playersOnBoard.playerIsHider(currentPlayerIndex);
        searchInvokingPlayerUsesCoalitionReduction = playersOnBoard.playerUsesCoalitionReduction(currentPlayerIndex);
        searchInvokingPlayerUsesMoveFiltering = playersOnBoard.playerUsesMoveFiltering(currentPlayerIndex);
    }

    public boolean searchInvokingPlayerUsesCoalitionReduction() {
        return searchInvokingPlayerUsesCoalitionReduction;
    }

    public void setSearchModeOff() {
        inSearch = false;
    }

    public boolean currentPlayerIsHider() {
        return playersOnBoard.playerIsHider(currentPlayerIndex);
    }

    public boolean previousPlayerIsHider() {
        return playersOnBoard.playerIsHider(previousPlayerIndex);
    }

    public boolean currentPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(currentPlayerIndex);
    }

    public boolean previousPlayerIsHuman() {
        return playersOnBoard.playerIsHuman(previousPlayerIndex);
    }

    private boolean inSearchFromSeekersPov() {
        return inSearch && !searchInvokingPlayerIsHider;
    }

    public String printNewRound() {
        String string = null;
        if (currentPlayerIsHider()) {
            string = ("\n ----------------------------\n");
            string += "         ROUND: " + currentRound;
            string += ("\n ----------------------------\n");
        }
        return string;
    }

    public boolean isHiderSurfacesRound() {
        return HIDER_SURFACES_ROUNDS.contains(currentRound);
    }

    public boolean isTerminal() {
        return seekersWon() || hiderWon();
    }

    public boolean seekersWon() {
        if (inSearchFromSeekersPov())
            return playersOnBoard.anySeekerOnHidersMostProbablePosition();
        else
            return playersOnBoard.anySeekerOnHidersActualPosition();
    }

    public boolean hiderWon() {
        return currentRound == MAX_NUMBER_OF_ROUNDS;
    }

    public boolean seekerWon(Seeker seeker) {
        if (inSearchFromSeekersPov())
            return playersOnBoard.seekerOnHidersMostProbablePosition(seeker);
        else
            return playersOnBoard.seekerOnHidersActualPosition(seeker);
    }

    public State performActionForCurrentAgent(Action action) {
        validateIsAvailableAction(action);
        if (inSearchFromSeekersPov())
            playersOnBoard.movePlayerFromSeekersPov(currentPlayerIndex, action);
        else
            playersOnBoard.movePlayerFromActualPosition(currentPlayerIndex, action);
        if (currentPlayerIsHider())
            lastHidersTransportation = action.getTransportation();
        setHidersMostProbablePosition(lastHidersTransportation);
        prepareStateForNextPlayer();
        performDoubleMoveIfShould();
        return this;
    }

    private void validateIsAvailableAction(Action action) {
        if (!isAvailableAction(action)) {
            throw new IllegalArgumentException("Error: invalid action passed as function parameter");
        }
    }

    private boolean isAvailableAction(Action action) {
        return getAvailableActionsForCurrentAgent().contains(action);
    }

    private void setHidersMostProbablePosition(Action.Transportation transportation) {
        if (currentPlayerIsHider())
            setHidersMostProbablePositionAfterHider(transportation);
        else
            playersOnBoard.removeCurrentSeekersPositionFromPossibleHidersPositions(currentPlayerIndex);
    }

    private void setHidersMostProbablePositionAfterHider(Action.Transportation transportation) {
        if (isHiderSurfacesRound())
            playersOnBoard.setHidersActualAsMostProbablePosition();
        else
            playersOnBoard.recalculateHidersMostProbablePosition(transportation);
    }

    private void  performDoubleMoveIfShould() {
        if (shouldCheckForHidersDoubleMoveAutomatically()) {
            Hider hider = (Hider)getPreviousAgent();
            if (hider.shouldUseDoubleMove(playersOnBoard, searchInvokingPlayerUsesMoveFiltering)) {
                skipAllSeekers();
                hider.removeDoubleMoveCard();
            }
        }
    }

    private boolean shouldCheckForHidersDoubleMoveAutomatically() {
        return previousPlayerIsHider() && (previousPlayerIsHuman() && inSearch || !previousPlayerIsHuman());
    }

    public void skipAllSeekers() {
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
        if (inSearchFromSeekersPov())
            availableActions = playersOnBoard.getAvailableActionsFromSeekersPov(currentPlayerIndex);
        else
            availableActions = playersOnBoard.getAvailableActionsForActualPosition(currentPlayerIndex);
        availableActions = addHidersBlackFairActions(availableActions);
        return availableActions;
    }

    private List<Action> addHidersBlackFairActions(List<Action> actions) {
        if (currentPlayerIsHider()) {
            if (notHumanInSearch())
                return addBlackFareActionsIfAvailableTickets(
                        (Hider) playersOnBoard.getPlayerAtIndex(currentPlayerIndex), actions);
            else
                return addBlackFareActionsForHiderIfOptimal(
                        (Hider) playersOnBoard.getPlayerAtIndex(currentPlayerIndex), actions);
        }
        return actions;
    }

    private boolean notHumanInSearch() {
        return currentPlayerIsHuman() && !inSearch;
    }

    List<Action> addBlackFareActionsIfAvailableTickets(Hider hider, List<Action> actions) {
        if (hider.hasBlackFareTicket())
            return addBlackFareActions(actions);
        return actions;
    }

    List<Action> addBlackFareActionsForHiderIfOptimal(Hider hider, List<Action> actions) {
        if (hider.shouldUseBlackfareTicket(currentRound, actions, searchInvokingPlayerUsesMoveFiltering))
            return addBlackFareActions(actions);
        return actions;
    }

    private List<Action> addBlackFareActions(List<Action> actions) {
        List<Action> blackFareActions = generateBlackfareActions(actions);
        actions.addAll(blackFareActions);
        return removeDuplicates(actions);
    }

    private List<Action> generateBlackfareActions(List<Action> actions) {
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

    public void updateHidersProbablePosition() {
        playersOnBoard.fixHidersProbablePosition();
    }

    public State copy(){
        return DeepCopy.copy(this);
    }
}