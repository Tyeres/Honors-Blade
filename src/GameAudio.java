import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

public class GameAudio {
    protected final static MediaPlayer hitAudio = new MediaPlayer(
            new Media(Objects.requireNonNull(PaintApplication.class.getResource("Audio/" + "hit.mp3")).toString()));
    protected final static MediaPlayer blockAudio = new MediaPlayer(
            new Media(Objects.requireNonNull(PaintApplication.class.getResource("Audio/" + "block.mp3")).toString()));
    protected final static MediaPlayer parryAudio = new MediaPlayer(
            new Media(Objects.requireNonNull(PaintApplication.class.getResource("Audio/" + "parry.mp3")).toString()));
    protected final static MediaPlayer feintAudio = new MediaPlayer(
            new Media(Objects.requireNonNull(PaintApplication.class.getResource("Audio/" + "feint sound.mp3")).toString()));
    protected final static MediaPlayer noStaminaAudio = new MediaPlayer(
            new Media(Objects.requireNonNull(PaintApplication.class.getResource("Audio/" + "out of stamina.mp3")).toString()));

    public static void playHitAudio() {
        hitAudio.seek(hitAudio.getStartTime());
        hitAudio.play();
        System.out.println("Hit audio played");
    }

    public static void playBlockAudio() {
        blockAudio.seek(blockAudio.getStartTime());
        blockAudio.play();
        System.out.println("Block audio played");
    }

    public static void playParryAudio() {
        parryAudio.seek(parryAudio.getStartTime());
        parryAudio.play();
        System.out.println("Parry audio played");
    }

    public static void playFeintAudio() {
        feintAudio.seek(feintAudio.getStartTime());
        feintAudio.play();
        System.out.println("Feint audio played");
    }

    public static void playNoStaminaAudio() {
        noStaminaAudio.seek(noStaminaAudio.getStartTime());
        noStaminaAudio.play();
        System.out.println("No stamina audio played");
    }
}
