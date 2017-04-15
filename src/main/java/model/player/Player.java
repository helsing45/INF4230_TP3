package main.java.model.player;

import main.java.model.Move;
import main.java.model.Node;
import main.java.model.Transport;

import java.util.LinkedList;

public abstract class Player {
    protected Node position;
    protected LinkedList<Move> prevPositions;

    public Player(Node position) {
        this.position = position;
        prevPositions = new LinkedList<>();
        prevPositions.add(new Move(position.getPosition(), Transport.NONE));
    }

    public Node getPosition() {
        return position;
    }

    public LinkedList<Move> getPrevPos() {
        return prevPositions;
    }

    public void change(Node n) {
        position = n;
    }
}
