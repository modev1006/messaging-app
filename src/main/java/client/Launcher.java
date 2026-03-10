package client;

public class Launcher {
    public static void main(String[] args) {
        try {
            ClientApp.main(args);
        } catch (Exception e) {
            System.err.println("=== ERREUR DÉTECTÉE AU LANCEMENT ===");
            e.printStackTrace();
        }
    }
}
