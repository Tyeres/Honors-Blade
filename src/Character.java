import ObjectsToSend.HealthStaminaPackage;
import ObjectsToSend.HeavyAttack;
import ObjectsToSend.LightAttack;

import java.io.IOException;
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
        this.guardStance = Controller.UP_GUARD;
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
    public synchronized void increaseStamina(int amount) throws IOException {
        this.stamina += amount;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
    }
    public synchronized void decreaseStamina(int cost) throws IOException {
        this.stamina -= cost;
        Combat.getToServerInput().writeObject(new HealthStaminaPackage(0, this.stamina));
        Combat.getToServerInput().flush();
    }
    public void decreaseHealth(int damage) throws IOException {
        if (damage > 0) {
            System.out.println(damage + " decreased in health");
            this.hp -= damage;
            Combat.getToServerInput().writeObject(new HealthStaminaPackage(damage, this.stamina));
            Combat.getToServerInput().flush();
        }
    }
}
