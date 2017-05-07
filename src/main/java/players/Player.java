package main.java.players;

import main.java.model.Action;
import main.java.model.State;
import main.java.strategies.Playouts;

import java.io.Serializable;

public abstract class Player implements Serializable {

    public enum Operator {
        HUMAN, COMPUTER
    }

    public enum Type {
        HIDER, SEEKER
    }

    private final Operator operator;
    private final Type type;
    private int taxiTickets;
    private int busTickets;
    private int metroTickets;
    private final Playouts.Uses playout;
    private final boolean useCoalitionReduction, useMoveFiltering;

    protected Player(Operator operator, Type type, int taxiTickets, int busTickets, int metroTickets, Playouts.Uses playout, boolean useCoalitionReduction, boolean useMoveFiltering) {
        this.operator = operator;
        this.type = type;
        this.taxiTickets = taxiTickets;
        this.busTickets = busTickets;
        this.metroTickets = metroTickets;
        this.playout = playout;
        this.useCoalitionReduction = useCoalitionReduction;
        this.useMoveFiltering = useMoveFiltering;
    }

    public int getTaxiTickets() {
        return taxiTickets;
    }

    public int getBusTickets() {
        return busTickets;
    }

    public int getMetroTickets() {
        return metroTickets;
    }

    public boolean isHider() {
        return type == Type.HIDER;
    }

    public boolean isSeeker() {
        return type == Type.SEEKER;
    }

    public boolean isHuman() {
        return operator == Operator.HUMAN;
    }

    public boolean hasTaxiTickets() {
        return taxiTickets > 0;
    }

    public boolean hasBusTickets() {
        return busTickets > 0;
    }

    public boolean hasUndergroundTickets() {
        return metroTickets > 0;
    }

    public void removeTicket(Action.Transportation transportation) {
        switch (transportation) {
            case TAXI:
                taxiTickets--;
                break;
            case BUS:
                busTickets--;
                break;
            case METRO:
                metroTickets--;
        }
    }

    protected void addTicket(Action.Transportation transportation) {
        switch (transportation) {
            case TAXI:
                taxiTickets++;
                break;
            case BUS:
                busTickets++;
                break;
            case METRO:
                metroTickets++;
        }
    }

    public final State getTerminalStateByPerformingSimulationFromState(State state) {
        while (!state.isTerminal()) {
            Action action = getActionForCurrentPlayerType(state);
            if (action != null) {
                state.performActionForCurrentAgent(action);
            } else
                state.skipCurrentAgent();
        }
        return state;
    }

    boolean usesBiasedPlayout() {
        return playout == Playouts.Uses.GREEDY;
    }

    public boolean usesCoalitionReduction() {
        return useCoalitionReduction;
    }

    public boolean usesMoveFiltering() {
        return useMoveFiltering;
    }

    private Action getActionForCurrentPlayerType(State state) {
        if (state.currentPlayerIsHider())
            return getActionForHiderFromStatesAvailableActionsForSimulation(state);
        else
            return getActionForSeekerFromStatesAvailableActionsForSimulation(state);
    }

    protected abstract Action getActionForHiderFromStatesAvailableActionsForSimulation(State state);

    protected abstract Action getActionForSeekerFromStatesAvailableActionsForSimulation(State state);

    public abstract double getRewardFromTerminalState(State terminalState);
}