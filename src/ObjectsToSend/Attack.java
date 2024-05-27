package ObjectsToSend;

import java.io.Serializable;

/**
 * Attack Direction is by default up
 */
public class Attack implements Serializable {
    // Damage values
    private int top;
    private int side;
    private final int chip;

    // Length in milliseconds
    private int duration;


    protected Attack(int top, int side, int chip, int duration) {
        this.top = top;
        this.side = side;
        this.chip = chip;
        this.duration = duration;
    }

    public int getTop() {
        return top;
    }

    public int getSide() {
        return side;
    }

    public int getChip() {
        return chip;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public void setTop(int top) {
        this.top = top;
    }

}
