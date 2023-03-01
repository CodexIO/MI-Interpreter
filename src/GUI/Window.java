package GUI;

// Java Program to create a text editor using java
import Interpreter.VirtualMachine;
import Assembler.Parser;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.net.URL;

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

    private final RSyntaxTextArea textEditor = new RSyntaxTextArea();
    RTextScrollPane scroll = new RTextScrollPane(textEditor);
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
        centerPanel.add(scroll);
        centerPanel.add(memoryPanel);

        lowerPanel.add(notificationPane);
        lowerPanel.setBorder(new LineBorder(Color.black));

        add(mainPanel);


        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/mi", "Assembler.MiTokenMaker");
        textEditor.setSyntaxEditingStyle("text/mi");
        //textEditor.setFont(new Font("Courier New", Font.PLAIN, 14));
        textEditor.setText("ADD B I 5, I 5, R0");

        scroll.setPreferredSize(new Dimension(400, 700));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Gutter gutter = scroll.getGutter();
        URL breakpointUrl = getClass().getResource("breakpoint.png");
        gutter.setBookmarkIcon(new ImageIcon(breakpointUrl));
        gutter.setBookmarkingEnabled(true);
    }

    // If a button is pressed
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();

        if (src == buttonPanel.assemble) {
            Parser parser = new Parser(textEditor.getText());
            parser.parse();
            vm.reset();
            vm.setMemory(parser.generateMachineCode());
            vm.setLineNumberToAddress(parser.getCommands());

            updateVmState();
            buttonPanel.run.setEnabled(true);
            buttonPanel.debug.setEnabled(true);
            buttonPanel.step.setEnabled(true);
        } else if (src == buttonPanel.run) {
            vm.setBreakpoints(new int[0]);
            vm.run();

            updateVmState();
            updateButtons();
        } else if (src == buttonPanel.debug) {
            Gutter gutter = scroll.getGutter();
            GutterIconInfo[] breakpoints = gutter.getBookmarks();
            int[] breakpointLines = new int[breakpoints.length];
            int i = 0;
            for (var info : breakpoints) {
                int lineNumber = -1;
                try {
                    lineNumber = textEditor.getLineOfOffset(info.getMarkedOffset()) + 1;
                    breakpointLines[i++] = lineNumber;
                } catch (BadLocationException exception) {
                    // This can't happen since we get the Offset from the textEditor itself.
                }
            }
            vm.setBreakpoints(breakpointLines);

            vm.run();

            highlightNextLineToBeExecuted();
            updateVmState();
            updateButtons();
        } else if (src == buttonPanel.step) {
            vm.step();

            highlightNextLineToBeExecuted();
            updateVmState();
            updateButtons();
        } else if (src == buttonPanel.stop) {

        } else if (src == buttonPanel.restart) {

        } else {
            evalMenuBar(e);
        }
    }

    private void highlightNextLineToBeExecuted() {
        int lineNumber = vm.getLineNumberOfNextCommand() - 1;
        int offset = 0;
        try {
            offset = textEditor.getLineStartOffset(lineNumber);
        } catch (BadLocationException ex) {
            // This should never happen
        }
        textEditor.setCaretPosition(offset);
    }

    private void updateButtons() {
        if (vm.programHaltet) {
            buttonPanel.step.setEnabled(false);
            buttonPanel.run.setEnabled(false);
            buttonPanel.debug.setEnabled(false);
        }
    }

    private void updateVmState() {
        registerPanel.updateRegisterValues();
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
                        w.write(textEditor.getText());

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
                    textEditor.print();
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
                        textEditor.setText(sb.toString());
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
                textEditor.setText("");
                break;
        }
    }
}
