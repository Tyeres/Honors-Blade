import java.io.IOException;

public class StaminaRegeneration {
    private final static int STAMINA_REGENERATION_AMOUNT = 3;

    public static void start() {
        new Thread(() -> {
            while (true) {
                try {
                    // The stamina must be less than the max stamina to regenerate
                    if (Controller.getCharacter().getStamina() +
                            STAMINA_REGENERATION_AMOUNT <= Controller.getCharacter().getMaxStamina()) {
                        Controller.getCharacter().increaseStamina(STAMINA_REGENERATION_AMOUNT);
                    }
                    // Since the previous condition is false, this will check if stamina is already maxed.
                    // If it's not maxed already, it should be set to max out (it's too much for the full STAMINA_REGENERATION_AMOUNT).
                    else if (Controller.getCharacter().getStamina() +
                    STAMINA_REGENERATION_AMOUNT != Controller.getCharacter().getMaxStamina() + STAMINA_REGENERATION_AMOUNT){
                        // The stamina regeneration amount will go over the max stamina amount. So, just max it now.
                        Controller.getCharacter().setStamina(Controller.getCharacter().getMaxStamina());
                    }

                    // (This is a record of the enemy client's stamina to display for the player
                    // and isn't the true enemy's stamina, but it is an estimation of the enemy's regenerating stamina).

                    if (Controller.getEnemyCharacterStamina() +
                    STAMINA_REGENERATION_AMOUNT <= Controller.getCharacter().getMaxStamina()) {
                        Controller.increaseEnemyStamina(STAMINA_REGENERATION_AMOUNT);
                    }
                    else if (Controller.getEnemyCharacterStamina() +
                    STAMINA_REGENERATION_AMOUNT != Controller.getCharacter().getMaxStamina() + STAMINA_REGENERATION_AMOUNT) {
                        Controller.setEnemyStamina(Controller.getCharacter().getMaxStamina());
                        System.out.println("Enemy Stamina: "+ Controller.getCharacter().getStamina());
                    }

                    Thread.sleep(2200);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
