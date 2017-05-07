package main.java.model;

import java.io.Serializable;

public class Action implements Serializable{

    public enum Transportation {
        TAXI, BUS, METRO, PASSE_DROIT
    }

    private final Transportation transportation;
    private final int destination;

    public Action(Transportation transportation, int destination) {
        this.transportation = transportation;
        this.destination = destination;
    }

    public Transportation getTransportation() {
        return transportation;
    }

    public int getDestination() {
        return destination;
    }

    public boolean isTransportationAction(Transportation transportation) {
        return this.transportation == transportation;
    }

    protected Action generatePasseDroitAction() {
        return new Action(Transportation.PASSE_DROIT, this.destination);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        Action action = (Action) object;
        return destination == action.destination && transportation == action.transportation;
    }

    @Override
    public String toString() {
        return transportation + " jusqu'à " + destination;
    }
}