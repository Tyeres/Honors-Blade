import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerReadInput implements ConnectInfo {



    public static void run(ObjectInputStream fromPlayer, ObjectOutputStream toEnemyPlayer,
                           int playerType) {
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
                    // If the player's hp or stamina changes, tell the enemy player
                    else if (key instanceof ObjectsToSend.HealthStaminaPackage) {
                        toEnemyPlayer.writeObject(key);
                        toEnemyPlayer.flush();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
