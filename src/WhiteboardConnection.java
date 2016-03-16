/**
 * Created by jmuia on 2016-03-14.
 */

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;


final class WhiteboardConnection implements Runnable, WhiteboardListener {
    Socket socket;
    BufferedReader in;
    DataOutputStream out;

    // Constructor
    public WhiteboardConnection(Socket socket) {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                sendColor(Whiteboard.registerColor());
                sendWhiteboard();
                listen();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                // Close streams and sockets
                Whiteboard.removeListener(this);
                out.close();
                in.close();
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void listen() throws IOException {
        Whiteboard.addListener(this);

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
    }

    private void handleMessage(String m) throws ParseException {
        String[] tokens = m.split(":");
        if (tokens.length != 2) { throw new ParseException("Could not parse message: " + m, -1); }

        if (tokens[0].equals("point")) {
            WhiteboardPoint point = new WhiteboardPoint(tokens[1]);
            Whiteboard.addPoint(point);
        }
    }

    private void sendColor(Color color) {
        try {
            out.writeBytes("color:" + color.getRGB() + "\n");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendWhiteboard() {
        try {
            out.writeBytes("whiteboard:" + Whiteboard.serialize() + "\n");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void update(WhiteboardPoint point) {
        try {
            out.writeBytes("point:" + point.serialize() + "\n");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
