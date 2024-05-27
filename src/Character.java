import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;

import java.io.Serializable;

/**
 * Attack Direction is by default up
 */
public class Character implements Serializable {
    private int maxStamina;
    private int stamina;
    private int hp;
    private final HeavyAttack heavyAttack;
    private final LightAttack lightAttack;
    private int guardStance;

    public Character() {
        this(20, 130, new HeavyAttack(), new LightAttack());
    }
    public Character(int maxStamina, int hp, HeavyAttack heavyAttack, LightAttack lightAttack) {
        this.maxStamina = maxStamina;
        this.stamina = this.maxStamina;
        this.hp = hp;
        this.heavyAttack = heavyAttack;
        this.lightAttack = lightAttack;
        this.guardStance = 0;
    }

    public int getHp() {
        return hp;
    }

    public HeavyAttack getHeavyAttack() {
        return heavyAttack;
    }

    public LightAttack getLightAttack() {
        return lightAttack;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }


    public int getMaxStamina() {
        return maxStamina;
    }

    public void setMaxStamina(int stamina) {
        this.maxStamina = stamina;
    }

    public int getStamina() {
        return this.stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    public int getGuardStance() {
        return guardStance;
    }

    public void setGuardStance(int guardStance) {
        this.guardStance = guardStance;
    }
    public void increaseStamina(int amount) {
        this.stamina += amount;
    }
    public void decreaseStamina(int cost) {
        this.stamina -= cost;
    }
}
