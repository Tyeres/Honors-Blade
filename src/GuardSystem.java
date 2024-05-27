import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

/**
 * This class sends objects to the server and updates the server with the new guard direction.
 */
public class GuardSystem {
    static private boolean canChangeGuard = true;
    static private double lastMouseX;
    static private double lastMouseY;
    static private final double moveThreshold = 10; // Minimum distance to trigger guard change

    private static final Character character = Controller.getCharacter();

    // This is the variable that represents the focused JavaFX rectangle guard.
    private static Rectangle fxGuard;

    public static void startControls(FXMLLoader loader, Scene scene) {
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
        // Tell the client the new guard
        character.setGuardStance(guardStance);

        // Tell the server the new guard. Send as object because that is what the server is prepared to receive first.
        try {
            Controller.getToServer().writeObject(guardStance);
        }
        catch (IOException ioException) {
            System.err.println(ioException + "\nError when sending guardStance to server in GuardSystem.java");
        }

        // Visually change the guard
        try {
            fxGuard.setVisible(false);
        } catch (Exception e) {
                        /* Do nothing. There would be an error for the first time, since
                         fxGuard is null. After that, there will be no errors. */
        }
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
}