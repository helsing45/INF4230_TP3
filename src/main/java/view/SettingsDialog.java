package main.java.view;

import javax.swing.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton joueurDetectiveButton;
    private JRadioButton computerDetectiveButton;
    private JSpinner spinner1;
    private JRadioButton computerCriminalButton;
    private JRadioButton joueurCriminalButton;
    private Listener listener;

    public SettingsDialog(Listener listener) {
        this.listener = listener;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        ButtonGroup detective = new ButtonGroup();
        detective.add(joueurDetectiveButton);
        detective.add(computerDetectiveButton);

        ButtonGroup criminal = new ButtonGroup();
        criminal.add(computerCriminalButton);
        criminal.add(joueurCriminalButton);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        spinner1.setModel(new SpinnerNumberModel(4.0,1.0,5.0,1.0));
    }

    private void onOK() {
        listener.onSettingsChange(isCriminalIsComputer(),isDetectiveIsComputer(),getDetectiveCount());
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public boolean isCriminalIsComputer(){
        return computerCriminalButton.isSelected();
    }

    public boolean isDetectiveIsComputer(){
        return computerDetectiveButton.isSelected();
    }

    public int getDetectiveCount(){
        return ((Double)spinner1.getValue()).intValue();
    }

    public interface Listener{
        void onSettingsChange(boolean isCriminalIsComputer, boolean isDetectiveIsComputer, int detectiveCount);
    }
}
