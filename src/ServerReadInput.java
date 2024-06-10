import java.io.IOException;
import java.io.ObjectInputStream;

public class ServerReadInput implements ConnectInfo {



    public static void run(ObjectInputStream fromPlayer, int playerType) {
        new Thread(() -> {
            while (true) {
                try {
                    Object key = fromPlayer.readObject();

                    if (key.equals(Controller.UP_GUARD) || key.equals(Controller.LEFT_GUARD) || key.equals(Controller.RIGHT_GUARD)) {
                        ServerDefense.setPlayerGuard((int) key, playerType);
                    }
                    else if (key.equals("Q")) {
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
