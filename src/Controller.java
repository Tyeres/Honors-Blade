import javafx.event.ActionEvent;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {

    public static final int NO_STAMINA_ACTION = -1;
    public static final int BLOCKED_ACTION = 0;

    public static final int ATTACK_ACTION = 1;
    public static final int PARRY_ACTION  = 2;


    private static final Character character = new Character();

    public final static int UP_GUARD = 0;
    public final static int LEFT_GUARD = 1;
    public final static int RIGHT_GUARD = 2;

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


    public void click(ActionEvent action) {
        System.out.println("Clicked");
    }

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
}
