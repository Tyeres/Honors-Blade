import ObjectsToSend.HealthStaminaPackage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Combat implements ConnectInfo {
    private Combat() {
    }

    private static ObjectOutputStream toServerInput;
    private static ObjectInputStream fromServerInput;
    private static boolean canAttack = false; // This should initiate as false because it should first be set to true when the fight starts.

    static Character character = Controller.getCharacter();

    public static void start(FXMLLoader loader, Scene scene) {

        // Starts the mouse buttons
        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Actions to perform on left-click
                leftClick();
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                // Actions to perform on right-click
                rightClick(loader);
            }
        });

        // Starts the ability to feint. Uses Q to activate.
        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("Q")) {
                try {
                    toServerInput.writeObject("Q");
                } catch (IOException exception) {
                    System.err.println(exception);
                }
            }
            else if (event.getCode() == KeyCode.ESCAPE) {
                System.exit(0);
            }
        });
    }

    /**
     * For light attacks
     */
    private static void leftClick() {
        // Make sure you are allowed to attack before doing it.
        if (canAttack) {
            canAttack = false;
            GuardSystem.setCanChangeGuard(false);
            indicateAttackFX();
            new Thread(() -> {
                try {
                    // Send to the server the light attack object to handle
                    Controller.getToCombatServer().writeObject(character.getLightAttack());
                    Controller.getToCombatServer().flush();
                    // Tell the server how much stamina I have
                    Controller.getToCombatServer().writeInt(character.getStamina());
                    Controller.getToCombatServer().flush();

                    // The server tells my action. Did I land the attack? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromCombatServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION) {
                        GameAudio.playHitAudio();
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST);
                    } else if (typeOfAction == Controller.BLOCKED_ACTION) {
                        GameAudio.playBlockAudio();
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST);
                    }
                    // Player has been parried
                    else if (typeOfAction == Controller.PARRIED_ACTION) {
                        GameAudio.playParryAudio();
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST + 5);
                        // Give the enemy player time to punish you for the parry
                        Thread.sleep(Controller.LIGHT_PARRY_STUN_LENGTH);
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        GameAudio.playNoStaminaAudio();
                    } else if (typeOfAction == Controller.INTERRUPTED_ACTION) {
                        // I'll have the player grunt when an attack is interrupted. So, reuse the playFeintAudio method
                        GameAudio.playFeintAudio();
                    }
                } catch (IOException | InterruptedException err) {
                    System.err.println(err);
                }
                // Attack over.
                endAttackFX();
                if (PaintApplication.isGameInProgress()) {
                    canAttack = true;
                    GuardSystem.setCanChangeGuard(true);
                }
            }).start();
        }
    }

    private static void rightClick(FXMLLoader loader) {
        if (canAttack) {
            canAttack = false;
            GuardSystem.setCanChangeGuard(false);
            indicateAttackFX();
            new Thread(() -> {
                try {
                    // Send to the server the heavy attack object to handle
                    // We do not know if this was a heavy or a parry. Let the server find this out.
                    Controller.getToCombatServer().writeObject(character.getHeavyAttack());
                    Controller.getToCombatServer().flush();
                    // Tell the server how much stamina I have
                    Controller.getToCombatServer().writeInt(character.getStamina());
                    Controller.getToCombatServer().flush();

                    // The server tells my action. Did I land a heavy? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromCombatServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST);
                        GameAudio.playHitAudio();
                    } else if (typeOfAction == Controller.BLOCKED_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST);
                        GameAudio.playBlockAudio();
                    }
                    // Player has been parried
                    else if (typeOfAction == Controller.PARRIED_ACTION) {
                        GameAudio.playParryAudio();
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST + 3);
                        // Give the enemy player time to punish you for the parry
                        Thread.sleep(Controller.HEAVY_PARRY_STUN_LENGTH);
                    } else if (typeOfAction == Controller.FEINT_ACTION) {
                        // Decrease stamina
                        if (character.getStamina() - Controller.FEINT_COST < 0)
                            character.setStamina(0);
                        else character.decreaseStamina(Controller.FEINT_COST);

                        GameAudio.playFeintAudio();
                    }
                    // Player has parried his opponent.
                    else if (typeOfAction == Controller.ACTIVE_PARRY_ACTION) {
                        // Let the player know he was the one who parried and that he himself was not parried.
                        parryFlashGuard(loader);
                        GameAudio.playParryAudio();
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        GameAudio.playNoStaminaAudio();
                    } else if (typeOfAction == Controller.INTERRUPTED_ACTION) {
                        // I'll have the player grunt when an attack is interrupted. So, reuse the playFeintAudio method
                        GameAudio.playFeintAudio();
                    }
                } catch (IOException | InterruptedException err) {
                    System.err.println(err);
                }
                // Attack over.
                endAttackFX();
                if (PaintApplication.isGameInProgress()) {
                    canAttack = true;
                    GuardSystem.setCanChangeGuard(true);
                }
            }).start();
        }
    }

    private static void indicateAttackFX() {
        GuardSystem.getFxGuard().setFill(Color.web("#1e2e3c"));
    }

    private static void endAttackFX() {
        GuardSystem.getFxGuard().setFill(Color.web("#007bee"));
    }

    /**
     * When you parry, the guard will flash gold for one second.
     *
     * @param loader loader is the loader instance in PaintApplication.java
     */
    private static void parryFlashGuard(FXMLLoader loader) {
        Polygon activeGuard = (Polygon) loader.getNamespace().get("ACTIVE_GUARD");
        Platform.runLater(() -> activeGuard.setFill(Color.web("#e6ff41")));

        try {
            Thread.sleep(Controller.PARRY_NOTIFICATION_LENGTH);
        } catch (InterruptedException e) {
            System.err.println(e + "\nParry flashing guardFX error");
        }
        Platform.runLater(() -> activeGuard.setFill(Color.BLACK));

    }

    public static void setInputConnection() {
        new Thread(() -> {
            try {
                // The Defense port was connected right beforehand;
                // give time for the server to start up its server for the input.
                Thread.sleep(10);
                Socket inputSocket = new Socket(SERVER_IP, Controller.getInputPort());
                // Send the server inputs.
                toServerInput = new ObjectOutputStream(inputSocket.getOutputStream());
                // Use this to learn when the enemy player has updated his health or stamina
                fromServerInput = new ObjectInputStream(inputSocket.getInputStream());
                startMonitoringEnemyHealthStamina(fromServerInput);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static ObjectOutputStream getToServerInput() {
        return toServerInput;
    }
    // Since this.fromServerInput is in the same scope as fromServerInput, I don't need to use an argument. However, I use one any ways for readability.


    public static void setCanAttack(boolean canAttack) {
        Combat.canAttack = canAttack;
    }

    // This method watches for when the enemy manually updates his stamina or health.
    private static void startMonitoringEnemyHealthStamina(ObjectInputStream fromServerInput) {
        new Thread(() -> {
            while (true) {
                try {
                    HealthStaminaPackage healthStaminaPackage = (HealthStaminaPackage) fromServerInput.readObject();
                    if (healthStaminaPackage.hp() == Integer.MAX_VALUE)
                        Controller.setEnemyCharacterHP(character.getMaxHP());
                    else if (healthStaminaPackage.hp() > 0) {
                        Controller.decreaseEnemyHP(healthStaminaPackage.hp());
                    }
                    Controller.setEnemyStamina(healthStaminaPackage.stamina());

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
