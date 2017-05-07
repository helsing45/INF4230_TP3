package main.java.strategies;

import main.java.model.State;
import main.java.players.Seeker;

public class CoalitionReduction {

    private static final double COALITION_REDUCTION_PARAMETER = 0.25;

    public static double getCoalitionReductionRewardFromTerminalState(State state, Seeker seeker) {
        if (state.seekerWon(seeker))
            return 1;
        else if (state.seekersWon())
            return 1 - COALITION_REDUCTION_PARAMETER;
        else
            return 0;
    }

    public static double getNormalRewardFromTerminalState(State state) {
        if (state.seekersWon())
            return 1;
        else
            return 0;
    }
}