package main.java.strategies;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;

import java.util.Collections;
import java.util.List;

public class Playouts {

    private static final double EPSILON = 0.25;

    public static Action getRandomAction(State state) {
        List<Action> availableActions = state.getAvailableActionsForCurrentAgent();
        Collections.shuffle(availableActions);
        if (availableActions.size() > 0)
            return availableActions.get(0);
        else
            return null;
    }

    public static Action getGreedyBiasedActionForCriminal(State state) {
        List<Action> actions = state.getAvailableActionsForCurrentAgent();
        if (actions.size() > 0) {
            if (shouldReturnBiasedAction())
                return getBiasedActionForCriminalConfidently(actions, state.getPlayersOnBoard());
            else
                return getRandomAction(state);
        }
        else
            return null;
    }

    public static Action getGreedyBiasedActionForDetective(State state) {
        List<Action> actions = state.getAvailableActionsForCurrentAgent();
        if (actions.size() > 0) {
            if (shouldReturnBiasedAction())
                return getBiasedActionForDetectiveConfidently(actions, state.getPlayersOnBoard());
            else
                return getRandomAction(state);
        }
        else
            return null;

    }

    private static boolean shouldReturnBiasedAction() {
        return Math.random() > EPSILON;
    }

    private static Action getBiasedActionForCriminalConfidently(List<Action> actions, PlayersOnBoard playersOnBoard) {
        return actions.stream()
                .max((action1, action2) -> Integer.compare(
                        playersOnBoard.shortestDistanceBetweenPositionAndClosestDetective(action1.getDestination()),
                        playersOnBoard.shortestDistanceBetweenPositionAndClosestDetective(action2.getDestination())))
                .get();
    }

    private static Action getBiasedActionForDetectiveConfidently(List<Action> actions, PlayersOnBoard playersOnBoard) {
        return actions.stream()
                .min((action1, action2) -> Integer.compare(
                        playersOnBoard.shortestDistanceBetweenPositionAndCriminalMostProbablePosition(
                                action1.getDestination()),
                        playersOnBoard.shortestDistanceBetweenPositionAndCriminalMostProbablePosition(
                                action2.getDestination())))
                .get();
    }
}