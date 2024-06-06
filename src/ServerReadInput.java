import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerReadInput implements ConnectInfo {
    public static void start() {
        try {
            ServerSocket serverSocket1 = new ServerSocket(INPUT_PORT); // Player 1
            ServerSocket serverSocket2 = new ServerSocket(INPUT_PORT_2); // Player 2

            Socket player1 = serverSocket1.accept();
            Socket player2 = serverSocket2.accept();


            ObjectInputStream fromPlayer1 = new ObjectInputStream(player1.getInputStream());
            ObjectInputStream fromPlayer2 = new ObjectInputStream(player2.getInputStream());

            run(fromPlayer1, 1);
            run(fromPlayer2, 2);


        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static void run(ObjectInputStream fromPlayer, int playerType) {
        new Thread(() -> {
            while (true) {
                try {
                    String key = (String) fromPlayer.readObject();
                    if (key.equals("Q")) {
                        if (playerType == 1) {
                            ServerCombat.setFeint1(true);
                        }
                        else {
                            ServerCombat.setFeint2(true);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
