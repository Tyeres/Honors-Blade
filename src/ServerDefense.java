import java.io.IOException;
import java.io.ObjectOutputStream;

public class ServerDefense {
    // Used for silently changing the guard (the clients to receive any data about it. It's only server-side.)
    // Only run if it's set NOT to silently run. That's why they are set to false by default.
    private static boolean silentGuardChange1 = false;
    private static boolean silentGuardChange2 = false;

    // The game starts with the guards pointing up
    static private int player1Guard = Controller.UP_GUARD;
    static private int player2Guard = Controller.UP_GUARD;

    private static ObjectOutputStream toPlayer1Defense;
    private static ObjectOutputStream toPlayer2Defense;

    public static void start(ObjectOutputStream toPlayer1Defense,
                             ObjectOutputStream toPlayer2Defense) {

        ServerDefense.toPlayer1Defense = toPlayer1Defense;
        ServerDefense.toPlayer2Defense = toPlayer2Defense;
    }

    private static void tellClientEnemyChangeGuard(ObjectOutputStream toOpponent2Defense,int playerType) {
        try {
            toOpponent2Defense.writeInt(getPlayerGuard(playerType));
            toOpponent2Defense.flush();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    // Change the values back to false when done. False for NOT silently changing the guard.
    public static void setSilentlyChangeGuard(boolean value, int playerType) {
        if (playerType == 1) {
            silentGuardChange1 = value;
        }
        else silentGuardChange2 = value;
    }

    public static int getPlayer1Guard() {
        return player1Guard;
    }

    public static int getPlayer2Guard() {
        return player2Guard;
    }

    public static void setPlayerGuard(int playerGuard, int playerType) {
        if (playerType == 1) {
            player1Guard = playerGuard;
            // Player 1 tells player 2 about guard change.
            // Only run if it's set NOT to silently run
            if (!silentGuardChange1) {
                tellClientEnemyChangeGuard(toPlayer2Defense, 1);
            }
        }
        else {
            player2Guard = playerGuard;
            // Player 2 tells player 1 about guard change
            // Only run if it's set NOT to silently run
            if (!silentGuardChange2) {
                tellClientEnemyChangeGuard(toPlayer1Defense, 2);
            }
        }
    }
    public static int getPlayerGuard(int playerType) {
        if (playerType == 1)
            return player1Guard;
        return player2Guard;
    }
    public static ObjectOutputStream getOpponentDefenseToClient(int playerType) {
        if (playerType == 2)
            return toPlayer1Defense;
        return toPlayer2Defense;
    }
    public static int getEnemyGuard(int playerType) {
        if (playerType == 1)
            return getPlayer2Guard();
        return getPlayer1Guard();
    }
}
