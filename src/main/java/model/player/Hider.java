package main.java.model.player;

import main.java.model.Node;

public class Hider extends Player {

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
}
