package main.java.model;

import main.java.controller.Controller;
import main.java.model.player.Hider;

import javax.swing.table.AbstractTableModel;

public class PreviousMoves extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    protected Controller controller;
    protected int numDetectives;
    boolean revealAll;

    public PreviousMoves(Controller b, boolean all) {
        controller = b;
        numDetectives = controller.getDetectives().length;
        revealAll = all;
    }

    public int getRowCount() {
        return controller.getCurrentMoves();
    }

    public int getColumnCount() {
        // + 1 because the first entry will be for Mr. X
        return numDetectives + 1;
    }

    public Object getValueAt(int r, int c) {
        String pos = "";
        if (c > 0) {
            // information of detective
            int ln = controller.getDetectives()[c - 1].getPrevPos().size();
            if (r + 1 < ln)
                pos = controller.getDetectives()[c - 1].getPrevPos().get(r + 1).toString();
        } else {
            // information of Mr. X
            Hider fg = controller.getMrX();
            if (!controller.isNeedToBeReveal(r + 1) && !revealAll)
                pos += "" + fg.getPrevPos().get(r + 1).toString();
            else
                pos += fg.getPrevPos().get(r + 1).toString();
        }
        return pos;
    }

    public String getColumnName(int c) {
        return c > 0 ? String.format("Detective %d",c): "Mr X";
    }
}