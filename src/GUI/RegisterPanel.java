package GUI;

import Assembler.Interpreter.VirtualMachine;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static GUI.RegisterViewType.*;

public class RegisterPanel extends JPanel implements ActionListener {
    VirtualMachine vm;
    private final JTextField[] registerTextFields = new JTextField[VirtualMachine.NUMBER_OF_REGISTERS];
    JComboBox<RegisterViewType> chooser = new JComboBox<>();

    public RegisterPanel(VirtualMachine vm) {
        this.vm = vm;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createChooserAndRegistersPanel());
    }

    private void updateRegister(int regNum) {
        RegisterViewType regView = (RegisterViewType) chooser.getSelectedItem();
        int regValue = vm.registers[regNum];
        boolean regChanged = vm.registersChanged[regNum];

        String text = "";
        switch (regView) {
            case DECIMAL:
                text = Integer.toString(regValue);
                break;
            case BINARY:
                text = Integer.toBinaryString(regValue);
                if (false /* SHOW LEADING ZEROS */) {
                    //text = String.format("%1$" + CONSTANTS.WORD_SIZE * 8 + "s", text).replace(" ", "0");
                }
                break;
            case HEX:
                text = "0x" + Integer.toHexString(regValue).toUpperCase();
                /*if (Enviroment.showLeadingZeros) {
                    text = String.format("%1$" + CONSTANTS.WORD_SIZE * 2 + "s", text).replace(" ", "0");
                }*/
                break;
            case FLOAT:
                float number = Float.intBitsToFloat(regValue);
                if (Float.isNaN(number)) {
                    text = "NaN";
                } else {
                    text = Float.toString(number);
                }
                break;
        }
        JTextField textField = registerTextFields[regNum];

        textField.setText(text);
        textField.setForeground(regChanged ? Color.red : Color.black);
        //textField.setColumns(Math.max(text.length(), 15));
    }


    private JPanel createChooserAndRegistersPanel() {
        JPanel chooserAndRegisters = new JPanel();
        JPanel chooserPanel = new JPanel();
        chooser.addItem(DECIMAL);
        chooser.addItem(BINARY);
        chooser.addItem(HEX);
        chooser.addItem(FLOAT);
        chooser.setSelectedItem(DECIMAL);
        chooser.addActionListener(this);

        chooserPanel.add(chooser);
        chooserAndRegisters.add(chooserPanel);

        for (int i = 0; i < VirtualMachine.NUMBER_OF_REGISTERS; i++) {
            JPanel registerPanel = new JPanel();
            registerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            registerPanel.add(new JLabel("R" + ((i < 10) ? i + "  " : i)));

            JTextField textField = new JTextField();
            textField.setText("0");
            textField.setHorizontalAlignment(SwingConstants.RIGHT);
            textField.setFocusable(false); //TODO: Later we want to allow changing Register Values mid run
            textField.setMargin(new Insets(0, 5, 0, 5));
            textField.setColumns(10);

            registerTextFields[i] = textField;
            registerPanel.add(textField);
            chooserAndRegisters.add(registerPanel);
        }

        chooserAndRegisters.setLayout(new BoxLayout(chooserAndRegisters, BoxLayout.Y_AXIS));

        return chooserAndRegisters;
    }

    public void updateRegisterValues(RegisterViewType viewType) {
        for (int i = 0; i < VirtualMachine.NUMBER_OF_REGISTERS; i++) {
            updateRegister(i);
        }
    }


    public void update() {
        for (int i = 0; i < VirtualMachine.NUMBER_OF_REGISTERS; i++) {
            JTextField reg = registerTextFields[i];

            reg.setText(Integer.toString(vm.registers[i]));
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        RegisterViewType viewType = (RegisterViewType) chooser.getSelectedItem();
        updateRegisterValues(viewType);
    }
}
