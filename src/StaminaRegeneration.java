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
                    Thread.sleep(2200);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
