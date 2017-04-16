package main.java.model.player;

import main.java.controller.Controller;
import main.java.model.*;

public class Hider extends Player implements IA {

    private static Controller currentController;
    private int blackTickets;

    public Hider(Node position) {
        super(position);
    }

    public int getBlackTickets() {
        return blackTickets;
    }

    public void setBlackTickets(int blackTickets) {
        this.blackTickets = blackTickets;
    }

    public boolean useBlackTicket() {
        if (blackTickets > 0) {
            blackTickets--;
            //retrieve the last move and change its type to black
            Move oldMove = prevPositions.getLast();
            Move newMove = new Move(oldMove.getNode(), Transport.BLACK);
            prevPositions.removeLast();
            prevPositions.add(newMove);
            return true;
        }
        return false;
    }

    public Transport changePosition(Node n) {
        Transport type = Transport.NONE;
        Link[] lk = position.getLinks();
        position = n;
        for (int i = 0; i < lk.length; i++)
            if (n.equals(lk[i].getToNode()))
                type = lk[i].getType();
        prevPositions.add(new Move(n.getId(), type));
        if (type == Transport.FERRY){
            if (blackTickets > 0) blackTickets --;
            else throw new IllegalArgumentException();
        }
        return type;
    }

    @Override
    public Move getNextMove(Controller controller) {
        Link link = getPosition().getLinks()[0];
        changePosition(link.getToNode());
        return new Move(link.getToNode().getId(),link.getType());
    }
}
