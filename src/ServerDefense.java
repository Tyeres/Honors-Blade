import ObjectsToSend.Attack;
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
            tellClientEnemyChangeGuard(toPlayer1Defense, 1);
        });

        // Player 2 tells player 1 about guard change
        player2Guard.addListener(e->{
            tellClientEnemyChangeGuard(toPlayer2Defense, 2);
        });
    }
    public static Thread sendEnemyPlayerMyAttack(Attack attack, int playerType) {
        Thread indicatorThread = new Thread(() -> {
            if (playerType == 1) {
                try {
                    // Send to player2Defense because the opponent needs to be receiving it
                    toPlayer2Defense.writeObject(Controller.INCOMING_ATTACK);
                    Thread.sleep((long) (attack.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));
                    // The client is waiting to receive a value to know when the parry window opens. It doesn't matter what the value is.
                    toPlayer2Defense.writeInt(0);
                    Thread.sleep((long) (attack.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));
                    // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                    toPlayer2Defense.writeInt(0);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            // If player 2
            else {
                try {
                    // Send to player1Defense because the opponent needs to be receiving it
                    toPlayer1Defense.writeObject(Controller.INCOMING_ATTACK);
                    Thread.sleep((long) (attack.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));
                    // The client is waiting to receive a value to know when the parry window opens. It doesn't matter what the value is.
                    toPlayer1Defense.writeInt(0);
                    Thread.sleep((long) (attack.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));
                    // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                    toPlayer1Defense.writeInt(0);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        indicatorThread.start();
        return indicatorThread;
    }

    private static void tellClientEnemyChangeGuard(ObjectOutputStream toOpponent2Defense,int playerType) {
        try {
            toOpponent2Defense.writeObject(getPlayerGuard(playerType));
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
    public static IntegerProperty getPlayerGuard(int playerType) {
        if (playerType == 1)
            return player1Guard;
        return player2Guard;
    }
    public static ObjectOutputStream getOpponentDefenseToServer(int playerType) {
        if (playerType == 2)
            return toPlayer1Defense;
        return toPlayer2Defense;
    }
}
