package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnection {
    private static ServerConnection instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> messageHandler;

    private ServerConnection() {}

    public static ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this::listenForMessages).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (messageHandler != null) {
                    final String msg = line;
                    javafx.application.Platform.runLater(() -> messageHandler.accept(msg));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        if (out != null) out.println(message);
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (Exception e) { e.printStackTrace(); }
        instance = null;
    }
}
