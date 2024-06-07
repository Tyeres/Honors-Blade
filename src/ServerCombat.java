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
                            // If player has enough stamina
                        } else {

                            // Wait for the attack to land go through
                            // Parry window is two thirds of the attack length. Attack window is closed for 1 third MS initially.
                            Thread.sleep(action.getDuration() / 3);

                            // This starts the parry window.
                            // If a right click has been used while this was open, then getBeenParried should read as true
                            setParryWindow(playerType, true);

                            // Parry window is open for two thirds of the attack length
                            Thread.sleep(action.getDuration() * 2L / 3);

                            setParryWindow(playerType, false);


                            int enemyStance = returnEnemyGuard(playerType);
                            int myStance = returnPlayerGuard(playerType);

                            // Write action


                            // Attack was parried
                            if (getBeenParried(playerType)) {
                                toOpponent1Combat.writeInt(Controller.PARRY_ACTION);
                                // Reset beenParried boolean
                                setBeenParried(playerType, false);
                            }
                            // Attack lands
                            else if (enemyStance != myStance) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                            }
                            // Attack is blocked
                            else if (enemyStance == myStance) {
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
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
                        }

                        // It's an attack
                        // If player hasn't enough stamina
                        else if (playerStamina < Controller.HEAVY_STAMINA_COST) {
                            toOpponent1Combat.writeInt(Controller.NO_STAMINA_ACTION);
                            // If player has enough stamina
                        } else {
                            // This sets the start for the feint window and resets it.
                            setFeint(playerType, false);

                            // Wait for the attack to land go through
                            // Parry window is two thirds of the attack length. Attack window is closed for 1 third MS initially.
                            Thread.sleep(action.getDuration() / 3);

                            // Do not open the parry window if the player feints
                            if (!getFeint(playerType)) {
                                // This starts the parry window.
                                // If a right click has been used while this was open, then getBeenParried should read as true
                                setParryWindow(playerType, true);

                                // Parry window is open for two thirds of the attack length
                                Thread.sleep(action.getDuration() * 2L / 3);


                                setParryWindow(playerType, false);
                            }


                            int enemyStance = returnEnemyGuard(playerType);
                            int myStance = returnPlayerGuard(playerType);

                            // Write action

                            if (getFeint(playerType)) {
                                toOpponent1Combat.writeInt(Controller.FEINT_ACTION);
                            }
                            // Attack was parried
                            else if (getBeenParried(playerType)) {
                                toOpponent1Combat.writeInt(Controller.PARRY_ACTION);
                                // Reset beenParried boolean
                                setBeenParried(playerType, false);
                            }
                            // Attack lands
                            else if (enemyStance != myStance) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                            }
                            // Attack is blocked
                            else if (enemyStance == myStance) {
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
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
            return ServerDefense.getPlayer2Guard();
        return ServerDefense.getPlayer1Guard();
    }

    private static int returnPlayerGuard(int playerType) {
        if (playerType == 1)
            return ServerDefense.getPlayer1Guard();
        return ServerDefense.getPlayer2Guard();
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
