import javafx.fxml.FXMLLoader;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * This class changes the guard for the opponent when the opponent's guard info in received
 * from the server. This class also shows the enemy's attack indicator.
 */
public class Defense implements ConnectInfo{

    private static Rectangle enemyFXGuard;

    public static void startDefense(FXMLLoader loader) {
        /* First, start by making the enemy guard center visible.
        This should be done after a connection has been made with the server/opponent. */
        ((Polygon) loader.getNamespace().get("ENEMY_GUARD")).setVisible(true);


        new Thread(()->{
            // A separate port must be used so that a conflict does not occur in Combat.java
            try (Socket socket = new Socket(SERVER_IP, DEFENSE_PORT)) {

                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    int enemyGuard = fromServer.readInt();

                    // Up
                    if (enemyGuard == Controller.UP_GUARD) {
                        setUpEnemyGuard(loader);
                    }
                    // Left
                    else if (enemyGuard == Controller.LEFT_GUARD) {
                        setLeftEnemyGuard(loader);
                    }
                    // Right
                    else {
                        setRightEnemyGuard(loader);
                    }
                }
            } catch (IOException ioException) {
                System.err.println(ioException);
            }
        }).start();
    }

    private static void setEnemyGuard(FXMLLoader loader, String rectangleGuardFXID) {
        try {
            enemyFXGuard.setVisible(false);
        } catch (Exception e) {
                        /* Do nothing. There would be an error for the first time, since
                         fxGuard is null. After that, there will be no errors. */
        }
        enemyFXGuard = (Rectangle) loader.getNamespace().get(rectangleGuardFXID);
        enemyFXGuard.setVisible(true);
    }
    protected static void setLeftEnemyGuard(FXMLLoader loader) {
        setEnemyGuard(loader, "ENEMY_LEFT_GUARD");
    }
    protected static void setRightEnemyGuard(FXMLLoader loader) {
        setEnemyGuard(loader, "ENEMY_RIGHT_GUARD");
    }
    protected static void setUpEnemyGuard(FXMLLoader loader) {
        setEnemyGuard(loader, "ENEMY_UP_GUARD");
    }
}
