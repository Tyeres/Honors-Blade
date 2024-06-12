import ObjectsToSend.Attack;
import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerCombat {

    static boolean parryWindow1 = false;
    static boolean parryWindow2 = false;
    static boolean beenParried1 = false;
    static boolean beenParried2 = false;
    static boolean feint1 = false;
    static boolean feint2 = false;


    private ServerCombat() {
    }

    public static void startServerCombat(ObjectOutputStream toPlayer1Combat, ObjectInputStream fromPlayer1Combat,
                                         ObjectOutputStream toPlayer2Combat, ObjectInputStream fromPlayer2Combat) {

        // For player 1
        calculateAttack(toPlayer1Combat, fromPlayer1Combat,
                toPlayer2Combat, fromPlayer2Combat, 1);

        // For player 2
        calculateAttack(toPlayer2Combat, fromPlayer2Combat,
                toPlayer1Combat, fromPlayer1Combat, 2);
    }

    private static void calculateAttack(ObjectOutputStream toOpponent1Combat, ObjectInputStream fromOpponent1Combat,
                                        ObjectOutputStream toOpponent2Combat, ObjectInputStream fromOpponent2Combat, int playerType) {

        new Thread(() -> {
            while (true) {
                try {
                    // The first received object should always be an Attack
                    Attack action = (Attack) fromOpponent1Combat.readObject();
                    // The second "object" received should always be an int
                    int playerStamina = fromOpponent1Combat.readInt();

                    // If a light attack
                    if (action instanceof LightAttack) {
                        // If player hasn't enough stamina
                        if (playerStamina < Controller.LIGHT_STAMINA_COST) {
                            toOpponent1Combat.writeInt(Controller.NO_STAMINA_ACTION);
                            toOpponent1Combat.flush();
                            // If player has enough stamina
                        } else {
                            System.out.println("1");

                            // This is so that the opponent sees the start of the attack indicator.
                            ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.INCOMING_ATTACK);
                            ServerDefense.getOpponentDefenseToServer(playerType).flush();

                            // Wait for the attack to land go through
                            Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));

                            // This starts the parry window.
                            // If a right click has been used while this was open, then getBeenParried should read as true
                            setParryWindow(playerType, true);
                            // The opponent client is waiting to receive a value to know when the parry window opens.
                            ServerDefense.getOpponentDefenseToServer(playerType).writeInt(0);
//                            ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            Thread.sleep((long)(action.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));

                            setParryWindow(playerType, false);

                            // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                            ServerDefense.getOpponentDefenseToServer(playerType).writeInt(0);
                            ServerDefense.getOpponentDefenseToServer(playerType).flush();

                            int enemyStance = returnEnemyGuard(playerType);
                            int myStance = returnPlayerGuard(playerType);

                            // Write action
                            System.out.println("2");

                            // Attack was parried
                            if (getBeenParried(playerType)) {
                                toOpponent1Combat.writeInt(Controller.PARRIED_ACTION);
                                toOpponent1Combat.flush();
                                // Reset beenParried boolean
                                setBeenParried(playerType, false);
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.PARRIED_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }
                            // Attack lands
                            else if (enemyStance != myStance) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.ATTACK_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }
                            // Attack is blocked
                            else {
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.BLOCKED_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                                System.out.println("3");
                            }
                        }
                    }
                    // Read heavy attack
                    else if (action instanceof HeavyAttack) {
                        // If enemy parry window is open
                        // Check first if it's a parry instead of an attack
                        if (getOpponentParryWindow(playerType)) {
                            // The opponent has been parried
                            setOpponentBeenParried(playerType, true);
                            // Tell the one who parries that the parry was successful
                            ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.ACTIVE_PARRY_ACTION);
                            ServerDefense.getOpponentDefenseToServer(playerType).flush();
                        }

                        // It's an attack
                        // If player hasn't enough stamina
                        else if (playerStamina < Controller.HEAVY_STAMINA_COST) {
                            toOpponent1Combat.writeInt(Controller.NO_STAMINA_ACTION);
                            toOpponent1Combat.flush();
                            // If player has enough stamina
                        } else {
                            // This sets the start for the feint window and resets it.
                            setFeint(playerType, false);


                            // This is so that the opponent sees the start of the attack indicator.
                            ServerDefense.getOpponentDefenseToServer(playerType).writeObject(Controller.INCOMING_ATTACK);
                            ServerDefense.getOpponentDefenseToServer(playerType).flush();

                            // Wait for the attack to land go through
                            // Parry window is two thirds of the attack length. Attack window is closed for 1 third MS initially.
                            Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));


                            // Do not open the parry window if the player feints
                            if (!getFeint(playerType)) {
                                // This starts the parry window.
                                // If a right click has been used while this was open, then getBeenParried should read as true
                                setParryWindow(playerType, true);

                                // The opponent client is waiting to receive a value to know when the parry window opens.
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(0);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();

                                // Parry window is open for two thirds of the attack length
                                Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));


                                setParryWindow(playerType, false);
                            }
                            else {
                                // The enemy is waiting to receive a value to know when the parry window opens.
                                // Send -1 so that the enemy knows never to open it.
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(-1);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }

                            // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                            ServerDefense.getOpponentDefenseToServer(playerType).writeInt(0);
                            ServerDefense.getOpponentDefenseToServer(playerType).flush();


                            int enemyStance = returnEnemyGuard(playerType);
                            int myStance = returnPlayerGuard(playerType);

                            // Write action

                            if (getFeint(playerType)) {
                                toOpponent1Combat.writeInt(Controller.FEINT_ACTION);
                                toOpponent1Combat.flush();
                                // Do not tell the opponent player. They will know by the lack of information.
                                // The attack indicator should just go away.
                            }
                            // Attack was parried
                            else if (getBeenParried(playerType)) {
                                // Tell the opponent he has been parried
                                toOpponent1Combat.writeInt(Controller.PARRIED_ACTION);
                                toOpponent1Combat.flush();
                                // Reset beenParried boolean
                                setBeenParried(playerType, false);
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.PARRIED_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }
                            // Attack lands
                            else if (enemyStance != myStance) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.ATTACK_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }
                            // Attack is blocked
                            else {
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToServer(playerType).writeInt(Controller.BLOCKED_ACTION);
                                ServerDefense.getOpponentDefenseToServer(playerType).flush();
                            }
                        }
                    }
                    // If the action is not a light or a heavy, then it's a feint.
                    // Do nothing.
                    // A feint should only be acted upon if received during a heavy attack.

                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private static int returnEnemyGuard(int playerType) {
        if (playerType == 1)
            return ServerDefense.getPlayer2Guard().get();
        return ServerDefense.getPlayer1Guard().get();
    }

    private static int returnPlayerGuard(int playerType) {
        if (playerType == 1)
            return ServerDefense.getPlayer1Guard().get();
        return ServerDefense.getPlayer2Guard().get();
    }

    public static void setParryWindow(int playerType, boolean value) {
        if (playerType == 1) {
            parryWindow1 = value;
        } else parryWindow2 = value;
    }

    public static void setBeenParried(int playerType, boolean value) {
        if (playerType == 1) {
            beenParried1 = value;
        } else beenParried2 = value;
    }

    public static void setOpponentBeenParried(int playerType, boolean value) {
        if (playerType == 1) {
            beenParried2 = value;
        } else beenParried1 = value;
    }

    public static boolean getOpponentParryWindow(int playerType) {
        if (playerType == 1) {
            return parryWindow2;
        } else return parryWindow1;
    }

    //    public static boolean getParryWindow(int playerType) {
//        if (playerType == 1) {
//            return parryWindow1;
//        }
//        else return parryWindow2;
//    }
    public static boolean getBeenParried(int playerType) {
        if (playerType == 1) {
            return beenParried1;
        } else return beenParried2;
    }


    public static void setFeint1(boolean feint1) {
        ServerCombat.feint1 = feint1;
    }


    public static void setFeint2(boolean feint2) {
        ServerCombat.feint2 = feint2;
    }

    public static boolean getFeint(int playerType) {
        if (playerType == 1)
            return ServerCombat.feint1;
        return ServerCombat.feint2;
    }

    public static void setFeint(int playerType, boolean value) {
        if (playerType == 1)
            setFeint1(value);
        else
            setFeint2(value);
    }

}
