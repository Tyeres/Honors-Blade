package ObjectsToSend;

import java.io.Serializable;

/**
 * The health and stamina should be packaged together to prevent an object being sent in between the two if sent
 * separately.
 */
public record HealthStaminaPackage(int hp, int stamina) implements Serializable {
}
