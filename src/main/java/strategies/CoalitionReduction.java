package main.java.strategies;

import main.java.model.State;
import main.java.players.Detective;

public class CoalitionReduction {

    private static final double COALITION_REDUCTION_PARAMETER = 0.25;

    public static double getCoalitionReductionRewardFromTerminalState(State state, Detective detective) {
        if (state.detectiveWon(detective))
            return 1;
        else if (state.detectiveWon())
            return 1 - COALITION_REDUCTION_PARAMETER;
        else
            return 0;
    }

    public static double getNormalRewardFromTerminalState(State state) {
        if (state.detectiveWon())
            return 1;
        else
            return 0;
    }
}