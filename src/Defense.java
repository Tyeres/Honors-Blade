import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

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

            try (Socket socket = new Socket(SERVER_IP, Controller.getDefensePort())) {

                ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    int enemyAction = fromServer.readInt();

                    // Incoming attack. The guard won't change during this time; so, you can use this in the same if & if else statement lineup
                    if (enemyAction == Controller.INCOMING_ATTACK) {
                        Platform.runLater(() -> {
                            // Change the indicator color
                            enemyFXGuard.setFill(Color.web(INCOMING_ATTACK_COLOR));
                        });
                        // Wait for parry window. If feint action is sent, the parry window will not open since it was a feint.
                        int didEnemyFeint = fromServer.readInt();
                        // 0 means that the enemy did not feint
                        if (didEnemyFeint == 0) {
                            Platform.runLater(() -> {
                                enemyFXGuard.setFill(Color.web(PARRY_WINDOW_COLOR));
                            });
                        }

                        // Attack End
                        fromServer.readInt();
                        Platform.runLater(() -> {
                            enemyFXGuard.setFill(Color.web(DEFAULT_COLOR));
                        });
                        // Incoming attack over

                        // Read the type of attack.
                        int typeOfAttack;
                        if (didEnemyFeint == 0) {
                            // Enemy did not feint. Read the type of attack
                            typeOfAttack = fromServer.readInt();
                        }
                        // Enemy feinted. Set the type of attack to a feint action. There is no if case for it because nothing should be done for it.
                        else typeOfAttack = Controller.FEINT_ACTION;

                        //  If the attack hits.
                        if (typeOfAttack == Controller.ATTACK_ACTION) {
                            /* Read the damage from the server and decrease health.
                            If you block it, there will still be chip damage if it's a heavy. */
                            // READING FROM SERVER
                            character.decreaseHealth(fromServer.readInt());
                            // Play audio
                            GameAudio.playHitAudio();
                        }
                        else if (typeOfAttack == Controller.BLOCKED_ACTION) {
                            GameAudio.playBlockAudio();
                            // Read for chip damage. If the attack is a light attack, the chip damage is 0.
                            character.decreaseHealth(fromServer.readInt());
                        }
                        else if (typeOfAttack == Controller.FEINT_ACTION) {
                            GameAudio.playFeintAudio();
                        }
                        // else if... It's an active parry action. You parried your opponent.
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
                PaintApplication.getAnnouncementText().setText("Connection lost. Opponent likely disconnected.");
                PaintApplication.getAnnouncementText().setVisible(true);
                PaintApplication.setGameInProgress(false); // This allows it so that the canAttack & the canChangeGuard is not set to true if an attack was active before the game ended and then ended while the game was over.
                Combat.setCanAttack(false);
                GuardSystem.setCanChangeGuard(false);
                new Thread(()->{
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.exit(-5);
                }).start();
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
}
