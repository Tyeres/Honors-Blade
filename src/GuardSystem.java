import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

/**
 * This class sends objects to the server and updates the server with the new guard direction.
 * The game starts with the up guard.
 */
public class GuardSystem {
    static byte i = 1;

    static private boolean canChangeGuard = false; // This should initiate as false because it should first be set to true when the fight starts.
    static private double lastMouseX;
    static private double lastMouseY;
    static private final double moveThreshold = 10; // Minimum distance to trigger guard change

    private static final Character character = Controller.getCharacter();

    // This is the variable that represents the focused JavaFX rectangle guard.
    private static Rectangle fxGuard;

    public static void startControls(FXMLLoader loader, Scene scene) {
        // The game starts with the guard in the up guard.
        fxGuard = (Rectangle) loader.getNamespace().get("UP_GUARD");

        scene.setOnMouseMoved(event -> {
            if (canChangeGuard) {
                double mouseX = event.getSceneX();
                double mouseY = event.getSceneY();

                // Calculate the distance moved
                double distanceX = mouseX - lastMouseX;
                double distanceY = mouseY - lastMouseY;

                // Calculate the angle of movement
                double angle = Math.atan2(distanceY, distanceX);
                double angleDegrees = Math.toDegrees(angle);

                // Update guard direction based on angle
                if (Math.abs(distanceX) > moveThreshold || Math.abs(distanceY) > moveThreshold) {
                    if (angleDegrees >= -45 && angleDegrees <= 80) {
                        // Right
                        setRightGuard(loader);
                    } else if (angleDegrees > 80 && angleDegrees < 100) {
                        // Down
                        // There is no down Guard
                    } else if (angleDegrees < -45 && angleDegrees >= -135) {
                        // Up
                        setUpGuard(loader);
                    } else {
                        // Left
                        setLeftGuard(loader);
                    }
                }

                // Update last mouse position
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        });
    }

    private static void setGuard(FXMLLoader loader, int guardStance, String rectangleGuardFXID) {
        // Tell the local self the new guard
        character.setGuardStance(guardStance);

        // Tell the server the new guard. Send as object because that is what the server is prepared to receive first.
        try {
            Combat.getToServerInput().writeObject(guardStance);
        } catch (IOException ioException) {
            System.err.println(ioException + "\nError when sending guardStance to server in GuardSystem.java");
        } catch (NullPointerException nullPointerException) {
            // This will initially be null a few times, but it should go away. Only notify if it happens more than thirty times.
            i++;
            if (i % 30 == 0) {
                throw nullPointerException;
            }
        }

        // Visually change the guard
        fxGuard.setVisible(false);

        fxGuard = (Rectangle) loader.getNamespace().get(rectangleGuardFXID);
        fxGuard.setVisible(true);
    }

    protected static void setLeftGuard(FXMLLoader loader) {
        setGuard(loader, Controller.LEFT_GUARD, "LEFT_GUARD");
    }

    protected static void setUpGuard(FXMLLoader loader) {
        setGuard(loader, Controller.UP_GUARD, "UP_GUARD");
    }

    protected static void setRightGuard(FXMLLoader loader) {
        setGuard(loader, Controller.RIGHT_GUARD, "RIGHT_GUARD");
    }

    public static boolean isCanChangeGuard() {
        return canChangeGuard;
    }

    public static void setCanChangeGuard(boolean canChangeGuard) {
        GuardSystem.canChangeGuard = canChangeGuard;
    }

    public static Rectangle getFxGuard() {
        return fxGuard;
    }
}