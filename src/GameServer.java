import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server must be prepared to receive an object (and attack or a feint) and then an int
 * (amount of stamina the player has) following the object using COMBAT_PORT from ConnectInfo.java.
 * Then, the server must send a calculated response for what has happened.
 * Using the DEFENSE_PORT from ConnectInfo.java, the server must tell the other client that
 * the opponent has switched his guard.
 */
public class GameServer implements ConnectInfo {
    public static void main(String[] args) {
        new Thread(()->{
            try {
                ServerSocket serverSocket = new ServerSocket(COMBAT_PORT);
                Socket socket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
