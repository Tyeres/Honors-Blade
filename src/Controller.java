import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Controller {
    // These two objects are serializable. So, they cannot have static objects within them. So, we will create new objects to use the attack numbers.
    public static final LightAttack lightAttack = new LightAttack();
    public static final HeavyAttack heavyAttack = new HeavyAttack();

    // Parry window is one third of the attack length. Attack window is closed for two thirds of the time length initially.
    static final double PARRY_WINDOW_CLOSED_LENGTH = 2.0 / 3;
    // Parry window is open for one third of the attack length
    static final double PARRY_WINDOW_OPENED_LENGTH = 1.0 / 3;
    static final int LIGHT_PARRY_STUN_LENGTH = 1400;
    static final int HEAVY_PARRY_STUN_LENGTH = 700;
    static final int ATTACK_INTERRUPT_STUN_LENGTH = 500;
    public static final int FEINT_COST = 5;

    private static int combatPort;
    private static int defensePort;
    private static int inputPort;

    public static final int NO_STAMINA_ACTION = -1;
    public static final int BLOCKED_ACTION = 0;
    public static final int ATTACK_ACTION = 1;
    public static final int PARRIED_ACTION  = 2;
    public static final int ACTIVE_PARRY_ACTION = 3;
    public static final int FEINT_ACTION = 4;
    public static final int INTERRUPTED_ACTION = 5;


    private static ProgressBar enemyHPBar;
    private static ProgressBar enemyStaminaBar;

    private static final Character character = new Character();
    // This is used to keep track of the enemy's HP and stamina. It should initially match the HP of your player too.
    private static int enemyCharacterHP = character.getHp();
    private static int enemyCharacterStamina = character.getMaxStamina();
    public static void decreaseEnemyHP(int damage) {
        setEnemyCharacterHP(enemyCharacterHP - damage);
    }

    public static void setEnemyStamina(int stamina) {
        enemyCharacterStamina = stamina;
        // Show the change
        Platform.runLater(()->{
            enemyStaminaBar.setProgress(convertStaminaToProgressBarProgression(enemyCharacterStamina));
        });
    }

    public final static int UP_GUARD = 10;
    public final static int LEFT_GUARD = 11;
    public final static int RIGHT_GUARD = 12;
    public final static int INCOMING_ATTACK = 13;
    public final static int NO_GUARD = 14;

    private static ObjectOutputStream toCombatServer;
    private static ObjectInputStream fromCombatServer;

    public static final int HEAVY_STAMINA_COST = 7;
    public static final int LIGHT_STAMINA_COST = 5;

    public static Character getCharacter() {
        return character;
    }
    public static void setEnemyCharacterHP(int hp) {
        enemyCharacterHP = hp;

        // Show the change
        Platform.runLater(()->{
            enemyHPBar.setProgress(convertHPToProgressBarProgression(enemyCharacterHP));
        });

        // If the enemy has died
        if (enemyCharacterHP <= 0) {
            PaintApplication.gameOver(true);
        }
    }
    public static int getEnemyCharacterHP() {
        return enemyCharacterHP;
    }
    public static int getEnemyCharacterStamina() {
        return enemyCharacterStamina;
    }

    public static ObjectOutputStream getToCombatServer() {
        return toCombatServer;
    }

    public static ObjectInputStream getFromCombatServer() {
        return fromCombatServer;
    }

    public static void setToCombatServer(ObjectOutputStream toCombatServer) {
        Controller.toCombatServer = toCombatServer;
    }

    public static void setFromCombatServer(ObjectInputStream fromCombatServer) {
        Controller.fromCombatServer = fromCombatServer;
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

    // This sets the enemy's ProgressBar object, not the enemy's hp.
    public static void setEnemyHPBar(ProgressBar enemyHPBar) {
        Controller.enemyHPBar = enemyHPBar;
    }

    // This sets the enemy's ProgressBar object, not the enemy's stamina.
    public static void setEnemyStaminaBar(ProgressBar enemyStaminaBar) {
        Controller.enemyStaminaBar = enemyStaminaBar;
    }

    public static double convertHPToProgressBarProgression(int hp) {
        return convertToProgressBarProgression(hp, character.getMaxHP());
    }
    public static double convertStaminaToProgressBarProgression(int stamina) {
        return convertToProgressBarProgression(stamina, character.getMaxStamina());
    }

    /**
     * This method converts the part to a form that the ProgressBar's setProgress() method can read. 1.0 is the
     * max progress for the bar, and 0 is an empty progress bar.
     */
    protected static double convertToProgressBarProgression(int part, int maxWhole) {
        return ((double) part) / maxWhole;
    }
}
