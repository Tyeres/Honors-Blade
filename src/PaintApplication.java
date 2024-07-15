import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PaintApplication extends Application implements ConnectInfo {

    private final FXMLLoader loader = new FXMLLoader(getClass().getResource("Game Window.fxml"));
    private StackPane stackPane;
    // Scene of the program
    static Scene scene;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file

        Parent root = loader.load();
        stackPane = (StackPane) loader.getNamespace().get("stackPane");

        ImageView imageView = (ImageView) loader.getNamespace().get("background");
        // Assuming 'root' is the Node that reflects the window's size
        imageView.fitWidthProperty().bind(stackPane.widthProperty());
        imageView.fitHeightProperty().bind(stackPane.heightProperty());


        // Create the scene
        scene = new Scene(root);

        // Set the scene to the stage
        primaryStage.setScene(scene);

        // Set the stage title
        primaryStage.setTitle("Honor's Blade");

        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("Press ESC to exit the game");

        // Show the stage
        primaryStage.show();
//        scene.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.ESCAPE) {
//                System.exit(0);
//            }
//        });
        connectGame();
        primaryStage.setOnCloseRequest(e->{
            System.exit(0);
        });
    }
    public void connectGame() {

            new Thread(() -> {
                // Assume false
                boolean isConnected = false;

                byte i = 0;
                // Loop trying to connect to server
                while (!isConnected) {
                    try  {
                        // Make sure you do not set the ports more than once.
                        if (i == 0) {
                            setPorts();
                            i++;
                        }
                        // Give the server time to create the new ports, and also make sure that the program is not trying to connect too fast.
                        Thread.sleep(50);

                        // DO NOT CLOSE THIS SOCKET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        Socket socket = new Socket(SERVER_IP, Controller.getCombatPort());

                        // To reach here, we must have connected. Set isConnected to true so that the loop ends.
                        isConnected = true;

                        // Clear the "Connecting to opponent" text when connected
                        clearConnectingText();

                        Controller.setFromCombatServer(new ObjectInputStream(socket.getInputStream()));
                        Controller.setToCombatServer(new ObjectOutputStream(socket.getOutputStream()));

                        // Give the server time to create the next server port
                        Thread.sleep(50);

                    } catch (Exception e) {
                        // Do nothing. isConnected stays set to false. It keeps trying to connect to server.
                    }
                } // Connecting to server loop ends here.

                // Start the game for the client after connection to server
                startGame(loader);
            }).start();

    }
    private void startGame(FXMLLoader loader) {
        Combat.setInputConnection();
        // Starts the guard system for switching guard
        GuardSystem.startControls(loader, scene);
        // Starts the guard system for the opponent and starts showing attack indicators
        Defense.startDefense(loader);
        StaminaRegeneration.start();
        Combat.start(this.loader, scene);

        initiateHealthAndStaminaBars(loader);
    }
    private void clearConnectingText() {
        Platform.runLater(()-> {
            // Clear the "Connecting to opponent" text when connected
            stackPane.getChildren().remove((javafx.scene.control.TextField) loader.getNamespace().get("connectingText"));
        });
    }
    private static void setPorts() throws IOException, InterruptedException {
        Socket socket = new Socket(SERVER_IP, STARTING_PORT);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

        int playerType = dataInputStream.readInt();

        if (playerType == 1) {
            Controller.setCombatPort(COMBAT_PORT);
            Controller.setDefensePort(DEFENSE_PORT);
            Controller.setInputPort(INPUT_PORT);
        }
        else {
            Controller.setCombatPort(COMBAT_PORT_2);
            Controller.setDefensePort(DEFENSE_PORT_2);
            Controller.setInputPort(INPUT_PORT_2);
            // Give the server time to connect to player 1 and then to get ready for the player 2 connection
            Thread.sleep(50);
        }
        socket.close();
    }
    private static void initiateHealthAndStaminaBars(FXMLLoader loader) {
        Controller.setEnemyHPBar((ProgressBar) loader.getNamespace().get("enemyHPBar"));
        Controller.setEnemyStaminaBar((ProgressBar) loader.getNamespace().get("enemyStaminaBar"));
        Controller.getCharacter().setMyHPBar((ProgressBar) loader.getNamespace().get("myHPBar"));
        Controller.getCharacter().setMyStaminaBar((ProgressBar) loader.getNamespace().get("myStaminaBar"));
    }
}
