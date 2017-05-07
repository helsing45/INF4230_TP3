package main.java.players;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;
import main.java.strategies.MoveFiltering;
import main.java.strategies.Playouts;

import java.util.List;

public class Criminal extends Player {
    private static final int TAXI_TICKETS = 4;
    private static final int BUS_TICKETS = 3;
    private static final int METRO_TICKETS = 3;
    private int doubleMoveCards;
    private int PasseDroits;

    public Criminal(boolean isHuman, boolean canUseCoalitionReduction, boolean useMoveFiltering) {
        super(isHuman, Role.CRIMINAL, TAXI_TICKETS, BUS_TICKETS, METRO_TICKETS, canUseCoalitionReduction, useMoveFiltering);
        this.doubleMoveCards = 2;
        this.PasseDroits = 5;
    }

    public void removeDoubleMoveCard() {
        doubleMoveCards--;
    }

    public void removePasseDroit() {
        PasseDroits--;
    }

    public boolean hasDoubleMoveCard() {
        return doubleMoveCards > 0;
    }

    public boolean hasPasseDroit() {
        return PasseDroits > 0;
    }

    @Override
    public void addTicket(Action.Transportation transportation) {
        super.addTicket(transportation);
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
        if (state.criminalWon()) return 1;
        else return 0;
    }

    public boolean shouldUsePasseDroit(int currentRound, List<Action> actions, boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasPasseDroit() && MoveFiltering.optimalToUseBlackFareTicket(currentRound, actions);
        else return hasPasseDroit() && MoveFiltering.shouldUseBlackFareTicketGreedy();
    }

    public boolean shouldUseDoubleMove(PlayersOnBoard playersOnBoard, boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasDoubleMoveCard() && MoveFiltering.optimalToUseDoubleMoveCard(playersOnBoard);
        else return hasDoubleMoveCard() && MoveFiltering.shouldUseDoubleMoveCardGreedy();
    }

    @Override
    public String toString() {
        return "Criminel";
    }
}