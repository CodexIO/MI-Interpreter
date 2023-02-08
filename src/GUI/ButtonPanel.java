package GUI;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ButtonPanel extends JPanel {

    public final JButton assemble = new JButton("Assemble");
    public final JButton run = new JButton("Run");
    public final JButton debug = new JButton("Debug");
    public final JButton step = new JButton("Step");
    public final JButton stop = new JButton("Stop");
    public final JButton restart = new JButton("Restart");

    public ButtonPanel(ActionListener actionListener) {
        assemble.addActionListener(actionListener);
        run.addActionListener(actionListener);
        debug.addActionListener(actionListener);
        step.addActionListener(actionListener);
        stop.addActionListener(actionListener);
        restart.addActionListener(actionListener);

        run.setEnabled(false);
        debug.setEnabled(false);
        step.setEnabled(false);
        stop.setEnabled(false);
        restart.setEnabled(false);

        this.add(assemble);
        this.add(run);
        this.add(debug);
        this.add(step);
        this.add(stop);
        this.add(restart);
    }
}