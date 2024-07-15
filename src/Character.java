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

    public void setHp(int hp) {
        this.hp = hp;
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

    public void setStamina(int stamina) {
        this.stamina = stamina;
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
        setStamina(this.stamina + amount);
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
    }
    public synchronized void decreaseStamina(int cost) throws IOException {
        setStamina(this.stamina - cost);
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
    }
    public void decreaseHealth(int damage) throws IOException {
        if (damage > 0) {
            setHp(this.hp - damage);
            Combat.getToServerInput().writeObject(new HealthStaminaPackage(damage, this.stamina));
            Combat.getToServerInput().flush();
        }
    }
}
