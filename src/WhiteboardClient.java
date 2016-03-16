import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmuia on 2016-03-14.
 */
public class WhiteboardClient {
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private Color color;
    private boolean connected;

    private List<ConnectionListener> listeners;

    public WhiteboardClient() {
        listeners = new ArrayList<>();
    }

    public void connect(String ipAddress, int portNumber) throws IOException {
        socket = new Socket(ipAddress, portNumber);
        out = new DataOutputStream(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
        listen();
        for (ConnectionListener listener: listeners) {
            listener.connectionOpened();
        }
    }

    private void listen() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    String input = in.readLine();
                    while (input != null) {
                        try {
                            handleMessage(input);
                        } catch (ParseException e) {
                            System.err.println(e.getMessage());
                        } finally {
                            input = in.readLine();
                        }
                    }
                    close("Server closed the connection.");

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private void handleMessage(String m) throws ParseException {
        String[] tokens = m.split(":");
        if (tokens.length != 2) { throw new ParseException("Could not parse message: " + m, -1); }

        if (tokens[0].equals("point")) {
            WhiteboardPoint point = new WhiteboardPoint(tokens[1]);
            Whiteboard.addPoint(point);

        } else if (tokens[0].equals("color")) {
            try {
                color = new Color(Integer.parseInt(tokens[1]));
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }

        } else if (tokens[0].equals("whiteboard")) {
            try {
                Whiteboard.initialize(tokens[1]);
            } catch (ParseException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    public void addPoint(Point point) throws IOException {
        if (color == null) { color = Color.black; }

        WhiteboardPoint wbp = new WhiteboardPoint(point, color);
        out.writeBytes("point:" + wbp.serialize() + "\n");
    }

    public void disconnect() {
        if (socket == null) { return; }

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        close(" ");
    }

    private void close(String reason) {
        connected = false;
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            for (ConnectionListener listener: listeners) {
                listener.connectionClosed(reason);
            }
        }
    }
}
