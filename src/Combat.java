import ObjectsToSend.Feint;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

public class Combat {
    private Combat() {
    }

    private static boolean canAttack = true;

    static Character character = Controller.getCharacter();

    public static void start(FXMLLoader loader, Scene scene) {
        // Starts the guard system for switching guard
        GuardSystem.startControls(loader, scene);
        // Starts the guard system for the opponent and starts showing attack indicators
        Defense.startDefense(loader);

        // Starts the mouse buttons
        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Actions to perform on left-click
                leftClick();
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                // Actions to perform on right-click
                rightClick();
            }
        });

        // Starts the ability to feint. Uses Q to activate.
        scene.setOnKeyPressed(event->{
            if (event.getCode().toString().equals("Q")) {
                try {
                    // For the server, when this is received, use a boolean for the heavy attack window.
                    // The boolean value changes when this is received. When the attack window ends, if the
                    // boolean value is set to that specific value, do not take away damage from opponent
                    // and do not allow to be parried.
                    Controller.getToServer().writeObject(new Feint());
                }
                catch (IOException exception) {
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
            new Thread(() -> {
                try {
                    // Send to the server the light attack object to handle
                    Controller.getToServer().writeObject(character.getLightAttack());
                    // Tell the server how much stamina I have
                    Controller.getToServer().writeInt(character.getStamina());

                    // The server tells my action. Did I land the attack? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION || typeOfAction == Controller.BLOCKED_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.LIGHT_STAMINA_COST);
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        playStaminaError();
                    }
                } catch (IOException err) {
                    System.err.println(err);
                }
                // Attack over. Set to true.
                canAttack = true;
                GuardSystem.setCanChangeGuard(true);
            }).start();
        }
    }

    private static void rightClick() {
        if (canAttack) {
            canAttack = false;
            GuardSystem.setCanChangeGuard(false);
            new Thread(() -> {
                try {
                    // Send to the server the heavy attack object to handle
                    // We do not know if this was a heavy or a parry. Let the server find this out.
                    Controller.getToServer().writeObject(character.getHeavyAttack());
                    // Tell the server how much stamina I have
                    Controller.getToServer().writeInt(character.getStamina());

                    // The server tells my action. Did I land a heavy? Was I blocked? Did I parry instead?
                    // Did I even have enough stamina to attack?
                    // The response will not be instantaneous. So, it needs to be in a thread.
                    int typeOfAction = Controller.getFromServer().readInt();

                    // Decrease stamina if attack went through.
                    if (typeOfAction == Controller.ATTACK_ACTION || typeOfAction == Controller.BLOCKED_ACTION) {
                        // Decrease the stamina of the player
                        character.decreaseStamina(Controller.HEAVY_STAMINA_COST);
                    }
                    // Play error if out of stamina.
                    else if (typeOfAction == Controller.NO_STAMINA_ACTION) {
                        playStaminaError();
                    }
                } catch (java.io.IOException err) {
                    System.err.println(err);
                }
                // Attack over. Set to true.
                canAttack = true;
                GuardSystem.setCanChangeGuard(true);
            }).start();
        }
    }
    static void playStaminaError() {
        final String NO_STAMINA_DIRECTORY = "./src/ErrorSounds/notification-sound-error-sound-effect-203788.mp3";
        Media media = new Media(new File(NO_STAMINA_DIRECTORY).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
}
