package main.java.players;

import main.java.model.Action;
import main.java.model.PlayersOnBoard;
import main.java.model.State;
import main.java.strategies.MoveFiltering;
import main.java.strategies.Playouts;

import java.util.List;

public class Hider extends Player {

    private static final int TAXI_TICKETS = 4;
    private static final int BUS_TICKETS = 3;
    private static final int METRO_TICKETS = 3;

    private int doubleMoveCards;
    private int blackFareTickets;

    public Hider(Operator operator, Playouts.Uses playout, boolean canUseCoalitionReduction, boolean useMoveFiltering) {
        super(operator, Type.HIDER, TAXI_TICKETS, BUS_TICKETS, METRO_TICKETS, playout, canUseCoalitionReduction, useMoveFiltering);
        this.doubleMoveCards = 2;
        this.blackFareTickets = 5;
    }

    public void removeDoubleMoveCard() {
        doubleMoveCards--;
    }

    public void removeBlackFareTicket() {
        blackFareTickets--;
    }

    public boolean hasDoubleMoveCard() {
        return doubleMoveCards > 0;
    }

    public boolean hasBlackFareTicket() {
        return blackFareTickets > 0;
    }

    @Override
    public void addTicket(Action.Transportation transportation) {
        super.addTicket(transportation);
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
        if (state.hiderWon())
            return 1;
        else
            return 0;
    }

    public boolean shouldUseBlackfareTicket(int currentRound, List<Action> actions,
                                            boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasBlackFareTicket() && MoveFiltering.optimalToUseBlackFareTicket(currentRound, actions);
        else
            return hasBlackFareTicket() && MoveFiltering.shouldUseBlackFareTicketGreedy();
    }

    public boolean shouldUseDoubleMove(PlayersOnBoard playersOnBoard,
                                       boolean searchInvokingPlayerUsesMoveFiltering) {
        if (searchInvokingPlayerUsesMoveFiltering)
            return hasDoubleMoveCard() && MoveFiltering.optimalToUseDoubleMoveCard(playersOnBoard);
        else
            return hasDoubleMoveCard() && MoveFiltering.shouldUseDoubleMoveCardGreedy();
    }

    @Override
    public String toString() {
        return "Criminel";
    }
}