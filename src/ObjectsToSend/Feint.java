package ObjectsToSend;

import java.io.Serializable;

/**
 * When this is sent to a server, you do not need to receive an integer following it.
 */
public class Feint extends Attack implements Serializable {
    public Feint() {
        super(0, 0, 0, 0);
    }
}
