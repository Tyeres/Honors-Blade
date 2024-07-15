import ObjectsToSend.HealthStaminaPackage;
import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

/**
 * Attack Direction is by default up
 */
public class Character {
    private final int maxStamina;
    private int stamina;
    private final int maxHP;
    private int hp;
    private final HeavyAttack heavyAttack;
    private final LightAttack lightAttack;
    private int guardStance;

    private ProgressBar myHPBar;
    private ProgressBar myStaminaBar;

    public Character() {
        this(20, 130, new HeavyAttack(), new LightAttack());
    }
    public Character(int maxStamina, int maxHP, HeavyAttack heavyAttack, LightAttack lightAttack) {
        this.maxStamina = maxStamina;
        this.stamina = this.maxStamina;
        this.maxHP = maxHP;
        this.hp = maxHP;
        this.heavyAttack = heavyAttack;
        this.lightAttack = lightAttack;
        this.guardStance = Controller.UP_GUARD;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMyHPBar(ProgressBar myHPBar) {
        this.myHPBar = myHPBar;
    }

    public void setMyStaminaBar(ProgressBar myStaminaBar) {
        this.myStaminaBar = myStaminaBar;
    }

    public HeavyAttack getHeavyAttack() {
        return heavyAttack;
    }

    public LightAttack getLightAttack() {
        return lightAttack;
    }

    /**
     *
     * @param hp
     * This method should not be used and would not work if used. This is because the
     * startMonitoringEnemyHealthStamina() method within Combat.java (which receives the written object) uses hp points to DECREASE the health,
     * not to SET the health. This could easily be fixed, but I currently have no use for this method any ways.
     */
    @Deprecated
    public void setHp(int hp) throws IOException {
        this.hp = hp;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(hp, this.stamina));
        Combat.getToServerInput().flush();
        // Show the change
        Platform.runLater(()->{
            myHPBar.setProgress(Controller.convertHPToProgressBarProgression(this.getHp()));
        });
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getStamina() {
        return this.stamina;
    }

    public void setStamina(int stamina) throws IOException {
        this.stamina = stamina;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
        // Show the change
        Platform.runLater(()->{
            myStaminaBar.setProgress(Controller.convertStaminaToProgressBarProgression(this.getStamina()));
        });
    }

    public int getGuardStance() {
        return guardStance;
    }

    public void setGuardStance(int guardStance) {
        this.guardStance = guardStance;
    }
    public synchronized void increaseStamina(int amount) throws IOException {
        this.stamina += amount;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
        // Show the change
        Platform.runLater(()->{
            myStaminaBar.setProgress(Controller.convertStaminaToProgressBarProgression(this.getStamina()));
        });
    }
    public synchronized void decreaseStamina(int cost) throws IOException {
        this.stamina -= cost;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
        // Show the change
        Platform.runLater(()->{
            myStaminaBar.setProgress(Controller.convertStaminaToProgressBarProgression(this.getStamina()));
        });
    }
    public void decreaseHealth(int damage) throws IOException {
        if (damage > 0) {
            this.hp -= damage;
            Combat.getToServerInput().writeObject(new HealthStaminaPackage(damage, this.stamina));
            Combat.getToServerInput().flush();
            Platform.runLater(()->{
                myHPBar.setProgress(Controller.convertHPToProgressBarProgression(this.getHp()));
            });
        }
    }
}
