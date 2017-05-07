package main.java.model;

import main.java.players.Player;
import main.java.utilities.BoardFileParser;
import main.java.utilities.BoardPositionFileParser;
import main.java.utilities.DistancesFileParser;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class BoardGame implements Serializable{

    private static final String BOARD_NODE_LOCATION = "src/main/resources/board_position.xml";
    private static final String BOARD_FILE_NAME = "src/main/resources/board_file.xml";
    private static final String CRIMINALS_DISTANCES_FILE_NAME = "src/main/resources/criminals_distances_file.xml";
    private static final String DETECTIVES_DISTANCES_FILE_NAME = "src/main/resources/detectives_distances_file.xml";

    private final List<List<Action>> positionsActions;
    private final List<List<Integer>> criminalsDistances;
    private final List<List<Integer>> detectivesDistances;
    private final List<Point> locations;

    public BoardGame(){
        BoardFileParser boardFileParser = new BoardFileParser(BOARD_FILE_NAME);
        List<List<Action>> positionsActions = boardFileParser.getParsedData();

        BoardPositionFileParser boardLocationParser = new BoardPositionFileParser(BOARD_NODE_LOCATION);
        List<Point> locations = boardLocationParser.getParsedData();

        DistancesFileParser distancesFileParser = new DistancesFileParser(CRIMINALS_DISTANCES_FILE_NAME);
        List<List<Integer>> criminalsDistances = distancesFileParser.getParsedData();

        distancesFileParser = new DistancesFileParser(DETECTIVES_DISTANCES_FILE_NAME);
        List<List<Integer>> detectivesDistances = distancesFileParser.getParsedData();
        this.positionsActions = positionsActions;
        this.criminalsDistances = criminalsDistances;
        this.detectivesDistances = detectivesDistances;
        this.locations = locations;
    }

    public Point getPoint(int position){
        return locations.get(position - 1);
    }

    public List<Integer> getDestinationsForPosition(int position) {
        return getActionsForPosition(position).stream()
                .map(Action::getDestination)
                .collect(Collectors.toList());
    }

    public List<Action> getActionsForPosition(int position) {
        return positionsActions.get(position - 1);
    }

    public List<Integer> getTransportationDestinationsForPosition (Action.Transportation transportation, int position) {
        return getTransportationActionsForPosition(transportation, position).stream()
                .map(Action::getDestination)
                .collect(Collectors.toList());
    }

    public List<Action> getTransportationActionsForPosition(Action.Transportation transportation, int position) {
        return positionsActions.get(position - 1).stream()
                .filter(action -> action.getTransportation() == transportation)
                .collect(Collectors.toList());
    }

    public int shortestDistanceBetween(int position1, int position2, Player.Role role) {
        if (position1 == position2)
            return 0;
        else
            return shortestDistanceBetweenDifferent(position1, position2, role);
    }

    public int shortestDistanceBetweenDifferent(int position1, int position2, Player.Role role) {
        int index1, index2;
        if (position1 < position2) {
            index1 = position1 - 1;
            index2 = (position2 - position1) - 1;
        }
        else {
            index1 = position2 - 1;
            index2 = (position1 - position2) - 1;
        }
        if (role == Player.Role.CRIMINAL)
            return criminalsDistances.get(index1).get(index2);
        else
            return detectivesDistances.get(index1).get(index2);
    }
}