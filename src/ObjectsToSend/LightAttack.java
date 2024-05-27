package ObjectsToSend;

import java.io.Serializable;

public class LightAttack extends Attack implements Serializable {

    public LightAttack() {
        super(12, 10, 0, 350);
    }
}
