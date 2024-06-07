import java.io.IOException;
import java.io.ObjectInputStream;

public class ServerReadInput implements ConnectInfo {


    public static void run(ObjectInputStream fromPlayer, int playerType) {
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
