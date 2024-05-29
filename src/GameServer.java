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
                    // Connect to the attack interface for player 1
                    ServerSocket player1ServerSocket1 = new ServerSocket(COMBAT_PORT);
                    Socket player1SocketCombat = player1ServerSocket1.accept();

                    Platform.runLater(() -> {
                        textArea.appendText("Player 1 IPV4: " +
                                player1SocketCombat.getInetAddress().toString() +"\n");
                    });
                    ObjectOutputStream toPlayer1Combat = new ObjectOutputStream(player1SocketCombat.getOutputStream());
                    ObjectInputStream fromPlayer1Combat = new ObjectInputStream(player1SocketCombat.getInputStream());

                    // Connect to the defense interface for player 1
                    ServerSocket player1ServerSocket2 = new ServerSocket(DEFENSE_PORT);
                    Socket player1SocketDefense = player1ServerSocket2.accept();

                    ObjectOutputStream toPlayer1Defense = new ObjectOutputStream(player1SocketDefense.getOutputStream());
                    ObjectInputStream fromPlayer1Defense = new ObjectInputStream(player1SocketDefense.getInputStream());


                    // Connect to the attack interface for player 2
                    ServerSocket player2ServerSocket1 = new ServerSocket(COMBAT_PORT_2);
                    Socket player2SocketCombat = player2ServerSocket1.accept();

                    Platform.runLater(() -> {
                        textArea.appendText("Player 2 IPV4: " +
                                player2SocketCombat.getInetAddress().toString() +"\n");
                    });
                    ObjectOutputStream toPlayer2Combat = new ObjectOutputStream(player2SocketCombat.getOutputStream());
                    ObjectInputStream fromPlayer2Combat = new ObjectInputStream(player2SocketCombat.getInputStream());

                    // Connect to the defense interface for player 2
                    ServerSocket player2ServerSocket2 = new ServerSocket(DEFENSE_PORT_2);

                    Socket player2SocketDefense = player2ServerSocket2.accept();

                    ObjectOutputStream toPlayer2Defense = new ObjectOutputStream(player2SocketDefense.getOutputStream());
                    ObjectInputStream fromPlayer2Defense = new ObjectInputStream(player2SocketDefense.getInputStream());


                    player1ServerSocket1.close();
                    player1ServerSocket2.close();
                    player2ServerSocket1.close();
                    player2ServerSocket2.close();
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
