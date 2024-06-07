import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.show();


        // Run loop in thread
        new Thread(() -> {
                try {
                    // This makes it so that the game will not start until both players are connected.
                    // This is because the program will not start the other ports until the method finishes.
                    // BRING THIS BACK WHEN DONE THE GAME!!!!!!!!!!!
//                    givePlayersRespectivePorts();

                    // REMOVE THIS METHOD WHEN DONE THE GAME!!!!!!!!!!!!!!!!!!!!!!
                    giveImmediatePlayer1Port();

                    // Connect to the attack interface for player 1
                    ServerSocket player1ServerSocket1 = new ServerSocket(COMBAT_PORT);
                    Socket player1SocketCombat = player1ServerSocket1.accept();

                    Platform.runLater(() -> {
                        textArea.appendText("Player 1 IPV4: " +
                                player1SocketCombat.getInetAddress().toString() + "\n");
                    });
                    ObjectOutputStream toPlayer1Combat = new ObjectOutputStream(player1SocketCombat.getOutputStream());
                    ObjectInputStream fromPlayer1Combat = new ObjectInputStream(player1SocketCombat.getInputStream());

                    // Connect to the defense interface for player 1
                    ServerSocket player1ServerSocket2 = new ServerSocket(DEFENSE_PORT);
                    Socket player1SocketDefense = player1ServerSocket2.accept();

                    System.out.println("Here1");
                    ObjectOutputStream toPlayer1Defense = new ObjectOutputStream(player1SocketDefense.getOutputStream());
                    System.out.println("Here2");
                    ObjectInputStream fromPlayer1Defense = new ObjectInputStream(player1SocketDefense.getInputStream());
                    System.out.println("Here3");


                    ServerSocket player1ServerSocketInput = new ServerSocket(INPUT_PORT); // Player 1

                    Socket player1InputSocket = player1ServerSocketInput.accept();

                    ObjectInputStream fromPlayer1 = new ObjectInputStream(player1InputSocket.getInputStream());

                    ServerReadInput.run(fromPlayer1, 1);



                    // REMOVE THIS METHOD WHEN DONE THE GAME!!!!!!!!!!!!!!!!!!!!!!
                    giveImmediatePlayer2Port();


                    // Connect to the attack interface for player 2
                    ServerSocket player2ServerSocket1 = new ServerSocket(COMBAT_PORT_2);
                    Socket player2SocketCombat = player2ServerSocket1.accept();

                    Platform.runLater(() -> {
                        textArea.appendText("Player 2 IPV4: " +
                                player2SocketCombat.getInetAddress().toString() + "\n");
                    });
                    ObjectOutputStream toPlayer2Combat = new ObjectOutputStream(player2SocketCombat.getOutputStream());
                    ObjectInputStream fromPlayer2Combat = new ObjectInputStream(player2SocketCombat.getInputStream());

                    // Connect to the defense interface for player 2
                    ServerSocket player2ServerSocket2 = new ServerSocket(DEFENSE_PORT_2);

                    Socket player2SocketDefense = player2ServerSocket2.accept();

                    ObjectOutputStream toPlayer2Defense = new ObjectOutputStream(player2SocketDefense.getOutputStream());
                    ObjectInputStream fromPlayer2Defense = new ObjectInputStream(player2SocketDefense.getInputStream());

                    ServerSocket player2ServerSocketInput = new ServerSocket(INPUT_PORT_2); // Player 2
                    Socket player2 = player2ServerSocketInput.accept();


                    ObjectInputStream fromPlayer2 = new ObjectInputStream(player2.getInputStream());

                    ServerReadInput.run(fromPlayer2, 2);


                    // Start the server's combat
                    ServerCombat.startServerCombat(toPlayer1Combat, fromPlayer1Combat, toPlayer2Combat, fromPlayer2Combat);

                    // Start the server's defense
                    ServerDefense.start(toPlayer1Defense, fromPlayer1Defense, toPlayer2Defense, fromPlayer2Defense);


//                    player1ServerSocket1.close();
//                    player1ServerSocket2.close();
//                    player2ServerSocket1.close();
//                    player2ServerSocket2.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }).start();
    }



    /**
     * This gives the two players their respective ports. This makes it so that there is only one version of the client.
     * This makes it so that the game will not start until both players are connected.
     * This is because the program will not start the other ports until the method finishes.
     */
    private static void givePlayersRespectivePorts() {
        try (ServerSocket serverSocket = new ServerSocket(STARTING_PORT)) {
            DataOutputStream dataOutputStream1 = new DataOutputStream(serverSocket.accept().getOutputStream());

            // Tell the client that it is player 1
            dataOutputStream1.writeInt(1);


            DataOutputStream dataOutputStream2 = new DataOutputStream(serverSocket.accept().getOutputStream());

            // Tell the client that is it player 2
            dataOutputStream2.writeInt(2);
        } catch (IOException e) {

        }
    }

    private static void giveImmediatePlayer1Port() {
        try (ServerSocket serverSocket = new ServerSocket(STARTING_PORT)) {
            DataOutputStream dataOutputStream1 = new DataOutputStream(serverSocket.accept().getOutputStream());

            // Tell the client that it is player 1
            dataOutputStream1.writeInt(1);
        } catch (IOException e) {

        }
    }

    private static void giveImmediatePlayer2Port() {
        try (ServerSocket serverSocket = new ServerSocket(STARTING_PORT)) {
            DataOutputStream dataOutputStream2 = new DataOutputStream(serverSocket.accept().getOutputStream());

            // Tell the client that is it player 2
            dataOutputStream2.writeInt(2);
        } catch (IOException e) {

        }
    }
}
