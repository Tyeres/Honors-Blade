import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PaintApplication extends Application implements ConnectInfo {

    private static final FXMLLoader loader = new FXMLLoader(PaintApplication.class.getResource("Game Window.fxml"));
    // Scene of the program
    static Scene scene;
    private static TextField announcementText;

    private static boolean gameInProgress = true;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file
        Parent root = loader.load();
        StackPane stackPane = (StackPane) loader.getNamespace().get("stackPane");
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
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                System.exit(0);
            }
        });
        connectGame();
        primaryStage.setOnCloseRequest(e -> {
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
                try {
                    // Make sure you do not set the ports more than once.
                    if (i == 0) {
                        setPorts();
                        i++;
                    }
                    // Give the server time to create the new ports, and also make sure that the program is not trying to connect too fast.
                    Thread.sleep(5);

                    // DO NOT CLOSE THIS SOCKET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    Socket socket = new Socket(SERVER_IP, Controller.getCombatPort());

                    // To reach here, we must have connected. Set isConnected to true so that the loop ends.
                    isConnected = true;


                    Controller.setFromCombatServer(new ObjectInputStream(socket.getInputStream()));
                    Controller.setToCombatServer(new ObjectOutputStream(socket.getOutputStream()));

                    // Give the server time to create the next server port
                    Thread.sleep(5);

                } catch (Exception e) {
                    // Do nothing. isConnected stays set to false. It keeps trying to connect to server.
                }
            } // Connecting to server loop ends here.

            // Start the game for the client after connection to server
            startGame();
        }).start();

    }

    private void startGame() {
        Combat.setInputConnection();
        // Starts the guard system for switching guard
        GuardSystem.startControls(loader, scene);
        // Starts the guard system for the opponent and starts showing attack indicators
        Defense.startDefense(loader);
        StaminaRegeneration.start();
        Combat.start(loader, scene);
        initiateHealthAndStaminaBars();

        try {
            // Wait until the server gives permission to start. This makes sure the countdown is in sync.
            Controller.getFromCombatServer().readInt();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        announcementText = ((javafx.scene.control.TextField) loader.getNamespace().get("announcementText"));
        for (int i = 5; i >= 0; i--) {
            int finalI = i;
            Platform.runLater(()->{
                if (finalI != 0)
                    announcementText.setText("Game starts in " + finalI + "!");
                else announcementText.setText("FIGHT!");
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Combat.setCanAttack(true);
        GuardSystem.setCanChangeGuard(true);
        announcementText.setVisible(false);
    }

    public static TextField getAnnouncementText() {
        return announcementText;
    }

    private static void setPorts() throws IOException, InterruptedException {
        Socket socket = new Socket(SERVER_IP, STARTING_PORT);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

        int playerType = dataInputStream.readInt();

        if (playerType == 1) {
            Controller.setCombatPort(COMBAT_PORT);
            Controller.setDefensePort(DEFENSE_PORT);
            Controller.setInputPort(INPUT_PORT);
        } else {
            Controller.setCombatPort(COMBAT_PORT_2);
            Controller.setDefensePort(DEFENSE_PORT_2);
            Controller.setInputPort(INPUT_PORT_2);
            // Give the server time to connect to player 1 and then to get ready for the player 2 connection
            Thread.sleep(7);
        }
        socket.close();
    }

    private static void initiateHealthAndStaminaBars() {
        Controller.setEnemyHPBar((ProgressBar) loader.getNamespace().get("enemyHPBar"));
        Controller.setEnemyStaminaBar((ProgressBar) loader.getNamespace().get("enemyStaminaBar"));
        Controller.getCharacter().setMyHPBar((ProgressBar) loader.getNamespace().get("myHPBar"));
        Controller.getCharacter().setMyStaminaBar((ProgressBar) loader.getNamespace().get("myStaminaBar"));
    }

    public static void gameOver(boolean gameWon) {
        // Player cannot do anything while the game is over.
        gameInProgress = false; // This allows it so that the canAttack & the canChangeGuard is not set to true if an attack was active before the game ended and then ended while the game was over.
        Combat.setCanAttack(false);
        GuardSystem.setCanChangeGuard(false);

        new Thread(() -> {
            Platform.runLater(() -> {
                announcementText.setVisible(true);
            });
            String winner;
            if (gameWon)
                winner = "You win!";
            else
                winner = "You lose!";
            for (int i = 5; i >= 0; i--) {
                int finalI = i;
                Platform.runLater(()->{
                    announcementText.setText("GAME OVER! " + winner + " Play again in " + finalI + " seconds.");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                // Fill the health and stamina. I don't need to fill the enemy's manually because when he does it, we will update it then.
                Controller.getCharacter().fillHpAndStamina();
                gameInProgress = true;
                Combat.setCanAttack(true);
                GuardSystem.setCanChangeGuard(true);
                announcementText.setVisible(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static boolean isGameInProgress() {
        return gameInProgress;
    }

    public static void setGameInProgress(boolean gameInProgress) {
        PaintApplication.gameInProgress = gameInProgress;
    }
}
