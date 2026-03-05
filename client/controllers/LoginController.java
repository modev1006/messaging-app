package client.controllers;

import client.ClientApp;
import client.ServerConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            ServerConnection conn = ServerConnection.getInstance();
            if (!conn.connect("localhost", 8080)) {
                errorLabel.setText("Impossible de se connecter au serveur.");
                return;
            }

            conn.setMessageHandler(response -> {
                if (response.startsWith("OK_LOGIN")) {
                    String user = response.split("\\|")[1];
                    try {
                        ClientApp.showChat(user);
                    } catch (Exception e) { e.printStackTrace(); }
                } else if (response.startsWith("FAIL_LOGIN")) {
                    String reason = response.contains("|") ? response.split("\\|")[1] : "Échec de connexion.";
                    errorLabel.setText(reason);
                }
            });

            conn.send("LOGIN|" + username + "|" + password);
        } catch (Exception e) {
            errorLabel.setText("Erreur de connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onGoToRegister() {
        try {
            ClientApp.showRegister();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
