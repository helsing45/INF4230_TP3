package main.java.players;

import main.java.model.Action;
import main.java.model.State;
import main.java.strategies.CoalitionReduction;
import main.java.strategies.Playouts;

public class Seeker extends Player {

    private static final int TAXI_TICKETS = 10;
    private static final int BUS_TICKETS = 8;
    private static final int UNDERGROUND_TICKETS = 4;

    public enum Color {
        NOIRE, BLEU, JAUNE, ROUGE, VERT
    }

    private final Color color;

    public Seeker(Operator operator, Color color, Playouts.Uses playout, boolean canUseCoalitionReduction, boolean useMoveFiltering) {
        super(operator, Type.SEEKER, TAXI_TICKETS, BUS_TICKETS, UNDERGROUND_TICKETS, playout, canUseCoalitionReduction, useMoveFiltering);
        this.color = color;
    }

    @Override
    protected Action getActionForHiderFromStatesAvailableActionsForSimulation(State state) {
        if (this.usesBiasedPlayout())
            return Playouts.getGreedyBiasedActionForHider(state);
        else
            return Playouts.getRandomAction(state);
    }

    @Override
    protected Action getActionForSeekerFromStatesAvailableActionsForSimulation(State state) {
        if (this.usesBiasedPlayout())
            return Playouts.getGreedyBiasedActionForSeeker(state);
        else
            return Playouts.getRandomAction(state);
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
        Seeker seeker = (Seeker) o;
        return color == seeker.color;
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