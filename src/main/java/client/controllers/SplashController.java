package client.controllers;

import client.ClientApp;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.setProgress(0);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0)),
            new KeyFrame(Duration.millis(2500),
                e -> statusLabel.setText("Chargement des modules..."),
                new KeyValue(progressBar.progressProperty(), 0.25)),
            new KeyFrame(Duration.millis(5000),
                e -> statusLabel.setText("Connexion au serveur..."),
                new KeyValue(progressBar.progressProperty(), 0.50)),
            new KeyFrame(Duration.millis(7500),
                e -> statusLabel.setText("Préparation de l'interface..."),
                new KeyValue(progressBar.progressProperty(), 0.75)),
            new KeyFrame(Duration.millis(10000),
                e -> statusLabel.setText("Prêt !"),
                new KeyValue(progressBar.progressProperty(), 1.0)),
            new KeyFrame(Duration.millis(11000),
                e -> {
                    try {
                        ClientApp.showLogin();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })
        );
        timeline.play();
    }
}
