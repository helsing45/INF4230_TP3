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
    private JCheckBox debugMode;
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
        Setting setting = new Setting();
        setting.setCriminalIsComputer(computerCriminalButton.isSelected());
        setting.setDetectiveIsComputer(computerDetectiveButton.isSelected());
        setting.setDebugMode(debugMode.isSelected());
        setting.setDetectiveCount(((Double)spinner1.getValue()).intValue());
        listener.onSettingsChange(setting);
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

    public static class Setting{
        boolean isCriminalIsComputer, isDetectiveIsComputer,debugMode;
        int detectiveCount;

        public Setting() {
            isCriminalIsComputer = true;
            isDetectiveIsComputer = true;
            debugMode = false;
            detectiveCount = 5;
        }

        public boolean isCriminalIsComputer() {
            return isCriminalIsComputer;
        }

        public void setCriminalIsComputer(boolean criminalIsComputer) {
            isCriminalIsComputer = criminalIsComputer;
        }

        public boolean isDetectiveIsComputer() {
            return isDetectiveIsComputer;
        }

        public void setDetectiveIsComputer(boolean detectiveIsComputer) {
            isDetectiveIsComputer = detectiveIsComputer;
        }

        public boolean isDebugMode() {
            return debugMode;
        }

        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }

        public int getDetectiveCount() {
            return detectiveCount;
        }

        public void setDetectiveCount(int detectiveCount) {
            this.detectiveCount = detectiveCount;
        }
    }

    public interface Listener{
        void onSettingsChange(Setting setting);
    }
}
