package main.java.model.player;

import main.java.controller.Controller;
import main.java.model.Link;
import main.java.model.Move;
import main.java.model.Node;
import main.java.model.Transport;

import java.util.TreeSet;

public class Seeker extends Player {
    private int taxiTickets;
    private int busTickets;
    private int ugTickets;

    public Seeker(Node position) {
        super(position);
        taxiTickets = 10;
        busTickets = 8;
        ugTickets = 4;
    }

    public Seeker(Seeker d) {
        super();
        this.taxiTickets = d.taxiTickets;
        this.busTickets = d.busTickets;
        this.ugTickets = d.ugTickets;
        this.position = d.position;
    }

    public int getTicketCount(Transport transport) {
        switch (transport) {
            case TAXI:
                return taxiTickets;
            case BUS:
                return busTickets;
            case UG:
                return ugTickets;
            default:
                return 0;
        }
    }

    /**
     * Indique s il y a au moins une possibilite de bouger
     */
    public boolean canMove(Controller board) {
        Node n = getPosition();
        Link[] links = n.getLinks();
        boolean canMove = false;
        for (Link link : links) {
            if (canUse(board, link))
                canMove = true;
        }
        return canMove;
    }

    public boolean canUse(Controller controller, Link link) {
        boolean canGoToThisNode = true;
        Node toNode = link.getToNode();
        Seeker[] seekers = controller.getDetectives();
        for (Seeker seeker : seekers) {
            if (toNode.equals(seeker.getPosition())) {
                canGoToThisNode = false;
            }
        }
        return getTicketCount(link.getType()) > 0 && canGoToThisNode;
    }

    public TreeSet<Move> getPossibleMoves(Controller board) {
        if (!canMove(board)) return null;

        Node n = getPosition();
        Link[] links = n.getLinks();
        TreeSet<Move> possibleMoves = new TreeSet<>();
        for (Link link : links) {
            //cannot use ferry, so ignore if the link is of type ferry
            if (link.getType() == Transport.FERRY) continue;
            if(canUse(board,link)){
                possibleMoves.add(new Move(link.getToNode().getId(), link.getType()));
            }
        }
        return possibleMoves;
    }

    public void change(Node n, Transport ticket) {
        position = n;
        if (ticket == Transport.TAXI) taxiTickets --;
        else if (ticket == Transport.BUS) busTickets --;
        else if (ticket == Transport.UG) ugTickets --;
    }

    public void changePosition(Node n, Transport ticket){
        prevPositions.add(new Move(n.getId(), ticket));
        change(n,ticket);
    }
}
