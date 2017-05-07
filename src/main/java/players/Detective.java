package main.java.players;

import main.java.model.Action;
import main.java.model.State;
import main.java.strategies.CoalitionReduction;
import main.java.strategies.Playouts;

public class Detective extends Player {

    private static final int TAXI_TICKETS = 10;
    private static final int BUS_TICKETS = 8;
    private static final int UNDERGROUND_TICKETS = 4;

    public enum Color {
        ROSE, BLEU, JAUNE, ROUGE, VERT
    }

    private final Color color;

    public Detective(boolean isHuman, Color color, boolean canUseCoalitionReduction, boolean useMoveFiltering) {
        super(isHuman, Role.DETECTIVE, TAXI_TICKETS, BUS_TICKETS, UNDERGROUND_TICKETS, canUseCoalitionReduction, useMoveFiltering);
        this.color = color;
    }

    @Override
    protected Action getActionForCriminalFromStatesAvailableActionsForSimulation(State state) {
        return Playouts.getGreedyBiasedActionForCriminal(state);
    }

    @Override
    protected Action getActionForDetectiveFromStatesAvailableActionsForSimulation(State state) {
        return Playouts.getGreedyBiasedActionForDetective(state);
    }

    @Override
    public double getRewardFromTerminalState(State state) {
        if (state.searchInvokingPlayerUsesCoalitionReduction())
            return CoalitionReduction.getCoalitionReductionRewardFromTerminalState(state, this);
        else
            return CoalitionReduction.getNormalRewardFromTerminalState(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Detective detective = (Detective) o;
        return color == detective.color;
    }

    @Override
    public int hashCode() {
        return color != null ? color.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Détective " + color;
    }
}