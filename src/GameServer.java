import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The server must be prepared to receive an object (and attack or a feint) and then an int
 * (amount of stamina the player has) following the object using COMBAT_PORT from ConnectInfo.java.
 * Then, the server must send a calculated response for what has happened.
 * Using the DEFENSE_PORT from ConnectInfo.java, the server must tell the other client that
 * the opponent has switched his guard.
 */
public class GameServer extends Application implements ConnectInfo {

    @Override
    public void start(Stage primaryStage) {

        Pane pane = new Pane();
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        pane.getChildren().add(textArea);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.setOnCloseRequest(e->{
            System.exit(0);
        });
        primaryStage.show();

        // Only allow for 50 games to run per server.
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        // Run loop in thread
        new Thread(() -> {

            while (true) {

                try {
                    ServerSocket serverSocket1 = new ServerSocket(COMBAT_PORT);
                    ServerSocket serverSocket2 = new ServerSocket(DEFENSE_PORT);

                    Socket player1SocketCombat = serverSocket1.accept();

                    Platform.runLater(() -> {
                        textArea.appendText("Player 1 IPV4: " +
                                player1SocketCombat.getInetAddress().toString() +"\n");
                    });
                    ObjectOutputStream toPlayer1 = new ObjectOutputStream(player1SocketCombat.getOutputStream());
                    ObjectInputStream fromPlayer1 = new ObjectInputStream(player1SocketCombat.getInputStream());


                    Socket player2SocketCombat = serverSocket1.accept();
                    Platform.runLater(() -> {
                        textArea.appendText("Player 1 IPV4: " +
                                player2SocketCombat.getInetAddress().toString() +"\n");
                    });
                    OutputStream toPlayer2 = player2SocketCombat.getOutputStream();
                    InputStream fromPlayer2 = player2SocketCombat.getInputStream();


                    serverSocket1.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Run in a thread so that the server could technically run multiple games.
                Runnable task = new Thread(() -> {

                });
                executorService.execute(task);
            }
        }).start();
    }

    private static void serverCombat() {

    }
}
