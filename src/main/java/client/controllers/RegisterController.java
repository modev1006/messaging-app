package client.controllers;

import client.ClientApp;
import client.ServerConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onRegister() {
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
                if (response.startsWith("OK_REGISTER")) {
                    try {
                        ClientApp.showLogin();
                    } catch (Exception e) { e.printStackTrace(); }
                } else if (response.startsWith("FAIL_REGISTER")) {
                    String reason = response.contains("|") ? response.split("\\|")[1] : "Échec d'inscription.";
                    errorLabel.setText(reason);
                }
            });

            conn.send("REGISTER|" + username + "|" + password);
        } catch (Exception e) {
            errorLabel.setText("Erreur de connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            ClientApp.showLogin();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
