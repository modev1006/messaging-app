package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 8080;
    private ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new dao.UserDAO().resetAllStatuses();
        Server server = new Server();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ServerLogger.log("Serveur démarré sur le port " + PORT + "...");
            while (true) {
                Socket client = serverSocket.accept();
                ServerLogger.log("Nouvelle connexion : " + client.getInetAddress());
                ClientHandler handler = new ClientHandler(client, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            ServerLogger.error("Échec du démarrage du serveur", e);
        }
    }

    public void addOnlineUser(String username, ClientHandler handler) {
        onlineUsers.put(username, handler);
    }

    public void removeOnlineUser(String username) {
        onlineUsers.remove(username);
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public ClientHandler getHandler(String username) {
        return onlineUsers.get(username);
    }

    public void broadcast(String message) {
        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendMessage(message);
        }
    }
}
