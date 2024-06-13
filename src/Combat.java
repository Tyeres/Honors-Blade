import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Combat implements ConnectInfo{
    private Combat() {
    }

    private static ObjectOutputStream toServerInput;
    private static boolean canAttack = true;

    static Character character = Controller.getCharacter();

    public static void start(FXMLLoader loader, Scene scene) {
        setInputConnection();
        // Starts the guard system for switching guard
        GuardSystem.startControls(loader, scene);
        // Starts the guard system for the opponent and starts showing attack indicators
        Defense.startDefense(loader);
        StaminaRegeneration.start();


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
                    Controller.getToServer().writeObject(character.getLightAttack());
                    Controller.getToServer().flush();
                    // Tell the server how much stamina I have
                    Controller.getToServer().writeInt(character.getStamina());
                    Controller.getToServer().flush();

                    // The server tells my action. Did I land the attack? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION) {
                        Defense.playHitAudio();
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST);
                    }
                    else if (typeOfAction == Controller.BLOCKED_ACTION) {
                        Defense.playBlockedAudio();
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST);
                    }
                    // Player has been parried
                    else if (typeOfAction == Controller.PARRIED_ACTION) {
                        Defense.playParryAudio();
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST + 5);
                        // Give the enemy player time to punish you for the parry
                        Thread.sleep(Controller.LIGHT_PARRY_STUN_LENGTH);
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        playStaminaError();
                    }
                } catch (IOException | InterruptedException err) {
                    System.err.println(err);
                }
                // Attack over.
                endAttackFX();
                canAttack = true;
                GuardSystem.setCanChangeGuard(true);
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
                    Controller.getToServer().writeObject(character.getHeavyAttack());
                    Controller.getToServer().flush();
                    // Tell the server how much stamina I have
                    Controller.getToServer().writeInt(character.getStamina());
                    Controller.getToServer().flush();

                    // The server tells my action. Did I land a heavy? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST);
                        Defense.playHitAudio();
                    }
                    else if (typeOfAction == Controller.BLOCKED_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST);
                        Defense.playBlockedAudio();
                    }
                    // Player has been parried
                    else if (typeOfAction == Controller.PARRIED_ACTION) {
                        Defense.playParryAudio();
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST + 3);
                        // Give the enemy player time to punish you for the parry
                        Thread.sleep(Controller.HEAVY_PARRY_STUN_LENGTH);
                    }
                    else if (typeOfAction == Controller.FEINT_ACTION) {
                        // Decrease stamina
                        if (character.getStamina() - 3 < 0)
                            character.setStamina(0);
                        else character.decreaseStamina(3);

                        Defense.playFeintAudio();
                    }
                    // Player has parried his opponent.
                    else if (typeOfAction == Controller.ACTIVE_PARRY_ACTION) {
                        // Let the player know he was the one who parried and that he himself was not parried.
                        flashGuard(loader);
                        Defense.playParryAudio();
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        playStaminaError();
                    }
                } catch (IOException | InterruptedException err) {
                    System.err.println(err);
                }
                // Attack over.
                endAttackFX();
                canAttack = true;
                GuardSystem.setCanChangeGuard(true);
            }).start();
        }
    }

    static void playStaminaError() {
        final String NO_STAMINA_DIRECTORY = "./src/Audio/notification-sound-error-sound-effect-203788.mp3";
        Media media = new Media(new File(NO_STAMINA_DIRECTORY).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    private static void indicateAttackFX() {
        GuardSystem.getFxGuard().setFill(Color.web("#1e2e3c"));
    }

    private static void endAttackFX() {
        GuardSystem.getFxGuard().setFill(Color.web("#007bee"));
    }

    /**
     * When you parry, the guard will flash gold for one second.
     * @param loader
     * loader is the loader instance in PaintApplication.java
     */
    private static void flashGuard(FXMLLoader loader) {
        Polygon activeGuard = (Polygon) loader.getNamespace().get("ACTIVE_GUARD");
        Platform.runLater(() -> {
            activeGuard.setFill(Color.web("#e6ff41"));
        });
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e + "\nFlashing guard error");
            }
            Platform.runLater(() -> {
                activeGuard.setFill(Color.BLACK);
            });
        }).start();
    }
    private static void setInputConnection() {
        new Thread(() -> {
            try {
                // The Defense port was connected right beforehand;
                // give time for the server to start up its server for the input.
                Thread.sleep(50);
                Socket inputSocket = new Socket(SERVER_IP, Controller.getInputPort());
                toServerInput = new ObjectOutputStream(inputSocket.getOutputStream());
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static ObjectOutputStream getToServerInput() {
        return toServerInput;
    }
}
