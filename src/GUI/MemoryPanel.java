package GUI;

import Assembler.Interpreter.VirtualMachine;

import javax.swing.*;
import java.awt.*;

public class MemoryPanel extends JPanel {

    private VirtualMachine vm;
    private JTextArea memory = new JTextArea();
    private JScrollPane scrollPane = new JScrollPane(memory, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public MemoryPanel(VirtualMachine vm) {
        this.vm = vm;
        this.add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(280, 400));

        memory.setFont(new Font("Courier New", Font.PLAIN, 14)); //TODO: Set this as the default Font for everything
        memory.setEditable(false); //TODO: Maybe we later wanna allow editing the memory
        renderMemory();
    }

    //TODO: @Slow Regenerating the Memory Representation String everytime the
    //            memory changes seems awfully slow. Is there a better solution?
    //            Vorschlag: JList? oder noch mal gucken, wie es der alte macht
    // @Felix Fragen
    public void renderMemory() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < VirtualMachine.MEMORY_LENGTH; i++) {
            if (i % 8 == 0) {
                if (i != 0) sb.append('\n');

                String address = String.format("%06X ", i);
                sb.append(address);
            }

            String data = String.format(" %02X", (byte) vm.getMemory(i, 1));
            sb.append(data);
        }
        memory.setText(sb.toString());

        // This is needed because after setting the Text, the Scrollbar
        // scrolls all the way down, which is not what we want.
        memory.setCaretPosition(0);
    }
}
