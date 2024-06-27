import ObjectsToSend.Attack;
import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ServerCombat {
    // These two objects are serializable. So, they cannot have static objects within them. So, we will create new objects to use the attack numbers.
    public static final LightAttack lightAttack = new LightAttack();
    public static final HeavyAttack heavyAttack = new HeavyAttack();

    private static boolean parryWindow1 = false;
    private static boolean parryWindow2 = false;
    private static boolean beenParried1 = false;
    private static boolean beenParried2 = false;
    private static boolean feint1 = false;
    private static boolean feint2 = false;
    private static boolean canDealDamage1 = true;
    private static boolean canDealDamage2 = true;


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
                    // The first received object should always be an Attack. This is the player's attack, not the player's opponent's attack.
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

                            // This is so that the opponent sees the start of the attack indicator.
                            ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.INCOMING_ATTACK);
                            ServerDefense.getOpponentDefenseToClient(playerType).flush();

                            // Wait for the attack to land go through
                            Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));

                            // This starts the parry window.
                            // If a right click has been used while this was open, then getBeenParried should read as true
                            setParryWindow(playerType, true);
                            // The opponent client is waiting to receive a value to know when the parry window opens.
                            ServerDefense.getOpponentDefenseToClient(playerType).writeInt(0);
                            ServerDefense.getOpponentDefenseToClient(playerType).flush();
                            Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));

                            setParryWindow(playerType, false);

                            // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                            ServerDefense.getOpponentDefenseToClient(playerType).writeInt(0);
                            ServerDefense.getOpponentDefenseToClient(playerType).flush();

                            int enemyStance = returnEnemyGuard(playerType);
                            int myStance = returnPlayerGuard(playerType);

                            // Write action

                            // Attack was parried
                            if (getBeenParried(playerType)) {
                                toOpponent1Combat.writeInt(Controller.PARRIED_ACTION);
                                toOpponent1Combat.flush();
                                // Reset beenParried boolean
                                setBeenParried(playerType, false);
                                // Tell the enemy player that he parried me
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.ACTIVE_PARRY_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();

                                // Change the guard to no-guard so that the player can be punished.
                                openGuardTemporarily(playerType, Controller.LIGHT_PARRY_STUN_LENGTH);
                            }
                            // Attack lands
                            else if (enemyStance != myStance && isCanDealDamage(playerType)) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.ATTACK_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                                // Tell the opponent player how much damage to take. It's a light attack.
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(lightAttack.getSide());
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                                // Do not let the enemy player do any damage temporarily. NO HYPER ARMOR :)
                                new Thread(()->{
                                    try {
                                        setOpponentCanDealDamage(false, playerType);
                                        Thread.sleep(Controller.ATTACK_INTERRUPT_STUN_LENGTH);
                                        setOpponentCanDealDamage(true, playerType);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).start();
                            }
                            // Attack is blocked
                            else if (enemyStance == myStance) {
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.BLOCKED_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                            }
                            // The attack was interrupted
                            else {
                                toOpponent1Combat.writeInt(Controller.INTERRUPTED_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.INTERRUPTED_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                            }
                        }
                    }
                    // Read heavy attack
                    else if (action instanceof HeavyAttack) {
                        // If enemy parry window is open
                        // Check first if it's a parry instead of an attack and that the guards are equal
                        if (returnPlayerGuard(playerType) == returnEnemyGuard(playerType) && getOpponentParryWindow(playerType)) {
                            // The opponent has been parried
                            setOpponentBeenParried(playerType, true);
                            // Tell the one who parries that the parry was successful
                            toOpponent1Combat.writeInt(Controller.ACTIVE_PARRY_ACTION);
                            toOpponent1Combat.flush();
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
                            ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.INCOMING_ATTACK);
                            ServerDefense.getOpponentDefenseToClient(playerType).flush();

                            // Wait for the attack to land go through
                            // Parry window is two thirds of the attack length. Attack window is closed for 1 third MS initially.
                            Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_CLOSED_LENGTH));


                            // Do not open the parry window if the player feints
                            if (!getFeint(playerType)) {
                                // This starts the parry window.
                                // If a right click has been used while this was open, then getBeenParried should read as true
                                setParryWindow(playerType, true);

                                // The opponent client is waiting to receive a value to know when the parry window opens.
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(0);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();

                                // Parry window is open for two thirds of the attack length
                                Thread.sleep((long) (action.getDuration() * Controller.PARRY_WINDOW_OPENED_LENGTH));


                                setParryWindow(playerType, false);
                            } else {
                                // The enemy is waiting to receive a value to know when the parry window opens.
                                // Send FEINT_ACTION so that the enemy knows never to open it.
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.FEINT_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                            }

                            // The client is waiting to receive a value to know when the attack ends. It doesn't matter what the value is.
                            ServerDefense.getOpponentDefenseToClient(playerType).writeInt(0);
                            ServerDefense.getOpponentDefenseToClient(playerType).flush();


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
                                // Tell the enemy player that he parried me
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.ACTIVE_PARRY_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();

                                // Change the guard to no-guard so that the player can be punished.
                                openGuardTemporarily(playerType, Controller.HEAVY_PARRY_STUN_LENGTH);
                            }
                            // Attack lands
                            else if (enemyStance != myStance && isCanDealDamage(playerType)) {
                                toOpponent1Combat.writeInt(Controller.ATTACK_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.ATTACK_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                                // Tell the opponent player how much damage to take. It's a heavy attack.
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(heavyAttack.getSide());
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                                // Do not let the enemy player do any damage temporarily. NO HYPER ARMOR :)
                                new Thread(()->{
                                    try {
                                        setOpponentCanDealDamage(false, playerType);
                                        Thread.sleep(Controller.ATTACK_INTERRUPT_STUN_LENGTH);
                                        setOpponentCanDealDamage(true, playerType);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).start();
                            }
                            // Attack is blocked
                            else if (enemyStance == myStance){
                                toOpponent1Combat.writeInt(Controller.BLOCKED_ACTION);
                                toOpponent1Combat.flush();
                                ServerDefense.getOpponentDefenseToClient(playerType).writeInt(Controller.BLOCKED_ACTION);
                                ServerDefense.getOpponentDefenseToClient(playerType).flush();
                            }
                            // The attack was interrupted
                            else {
                                toOpponent1Combat.writeInt(Controller.INTERRUPTED_ACTION);
                                toOpponent1Combat.flush();
                                // You do not need to the opponentDefense about it because all they know is that their attack went through
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

    /**
     * This is used to make the guard go away temporarily when parried. This makes it very easy to punish the opponent without needing
     * to change guards.
     */
    protected static void openGuardTemporarily(int playerType, long duration) throws InterruptedException {

        // Save the original guard so that it's not forgotten
        int temp = ServerDefense.getPlayerGuard(playerType);
        // This makes it so that when the guard changes, the client will not hear anything about it.
        ServerDefense.setSilentlyChangeGuard(true, playerType);
        // Change the guard to no-guard so that the player can be punished.
        ServerDefense.setPlayerGuard(Controller.NO_GUARD, playerType);
        // Wait for the attack duration to end
        Thread.sleep(duration);

        // Set back to original guard
        ServerDefense.setPlayerGuard(temp, playerType);

        // Release. This allows the enemy client to hear about guard changes again.
        ServerDefense.setSilentlyChangeGuard(false, playerType);
    }

    /**
     * This method was going to be used in addition to the openGuardTemporarily. It was going to be used for heavy attacks in case the player
     * feinted. I was going to make the guard go away during attacks too, but the issue is that the defensive guard and the attacking guard is
     * shared in the one guardStance variable. Making the guard go away during an attack would make it so that all attacks would go through.
     * The solution would be to create two separate guard variables for each player, but I do not think this is a big enough deal to
     * make such a change.
     */
    @Deprecated
    protected static int openGuardIndefinitely(int playerType) {
        int guardStance = ServerDefense.getPlayerGuard(playerType);
        // This makes it so that when the guard changes, the client will not hear anything about it.
        ServerDefense.setSilentlyChangeGuard(true, playerType);
        // Change the guard to no-guard so that the player can be punished.
        ServerDefense.setPlayerGuard(Controller.NO_GUARD, playerType);
        // Return the original guard stance so that it isn't forgotten.
        return guardStance;
    }
    private static void setOpponentCanDealDamage(boolean value, int playerType) {
        // Change the opponent's
        if (playerType == 1) {
            canDealDamage2 = value;
        }
        else canDealDamage1 = value;
    }
    private static boolean isCanDealDamage(int playerType) {
        if (playerType == 1) {
            return canDealDamage1;
        }
        return canDealDamage2;
    }
}
