package ObjectsToSend;

import java.io.Serializable;

public class HeavyAttack extends Attack implements Serializable {

    public HeavyAttack() {
        super(35, 27, 2, 800);
    }
}
