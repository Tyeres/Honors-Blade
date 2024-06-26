import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {
    // Parry window is two thirds of the attack length. Attack window is closed for 1 third MS initially.
    static final double PARRY_WINDOW_CLOSED_LENGTH = 1.0 / 3;
    // Parry window is open for two thirds of the attack length
    static final double PARRY_WINDOW_OPENED_LENGTH = 2.0 / 3;
    static final int LIGHT_PARRY_STUN_LENGTH = 1400;
    static final int HEAVY_PARRY_STUN_LENGTH = 700;

    private static int combatPort;
    private static int defensePort;
    private static int inputPort;

    public static final int NO_STAMINA_ACTION = -1;
    public static final int BLOCKED_ACTION = 0;
    public static final int ATTACK_ACTION = 1;
    public static final int PARRIED_ACTION  = 2;
    public static final int ACTIVE_PARRY_ACTION = 3;
    public static final int FEINT_ACTION = 4;


    private static final Character character = new Character();

    public final static int UP_GUARD = 10;
    public final static int LEFT_GUARD = 11;
    public final static int RIGHT_GUARD = 12;
    public final static int INCOMING_ATTACK = 13;
    public final static int NO_GUARD = 14;

    private static ObjectOutputStream toServer;
    private static ObjectInputStream fromServer;

    public static final int HEAVY_STAMINA_COST = 5;
    public static final int LIGHT_STAMINA_COST = 3;

//    public static boolean isUpPressed;
//    public static boolean isLeftPressed;
//    public static boolean isRightPressed;
//    public static final String UP = "W";
//    public static final String LEFT = "A";
//    public static final String RIGHT = "D";


//    public void click(ActionEvent action) {
//
//    }

    public static Character getCharacter() {
        return character;
    }

    public static ObjectOutputStream getToServer() {
        return toServer;
    }

    public static ObjectInputStream getFromServer() {
        return fromServer;
    }

    public static void setToServer(ObjectOutputStream toServer) {
        Controller.toServer = toServer;
    }

    public static void setFromServer(ObjectInputStream fromServer) {
        Controller.fromServer = fromServer;
    }

    public static int getCombatPort() {
        return combatPort;
    }

    public static void setCombatPort(int combatPort) {
        Controller.combatPort = combatPort;
    }

    public static int getDefensePort() {
        return defensePort;
    }

    public static void setDefensePort(int defensePort) {
        Controller.defensePort = defensePort;
    }

    public static int getInputPort() {
        return inputPort;
    }

    public static void setInputPort(int inputPort) {
        Controller.inputPort = inputPort;
    }
}
