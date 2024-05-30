import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * This class changes the guard for the opponent when the opponent's guard info in received
 * from the server. This class also shows the enemy's attack indicator.
 */
public class Defense implements ConnectInfo {

    final static Character character = Controller.getCharacter();

    static final String PARRY_WINDOW_COLOR = "#ffffff";
    static final String INCOMING_ATTACK_COLOR = "#ff0000";
    static final String DEFAULT_COLOR = "#b2b2b2";

    private static Rectangle enemyFXGuard;

    public static void startDefense(FXMLLoader loader) {
        /* First, start by making the enemy guard center visible.
        This should be done after a connection has been made with the server/opponent. */
        ((Polygon) loader.getNamespace().get("ENEMY_GUARD")).setVisible(true);

        // The guard is up by default when the game starts for both parties.
        enemyFXGuard = (Rectangle) loader.getNamespace().get("ENEMY_UP_GUARD");
        enemyFXGuard.setVisible(true);


        new Thread(() -> {
            // A separate port must be used so that a conflict does not occur in Combat.java
            try (Socket socket = new Socket(SERVER_IP, Controller.getDefensePort())) {

                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    int enemyAction = fromServer.readInt();

                    // Incoming attack. The guard won't change during this time.
                    if (enemyAction == Controller.INCOMING_ATTACK) {
                        Platform.runLater(() -> {
                            // Change the indicator color
                            enemyFXGuard.setFill(Color.web(INCOMING_ATTACK_COLOR));
                        });
                        // Wait for parry window.
                        fromServer.readInt();
                        Platform.runLater(() -> {
                            enemyFXGuard.setFill(Color.web(PARRY_WINDOW_COLOR));
                        });

                        // Attack End
                        fromServer.readInt();
                        Platform.runLater(() -> {
                            enemyFXGuard.setFill(Color.web(DEFAULT_COLOR));
                        });
                        // Incoming attack over


                        // Read the type of attack.

                        //  If the attack hits.
                        if (fromServer.readInt() == Controller.ATTACK_ACTION) {
                            /* Read the damage from the server and decrease health.
                            If you block it, there will still be chip damage if it's a heavy. */
                            character.decreaseHealth(fromServer.readInt());
                            // Play audio
                            playHitAudio();
                        }
                        if (fromServer.readInt() == Controller.BLOCKED_ACTION) {
                            playBlockedAudio();
                        }
                        // It's a parry action
                        else {
                            // Play audio
                            playParryAudio();
                        }
                    }
                    // Up guard
                    else if (enemyAction == Controller.UP_GUARD) {
                        setUpEnemyGuard(loader);
                    }
                    // Left guard
                    else if (enemyAction == Controller.LEFT_GUARD) {
                        setLeftEnemyGuard(loader);
                    }
                    // Right guard
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

        enemyFXGuard.setVisible(false);

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
    private static void playAudio(String directory) {
        Media media = new Media(new File(directory).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
    public static void playHitAudio() {
        playAudio("./src/Audio/hit.mp3");
    }
    public static void playBlockedAudio() {
        playAudio("./src/Audio/block.mp3");
    }
    public static void playParryAudio() {
        playAudio("./src/Audio/parry.mp3");
    }
}
