package main.java.players;

import main.java.model.Action;
import main.java.model.State;
import main.java.search.MctsDomainAgent;
import main.java.strategies.CoalitionReduction;
import main.java.strategies.MoveFiltering;
import main.java.strategies.Playouts;

public abstract class Player implements MctsDomainAgent<State> {

    public enum Operator {
        HUMAN, COMPUTER, RANDOM
    }

    public enum Type {
        HIDER, SEEKER
    }

    private final Operator operator;
    private final Type type;
    private int taxiTickets;
    private int busTickets;
    private int undergroundTickets;
    private final Playouts.Uses playout;
    private final CoalitionReduction.Uses coalitionReduction;
    private final MoveFiltering.Uses moveFiltering;

    protected Player(Operator operator, Type type, int taxiTickets, int busTickets, int undergroundTickets,
                     Playouts.Uses playout, CoalitionReduction.Uses coalitionReduction,
                     MoveFiltering.Uses moveFiltering) {
        this.operator = operator;
        this.type = type;
        this.taxiTickets = taxiTickets;
        this.busTickets = busTickets;
        this.undergroundTickets = undergroundTickets;
        this.playout = playout;
        this.coalitionReduction = coalitionReduction;
        this.moveFiltering = moveFiltering;
    }

    public int getTaxiTickets() {
        return taxiTickets;
    }

    public int getBusTickets() {
        return busTickets;
    }

    public int getUndergroundTickets() {
        return undergroundTickets;
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

    public boolean isRandom() {
        return operator == Operator.RANDOM;
    }

    public boolean hasTaxiTickets() {
        return taxiTickets > 0;
    }

    public boolean hasBusTickets() {
        return busTickets > 0;
    }

    public boolean hasUndergroundTickets() {
        return undergroundTickets > 0;
    }

    public void removeTicket(Action.Transportation transportation) {
        switch (transportation) {
            case TAXI:
                taxiTickets--;
                break;
            case BUS:
                busTickets--;
                break;
            case UNDERGROUND:
                undergroundTickets--;
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
            case UNDERGROUND:
                undergroundTickets++;
        }
    }

    @Override
    public final State getTerminalStateByPerformingSimulationFromState(State state) {
        while (!state.isTerminal()) {
            Action action = getActionForCurrentPlayerType(state);
            if (action != null) {
                state.performActionForCurrentAgent(action);
            }
            else
                state.skipCurrentAgent();
        }
        return state;
    }

    public boolean usesBiasedPlayout () {
        switch (playout) {
            case BASIC:
                return false;
            default:
                return true;
        }
    }

    public boolean usesCoalitionReduction() {
        switch (coalitionReduction) {
            case YES:
                return true;
            default:
                return false;
        }
    }

    public boolean usesMoveFiltering() {
        switch (moveFiltering) {
            case YES:
                return true;
            default:
                return false;
        }
    }

    private Action getActionForCurrentPlayerType(State state) {
        if (state.currentPlayerIsHider())
            return getActionForHiderFromStatesAvailableActionsForSimulation(state);
        else
            return getActionForSeekerFromStatesAvailableActionsForSimulation(state);
    }

    protected abstract Action getActionForHiderFromStatesAvailableActionsForSimulation(State state);

    protected abstract Action getActionForSeekerFromStatesAvailableActionsForSimulation(State state);
}