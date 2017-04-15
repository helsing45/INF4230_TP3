package main.java.view;

import main.java.BoardGame;
import main.java.controller.Controller;
import main.java.model.PreviousMoves;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    public TableDialog(JFrame parentFrame, String title, Controller b, BoardGame playGame, boolean revealAll) {
        super(parentFrame, title, false);
        setResizable(true);
        TableModel model = new PreviousMoves(b, revealAll);
        JTable table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(playGame);
        setSize(500, table.getRowHeight() * (b.getCurrentMoves() + 4));
        getContentPane().add(new JScrollPane(table));
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent ae) {
        dispose();
    }
}