package client.controllers;

import client.ClientApp;
import client.ServerConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatController implements Initializable {

    @FXML private ListView<String> usersList;
    @FXML private VBox chatBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageField;
    @FXML private Label statusLabel;
    @FXML private Label contactNameLabel;
    @FXML private Label contactStatusLabel;
    @FXML private Label typingLabel;

    private String currentUser;
    private String selectedContact;

    private PauseTransition typingTimer;
    private PauseTransition typingSendTimer;

    // Liste des messages pour le chat actif
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // Compteurs de messages non lus par contact
    private Map<String, Long> unreadCounts = new HashMap<>();
    private String lastUsersData = "";
    private boolean updatingList = false;

    // Nombre de messages non lus quand on ouvre une conversation
    private int unreadCountOnOpen = 0;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FULL_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== CLASSE INTERNE =====
    private static class ChatMessage {
        int id;
        String sender;
        String content;
        String rawDateTime;
        String statut;

        ChatMessage(int id, String sender, String content, String rawDateTime, String statut) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.rawDateTime = rawDateTime;
            this.statut = statut;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = ClientApp.getCurrentUser();

        ServerConnection conn = ServerConnection.getInstance();
        conn.setMessageHandler(this::handleServerMessage);
        conn.send("GET_USERS");

        typingTimer = new PauseTransition(Duration.seconds(3));
        typingTimer.setOnFinished(e -> typingLabel.setText(""));

        typingSendTimer = new PauseTransition(Duration.millis(800));
        typingSendTimer.setOnFinished(e -> {});

        messageField.textProperty().addListener((obs, oldText, newText) -> {
            if (selectedContact != null && !newText.isEmpty()) {
                if (typingSendTimer.getStatus() != javafx.animation.Animation.Status.RUNNING) {
                    conn.send("TYPING|" + selectedContact);
                }
                typingSendTimer.playFromStart();
            }
        });

        usersList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !updatingList) {
                selectedContact = newVal.contains(" ") ? newVal.split(" ")[0] : newVal;
                contactNameLabel.setText(selectedContact);
                if (newVal.contains("🟢")) {
                    contactStatusLabel.setText("En ligne");
                    contactStatusLabel.getStyleClass().setAll("chat-contact-status", "status-online");
                } else {
                    contactStatusLabel.setText("Hors ligne");
                    contactStatusLabel.getStyleClass().setAll("chat-contact-status", "status-offline");
                }
                chatMessages.clear();
                typingLabel.setText("");
                // Sauvegarder le nombre de non lus pour afficher le séparateur
                Long count = unreadCounts.get(selectedContact);
                unreadCountOnOpen = (count != null) ? count.intValue() : 0;
                conn.send("GET_HISTORY|" + selectedContact);
                conn.send("MARK_READ|" + selectedContact);
                unreadCounts.remove(selectedContact);
                refreshUsersListWithBadges();
            }
        });

        // Auto-scroll en bas
        chatBox.heightProperty().addListener((obs, old, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void playNotificationSound() {
        try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception e) {}
    }

    // ===== INDICATEURS DE STATUT =====
    private String getStatusIndicator(String statut) {
        if (statut == null) return " ✓";
        switch (statut) {
            case "LU": return " ✓✓";
            case "RECU": return " ✓✓";
            case "ENVOYE":
            default: return " ✓";
        }
    }

    private String getStatusStyleClass(String statut) {
        if (statut == null) return "status-envoye";
        switch (statut) {
            case "LU": return "status-lu";
            case "RECU": return "status-recu";
            case "ENVOYE":
            default: return "status-envoye";
        }
    }

    // ===== RENDU DU CHAT =====
    private void renderChat() {
        Platform.runLater(() -> {
            chatBox.getChildren().clear();
            LocalDate lastDate = null;
            int totalMessages = chatMessages.size();
            int unreadSeparatorIndex = totalMessages - unreadCountOnOpen;

            for (int idx = 0; idx < totalMessages; idx++) {
                ChatMessage msg = chatMessages.get(idx);

                // Séparateur de date
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(msg.rawDateTime.trim());
                    LocalDate msgDate = dateTime.toLocalDate();
                    if (lastDate == null || !lastDate.equals(msgDate)) {
                        lastDate = msgDate;
                        chatBox.getChildren().add(createSeparator(getDateLabel(msgDate)));
                    }
                } catch (Exception e) {}

                // Séparateur "N nouveaux messages"
                if (unreadCountOnOpen > 0 && idx == unreadSeparatorIndex) {
                    String label = unreadCountOnOpen + " nouveau" + (unreadCountOnOpen > 1 ? "x" : "") + " message" + (unreadCountOnOpen > 1 ? "s" : "");
                    chatBox.getChildren().add(createUnreadSeparator(label));
                }

                boolean isMe = msg.sender.equals(currentUser);
                chatBox.getChildren().add(createMessageBubble(msg, isMe));
            }
        });
    }

    private HBox createSeparator(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("date-separator");
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8, 0, 8, 0));
        return box;
    }

    private HBox createUnreadSeparator(String text) {
        Label label = new Label("▼  " + text + "  ▼");
        label.getStyleClass().add("unread-separator");
        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6, 0, 6, 0));
        return box;
    }

    private HBox createMessageBubble(ChatMessage msg, boolean isMe) {
        String time = formatWhatsAppTime(msg.rawDateTime);

        VBox bubble = new VBox(2);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(6, 10, 6, 10));

        // Contenu du message
        Label contentLabel = new Label(msg.content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("msg-content");

        // Ligne du bas : heure + statut
        String bottomText = time;
        if (isMe) {
            bottomText += getStatusIndicator(msg.statut);
        }
        Label timeLabel = new Label(bottomText);
        timeLabel.getStyleClass().add("msg-time");
        if (isMe) {
            timeLabel.getStyleClass().add(getStatusStyleClass(msg.statut));
        }

        bubble.getChildren().addAll(contentLabel, timeLabel);

        if (isMe) {
            bubble.getStyleClass().add("msg-bubble-out");
        } else {
            bubble.getStyleClass().add("msg-bubble-in");
            // Afficher le nom de l'expéditeur
            Label senderLabel = new Label(msg.sender);
            senderLabel.getStyleClass().add("msg-sender");
            bubble.getChildren().add(0, senderLabel);
        }

        HBox row = new HBox(bubble);
        row.setPadding(new Insets(1, 0, 1, 0));
        if (isMe) {
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
        }
        return row;
    }

    // ===== DATE FORMATTING =====
    private String getDateLabel(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "Aujourd'hui";
        if (date.equals(today.minusDays(1))) return "Hier";
        if (date.getYear() == today.getYear()) return date.format(DateTimeFormatter.ofPattern("d MMMM"));
        return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private String formatWhatsAppTime(String rawDateTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawDateTime.trim());
            LocalDate today = LocalDate.now();
            LocalDate msgDate = dateTime.toLocalDate();
            String time = dateTime.format(TIME_FMT);
            if (msgDate.equals(today)) return time;
            if (msgDate.equals(today.minusDays(1))) return "Hier " + time;
            if (msgDate.getYear() == today.getYear()) return dateTime.format(DATE_FMT) + " " + time;
            return dateTime.format(FULL_FMT) + " " + time;
        } catch (Exception e) {
            if (rawDateTime.length() >= 16) return rawDateTime.substring(11, 16);
            return rawDateTime;
        }
    }

    // ===== MESSAGE HANDLER =====
    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            String[] parts = message.split("\\|");
            switch (parts[0]) {
                case "USERS":
                    updateUsersList(parts.length > 1 ? parts[1] : "");
                    break;
                case "MESSAGE":
                    if (parts.length >= 4) {
                        String sender = parts[1];
                        String content = parts[2];
                        String dateTime = parts[3];
                        int msgId = 0;
                        if (parts.length >= 5) {
                            try { msgId = Integer.parseInt(parts[4]); } catch (Exception e) {}
                        }
                        if (sender.equals(selectedContact)) {
                            chatMessages.add(new ChatMessage(msgId, sender, content, dateTime, "RECU"));
                            unreadCountOnOpen = 0; // Plus de séparateur après nouveau message
                            renderChat();
                            ServerConnection.getInstance().send("MARK_READ|" + sender);
                        } else {
                            unreadCounts.merge(sender, 1L, Long::sum);
                            refreshUsersListWithBadges();
                        }
                        playNotificationSound();
                    }
                    break;
                case "FILE":
                    if (parts.length >= 5) {
                        String sender = parts[1];
                        String fileName = parts[2];
                        String base64Data = parts[3];
                        String dateTime = parts[4];
                        int msgId = 0;
                        if (parts.length >= 6) {
                            try { msgId = Integer.parseInt(parts[5]); } catch (Exception e) {}
                        }
                        try {
                            File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
                            File outFile = new File(downloadsDir, fileName);
                            Files.write(outFile.toPath(), Base64.getDecoder().decode(base64Data));
                        } catch (Exception e) {}
                        if (sender.equals(selectedContact)) {
                            chatMessages.add(new ChatMessage(msgId, sender, "📎 " + fileName, dateTime, "RECU"));
                            unreadCountOnOpen = 0;
                            renderChat();
                            ServerConnection.getInstance().send("MARK_READ|" + sender);
                        } else {
                            unreadCounts.merge(sender, 1L, Long::sum);
                            refreshUsersListWithBadges();
                        }
                        playNotificationSound();
                    }
                    break;
                case "TYPING":
                    if (parts.length >= 2) {
                        typingLabel.setText("✍️ " + parts[1] + " est en train d'écrire...");
                        typingTimer.playFromStart();
                    }
                    break;
                case "HISTORY":
                    chatMessages.clear();
                    if (parts.length > 1) {
                        for (int i = 1; i < parts.length; i++) {
                            String[] m = parts[i].split("~");
                            if (m.length >= 5) {
                                int msgId = 0;
                                String statut = "ENVOYE";
                                try { msgId = Integer.parseInt(m[3]); statut = m[4]; } catch (Exception e) {}
                                chatMessages.add(new ChatMessage(msgId, m[0], m[1], m[2], statut));
                            } else if (m.length >= 3) {
                                chatMessages.add(new ChatMessage(0, m[0], m[1], m[2], "ENVOYE"));
                            }
                        }
                    }
                    renderChat();
                    break;
                case "MSG_SENT":
                    if (parts.length >= 3) {
                        try {
                            int msgId = Integer.parseInt(parts[1]);
                            String statut = parts[2];
                            for (int i = chatMessages.size() - 1; i >= 0; i--) {
                                ChatMessage cm = chatMessages.get(i);
                                if (cm.sender.equals(currentUser) && cm.id == 0) {
                                    cm.id = msgId;
                                    cm.statut = statut;
                                    break;
                                }
                            }
                        } catch (NumberFormatException e) {}
                    }
                    break;
                case "STATUS_UPDATE":
                    if (parts.length >= 3) {
                        try {
                            int msgId = Integer.parseInt(parts[1]);
                            String newStatus = parts[2];
                            boolean changed = false;
                            for (ChatMessage cm : chatMessages) {
                                if (cm.id == msgId) {
                                    cm.statut = newStatus;
                                    changed = true;
                                }
                            }
                            if (changed) renderChat();
                        } catch (NumberFormatException e) {}
                    }
                    break;
                case "USER_CONNECTED":
                case "USER_DISCONNECTED":
                    ServerConnection.getInstance().send("GET_USERS");
                    break;
                case "UNREAD_COUNTS":
                    unreadCounts.clear();
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        for (String entry : parts[1].split(",")) {
                            String[] kv = entry.split(":");
                            if (kv.length >= 2) {
                                try { unreadCounts.put(kv[0], Long.parseLong(kv[1])); } catch (Exception e) {}
                            }
                        }
                    }
                    refreshUsersListWithBadges();
                    break;
                case "ERROR":
                    statusLabel.setText(parts.length > 1 ? parts[1] : "Erreur serveur");
                    statusLabel.getStyleClass().setAll("status-label", "status-offline");
                    break;
            }
        });
    }

    private void updateUsersList(String data) {
        lastUsersData = data;
        refreshUsersListWithBadges();
    }

    private void refreshUsersListWithBadges() {
        if (updatingList) return;
        updatingList = true;
        try {
            String savedContact = selectedContact;
            usersList.getItems().clear();
            if (lastUsersData.isEmpty()) return;
            for (String u : lastUsersData.split(",")) {
                String[] info = u.split(":");
                if (info.length >= 2 && !info[0].equals(currentUser)) {
                    String statusIcon = info[1].equals("ONLINE") ? " 🟢" : " ⚫";
                    String badge = "";
                    Long count = unreadCounts.get(info[0]);
                    if (count != null && count > 0) badge = " (" + count + ")";
                    usersList.getItems().add(info[0] + statusIcon + badge);
                }
            }
            if (savedContact != null) {
                for (int i = 0; i < usersList.getItems().size(); i++) {
                    if (usersList.getItems().get(i).startsWith(savedContact + " ")) {
                        usersList.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } finally {
            updatingList = false;
        }
    }

    @FXML
    private void onSendMessage() {
        if (selectedContact == null || messageField.getText().trim().isEmpty()) return;
        String content = messageField.getText().trim();
        ServerConnection.getInstance().send("SEND|" + selectedContact + "|" + content);
        chatMessages.add(new ChatMessage(0, currentUser, content, LocalDateTime.now().toString(), "ENVOYE"));
        unreadCountOnOpen = 0;
        renderChat();
        messageField.clear();
    }

    @FXML
    private void onAttachFile() {
        if (selectedContact == null) {
            statusLabel.setText("Sélectionnez d'abord un contact.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Envoyer un fichier");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt", "*.xlsx"),
            new FileChooser.ExtensionFilter("Tous", "*.*")
        );
        File file = fc.showOpenDialog(ClientApp.getPrimaryStage());
        if (file == null) return;
        if (file.length() > 5 * 1024 * 1024) {
            statusLabel.setText("⚠️ Fichier trop volumineux (max 5 Mo)");
            return;
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            ServerConnection.getInstance().send("SEND_FILE|" + selectedContact + "|" + file.getName() + "|" + base64);
            long size = file.length();
            String sizeStr = size < 1024 ? size + " o" : size < 1024*1024 ? String.format("%.1f Ko", size/1024.0) : String.format("%.1f Mo", size/(1024.0*1024));
            chatMessages.add(new ChatMessage(0, currentUser, "📎 " + file.getName() + " (" + sizeStr + ")", LocalDateTime.now().toString(), "ENVOYE"));
            unreadCountOnOpen = 0;
            renderChat();
            statusLabel.setText("✅ Fichier envoyé : " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur lors de l'envoi du fichier");
        }
    }

    @FXML
    private void onLogout() {
        ServerConnection.getInstance().send("LOGOUT");
        ServerConnection.getInstance().disconnect();
        try { ClientApp.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }
}
