public class StaminaRegeneration {
    private final static int STAMINA_REGENERATION_AMOUNT = 3;

    public static void start() {
        new Thread(() -> {
            while (true) {
                if (Controller.getCharacter().getStamina() +
                        STAMINA_REGENERATION_AMOUNT >= Controller.getCharacter().getMaxStamina()) {
                    Controller.getCharacter().increaseStamina(STAMINA_REGENERATION_AMOUNT);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
