package main.java.model;

import main.java.players.Criminal;
import main.java.players.Player;
import main.java.players.Detective;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayersOnBoard implements Serializable {
    public static final int NUMBER_OF_PLAYERS = 6;
    private static final int CRIMINAL_INDEX = 0;
    private static final int SKIP_CRIMINAL = 1;
    private static final List<Integer> POSSIBLE_STARTING_POSITIONS = new ArrayList<Integer>(
            Arrays.asList(13, 26, 34, 50, 53, 62, 91, 94, 103, 112, 117, 132, 138, 141, 155, 174, 197, 198));
    private static final double[] DISTANCE_TO_CRIMINAL_PROBABILITIES = {0.196, 0.671, 0.540, 0.384, 0.196};

    private BoardGame board;
    private Player[] players;
    private int[] playersActualPositions;
    private List<Integer> criminalsPossiblePositions;
    private int criminalsMostProbablePosition;
    private int criminalsMostProbablePositionPreviousRound;

    private PlayersOnBoard() {
    }

    public PlayersOnBoard(Player[] players) {
        BoardGame board = new BoardGame();
        int[] playersPositions = generateRandomPlayersPositions(players.length);
        List<Integer> criminalsPossibleLocations = calculateInitialCriminalsPossibleLocations(playersPositions);
        Collections.shuffle(criminalsPossibleLocations);
        int criminalsMostProbablePosition = criminalsPossibleLocations.get(0);
        this.board = board;
        this.players = players;
        this.playersActualPositions = playersPositions;
        this.criminalsPossiblePositions = criminalsPossibleLocations;
        this.criminalsMostProbablePosition = criminalsMostProbablePosition;
    }
    public Point getLocationOf(int playerId){
        return board.getPoint(playersActualPositions[playerId]);
    }

    private static int[] generateRandomPlayersPositions(int numberOfPlayers) {
        Collections.shuffle(POSSIBLE_STARTING_POSITIONS);
        return IntStream.range(0, numberOfPlayers)
                .map(POSSIBLE_STARTING_POSITIONS::get).toArray();
    }

    private static List<Integer> calculateInitialCriminalsPossibleLocations(int[] playersPositions) {
        List<Integer> criminalsPossibleLocations = new ArrayList<>(POSSIBLE_STARTING_POSITIONS);
        criminalsPossibleLocations.removeAll(getDetectivesPositions(playersPositions));
        return criminalsPossibleLocations;
    }

    private static List<Integer> getDetectivesPositions(int[] playersPositions) {
        return Arrays.stream(playersPositions)
                .skip(SKIP_CRIMINAL).boxed().collect(Collectors.toList());
    }

    public double criminalsAverageDistanceToDetectives(Player.Role role) {
        int criminalsPosition = playersActualPositions[CRIMINAL_INDEX];
        return Arrays.stream(playersActualPositions)
                .skip(SKIP_CRIMINAL)
                .map(position -> board.shortestDistanceBetween(criminalsPosition, position, role))
                .average().getAsDouble();
    }

    protected int getNumberOfPlayers() {
        return players.length;
    }

    protected boolean playerIsCriminal(int playerIndex) {
        return getPlayerAtIndex(playerIndex).isCriminal();
    }

    protected boolean playerIsHuman(int playerIndex) {
        return getPlayerAtIndex(playerIndex).isHuman();
    }

    protected boolean playerUsesCoalitionReduction(int playerIndex) {
        return getPlayerAtIndex(playerIndex).usesCoalitionReduction();
    }

    protected boolean playerUsesMoveFiltering(int playerIndex) {
        return getPlayerAtIndex(playerIndex).usesMoveFiltering();
    }

    protected Player getPlayerAtIndex(int playerIndex) {
        return players[playerIndex];
    }

    public String getPlayer(int playerIndex){
        return players[playerIndex].toString();
    }

    public String getPlayerInfo(int playerIndex, boolean revealAll){
        String string =  players[playerIndex] + " sur ";
        string += playerIndex != CRIMINAL_INDEX || revealAll ? playersActualPositions[playerIndex] :"inconnu";
         string += " (taxi : " + players[playerIndex].getTaxiTickets() +
                ", bus : " + players[playerIndex].getBusTickets() +
                ", metro : " + players[playerIndex].getMetroTickets() + ") ";
         return string;
    }

    protected boolean anyDetectiveOnCriminalsMostProbablePosition() {
        return anyDetectiveOnPosition(criminalsMostProbablePositionPreviousRound);
    }

    protected boolean anyDetectiveOnCriminalsActualPosition() {
        return anyDetectiveOnPosition(playersActualPositions[CRIMINAL_INDEX]);
    }

    protected boolean anyDetectiveOnPosition(int position) {
        for (int index = 0; index < playersActualPositions.length; index++) {
            if(index != CRIMINAL_INDEX && playersActualPositions[index] == position){
                return true;
            }
        }
        return false;
    }

    protected boolean seekerOnCriminalsMostProbablePosition(Detective detective) {
        return seekerOnPosition(detective, criminalsMostProbablePositionPreviousRound);
    }

    protected boolean seekerOnCriminalsActualPosition(Detective detective) {
        return seekerOnPosition(detective, playersActualPositions[CRIMINAL_INDEX]);
    }

    protected boolean seekerOnPosition(Detective detective, int position) {
        int seekerIndex = IntStream.range(1, players.length).filter(i -> players[i].equals(detective)).findFirst().getAsInt();
        return playersActualPositions[seekerIndex] == position;
    }

    protected List<Action> getAvailableActionsFromDetectivesPov(int playerIndex) {
        if (playerIsCriminal(playerIndex))
            return getAvailableActionsForCriminalFromDetectivesPov(playerIndex);
        else
            return getAvailableActionsForActualPosition(playerIndex);
    }

    protected List<Action> getAvailableActionsForCriminalFromDetectivesPov(int playerIndex) {
        List<Action> possibleActions = board.getActionsForPosition(criminalsMostProbablePosition);
        return getAvailableActionsFromPossibleActionsForPlayer(possibleActions, playerIndex);
    }

    protected List<Action> getAvailableActionsForActualPosition(int playerIndex) {
        List<Action> possibleActions = board.getActionsForPosition(playersActualPositions[playerIndex]);
        return getAvailableActionsFromPossibleActionsForPlayer(possibleActions, playerIndex);
    }

    private List<Action> getAvailableActionsFromPossibleActionsForPlayer(
            List<Action> possibleActions, int playerIndex) {
        List<Action> availableActions = new ArrayList<>(possibleActions);
        availableActions = removeActionsWithOccupiedDestinations(availableActions);
        availableActions = removeActionsBecauseOfNoPlayersTickets(availableActions, playerIndex);
        if (!playerIsCriminal(playerIndex))
            availableActions = removeTransportationActions(Action.Transportation.PASSE_DROIT, availableActions);
        else
            availableActions = fixCriminalsBlackFareActions((Criminal) players[playerIndex], availableActions);
        return availableActions;
    }

    private List<Action> removeActionsWithOccupiedDestinations(List<Action> actions) {
        return actions.stream()
                .filter(this::actionsDestinationNotOccupied).collect(Collectors.toList());
    }

    private boolean actionsDestinationNotOccupied(Action action) {
        int destinationPosition = action.getDestination();
        return Arrays.stream(playersActualPositions)
                .skip(SKIP_CRIMINAL)
                .allMatch(position -> position != destinationPosition);
    }

    private List<Action> removeActionsBecauseOfNoPlayersTickets(List<Action> actions, int playerIndex) {
        Player player = players[playerIndex];
        if (!player.hasTaxiTickets())
            actions = removeTransportationActions(Action.Transportation.TAXI, actions);
        if (!player.hasBusTickets())
            actions = removeTransportationActions(Action.Transportation.BUS, actions);
        if (!player.hasUndergroundTickets())
            actions = removeTransportationActions(Action.Transportation.METRO, actions);
        return actions;
    }

    private List<Action> fixCriminalsBlackFareActions(Criminal criminal, List<Action> actions) {
        if (!criminal.hasPasseDroit())
            actions = removeTransportationActions(Action.Transportation.PASSE_DROIT, actions);
        return actions;
    }

    private List<Action> removeTransportationActions(Action.Transportation transportation, List<Action> actions) {
        return actions.stream()
                .filter(action -> !action.isTransportationAction(transportation))
                .collect(Collectors.toList());
    }

    protected void movePlayerFromActualPosition(int playerIndex, Action action) {
        removeTransportationCard(playerIndex, action);
        playersActualPositions[playerIndex] = action.getDestination();
    }

    protected void movePlayerFromDetectivesPov(int playerIndex, Action action) {
        removeTransportationCard(playerIndex, action);
        if (playerIsCriminal(playerIndex))
            criminalsMostProbablePosition = action.getDestination();
        else
            playersActualPositions[playerIndex] = action.getDestination();
    }

    private void removeTransportationCard(int playerIndex, Action action) {
        if (playerIsCriminal(playerIndex) && action.isTransportationAction(Action.Transportation.PASSE_DROIT)) {
            Criminal criminal = (Criminal)getPlayerAtIndex(CRIMINAL_INDEX);
            criminal.removePasseDroit();
        }
        else
            players[playerIndex].removeTicket(action.getTransportation());
        if (!playerIsCriminal(playerIndex)) {
            Criminal criminal = (Criminal)getPlayerAtIndex(CRIMINAL_INDEX);
            criminal.addTicket(action.getTransportation());
        }
    }

    protected void setCriminalsActualAsMostProbablePosition() {
        criminalsPossiblePositions = new ArrayList<>();
        criminalsPossiblePositions.add(playersActualPositions[CRIMINAL_INDEX]);
        criminalsMostProbablePositionPreviousRound = criminalsMostProbablePosition;
        criminalsMostProbablePosition = playersActualPositions[CRIMINAL_INDEX];
    }

    public int[] getPlayersActualPositions(){
        return playersActualPositions;
    }

    protected void recalculateCriminalsMostProbablePosition(Action.Transportation transportation) {
        criminalsPossiblePositions = recalculateCriminalsPossiblePositions(transportation);
        criminalsMostProbablePositionPreviousRound = criminalsMostProbablePosition;
        criminalsMostProbablePosition = getMostProbableCriminalsPosition();
    }

    protected void removeCurrentDetectivePositionFromPossibleCriminalPositions(int playerIndex) {
        criminalsPossiblePositions.remove(new Integer(playersActualPositions[playerIndex]));
        criminalsMostProbablePositionPreviousRound = criminalsMostProbablePosition;
        criminalsMostProbablePosition = getMostProbableCriminalsPosition();
    }

    private List<Integer> recalculateCriminalsPossiblePositions(Action.Transportation transportation) {
        List<Integer> newCriminalsPossiblePositions = new ArrayList<>();
        for (int position : criminalsPossiblePositions) {
            if (transportation == Action.Transportation.PASSE_DROIT)
                newCriminalsPossiblePositions.addAll(board.getDestinationsForPosition(position));
            else {
                newCriminalsPossiblePositions.addAll(
                        board.getTransportationDestinationsForPosition(transportation, position));
            }
        }
        newCriminalsPossiblePositions.removeAll(getDetectivesPositions(playersActualPositions));
        return new ArrayList<>(new LinkedHashSet<>(newCriminalsPossiblePositions));
    }

    private int getMostProbableCriminalsPosition() {
        if (criminalsPossiblePositions.size() < 1)
            return -1;
        else
            return getMostProbableCriminalsPositionConfidently();
    }

    private int getMostProbableCriminalsPositionConfidently() {
        double[] probabilities = setPositionsProbabilities();
        return rouletteWheelSelect(probabilities);
    }

    private double[] setPositionsProbabilities() {
        List<Integer> detectivesPositions = getDetectivesPositions(playersActualPositions);
        double[] probabilities = new double[criminalsPossiblePositions.size()];
        for (int i = 0; i < criminalsPossiblePositions.size(); i++) {
            int position = criminalsPossiblePositions.get(i);
            int minDistance = detectivesPositions.stream().min(((position1, position2) -> Integer.compare(
                    board.shortestDistanceBetween(position1, position, Player.Role.CRIMINAL),
                    board.shortestDistanceBetween(position2, position, Player.Role.CRIMINAL)))).get();
            int probabilityIndex = minDistance - 1;
            if (probabilityIndex > 4)
                probabilityIndex = 4;
            probabilities[i] = DISTANCE_TO_CRIMINAL_PROBABILITIES[probabilityIndex];
        }
        return probabilities;
    }

    private int rouletteWheelSelect(double[] probabilities) {
        boolean notAccepted = true;
        int chosen = 0;
        double max_weight = Arrays.stream(probabilities).max().getAsDouble();
        while (notAccepted){
            chosen = (int)(probabilities.length * Math.random());
            if(Math.random() < probabilities[chosen] / max_weight) {
                notAccepted = false;
            }
        }
        return criminalsPossiblePositions.get(chosen);
    }

    public void fixCriminalProbablePosition() {
        criminalsMostProbablePositionPreviousRound = criminalsMostProbablePosition;
    }

    public int shortestDistanceBetweenPositionAndCriminalMostProbablePosition(int position) {
        return board.shortestDistanceBetween(position, criminalsMostProbablePosition, Player.Role.DETECTIVE);
    }

    public int shortestDistanceBetweenPositionAndClosestDetective(int position) {
        return getDetectivesPositions(playersActualPositions).stream()
                .map(detectivesPosition -> board.shortestDistanceBetween(position, detectivesPosition, Player.Role.CRIMINAL))
                .min(Integer::compare).get();
    }
}