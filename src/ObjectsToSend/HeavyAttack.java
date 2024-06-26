package ObjectsToSend;

import java.io.Serializable;

public class HeavyAttack extends Attack implements Serializable {

    public HeavyAttack() {
        super(33, 27, 3, 800);
    }
}
