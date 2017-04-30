package main.java.view;

import main.java.BoardGame;
import main.java.model.Action;
import main.java.model.HistoryTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TableDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    public TableDialog(JFrame parentFrame, String title, List<Action[]> history, BoardGame playGame, boolean revealAll) {
        super(parentFrame, title, false);
        setResizable(true);
        TableModel model = new HistoryTableModel(history, revealAll);
        JTable table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(playGame);
        setSize(500, table.getRowHeight() * (history.size() + 4));
        getContentPane().add(new JScrollPane(table));
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent ae) {
        dispose();
    }
}