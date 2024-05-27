import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PaintApplication extends Application implements ConnectInfo {

    public final FXMLLoader loader = new FXMLLoader(getClass().getResource("Game Window.fxml"));
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
        primaryStage.setTitle("Fort Honor");

        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");

        // Show the stage
        primaryStage.show();
        connectGame();
        primaryStage.setOnCloseRequest(e->{
            System.exit(0);
        });
    }
    public void connectGame() {

            new Thread(() -> {
                // Assume false
                boolean isConnected = false;

                // Loop trying to connect to server
                while (!isConnected) {
                    try (Socket socket = new Socket(SERVER_IP, COMBAT_PORT)) {

                        // To reach here, we must have connected. Set isConnected to true so that the loop ends.
                        isConnected = true;

                        // Clear the "Connecting to opponent" text when connected
                        clearConnectingText();

                        Controller.setToServer(new ObjectOutputStream(socket.getOutputStream()));
                        Controller.setFromServer(new ObjectInputStream(socket.getInputStream()));

                    } catch (IOException e) {
                        // Do nothing. isConnected stays set to false. It keeps trying to connect to server.
                    }
                } // Connecting to server loop ends here.

                // Start the controls for attacks after connection to server
                Combat.start(this.loader, scene);
            }).start();

    }
    private void clearConnectingText() {
        Platform.runLater(()-> {
            // Clear the "Connecting to opponent" text when connected
            stackPane.getChildren().remove((javafx.scene.control.TextField) loader.getNamespace().get("connectingText"));
        });
    }
}
