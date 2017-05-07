package main.java.players;

import main.java.model.Action;
import main.java.model.State;

import java.io.Serializable;

public abstract class Player implements Serializable {


    public static java.awt.Color[] colors = new java.awt.Color[]{
            java.awt.Color.BLACK,
            java.awt.Color.MAGENTA,
            java.awt.Color.BLUE,
            java.awt.Color.YELLOW,
            java.awt.Color.RED,
            java.awt.Color.GREEN
    };

    public enum Role {
        CRIMINAL, DETECTIVE
    }

    private boolean isHuman;
    private final Role role;
    private int taxiTickets;
    private int busTickets;
    private int metroTickets;
    private final boolean useCoalitionReduction, useMoveFiltering;

    protected Player(boolean isHuman, Role role, int taxiTickets, int busTickets, int metroTickets, boolean useCoalitionReduction, boolean useMoveFiltering) {
        this.isHuman = isHuman;
        this.role = role;
        this.taxiTickets = taxiTickets;
        this.busTickets = busTickets;
        this.metroTickets = metroTickets;
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

    public boolean isCriminal() {
        return role == Role.CRIMINAL;
    }

    public boolean isDetective() {
        return role == Role.DETECTIVE;
    }

    public boolean isHuman() {
        return isHuman;
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

    public boolean usesCoalitionReduction() {
        return useCoalitionReduction;
    }

    public boolean usesMoveFiltering() {
        return useMoveFiltering;
    }

    private Action getActionForCurrentPlayerType(State state) {
        if (state.currentPlayerIsCriminal())
            return getActionForCriminalFromStatesAvailableActionsForSimulation(state);
        else
            return getActionForDetectiveFromStatesAvailableActionsForSimulation(state);
    }

    protected abstract Action getActionForCriminalFromStatesAvailableActionsForSimulation(State state);

    protected abstract Action getActionForDetectiveFromStatesAvailableActionsForSimulation(State state);

    public abstract double getRewardFromTerminalState(State terminalState);
}