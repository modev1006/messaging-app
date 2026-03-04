package server;

import dao.MessageDAO;
import dao.UserDAO;
import model.Message;
import model.MessageStatus;
import model.Status;
import model.User;
import util.PasswordUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private User currentUser;
    private UserDAO userDAO = new UserDAO();
    private MessageDAO messageDAO = new MessageDAO();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = in.readLine()) != null) {
                handleCommand(message);
            }
        } catch (IOException e) {
            ServerLogger.error("Erreur client", e);
        } finally {
            disconnect();
        }
    }

    private void handleCommand(String input) {
        try {
            String[] parts = input.split("\\|");
            String command = parts[0];
            switch (command) {
                case "REGISTER": handleRegister(parts); break;
                case "LOGIN": handleLogin(parts); break;
                case "SEND": handleSend(parts); break;
                case "GET_USERS": handleGetUsers(); break;
                case "GET_HISTORY": handleGetHistory(parts); break;
                case "TYPING": handleTyping(parts); break;
                case "SEND_FILE": handleSendFile(parts); break;
                case "MARK_READ": handleMarkRead(parts); break;
                case "LOGOUT": disconnect(); break;
                default: out.println("ERROR|Commande non reconnue");
            }
        } catch (Throwable t) {
            ServerLogger.error("Échec de l'exécution de la commande", (t instanceof Exception ? (Exception)t : null));
            out.println("ERROR|Erreur interne du serveur : " + t.getMessage());
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 3) return;
        String username = parts[1];
        String password = parts[2];
        if (userDAO.findByUsername(username) != null) {
            out.println("FAIL_REGISTER|Le nom d'utilisateur existe déjà");
            return;
        }
        User user = new User(username, PasswordUtil.hashPassword(password));
        if (userDAO.save(user)) {
            out.println("OK_REGISTER");
            ServerLogger.log("Nouvel utilisateur inscrit : " + username);
        } else {
            out.println("FAIL_REGISTER|Erreur de base de données");
        }
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) return;
        String username = parts[1];
        String password = parts[2];
        User user = userDAO.findByUsername(username);
        if (user != null && PasswordUtil.checkPassword(password, user.getPassword())) {
            if (server.isUserOnline(username)) {
                out.println("FAIL_LOGIN|Utilisateur déjà connecté");
                return;
            }
            currentUser = user;
            currentUser.setStatus(Status.ONLINE);
            userDAO.update(currentUser);
            server.addOnlineUser(username, this);
            out.println("OK_LOGIN|" + username);
            ServerLogger.log("Utilisateur connecté : " + username);
            server.broadcast("USER_CONNECTED|" + username);
            deliverPendingMessages();
            sendUnreadCounts();
        } else {
            out.println("FAIL_LOGIN|Identifiants invalides");
        }
    }

    private void handleSend(String[] parts) {
        if (currentUser == null || parts.length < 3) return;
        String receiverName = parts[1];
        String content = parts[2];

        if (content == null || content.trim().isEmpty()) {
            out.println("ERROR|Message vide non autorisé");
            return;
        }
        if (content.length() > 1000) {
            out.println("ERROR|Message trop long (max 1000 caractères)");
            return;
        }

        User receiver = userDAO.findByUsername(receiverName);
        if (receiver == null) {
            out.println("FAIL_SEND|Le destinataire n'existe pas");
            return;
        }
        Message msg = new Message(currentUser, receiver, content);
        messageDAO.save(msg);

        // Envoyer confirmation à l'expéditeur avec l'ID du message
        out.println("MSG_SENT|" + msg.getId() + "|" + msg.getStatut());

        ClientHandler receiverHandler = server.getHandler(receiverName);
        if (receiverHandler != null) {
            receiverHandler.sendMessage("MESSAGE|" + currentUser.getUsername() + "|" + content + "|" + msg.getDateEnvoi() + "|" + msg.getId());
            msg.setStatut(MessageStatus.RECU);
            messageDAO.save(msg);
            // Notifier l'expéditeur que le message a été reçu
            out.println("STATUS_UPDATE|" + msg.getId() + "|RECU");
        }
        ServerLogger.log("Message de " + currentUser.getUsername() + " à " + receiverName);
    }

    private void handleTyping(String[] parts) {
        if (currentUser == null || parts.length < 2) return;
        String receiverName = parts[1];
        ClientHandler receiverHandler = server.getHandler(receiverName);
        if (receiverHandler != null) {
            receiverHandler.sendMessage("TYPING|" + currentUser.getUsername());
        }
    }

    private void handleSendFile(String[] parts) {
        if (currentUser == null || parts.length < 4) return;
        String receiverName = parts[1];
        String fileName = parts[2];
        String base64Data = parts[3];

        User receiver = userDAO.findByUsername(receiverName);
        if (receiver == null) {
            out.println("FAIL_SEND|Le destinataire n'existe pas");
            return;
        }

        Message msg = new Message(currentUser, receiver, "[FICHIER] " + fileName);
        messageDAO.save(msg);

        // Confirmation d'envoi
        out.println("MSG_SENT|" + msg.getId() + "|" + msg.getStatut());

        ClientHandler receiverHandler = server.getHandler(receiverName);
        if (receiverHandler != null) {
            receiverHandler.sendMessage("FILE|" + currentUser.getUsername() + "|" + fileName + "|" + base64Data + "|" + msg.getDateEnvoi() + "|" + msg.getId());
            msg.setStatut(MessageStatus.RECU);
            messageDAO.save(msg);
            // Notifier l'expéditeur que le fichier a été reçu
            out.println("STATUS_UPDATE|" + msg.getId() + "|RECU");
        }
        ServerLogger.log("Fichier de " + currentUser.getUsername() + " à " + receiverName + " : " + fileName);
    }

    private void handleGetUsers() {
        List<User> users = userDAO.findAll();
        String list = users.stream().map(u -> u.getUsername() + ":" + u.getStatus()).collect(Collectors.joining(","));
        out.println("USERS|" + list);
        // Envoyer aussi les compteurs de messages non lus
        if (currentUser != null) {
            sendUnreadCounts();
        }
    }

    private void handleGetHistory(String[] parts) {
        if (currentUser == null || parts.length < 2) return;
        String otherUsername = parts[1];
        User other = userDAO.findByUsername(otherUsername);
        if (other == null) return;
        List<Message> history = messageDAO.getHistory(currentUser, other);
        // Format : sender~contenu~dateEnvoi~id~statut (séparateur ~ pour éviter conflit avec : dans dateEnvoi)
        String data = history.stream().map(m -> m.getSender().getUsername() + "~" + m.getContenu() + "~" + m.getDateEnvoi() + "~" + m.getId() + "~" + m.getStatut()).collect(Collectors.joining("|"));
        out.println("HISTORY|" + data);
    }

    private void handleMarkRead(String[] parts) {
        if (currentUser == null || parts.length < 2) return;
        String senderName = parts[1];
        User sender = userDAO.findByUsername(senderName);
        if (sender == null) return;

        List<Integer> updatedIds = messageDAO.markAsRead(sender, currentUser);

        // Notifier l'expéditeur que ses messages ont été lus
        ClientHandler senderHandler = server.getHandler(senderName);
        if (senderHandler != null) {
            for (Integer id : updatedIds) {
                senderHandler.sendMessage("STATUS_UPDATE|" + id + "|LU");
            }
        }
        // Envoyer les compteurs mis à jour au lecteur
        sendUnreadCounts();
        ServerLogger.log(currentUser.getUsername() + " a lu les messages de " + senderName);
    }

    private void deliverPendingMessages() {
        List<Message> pending = messageDAO.getPendingMessages(currentUser);
        for (Message m : pending) {
            sendMessage("MESSAGE|" + m.getSender().getUsername() + "|" + m.getContenu() + "|" + m.getDateEnvoi() + "|" + m.getId());
            m.setStatut(MessageStatus.RECU);
            messageDAO.save(m);
            // Notifier l'expéditeur que le message a été reçu
            ClientHandler senderHandler = server.getHandler(m.getSender().getUsername());
            if (senderHandler != null) {
                senderHandler.sendMessage("STATUS_UPDATE|" + m.getId() + "|RECU");
            }
        }
    }

    public void sendMessage(String msg) { if (out != null) out.println(msg); }

    private void sendUnreadCounts() {
        if (currentUser == null) return;
        Map<String, Long> counts = messageDAO.getUnreadCounts(currentUser);
        if (counts.isEmpty()) {
            out.println("UNREAD_COUNTS|");
        } else {
            String data = counts.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
            out.println("UNREAD_COUNTS|" + data);
        }
    }

    private void disconnect() {
        try {
            if (currentUser != null) {
                currentUser.setStatus(Status.OFFLINE);
                userDAO.update(currentUser);
                server.removeOnlineUser(currentUser.getUsername());
                server.broadcast("USER_DISCONNECTED|" + currentUser.getUsername());
                ServerLogger.log("Utilisateur déconnecté : " + currentUser.getUsername());
            }
        } catch (Exception e) {
            ServerLogger.error("Erreur de déconnexion", e);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) { /* ignore socket close errors */ }
        }
    }
}
