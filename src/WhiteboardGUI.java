import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created by jmuia on 2016-03-14.
 */
public class WhiteboardGUI implements WhiteboardListener, ConnectionListener {
    private WhiteboardClient client;

    private JFrame mainFrame;
    private WhiteboardPanel whiteboard;

    private JTextField ipField;
    private JTextField portField;

    private JLabel messageLabel;

    private JButton connectionButton;

    public WhiteboardGUI(final WhiteboardClient client) {
        this.client = client;
        final WhiteboardGUI that = this;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                initGUI();
                client.addListener(that);
                Whiteboard.addListener(that);
            }
        });
    }

    private void initGUI() {
        final WhiteboardGUI that = this;

        mainFrame = new JFrame("Collaborative Whiteboard");
        mainFrame.setSize(new Dimension(1024, 512));
        mainFrame.setResizable(false);
        mainFrame.setLayout(new GridBagLayout());

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Whiteboard.removeListener(that);
                client.removeListener(that);
                client.disconnect();
                mainFrame.dispose();
                System.exit(0);
            }
        });

        JPanel connectionPanel = initConnectionPanel();
        whiteboard = initWhiteboard();
        messageLabel = initMessageLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        mainFrame.add(connectionPanel, gbc);

        gbc.gridy = 1;
        mainFrame.add(messageLabel, gbc);

        gbc.gridy = 2; gbc.weighty = 1;
        mainFrame.add(whiteboard, gbc);

        displayDisconnectedUI();

        mainFrame.setVisible(true);
    }

    private WhiteboardPanel initWhiteboard() {
        WhiteboardPanel panel = new WhiteboardPanel();
        panel.setBackground(Color.WHITE);
        panel.setVisible(true);
        panel.addMouseMotionListener(new WhiteboardPenListener());
        return panel;
    }

    private JLabel initMessageLabel() {
        JLabel label = new JLabel(" ", SwingConstants.CENTER);
        label.setForeground(Color.red);
        return label;
    }

    private JPanel initConnectionPanel() {
        JPanel panel = new JPanel();
        JLabel ipLabel = new JLabel("IP");
        JLabel portLabel = new JLabel("Port");
        ipField = new JTextField(46);
        portField = new JTextField(5);
        connectionButton = new JButton("Connect");
        connectionButton.addActionListener(new ConnectionButtonListener());

        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(connectionButton);
        return panel;
    }

    @Override
    public void connectionOpened() {
        displayConnectedUI();
    }

    @Override
    public void connectionClosed(String reason) {
        displayDisconnectedUI();
        messageLabel.setText(reason);
        Whiteboard.clear();
    }

    @Override
    public void update(WhiteboardPoint point) {
        Graphics g = whiteboard.getGraphics();
        g.setColor(point.getColor());
        g.fillOval(point.x, point.y, 4, 4);
    }

    private void displayDisconnectedUI() {
        connectionButton.setText("Connect");
        ipField.setEditable(true);
        portField.setEditable(true);
        ipField.setBackground(Color.WHITE);
        portField.setBackground(Color.WHITE);
        whiteboard.setBackground(Color.lightGray);
    }

    private void displayConnectedUI() {
        connectionButton.setText("Disconnect");
        ipField.setEditable(false);
        portField.setEditable(false);
        ipField.setBackground(Color.lightGray);
        portField.setBackground(Color.lightGray);
        whiteboard.setBackground(Color.WHITE);
        messageLabel.setText(" ");
    }

    private class ConnectionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                } else {
                    client.connect(ipField.getText(), Integer.parseInt(portField.getText()));
                }
            } catch (NumberFormatException nfe) {
                messageLabel.setText("Port number must be an integer.");
            } catch (IOException ioe) {
                messageLabel.setText(ioe.getMessage());
            }
        }
    }

    private class WhiteboardPenListener implements MouseMotionListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!client.isConnected()) { return; }

            try {
                client.addPoint(e.getPoint());
            } catch (IOException ioe) {
                displayDisconnectedUI();
                messageLabel.setText(ioe.getMessage());
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {}
    }

    private class WhiteboardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (WhiteboardPoint point: Whiteboard.getPoints()) {
                g.setColor(point.getColor());
                g.fillOval(point.x, point.y, 4, 4);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new WhiteboardGUI(new WhiteboardClient());
    }
}
