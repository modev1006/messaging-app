package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ClientApp extends Application {
    private static Stage primaryStage;
    private static String currentUser;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            primaryStage.setTitle("Messagerie");
            showSplash();
        } catch (Exception e) {
            System.err.println("!!! ERREUR DANS LA MÉTHODE START !!!");
            e.printStackTrace();
        }
    }

    public static void showSplash() throws Exception {
        java.net.URL res = ClientApp.class.getResource("/resources/splash.fxml");
        if (res == null) {
            System.err.println("ERREUR : splash.fxml non trouvé !");
            return;
        }
        Parent root = FXMLLoader.load(res);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/resources/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showLogin() throws Exception {
        java.net.URL res = ClientApp.class.getResource("/resources/login.fxml");
        if (res == null) {
            System.err.println("ERREUR : login.fxml non trouvé !");
            return;
        }
        Parent root = FXMLLoader.load(res);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/resources/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Messagerie — Connexion");
        primaryStage.show();
    }

    public static void showRegister() throws Exception {
        java.net.URL res = ClientApp.class.getResource("/resources/register.fxml");
        if (res == null) {
            System.err.println("ERREUR : register.fxml non trouvé !");
            return;
        }
        Parent root = FXMLLoader.load(res);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/resources/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Messagerie — Inscription");
        primaryStage.show();
    }

    public static void showChat(String username) throws Exception {
        currentUser = username;
        java.net.URL res = ClientApp.class.getResource("/resources/chat.fxml");
        if (res == null) {
            System.err.println("ERREUR : chat.fxml non trouvé !");
            return;
        }
        Parent root = FXMLLoader.load(res);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ClientApp.class.getResource("/resources/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setTitle("Messagerie — " + username);
        primaryStage.show();
    }

    public static String getCurrentUser() { return currentUser; }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
