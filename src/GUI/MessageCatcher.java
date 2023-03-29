package GUI;

import javax.swing.*;
import java.io.OutputStream;

public class MessageCatcher extends OutputStream {
    private final JTextArea textArea;
    private final StringBuilder sb = new StringBuilder();

    public MessageCatcher(JTextArea area) {
        textArea = area;
    }

    @Override
    public void write(int b) {
        if (b == '\r')
            return;

        if (b == '\n') {
            final String text = sb + "\n";
            //textArea.append(text);
            SwingUtilities.invokeLater(() -> textArea.append(text));
            sb.setLength(0);

            return;
        }

        sb.append((char) b);
    }
}
