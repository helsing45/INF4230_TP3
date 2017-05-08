package main.java.view;

import javax.swing.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private static final int DEFAULT_DETECTIVE_COUNT = 4, DEFAULT_ITERATION_COUNT = 5;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton humanDetectiveButton;
    private JRadioButton computerDetectiveButton;
    private JSpinner detectiveCountSpinner;
    private JRadioButton computerCriminalButton;
    private JRadioButton humanCriminalButton;
    private JCheckBox debugMode;
    private JSpinner iterationCountSpinner;
    private Listener listener;

    public SettingsDialog(Listener listener) {
        this.listener = listener;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        ButtonGroup detective = new ButtonGroup();
        detective.add(humanDetectiveButton);
        detective.add(computerDetectiveButton);

        ButtonGroup criminal = new ButtonGroup();
        criminal.add(computerCriminalButton);
        criminal.add(humanCriminalButton);

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

        detectiveCountSpinner.setModel(new SpinnerNumberModel(DEFAULT_DETECTIVE_COUNT, 1.0, 5.0, 1.0));
        iterationCountSpinner.setModel(new SpinnerNumberModel(DEFAULT_ITERATION_COUNT, 1.0, 20000, 1.0));
    }

    private void onOK() {
        Setting setting = new Setting();
        setting.setCriminalHuman(humanCriminalButton.isSelected());
        setting.setDetectiveHuman(humanDetectiveButton.isSelected());
        setting.setDebugMode(debugMode.isSelected());
        setting.setDetectiveCount(((Double) detectiveCountSpinner.getValue()).intValue());
        setting.setIterationCount(((Double) iterationCountSpinner.getValue()).intValue());
        listener.onSettingsChange(setting);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public boolean isCriminalIsComputer() {
        return computerCriminalButton.isSelected();
    }

    public boolean isDetectiveIsComputer() {
        return computerDetectiveButton.isSelected();
    }

    public int getDetectiveCount() {
        return ((Double) detectiveCountSpinner.getValue()).intValue();
    }


    public static class Setting {
        boolean isCriminalHuman, isDetectiveHuman, debugMode;
        int detectiveCount, iterationCount;

        public Setting() {
            isCriminalHuman = false;
            isDetectiveHuman = false;
            debugMode = false;
            detectiveCount = DEFAULT_DETECTIVE_COUNT;
            iterationCount = DEFAULT_ITERATION_COUNT;
        }

        public boolean isCriminalHuman() {
            return isCriminalHuman;
        }

        public void setCriminalHuman(boolean criminalHuman) {
            isCriminalHuman = criminalHuman;
        }

        public boolean isDetectiveHuman() {
            return isDetectiveHuman;
        }

        public void setDetectiveHuman(boolean detectiveHuman) {
            isDetectiveHuman = detectiveHuman;
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

        public int getIterationCount() {
            return iterationCount;
        }

        public void setIterationCount(int iterationCount) {
            this.iterationCount = iterationCount;
        }
    }

    public interface Listener {
        void onSettingsChange(Setting setting);
    }
}
