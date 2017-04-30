package main.java.model;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class HistoryTableModel extends AbstractTableModel {

    private boolean revealAll;
    private List<Action[]> history;

    public HistoryTableModel(List<Action[]> history, boolean revealAll) {
        this.history = history;
        this.revealAll = revealAll;
    }

    @Override
    public int getRowCount() {
        return history.size();
    }

    @Override
    public int getColumnCount() {
        if (history.size() > 0) {
            return history.get(0).length;
        }
        return 0;
    }

    public boolean showLocation(int rowIndex, int columnIndex){
        return revealAll || (State.HIDER_SURFACES_ROUNDS.contains(rowIndex) && columnIndex == 0)|| columnIndex > 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Action action = history.get(rowIndex)[columnIndex];
        if(action == null) return "";
        return action.getTransportation().toString() + (showLocation(rowIndex, columnIndex) ? " jusqu'a "+ action.getDestination() : "");
    }
}
