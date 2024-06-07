import java.io.*;

public class ServerDefense {
    // The game starts with the guards pointing up
    static private int player1Guard = Controller.UP_GUARD;
    static private int player2Guard = Controller.UP_GUARD;

    public static void start(ObjectOutputStream toPlayer1Defense, ObjectInputStream fromPlayer1Defense,
                             ObjectOutputStream toPlayer2Defense, ObjectInputStream fromPlayer2Defense) {
        // Player 1
        startGuard(toPlayer1Defense, fromPlayer1Defense, toPlayer2Defense, fromPlayer2Defense, 1);

        // Player 2
        startGuard(toPlayer2Defense, fromPlayer2Defense, toPlayer1Defense, fromPlayer1Defense, 2);

    }

    private static void startGuard(ObjectOutputStream toOpponent1Defense, ObjectInputStream fromOpponent1Defense,
                                   ObjectOutputStream toOpponent2Defense, ObjectInputStream fromOpponent2Defense, int playerType) {


    }

    public static int getPlayer1Guard() {
        return player1Guard;
    }

    public static int getPlayer2Guard() {
        return player2Guard;
    }
}
