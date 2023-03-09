package GUI;

import Interpreter.VirtualMachine;

import javax.swing.*;
import java.awt.*;

public class FlagsPanel extends JPanel {
    VirtualMachine vm;

    private final JCheckBox carryBox =    new JCheckBox("Carry");
    private final JCheckBox zeroBox =     new JCheckBox("Zero");
    private final JCheckBox overflowBox = new JCheckBox("Overflow");
    private final JCheckBox negativeBox = new JCheckBox("Negative");

    public FlagsPanel(VirtualMachine vm) {
        this.vm = vm;

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        setLayout(new FlowLayout(FlowLayout.RIGHT));

        innerPanel.add(carryBox);
        innerPanel.add(zeroBox);
        innerPanel.add(overflowBox);
        innerPanel.add(negativeBox);

        carryBox.setEnabled(false);
        zeroBox.setEnabled(false);
        overflowBox.setEnabled(false);
        negativeBox.setEnabled(false);

        this.add(innerPanel);
        updateFlags();
    }

    public void updateFlags() {
        carryBox.setSelected(vm.carry);
        zeroBox.setSelected(vm.zero);
        overflowBox.setSelected(vm.overflow);
        negativeBox.setSelected(vm.negative);
    }
}
