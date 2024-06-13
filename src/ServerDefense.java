import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerDefense {
    // The game starts with the guards pointing up
    static private final IntegerProperty player1Guard = new SimpleIntegerProperty(Controller.UP_GUARD);
    static private final IntegerProperty player2Guard = new SimpleIntegerProperty(Controller.UP_GUARD);

    private static ObjectOutputStream toPlayer1Defense;
    private static ObjectOutputStream toPlayer2Defense;

    public static void start(ObjectOutputStream toPlayer1Defense, ObjectInputStream fromPlayer1Defense,
                             ObjectOutputStream toPlayer2Defense, ObjectInputStream fromPlayer2Defense) {

        ServerDefense.toPlayer1Defense = toPlayer1Defense;
        ServerDefense.toPlayer2Defense = toPlayer2Defense;

        // Player 1 tells player 2 about guard change
        player1Guard.addListener(e->{
                tellClientEnemyChangeGuard(toPlayer2Defense, 1);

        });

        // Player 2 tells player 1 about guard change
        player2Guard.addListener(e->{
                tellClientEnemyChangeGuard(toPlayer1Defense, 2);
        });
    }

    private static void tellClientEnemyChangeGuard(ObjectOutputStream toOpponent2Defense,int playerType) {
        try {
            toOpponent2Defense.writeInt(getPlayerGuard(playerType));
            toOpponent2Defense.flush();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static IntegerProperty getPlayer1Guard() {
        return player1Guard;
    }

    public static IntegerProperty getPlayer2Guard() {
        return player2Guard;
    }

    public static void setPlayerGuard(int playerGuard, int playerType) {
        if (playerType == 1) {
            ServerDefense.player1Guard.set(playerGuard);
        }
        else
            ServerDefense.player2Guard.set(playerGuard);
    }
    public static int getPlayerGuard(int playerType) {
        if (playerType == 1)
            return player1Guard.get();
        return player2Guard.get();
    }
    public static ObjectOutputStream getOpponentDefenseToServer(int playerType) {
        if (playerType == 2)
            return toPlayer1Defense;
        return toPlayer2Defense;
    }
}
