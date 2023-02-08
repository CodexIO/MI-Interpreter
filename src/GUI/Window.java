package GUI;

// Java Program to create a text editor using java
import Assembler.Interpreter.VirtualMachine;
import Assembler.Parser;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

class Window extends JFrame implements ActionListener {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 800;

    VirtualMachine vm = new VirtualMachine();

    private final ButtonPanel buttonPanel = new ButtonPanel(this);
    private final RegisterPanel registerPanel = new RegisterPanel(vm);
    private final MemoryPanel memoryPanel = new MemoryPanel(vm);
    private final FlagsPanel flagPanel = new FlagsPanel();

    //These are used to divide the different Views we want to render
    private final JPanel upperPanel = new JPanel();
    private final JPanel centerPanel = new JPanel();
    private final JPanel lowerPanel = new JPanel();

    private final TextEditorPane editorPane = new TextEditorPane();
    private final JTextPane notificationPane = new JTextPane();

    private final JPanel mainPanel = new JPanel();


    // Constructor
    Window()
    {
        setTitle("MI-Interpreter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initGUI();
        initMenuBar();

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setVisible(true);
    }

    private void initMenuBar() {
        // Create a menubar
        JMenuBar mb = new JMenuBar();

        // Create amenu for menu
        JMenu m1 = new JMenu("File");

        // Create menu items
        JMenuItem mi1 = new JMenuItem("New");
        JMenuItem mi2 = new JMenuItem("Open");
        JMenuItem mi3 = new JMenuItem("Save");
        JMenuItem mi9 = new JMenuItem("Print");

        // Add action listener
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        mi9.addActionListener(this);

        m1.add(mi1);
        m1.add(mi2);
        m1.add(mi3);
        m1.add(mi9);

        // Create amenu for menu
        JMenu m2 = new JMenu("Edit");

        // Create menu items
        JMenuItem mi4 = new JMenuItem("cut");
        JMenuItem mi5 = new JMenuItem("copy");
        JMenuItem mi6 = new JMenuItem("paste");

        // Add action listener
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);

        m2.add(mi4);
        m2.add(mi5);
        m2.add(mi6);

        JMenuItem mc = new JMenuItem("close");
        mc.addActionListener(this);

        JMenuItem runButton = new JMenuItem("Run");
        runButton.addActionListener(this);

        mb.add(m1);
        mb.add(m2);
        mb.add(mc);
        mb.add(runButton);

        setJMenuBar(mb);
    }

    private void initGUI() {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(upperPanel);
        mainPanel.add(centerPanel);
        mainPanel.add(lowerPanel);

        upperPanel.add(buttonPanel);
        upperPanel.setBorder(new LineBorder(Color.black));

        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBorder(new LineBorder(Color.black));
        centerPanel.add(registerPanel);
        centerPanel.add(editorPane);
        centerPanel.add(memoryPanel);

        lowerPanel.add(notificationPane);
        lowerPanel.setBorder(new LineBorder(Color.black));

        add(mainPanel);

        editorPane.setText("ADD B I 5, I 5, R0");
    }

    // If a button is pressed
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();

        if (src == buttonPanel.assemble) {
            Parser parser = new Parser(editorPane.getText());
            parser.parse();
            vm.reset();
            vm.setMemory(parser.generateMachineCode());

            updateVmState();
            buttonPanel.run.setEnabled(true);
        } else if (src == buttonPanel.run) {
            vm.reset();
            vm.run();

            updateVmState();
            buttonPanel.run.setEnabled(false);
        } else if (src == buttonPanel.debug) {
            //TODO: Implement Debugging
        } else if (src == buttonPanel.step) {

        } else if (src == buttonPanel.stop) {

        } else if (src == buttonPanel.restart) {

        } else {
            evalMenuBar(e);
        }
    }

    private void updateVmState() {
        registerPanel.update();
        memoryPanel.renderMemory();
        notificationPane.setText(vm.toString());
    }

    private void evalMenuBar(ActionEvent e) {
        String s = e.getActionCommand();

        switch (s) {
            case "Save": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsSaveDialog function to show the save dialog
                int r = j.showSaveDialog(null);

                if (r == JFileChooser.APPROVE_OPTION) {

                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try (FileWriter wr = new FileWriter(fi, false)){
                        // Create a file writer
                        BufferedWriter w;

                        // Create buffered writer to write
                        w = new BufferedWriter(wr);

                        // Write
                        w.write(editorPane.getText());

                        w.flush();
                        w.close();
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(this, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(this, "the user cancelled the operation");
                break;
            }
            case "Print":
                try {
                    // print the file
                    editorPane.print();
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(this, evt.getMessage());
                }
                break;
            case "Open": {
                // Create an object of JFileChooser class
                JFileChooser j = new JFileChooser("f:");

                // Invoke the showsOpenDialog function to show the save dialog
                int r = j.showOpenDialog(null);

                // If the user selects a file
                if (r == JFileChooser.APPROVE_OPTION) {
                    // Set the label to the path of the selected directory
                    File fi = new File(j.getSelectedFile().getAbsolutePath());

                    try (FileReader fr = new FileReader(fi)){
                        StringBuilder sb = new StringBuilder();
                        String s1;

                        // File reader
                        BufferedReader br;

                        // Buffered reader
                        br = new BufferedReader(fr);

                        // Initialize sl
                        sb.append(br.readLine());

                        // Take the input from the file
                        while ((s1 = br.readLine()) != null) {
                            sb.append("\n");
                            sb.append(s1);
                        }

                        // Set the text
                        editorPane.setText(sb.toString());
                    } catch (Exception evt) {
                        JOptionPane.showMessageDialog(this, evt.getMessage());
                    }
                }
                // If the user cancelled the operation
                else
                    JOptionPane.showMessageDialog(this, "the user cancelled the operation");
                break;
            }
            case "New":
                editorPane.setText("");
                break;
            case "Run":
                Parser parser = new Parser(editorPane.getText());
                parser.parse();
                vm.reset();
                vm.setMemory(parser.generateMachineCode());
                vm.run();
                notificationPane.setText(vm.toString());

                registerPanel.update();
                memoryPanel.renderMemory();

                break;
            case "close":
                System.exit(0);
        }
    }
}
